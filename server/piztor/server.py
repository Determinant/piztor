from twisted.internet.protocol import Protocol
from twisted.internet.protocol import Factory
from twisted.internet.endpoints import TCP4ServerEndpoint
from twisted.protocols.policies import TimeoutMixin

from sqlalchemy import create_engine, and_
from sqlalchemy.orm import sessionmaker
from sqlalchemy.orm.exc import NoResultFound, MultipleResultsFound

import struct
import os
import logging

from exc import *
from model import *

def get_hex(data):
    return "".join([hex(ord(c))[2:].zfill(2) for c in data])

db_path = "root:helloworld@localhost/piztor"
#db_path = "piztor.sqlite"
FORMAT = "%(asctime)-15s %(message)s"
logging.basicConfig(format = FORMAT)
logger = logging.getLogger('piztor_server')
logger.setLevel(logging.INFO)
engine = create_engine('mysql://' + db_path, echo = False, pool_size = 1024)


class _SectionSize:
    LENGTH = 4
    OPT_ID = 1
    STATUS = 1
    USER_ID = 4
    USER_TOKEN = 32
    GROUP_ID = 2
    ENTRY_CNT = 4
    LATITUDE = 8
    LONGITUDE = 8
    LOCATION_ENTRY = USER_ID + LATITUDE + LONGITUDE
    PADDING = 1

_MAX_AUTH_HEAD_SIZE = _SectionSize.USER_TOKEN + \
                      MAX_USERNAME_SIZE + \
                      _SectionSize.PADDING
_HEADER_SIZE = _SectionSize.LENGTH + \
                _SectionSize.OPT_ID

class _OptCode:
    user_auth = 0x00
    location_update = 0x01
    location_info= 0x02
    user_info = 0x03
    user_logout = 0x04

class _StatusCode:
    sucess = 0x00
    failure = 0x01

class RequestHandler(object):
    def __init__(self):
        Session = sessionmaker(bind = engine)
        self.session = Session()

    def __del__(self):
        self.session.close()
#        self.engine.dispose()

    def check_size(self, tr_data):
        if len(tr_data) > self._max_tr_data_size:
            raise BadReqError("Authentication: Request size is too large")

    @classmethod
    def get_uauth(cls, token, username, session):
        try:
            uauth = session.query(UserAuth) \
                    .filter(UserAuth.token == token).one()

            if uauth.user.username != username:
                logger.warning("Toke and username mismatch")
                return None

            return uauth

        except NoResultFound:
            logger.warning("Incorrect token")
            return None

        except MultipleResultsFound:
            raise DBCorruptionError()

    @classmethod
    def trunc_padding(cls, data):
        leading = bytes()  
        for i in xrange(len(data)):
            ch = data[i]
            if ch == '\x00':
                return (leading, data[i + 1:])
            else:
                leading += ch
        # padding not found
        return (None, data)

class UserAuthHandler(RequestHandler):

    _max_tr_data_size = MAX_USERNAME_SIZE + \
                        _SectionSize.PADDING + \
                        MAX_PASSWORD_SIZE + \
                        _SectionSize.PADDING

    _response_size = \
            _SectionSize.LENGTH + \
            _SectionSize.OPT_ID + \
            _SectionSize.STATUS + \
            _SectionSize.USER_ID + \
            _SectionSize.USER_TOKEN

    _failed_response = \
            struct.pack("!LBBL32s", _response_size,
                                    _OptCode.user_auth, 
                                    _StatusCode.failure,
                                    0,
                                    bytes('\x00' * 32))


    def handle(self, tr_data):
        self.check_size(tr_data)
        logger.info("Reading auth data...")
        pos = -1
        for i in xrange(0, len(tr_data)):
            if tr_data[i] == '\x00':
                pos = i
                break
        if pos == -1:
            raise BadReqError("Authentication: Malformed request body")

        username = tr_data[0:pos]
        password = tr_data[pos + 1:-1]
        logger.info("Trying to login with " \
                    "(username = {0}, password = {1})" \
                .format(username, password))

        try:
            user = self.session.query(UserModel) \
                .filter(UserModel.username == username).one()
        except NoResultFound:
            logger.info("No such user: {0}".format(username))
            return self._failed_response

        except MultipleResultsFound:
            raise DBCorruptionError()

        uauth = user.auth
        if uauth is None:
            raise DBCorruptionError()
        if not uauth.check_password(password):
            logger.info("Incorrect password: {0}".format(password))
            return self._failed_response
        else:
            logger.info("Logged in sucessfully: {0}".format(username))
            uauth.regen_token()
            #logger.info("New token generated: " + get_hex(uauth.token))
            self.session.commit()
            return struct.pack("!LBBL32s", self._response_size,
                                           _OptCode.user_auth,
                                           _StatusCode.sucess,
                                           user.id,
                                           uauth.token)


class LocationUpdateHandler(RequestHandler):

    _max_tr_data_size = _MAX_AUTH_HEAD_SIZE + \
                        _SectionSize.LATITUDE + \
                        _SectionSize.LONGITUDE

    _response_size = \
            _SectionSize.LENGTH + \
            _SectionSize.OPT_ID + \
            _SectionSize.STATUS

    def handle(self, tr_data):
        self.check_size(tr_data)
        logger.info("Reading location update data...")
        try:
            token, = struct.unpack("!32s", tr_data[:32])
            username, tail = RequestHandler.trunc_padding(tr_data[32:])
            if username is None: 
                raise struct.error
            lat, lng = struct.unpack("!dd", tail)
        except struct.error:
            raise BadReqError("Location update: Malformed request body")

        logger.info("Trying to update location with "
                    "(token = {0}, username = {1}, lat = {2}, lng = {3})"\
                .format(get_hex(token), username, lat, lng))

        uauth = RequestHandler.get_uauth(token, username, self.session)
        # Authentication failure
        if uauth is None:
            logger.warning("Authentication failure")
            return struct.pack("!LBB",  self._response_size,
                                        _OptCode.location_update,
                                        _StatusCode.failure)

        ulocation = uauth.user.location
        ulocation.lat = lat
        ulocation.lng = lng

        logger.info("Location is updated sucessfully")
        self.session.commit()
        return struct.pack("!LBB",  self._response_size,
                                    _OptCode.location_update,
                                    _StatusCode.sucess)

class LocationInfoHandler(RequestHandler):

    _max_tr_data_size = _MAX_AUTH_HEAD_SIZE + \
                        _SectionSize.GROUP_ID

    @classmethod
    def _response_size(cls, item_num):
        return _SectionSize.LENGTH + \
                _SectionSize.OPT_ID + \
                _SectionSize.STATUS + \
                _SectionSize.LOCATION_ENTRY * item_num

    def handle(self, tr_data):
        self.check_size(tr_data)
        logger.info("Reading location request data..")
        try:
            token, = struct.unpack("!32s", tr_data[:32])
            username, tail = RequestHandler.trunc_padding(tr_data[32:])
            if username is None:
                raise struct.error
            comp_id, sec_id = struct.unpack("!BB", tail)
        except struct.error:
            raise BadReqError("Location request: Malformed request body")

        logger.info("Trying to request locatin with " \
                    "(token = {0}, comp_id = {1}, sec_id = {2})" \
            .format(get_hex(token), comp_id, sec_id))

        uauth = RequestHandler.get_uauth(token, username, self.session)
        # Auth failure
        if uauth is None:
            logger.warning("Authentication failure")
            return struct.pack("!LBB", self._response_size(0),
                                        _OptCode.location_info,
                                        _StatusCode.failure)

        if sec_id == 0xff:  # All members in the company
            ulist = self.session.query(UserModel) \
                    .filter(UserModel.comp_id == comp_id).all()
        else:
            ulist = self.session.query(UserModel) \
                    .filter(and_(UserModel.comp_id == comp_id,
                                UserModel.sec_id == sec_id)).all()
        reply = struct.pack(
                "!LBB", 
                self._response_size(len(ulist)),
                _OptCode.location_info, 
                _StatusCode.sucess)

        for user in ulist:
            loc = user.location
            reply += struct.pack("!Ldd", user.id, loc.lat, loc.lng)

        return reply

def pack_gid(user):
    return struct.pack("!BB", user.comp_id, user.sec_id)

def pack_sex(user):
    return struct.pack("!B", 0x01 if user.sex else 0x00)


class UserInfoHandler(RequestHandler):

    _max_tr_data_size = _MAX_AUTH_HEAD_SIZE + \
                        _SectionSize.USER_ID

    _failed_response_size = \
            _SectionSize.LENGTH + \
            _SectionSize.OPT_ID + \
            _SectionSize.STATUS

    _fail_response = \
        struct.pack("!LBB", _failed_response_size,
                            _OptCode.user_info,
                            _StatusCode.failure)

    _code_map = {0x00 : pack_gid,
                0x01 : pack_sex}

    @classmethod
    def pack_entry(cls, user, entry_code):
        pack_method = cls._code_map[entry_code]
        info_key = entry_code
        return struct.pack("!B", info_key) + pack_method(user)

    def handle(self, tr_data):
        self.check_size(tr_data)
        logger.info("Reading user info request data...")
        try:
            token, = struct.unpack("!32s", tr_data[:32])
            username, tail = RequestHandler.trunc_padding(tr_data[32:])
            if username is None:
                raise struct.error
            uid, = struct.unpack("!L", tail)
        except struct.error:
            raise BadReqError("User info request: Malformed request body")

        logger.info("Trying to user info with " \
                    "(token = {0}, uid = {1})" \
            .format(get_hex(token), uid))

        uauth = RequestHandler.get_uauth(token, username, self.session)
        # Auth failure
        if uauth is None:
            logger.warning("Authentication failure")
            return self._fail_response
        # TODO: check the relationship between user and quser
        user = uauth.user 

        reply = struct.pack("!BB", _OptCode.user_info,
                                    _StatusCode.sucess)
        try:
            quser = self.session.query(UserModel) \
                    .filter(UserModel.id == uid).one()
        except NoResultFound:
            logger.info("No such user: {0}".format(username))
            return self._fail_response

        except MultipleResultsFound:
            raise DBCorruptionError()

        for code in self._code_map:
            reply += UserInfoHandler.pack_entry(quser, code)
        reply = struct.pack("!L", len(reply) + _SectionSize.LENGTH) + reply
        return reply

class UserLogoutHandler(RequestHandler):

    _max_tr_data_size = _MAX_AUTH_HEAD_SIZE

    _response_size = \
            _SectionSize.LENGTH + \
            _SectionSize.OPT_ID + \
            _SectionSize.STATUS

    def handle(self, tr_data):
        self.check_size(tr_data)
        logger.info("Reading user logout data...")
        try:
            token, = struct.unpack("!32s", tr_data[:32])
            username, tail = RequestHandler.trunc_padding(tr_data[32:])
            if username is None: 
                raise struct.error
        except struct.error:
            raise BadReqError("User logout: Malformed request body")

        logger.info("Trying to logout with "
                    "(token = {0}, username = {1})"\
                .format(get_hex(token), username))

        uauth = RequestHandler.get_uauth(token, username, self.session)
        # Authentication failure
        if uauth is None:
            logger.warning("Authentication failure")
            return struct.pack("!LBB",  self._response_size,
                                        _OptCode.location_update,
                                        _StatusCode.failure)
        uauth.regen_token()
        logger.info("User Logged out successfully!")
        self.session.commit()
        return struct.pack("!LBB",  self._response_size,
                                    _OptCode.user_logout,
                                    _StatusCode.sucess)
       

class PTP(Protocol, TimeoutMixin):

    handlers = [UserAuthHandler,
                LocationUpdateHandler,
                LocationInfoHandler,
                UserInfoHandler,
                UserLogoutHandler]

    handler_num = len(handlers)

    _MAX_REQUEST_SIZE = _HEADER_SIZE + \
                        max([h._max_tr_data_size for h in handlers])

    @classmethod
    def check_header(cls, header):
        return 0 <= header < cls.handler_num

    def __init__(self, factory):
        self.buff = bytes()
        self.length = -1
        self.factory = factory

    def timeoutConnection(self):
        logger.info("The connection times out")

    def connectionMade(self):
        logger.info("A new connection is made")
        self.setTimeout(self.factory.timeout)

    def dataReceived(self, data):
        self.buff += data
        self.resetTimeout()
        logger.info("Buffer length is now: %d", len(self.buff))
        if len(self.buff) <= 4:
            return
        try:
            if self.length == -1:
                try:
                    self.length, self.optcode = struct.unpack("!LB", self.buff[:5])
                    if not PTP.check_header(self.optcode):    # invalid header
                        raise struct.error
                except struct.error:
                    raise BadReqError("Malformed request header")
                if self.length > PTP._MAX_REQUEST_SIZE:
                    print self.length, PTP._MAX_REQUEST_SIZE
                    raise BadReqError("The size of remaining part is too big")
            if len(self.buff) == self.length:
                h = PTP.handlers[self.optcode]()
                reply = h.handle(self.buff[5:])
                logger.info("Wrote: %s", get_hex(reply))
                self.transport.write(reply)
                self.transport.loseConnection()
            elif len(self.buff) > self.length:
                raise BadReqError("The actual length is larger than promised")
        except BadReqError as e:
            logger.warn("Rejected a bad request: %s", str(e))
        except DBCorruptionError:
            logger.error("*** Database corruption ***")
        finally:
            self.transport.loseConnection()

    def connectionLost(self, reason):
        logger.info("The connection is lost")
        self.setTimeout(None)

class PTPFactory(Factory):
    def __init__(self, timeout = 10):
        self.timeout = timeout
    def buildProtocol(self, addr):
        return PTP(self)

if os.name!='nt':
    from twisted.internet import epollreactor
    epollreactor.install()
else:
    from twisted.internet import iocpreactor
    iocpreactor.install()

from twisted.internet import reactor

f = PTPFactory()
f.protocol = PTP
reactor.listenTCP(2222, f)
logger.warning("The server is lanuched")
reactor.run()
