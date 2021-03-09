from pymongo import MongoClient
from pymongo.collection import Collection
from pymongo.database import Database

URI_FORMAT = "mongodb://{hostname}:{port}"


def get_client(hostname, port) -> MongoClient:
    return MongoClient(URI_FORMAT.format(hostname=hostname, port=port))


def get_db(client, database) -> Database:
    return client[database]


def get_collection(database, collection) -> Collection:
    return database[collection]
