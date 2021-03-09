import base64

from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes


def decrypt(content_to_decrypt, public_key) -> bytearray:
    bytes = bytearray("", 'utf-8')
    # TODO: add decryption functionality using content_to_decrypt and public_key
    return bytes


def decrypt_aes(content_to_decrypt, secret_key) -> bytes:
    """
    Decrypts content_to_decrypt using the AES algorithm
    content_to_decrypt 16-bytes block length
    secret_key 32-bytes length

    :param content_to_decrypt: bytes
    :param secret_key: string
    :return: decrypted bytes
    """
    backend = default_backend()

    key = base64.b64decode(secret_key)
    enc = base64.b64decode(content_to_decrypt)

    iv = b'0705090625458532'
    cipher = Cipher(algorithms.AES(key), modes.CBC(iv), backend=backend)

    decryptor = cipher.decryptor()

    return unpad(decryptor.update(enc) + decryptor.finalize())


def unpad(s):
    return s[:-ord(s[len(s) - 1:])]


