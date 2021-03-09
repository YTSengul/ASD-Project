import requests

from submarine.ConnectionModule.src import APPLICATION_JSON, REQUEST_TYPES
from submarine.ConnectionModule.src.Errors.SubmarinePersistenceError import SubmarineHttpRequestError


def do_http_request_with_body(endpoint, request_type, body):
    if request_type == REQUEST_TYPES["put"]:
        return requests.put(endpoint, body, headers=APPLICATION_JSON).status_code
    elif request_type == REQUEST_TYPES["get"]:
        return requests.get(endpoint, body).status_code
    elif request_type == REQUEST_TYPES["post"]:
        return requests.post(endpoint, body, headers=APPLICATION_JSON).status_code

    raise SubmarineHttpRequestError(None, 'HTTP-Request type was not found')
