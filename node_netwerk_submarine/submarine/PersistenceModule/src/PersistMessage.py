import requests

from submarine.PersistenceModule.src import *
from submarine.PersistenceModule.src.DatabaseConnection import *
from submarine.PersistenceModule.src.Errors.SubmarineHttpRequestError import SubmarineHttpRequestError
from submarine.PersistenceModule.src import DATABASE_HOSTNAME, DATABASE_PORT, DATABASE_NAME, MESSAGE_COLLECTION, \
    MESSAGE_PERSISTENCE_ERROR_MESSAGE
from submarine.PersistenceModule.src.DatabaseConnection import get_client, get_db, get_collection
from submarine.PersistenceModule.src.Errors.SubmarinePersistenceError import SubmarinePersistenceError

ALIAS_URL = "http://acc-server.sanstech.net/chatter/get-ip/{alias}"


def persist_message(message) -> None:
    # Client given as param to add_message_to_database to make it easier to test with mocks
    client = get_client(DATABASE_HOSTNAME, DATABASE_PORT)
    return add_message_to_database(message, client, DATABASE_NAME, MESSAGE_COLLECTION)


def add_message_to_database(message, client, database_name, collection_name) -> None:
    try:
        database = get_db(client, database_name)
        collection = get_collection(database, collection_name)
        collection.insert_one({"message": message})
    except Exception as e:
        raise SubmarinePersistenceError(e, MESSAGE_PERSISTENCE_ERROR_MESSAGE)


def get_ip_from_alias(alias) -> str:
    response = requests.get(ALIAS_URL.format(alias=alias))

    if response.status_code == requests.codes.ok:
        return response.json()['ipAddress']

    raise SubmarineHttpRequestError(response.reason)

