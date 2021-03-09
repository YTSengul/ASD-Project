import base64

from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes

BLOCK_SIZE = 16


def encrypt(content_to_encrypt, public_key) -> bytearray:
    # TODO: add encryption functionality using content_to_encrypt and public_key
    return content_to_encrypt.encode()


def encrypt_aes(content_to_encrypt, secret_key) -> bytes:
    backend = default_backend()
    key = base64.b64decode(secret_key)

    raw = pad(content_to_encrypt)
    iv = b'0705090625458532'
    cipher = Cipher(algorithms.AES(key), modes.CBC(iv), backend=backend)
    encryptor = cipher.encryptor()

    return base64.b64encode(encryptor.update(raw.encode()) + encryptor.finalize())


def pad(s):
    return s + (BLOCK_SIZE - len(s) % BLOCK_SIZE) * chr(BLOCK_SIZE - len(s) % BLOCK_SIZE)


