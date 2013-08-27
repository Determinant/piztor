class PiztorError(Exception):
    pass

class DBCurruptedError(PiztorError):
    pass

class BadReqError(PiztorError):
    pass
