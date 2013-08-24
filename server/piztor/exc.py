class PiztorError(Exception):
    pass

class ConnectionError(PiztorError):
    pass

class ReqReadError(ConnectionError):
    pass

class BadReqError(ConnectionError):
    pass

class InvalidTokenError(ConnectionError):
    pass
