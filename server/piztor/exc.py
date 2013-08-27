class PiztorError(Exception):
    pass

class DBCorruptionError(PiztorError):
    pass

class BadReqError(PiztorError):
    pass
