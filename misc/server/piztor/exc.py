class PiztorError(Exception):
    pass

class DBCurruptedError(PiztorError):
    pass

class ConnectionError(PiztorError):
    pass

class ReqReadError(ConnectionError):
    pass

class BadReqError(ConnectionError):
    pass

class BadTokenError(ConnectionError):
    pass
