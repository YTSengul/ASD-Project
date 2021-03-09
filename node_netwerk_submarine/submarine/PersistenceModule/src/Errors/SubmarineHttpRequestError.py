class SubmarineHttpRequestError(Exception):
    """Raise when an outgoing HTTP request does not return 200 OK"""

    def __init__(self, message):
        # Call the base class constructor with the parameters it needs
        super(Exception, self).__init__(message)

        self.message = message
