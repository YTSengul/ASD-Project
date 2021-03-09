import json
import time
from datetime import datetime, timedelta, timezone
from threading import Thread

import pymongo
import schedule

from submarine.ConnectionModule.src import CLIENT_CONNECTION_REFUSED_ERROR_MESSAGE
from submarine.ConnectionModule.src.NodeRouting import handle_resend_message
from submarine.PersistenceModule.src import MESSAGE_RESEND_INTERVAL_IN_MINUTES, DATABASE_NAME, DATABASE_HOSTNAME, \
    DATABASE_PORT, MESSAGE_COLLECTION, MESSAGE_SAVED_TIME_IN_DAYS
from submarine.PersistenceModule.src.DatabaseConnection import get_client, get_db, get_collection


def setup_resend_saved_messages():
    now = datetime.now(timezone.utc)
    too_old_date = now - timedelta(days=MESSAGE_SAVED_TIME_IN_DAYS)

    database_client = get_client(DATABASE_HOSTNAME, DATABASE_PORT)
    submarine_database = get_db(database_client, DATABASE_NAME)
    collection = get_collection(submarine_database, MESSAGE_COLLECTION)

    list_with_messages_from_the_database = \
        list(submarine_database.get_collection("messages").find({}).sort("_id", pymongo.DESCENDING))

    resend_saved_messages(collection, list_with_messages_from_the_database, too_old_date)


def resend_saved_messages(collection, message_list, too_old_date) -> None:
    for single_message in message_list:
        time_message_was_sent = single_message['_id'].generation_time
        del single_message['_id']

        if time_message_was_sent < too_old_date:
            delete_message(collection, single_message)
        else:
            reformatted_message = json.dumps(single_message)
            response = handle_resend_message(reformatted_message)

            if response != CLIENT_CONNECTION_REFUSED_ERROR_MESSAGE:
                delete_message(collection, single_message)


def execute_schedule(interval_in_minutes) -> None:
    schedule.every(interval_in_minutes).minutes.do(setup_resend_saved_messages())
    while True:
        schedule.run_pending()
        time.sleep(1)


def delete_message(collection, message):
    collection.delete_one(message)


def start_threads() -> None:
    scan_the_database_for_messages = Thread(target=execute_schedule(MESSAGE_RESEND_INTERVAL_IN_MINUTES))
    scan_the_database_for_messages.start()


if __name__ == "__main__":
    start_threads()
