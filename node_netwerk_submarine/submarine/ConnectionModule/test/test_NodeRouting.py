import mock
import pytest
from jsonschema import ValidationError

from submarine.ConnectionModule.src import NodeRouting

mock_socket = mock.Mock()
mock_socket.recv.return_value = mock_socket


def test_validation_failed_integration():
    with pytest.raises(ValidationError):
        NodeRouting.validate_message("hallo", mock_socket)


def test_validation_succeeds_integration():
    assert NodeRouting.validate_message(
        '{"destination":{"hostname":"127.0.0.1","port":1234,"alias":"C0eN"},"command":"relay","data":"Please work"}',
        mock_socket) == {'destination': {'hostname': '127.0.0.1', 'port': 1234, 'alias': 'C0eN'}, 'command': 'relay',
                         'data': 'Please work'}, mock_socket

