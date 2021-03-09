import datetime
import socket
from datetime import datetime, timedelta, timezone

import pymongo
import pytest

from submarine.PersistenceModule.src.DatabaseConnection import get_client, get_db, get_collection
from submarine.PersistenceModule.src.DatabaseDirectory import resend_saved_messages, delete_message
from submarine.PersistenceModule.src.Errors.SubmarineHttpRequestError import SubmarineHttpRequestError

TEST_CLIENT = get_client("localhost", 27017)
TEST_DATABASE = get_db(TEST_CLIENT, "submarine")
TEST_COLLECTION = get_collection(TEST_DATABASE, "messages")

TEST_DATA = "Please work"
TEST_MESSAGE = {"destination": {"hostname": "43.43.43.43", "port": 12345, "alias": "Test Co3n"}, "command": "RELAY",
                "data": TEST_DATA}

def setup_database_inputs():
    TEST_COLLECTION.delete_many({})
    TEST_COLLECTION.insert_one(TEST_MESSAGE)
    TEST_COLLECTION.insert_one(
        {"destination": {"hostname": "43.43.43.43", "port": 12345, "alias": "teSt Pim"}, "command": "RELAY",
         "data": TEST_DATA})
    TEST_COLLECTION.insert_one(
        {"destination": {"hostname": "43.43.43.43", "port": 12345, "alias": "TEST Hakker2000"}, "command": "RELAY",
         "data": TEST_DATA})


def insert_test_record():
    TEST_COLLECTION.insert_one(
        {"destination": {"hostname": "127.0.0.1", "port": 25010, "alias": "The one and only Coen was here"},
         "command": "relay", "data": TEST_DATA})


def test_getting_all_results():
    # Init
    setup_database_inputs()

    # Call
    list_with_messages_from_out_the_database = \
        list(TEST_DATABASE.get_collection("messages").find({}).sort("_id", pymongo.DESCENDING))

    # Assert
    assert len(list_with_messages_from_out_the_database) == 3


def test_result_is_equal_to_message_string():
    # Init
    setup_database_inputs()

    # Call
    list_with_messages_from_out_the_database = \
        list(TEST_DATABASE.get_collection("messages").find({}).sort("_id", pymongo.DESCENDING))
    for single_message in list_with_messages_from_out_the_database:
        del single_message['_id']

    # Assert
    assert single_message == (
        {"destination": {"hostname": "43.43.43.43", "port": 12345, "alias": "Test Co3n"}, "command": "RELAY",
         "data": "Please work"})


def test_delete_items_from_out_collection_does_not_delete_whole_collection():
    # Init
    setup_database_inputs()

    # Call
    list_with_messages_from_out_the_database = list(
        TEST_DATABASE.get_collection("messages").find({}).sort("_id", pymongo.DESCENDING))
    TEST_COLLECTION.delete_one(
        {"destination": {"hostname": "43.43.43.43", "port": 12345, "alias": "TEST Hakker2000"}, "command": "RELAY",
         "data": TEST_DATA})

    # Assert
    assert len(list_with_messages_from_out_the_database) == 3
    assert TEST_COLLECTION.count_documents({}) == 2


def test_message_gets_deleted_when_too_old():
    # Init
    now = datetime.now(timezone.utc)

    # Very little difference between now, since the _id is being used in mongodb for timestamp
    # This means that the timestamp cannot easily be manually changed
    # Hence it is tested with very little time difference
    too_old_date = now - timedelta(microseconds=1)

    setup_database_inputs()

    message_list = list(TEST_DATABASE.get_collection("messages").find({}).sort("_id", pymongo.DESCENDING))
    assert len(message_list) == 3

    # Call
    resend_saved_messages(TEST_COLLECTION, message_list, too_old_date)

    # Assert that all are deleted, since the messages are too old
    new_message_list = list(TEST_DATABASE.get_collection("messages").find({}).sort("_id", pymongo.DESCENDING))
    assert len(new_message_list) == 0


def test_message_does_not_get_deleted_when_the_message_is_not_too_old():
    # Init
    now = datetime.now(timezone.utc)
    too_old_date = now - timedelta(days=1)

    setup_database_inputs()
    message_list = list(TEST_DATABASE.get_collection("messages").find({}).sort("_id", pymongo.DESCENDING))
    assert len(message_list) == 3

    # Call
    with pytest.raises(SubmarineHttpRequestError):
        # Assert that the function tried to send a message
        # This will obviously raise an error based of the dummy port and ip
        resend_saved_messages(TEST_COLLECTION, message_list, too_old_date)

    # Assert that messages are not deleted, since they weren't sent
    new_message_list = list(TEST_DATABASE.get_collection("messages").find({}).sort("_id", pymongo.DESCENDING))
    assert len(new_message_list) == 3


def test_delete_message_from_database():
    setup_database_inputs()
    message_list = list(TEST_DATABASE.get_collection("messages").find({}).sort("_id", pymongo.DESCENDING))
    assert len(message_list) == 3

    # Call
    delete_message(TEST_COLLECTION, TEST_MESSAGE)

    # Assert that messages are not deleted, since they weren't sent
    new_message_list = list(TEST_DATABASE.get_collection("messages").find({}).sort("_id", pymongo.DESCENDING))
    assert len(new_message_list) == 2
