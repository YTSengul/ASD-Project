import json
import socket

from submarine.ConnectionModule.src import CONNECTION_REFUSED_ERROR_MESSAGE, CLIENT_CONNECTION_REFUSED_ERROR_MESSAGE, PORT
from submarine.ConnectionModule.src.HttpRequests import do_http_request_with_body
from submarine.PersistenceModule.src.PersistMessage import get_ip_from_alias, persist_message


def create_connection_with_node(ip, port) -> socket:
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect((ip, port))
        return s
    except ConnectionRefusedError as e:
        raise ConnectionRefusedError(e, CONNECTION_REFUSED_ERROR_MESSAGE)


def resolve_destination(message) -> [str, int]:
    ip_dict = {
        "127.0.0.1": "127.0.0.1",
        "94.124.143.122": "10.20.3.122",
        "94.124.143.132": "10.20.3.132",
        "94.124.143.139": "10.20.3.139",
        "192.168.99.100": "192.168.99.100",
        "asd-p1-server1.asd.icaprojecten.nl": "10.20.3.122",
        "asd-p1-server2.asd.icaprojecten.nl": "10.20.3.132",
        "asd-p1-server3.asd.icaprojecten.nl": "10.20.3.139"
    }

    message = json.loads(message)
    dest = message['destination']
    print(dest)

    if 'alias' in dest:
        ip = get_ip_from_alias(message['destination']['alias'])
        port = PORT
    else:
        ip = ip_dict[dest['hostname']]
        port = dest['port']

    return ip, port


def resend_message(message, ip, port) -> str:
    try:
        s = create_connection_with_node(ip, port)
        message_to_be_sent = str(len(message)) + message
        s.sendall(message_to_be_sent.encode())
    except ConnectionRefusedError:
        return CLIENT_CONNECTION_REFUSED_ERROR_MESSAGE
    data = s.recv(8080)
    print(data.decode())
    s.close()


def send_message(message, ip, port) -> None:
    try:
        s = create_connection_with_node(ip, port)
        message_to_be_sent = str(len(message)) + message
        s.sendall(message_to_be_sent.encode())
    except ConnectionRefusedError:
        persist_message(message)
        return CLIENT_CONNECTION_REFUSED_ERROR_MESSAGE
    data = s.recv(8080)
    print(data.decode())
    s.close()


def send_incoming_request(message) -> str:
    json_message = json.loads(message)

    data = json.loads(json_message['data'])
    result = do_http_request_with_body(
        data['endpoint'], data['request_type'], json.dumps(data['body']))
    return str(result)


def send_incoming_request_relay(message, ip, port) -> str:
    with create_connection_with_node(ip, port) as s:
        try:
            message_to_be_sent = str(len(message)) + message
            s.sendall(message_to_be_sent.encode())
            data = s.recv(8080).decode()
            print(data)
            s.close()
            return data
        except ConnectionRefusedError:
            s.close()
            return CLIENT_CONNECTION_REFUSED_ERROR_MESSAGE


if __name__ == "__main__":
    IP = '127.0.0.1'
    PORT = 25010
    CONNECTION_MESSAGE = {'destination': {'hostname': '127.0.0.1', 'port': 25010}, 'command': 'relay',
                          'data': 'sFR4+S0aTXm5FhfEfpFt7hTpFLYMK8MNG7PFfmi1nlGUKHBB+U3pQLv9RaKZas8DlIm5fvoQjDm527V4n9M8ML8qn/HRl5U1zk+9YGL1BOimxCvMfhDByE2/6QEIuVLtnAMazNrIdaYN5G4HHH78LQ=='}

    full_onion = {"destination": {"hostname": "127.0.0.1", "port": 25010}, "command": "RELAY",
                  "data": "AXSqAXRLvEHQSHjLP3QFk6vE1Qoes5ao+RhxnoB4hIrClJomK0S/EsFJ7VxUc4+xNeP3c9g4osxV1/y9JpL96bozEhEUufig8OWqx9OOhhrjLjVirjvoNwt2kpb1FGaSQDCqe/eBN0xFrc1r2R/EQx5EOayNmFXJ5pe9WDxO0sL3WLd79CVrPoXH2My6IN72Kq4aSoOCR5B2tzQg5YGEd5b3ULeDALbjDCBS2MZ3YEScwClnfFyFgkEwBYKoeVNPVrwo/YlNTlOnqSGeMvqIyzW6Afck5wh+SE7YFd8LJ9ibgELlwxOorzrkQgggmP0k6fisxTFMg902VSCFgQXYY0w/Kqe/tInLadDS1w7ZhOCD426KWZqZCHUYGww+xXxEH1PsW1kPUsyCPoRymu4gM1j1q0iOokte3VZAHuSMATvNxxUwkFCG7SHyBtkTWdkc+xjb00LoMFvcjH2zRb3yYXe/vZrtBigFEE3rAC3DtxRq+X1xG/wE0Lx3rUYGLYcst4L6GiFg7BTOLDvaQbuQITWmMw8zZKFiGWlSBOGITbnMnIeBT02E3RyrLHoHgXZdyarXjGNdrvpzcAed9JffcDb+2/KNIVTN8rPZY0oWe/jkNFZfDDXgUPfm4ZSj78sbSBWfJC9UxlNOT01zSFn/bw2Q4LmGmJ24amBBe7gV9PhYgnRtwuAa8kT8PxogbNV+yw7yJEEczJ7tOPnE4yzzNGPhq2uUYkMRUxR0LAy+IUbkOJ3ygu+430IA10CTPdyf6sCQzOS3Paeurb2B7CiN2ijzb9tCJK7M62BO87H18ug04KJiUpa38AYvqkxY6BmcxhkJ8vKceeDAszo0SjviWXVd+rsfeXwLS+UTS7j/LEaCXQssLLOcU4EzUW2r1cKXA6Brfw30U1F3vkx6OP3TT+Ppvtz57MsUJhAc0/bHK7g5Rs2LONHMxtpQg3Rkryq0uKZU5zypRew+34TmK6MPcxQADLj51pMpgqkY3wRuwhIlH6nSmYOvzofdhZ6utzQMQRE+HRojMVvqoU1Hxo/D2+LZ5OxAveRNOwD1dluAIr0I6fhFwdGmspwmMSJN7fJyyHiGAYawcDRHytPgY3CL7K+Xc8hiDjW4mTQvgLsYI3o5zR7ZmVzcdffUYNMNk0Nk3nXNaLShRS/qkyXiCL4ZdpQwPWlmZTqkE5Xr73m0iHcVAFvHuUuY9uB7X3rYDeiT/wt7nuAjoWJm8zgIcPsWNoNjkO5yKvc4Sw10tA0+UpfNZ7N7SxwxFftqhow0dWvlUMwWeoQ29lucYG0UhnV6d8OBdE1FOqd1mx/a0qumDqO3oZj/dEWJeEvPlfmWODddFpckf4UJqMmNZFLyp96RXTYzUppnNvDm7G7+PumT0qVTenUJe7S8+70Tg5i26nM8VMEtK+I1wsXuItS5jpESpYRYDwGsMzIF3lfWRwYgXhL1Pr8sTkWjL2eRsF6sERyU"}

    full_onion_http_request = '{"destination":{"hostname":"127.0.0.1","port":25010},"command":"HTTP_RELAY","data":"AXSqAXRLvEHQSHjLP3QFk9ny5OWxEs8adbdhPXD80ZZvxekRPVEJwfPNmxqenZ5fUP/n/at4QYAal69w4PKDiJ32KVLbctV/ImsL6nZg+8b5pfn36r5KcvtAMdiq5pYyXLsLgitjoRVP4hnt21nYBtR5IG4Fxza7TSfiEoIGAfnOwdFka7FctRtPe2v+qF2xv09e3niAN8hpfEr27SWwca5p5T/zAX5jxz1VN/raZZ1rxUYMBAODPojqUFTUuITGummvM8/8SI0kX89w7XmVh7DqXQXTTuORdwsZtI4uIK2R9tTFjITwAZa4NHFcH7lZcbzXywBoqkDnBxeyebUR8GG7ElhHcTrvRy61mQVqwm5QZN86Tn3kh79VRaomi8DwIM2yzTwr0VDNMLLUrLDgApTrqXUEXgM+uGVfDkFmzW7cKF4KnWlukTCNsVWZWvWgbCu5fZJqF44SLyUgoFnidzniUPBBW3hd3k1a4cIG6asCEaRyL3JlCOYwvkM96tLT2r1+ThgNHmJ0VxOhfLyKO1ycsm9EYYaTGoGPwsKWPKepxgsw29m+UD0jeluQA0mJb5/9fywGO41RlItWe2+A+sXNCv6hmt5vVFjBgn+/NK9UaRAqSQixNnwnEeM1/lAStIfCChWd9fSZxv+Y9NVDyrp/8OZofwaG0CLZu3FaXJGjarl8dz686hdELgtfT42KbB7RTbhEYVk+uJC2siNN0UJ3bdk/3uQPKN9goysgk4mBTSt3EPiTV8KJLJnct6Yo/PGFexqq+bkYHd0zrFTyl/nQET5xVUesoArB7485Bk4Jn7eVDyu9O5xkkjG0zu+t8lmO8bOiOm3BSLpp5A3iIdvIep8bMcLkQ8aR75gMEVyBGSCPgs0+R4qYeZa+HN1QrxGWOt5WwpwwDcaUSN7swcMgjk6JdJmd1d91z32zwD4Av1yqBk3GDungNrTvBytplrVEXpT6EDf0y8rIR+5nTWFLnLqFleHnCluy6uPlDcA+ac0g/Pdt/F+ecfd//1pbu5LEY5Lhp5ztnwRhUfew0w=="}'
    destination = resolve_destination(full_onion_http_request)
    send_message(full_onion_http_request, destination[0], destination[1])
