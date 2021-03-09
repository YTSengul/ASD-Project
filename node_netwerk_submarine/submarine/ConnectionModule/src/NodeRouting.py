import getopt
import json
import os
import re
import socket
import sys
import warnings
from threading import Thread

from jsonschema import validate, ValidationError

from submarine.ConnectionModule.src import CONNECTION_REFUSED_ERROR_MESSAGE, INVALID_MESSAGE_LENGTH, TEXT_FORMAT
from submarine.ConnectionModule.src.ClientNode import resolve_destination, send_incoming_request, \
    send_incoming_request_relay, resend_message
from submarine.ConnectionModule.src.ClientNode import send_message
from submarine.EncryptionModule import AES_KEY
from submarine.EncryptionModule.src.Decryption import decrypt_aes

NODE_ROUTING_SCHEMA = os.path.join(os.path.dirname(__file__), 'NodeRoutingSchema.json')
with open(NODE_ROUTING_SCHEMA) as node_schema:
    NODE_ROUTING_SCHEMA_PATH = json.load(node_schema)

DEFAULT_PORT = "25010"
DEFAULT_IP = "127.0.0.1"
COMMANDLINE_EXCEPTION = "Please add a '-i [ipaddress] -p [portnumber]' to the execution"


def get_commandline_ip_and_port(argv) -> [str, int]:
    port = DEFAULT_PORT
    ip = DEFAULT_IP
    try:
        opts, args = getopt.getopt(argv, "i:p:")
    except getopt.GetoptError as e:
        raise SyntaxError(e, COMMANDLINE_EXCEPTION)
    for opt, arg in opts:
        if opt == '-p':
            port = arg
        elif opt == '-i':
            ip = arg
    return ip, int(port)


def connect_to_incoming_node(ip, port) -> None:
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.bind((ip, port))
    except ConnectionRefusedError as e:
        raise ConnectionRefusedError(e, CONNECTION_REFUSED_ERROR_MESSAGE)
    print('Connected to {}'.format(s.getsockname()))
    s.listen()
    while True:
        try:
            client_socket, address = s.accept()
        except ConnectionRefusedError as e:
            raise ConnectionRefusedError(e, CONNECTION_REFUSED_ERROR_MESSAGE)
        start_threads(client_socket)


def start_threads(s) -> None:
    start_listening_for_message = Thread(target=handle_incoming_connection, kwargs={'message': read_message(s), 's': s})
    start_listening_for_message.start()


def read_message(s) -> str:
    message_length_regex = "[0-9]+"
    full_message = s.recv(10).decode(TEXT_FORMAT)
    message_length = re.search(message_length_regex, full_message)
    if message_length is not None:
        message_length = message_length.group(0)
        message_length = int(message_length)
        # Remove length delimiter to create JSON from message
        full_message = str(re.sub(message_length_regex, "", full_message))
        while len(full_message) < message_length:
            full_message += s.recv(message_length).decode(TEXT_FORMAT)

        return full_message
    else:
        s.sendall(b'Your message hasn\'t been validated')
        warnings.warn(INVALID_MESSAGE_LENGTH)


def validate_message(message, s) -> str:
    if len(message) > 0:
        try:
            json_message = json.loads(message)
            validate(instance=json_message, schema=NODE_ROUTING_SCHEMA_PATH)
            return json_message
        except Exception as e:
            s.sendall(b'Your message hasn\'t been validated')
            raise ValidationError(e)


def handle_incoming_connection(message, s) -> None:
    validate_message(message, s)
    json_message = json.loads(message)

    if json_message['destination']['hostname'] == "127.0.0.1":
        s.close()
        return

    decrypted = decrypt_aes(json_message['data'], AES_KEY).decode()
    validate_message(decrypted, s)
    decrypted_json_message = json.loads(decrypted)

    decrypted_command = decrypted_json_message['command']
    determine_message_handler(decrypted, decrypted_json_message, decrypted_command, s)


def determine_message_handler(message, json_message, command, s) -> None:
    message_handlers = {
        "HTTP_REQUEST": handle_incoming_request,
        "HTTP_RELAY": handle_incoming_request_relay,
        "RELAY": handle_incoming_message
    }

    if 'alias' in json_message:
        handle_incoming_message(message, s)
    else:
        message_handlers[command](message, s)


def handle_resend_message(message) -> str:
    destination = resolve_destination(message)
    response = resend_message(message, destination[0], destination[1])
    return response


def handle_incoming_message(message, s) -> None:
    destination = resolve_destination(message)
    send_message(message, destination[0], destination[1])
    s.close()


def handle_incoming_request(message, s) -> None:
    response_from_request = send_incoming_request(message)
    s.sendall(response_from_request.encode())
    s.close()


def handle_incoming_request_relay(message, s) -> None:
    destination = resolve_destination(message)
    socket_response = send_incoming_request_relay(
        message, destination[0], destination[1])
    s.sendall(socket_response.encode())
    s.close()


if __name__ == "__main__":
    connection_info = get_commandline_ip_and_port(sys.argv[1:])
    connect_to_incoming_node(connection_info[0], connection_info[1])
