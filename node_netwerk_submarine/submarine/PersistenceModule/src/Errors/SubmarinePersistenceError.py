class SubmarinePersistenceError(Exception):
    """Raise when something goes wrong when using the database of submarine"""

    def __init__(self, error, message):
        # Call the base class constructor with the parameters it needs
        super(Exception, self).__init__(error, message)

        self.error = error
        self.message = message
