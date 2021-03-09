import json
import os

import mongomock
import pytest
from jsonschema import validate, ValidationError

from submarine.PersistenceModule.src.DatabaseConnection import get_collection, get_db
from submarine.PersistenceModule.src.Errors.SubmarineHttpRequestError import SubmarineHttpRequestError
from submarine.PersistenceModule.src.Errors.SubmarinePersistenceError import SubmarinePersistenceError
from submarine.PersistenceModule.src.PersistMessage import add_message_to_database, get_ip_from_alias
from submarine.PersistenceModule.test import TEST_DATABASE_HOSTNAME, TEST_DATABASE_PORT, TEST_MESSAGE, \
    TEST_DATABASE_NAME, TEST_COLLECTION_NAME

MESSAGE_SCHEMA = os.path.join(os.path.dirname(__file__), 'MessageSchema.json')
with open(MESSAGE_SCHEMA) as message_schema:
    MESSAGE_SCHEMA_PATH = json.load(message_schema)

URI_FORMAT = "mongodb://{hostname}:{port}"


def get_mock_client(hostname, port) -> mongomock.MongoClient:
    return mongomock.MongoClient(URI_FORMAT.format(hostname=hostname, port=port))


def get_message_from_database(collection, message):
    return collection.find_one({"message.destination": message["destination"]})


@mongomock.patch(servers=(('server.example.com', 27017),))
def test_add_message_to_database_wrong_input_fails():
    wrong_input = "wrong"
    with pytest.raises(SubmarinePersistenceError):
        add_message_to_database(wrong_input, "invalid client", wrong_input, wrong_input)


@mongomock.patch(servers=(('server.example.com', 27017),))
def test_add_message_to_database_integration_succeeds():
    mock_client = get_mock_client(TEST_DATABASE_HOSTNAME, TEST_DATABASE_PORT)

    try:
        add_message_to_database(
            TEST_MESSAGE, mock_client, TEST_DATABASE_NAME, TEST_COLLECTION_NAME)
    except SubmarinePersistenceError:
        pytest.fail("Unexpected error; Adding the message did not succeed")

    inserted_message = get_message_from_database(
        get_collection(get_db(mock_client, TEST_DATABASE_NAME), TEST_COLLECTION_NAME), TEST_MESSAGE)

    # Tests if the message was inserted into the database with get_message_from_database()
    # Checks if the _id and timestamp has been added to the message
    # Also checks if the message still remains according to the wished format
    try:
        validate(instance=inserted_message, schema=MESSAGE_SCHEMA_PATH)
    except ValidationError:
        pytest.fail("Unexpected error; Validation did not succeed")


def test_get_ip_from_alias_raises_error():
    # Call
    with pytest.raises(SubmarineHttpRequestError):
        get_ip_from_alias(None)

