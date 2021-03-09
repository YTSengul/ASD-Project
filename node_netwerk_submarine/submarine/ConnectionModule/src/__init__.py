# Globals for ConnectionModule
CONNECTION_REFUSED_ERROR_MESSAGE = "Couldn't connect with target machine, check port and ip"
CLIENT_CONNECTION_REFUSED_ERROR_MESSAGE = "Connection was refused"
INVALID_MESSAGE = "The received message is invalid: "
INVALID_MESSAGE_LENGTH = "Message did not contain a length indicator, so the node could not determine if the message was fully received"
TEXT_FORMAT = "utf-8"

APPLICATION_JSON = {"content-type": "application/json"}

PORT = 25010

REQUEST_TYPES = {
    "put": "PUT",
    "get": "GET",
    "post": "POST"

}
