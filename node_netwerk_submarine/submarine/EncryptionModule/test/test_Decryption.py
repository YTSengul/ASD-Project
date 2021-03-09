import json

import pytest

from submarine.EncryptionModule import AES_KEY
from submarine.EncryptionModule.src import Decryption, Encryption

KEY = "9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q="


def test_decrypt_aes():
    # Init
    to_decrypt = b'TOjKg4DdM+i68zzwqvlEFA=='

    # Call
    result = Decryption.decrypt_aes(to_decrypt, KEY)

    # Assert
    assert result == b'abcd'


def test_decrypt_aes_with_encrypt_aes():
    # Init
    string = "somestring"

    # Call
    e = Encryption.encrypt_aes(string, KEY)
    result = Decryption.decrypt_aes(e, KEY)

    # Assert
    assert result == string.encode()


def test_decrypt_aes_with_wrong_key():
    # Init
    key = "9L8Eis8A3y67SlVhyxDbPdEfd/YK8R717wyD1JFTrf4="
    to_decrypt = b'5F4t6lWVPxejzetFxnG03pqRnvmCNELSBXnCyZKkquk='

    # Call
    result = Decryption.decrypt_aes(to_decrypt, key)

    # Assert
    assert result != b'abcd'


def test_decrypt_aes_with_wrong_bit_length():
    # Init
    to_decrypt = b'5F4t6lWVPxejzetFxnG03pqRnvmCNELSBXnCyZk='

    # Call
    with pytest.raises(ValueError):
        Decryption.decrypt_aes(to_decrypt, KEY)


def test_decrypt_multiple_layers_off_json_message():
    # Init
    text = 'This is hidden'
    data = {'destination': {'hostname': '127.0.0.1', 'port': 25010}, 'command': 'relay', 'data': text}
    data['data'] = Encryption.encrypt_aes(json.dumps(data), AES_KEY).decode()
    data['data'] = Encryption.encrypt_aes(json.dumps(data), AES_KEY).decode()
    data['data'] = Encryption.encrypt_aes(json.dumps(data), AES_KEY).decode()

    # Call
    result_layer1 = json.loads(Decryption.decrypt_aes(data['data'], AES_KEY))
    result_layer2 = json.loads(Decryption.decrypt_aes(result_layer1['data'], AES_KEY))
    result_layer3 = json.loads(Decryption.decrypt_aes(result_layer2['data'], AES_KEY))

    # Assert
    assert result_layer1['data'] != text
    assert result_layer2['data'] != text
    assert result_layer3['data'] == text
