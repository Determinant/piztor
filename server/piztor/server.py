from twisted.internet.protocol import Protocol
from twisted.internet.protocol import Factory
from twisted.internet.endpoints import TCP4ServerEndpoint
from twisted.internet import reactor

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.orm.exc import NoResultFound, MultipleResultsFound

import struct
import os
import logging

from exc import *
from model import *

def get_hex(data):
    return "".join([hex(ord(c))[2:].zfill(2) for c in data])

def print_datagram(data):
    print "=================================="
    print "Received datagram:"
    print get_hex(data)
    print "=================================="

db_path = "piztor.sqlite"
FORMAT = "%(asctime)-15s %(message)s"
logging.basicConfig(format = FORMAT)
logger = logging.getLogger('piztor_server')
logger.setLevel(logging.INFO)


class _SectionSize:
    LENGTH = 4
    OPT_ID = 1
    STATUS = 1
    USER_ID = 4
    USER_TOKEN = 32
    GROUP_ID = 4
    ENTRY_CNT = 4
    LATITUDE = 8
    LONGITUDE = 8
    LOCATION_ENTRY = USER_ID + LATITUDE + LONGITUDE
    PADDING = 1

class _OptCode:
    user_auth = 0x00
    location_update = 0x02
    location_request= 0x03

class _StatusCode:
    sucess = 0x00
    failure = 0x01

class RequestHandler(object):
    def __init__(self):
        self.engine = create_engine('sqlite:///' + db_path, echo = False)
        self.Session = sessionmaker(bind = self.engine)

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
            raise DBCorruptedError()

    @classmethod
    def trunc_padding(cls, data):
        leading = bytes()  
        for i in xrange(len(data)):
            ch = data[i]
            if ch == '\x00':
                print get_hex(leading), get_hex(data[i + 1:])
                return (leading, data[i + 1:])
            else:
                leading += ch
        # padding not found
        return (None, data)

class UserAuthHandler(RequestHandler):

    _user_auth_response_size = \
            _SectionSize.LENGTH + \
            _SectionSize.OPT_ID + \
            _SectionSize.STATUS + \
            _SectionSize.USER_ID + \
            _SectionSize.USER_TOKEN

    def handle(self, tr_data):
        logger.info("Reading auth data...")
        pos = -1
        for i in xrange(0, len(tr_data)):
            if tr_data[i] == '\x00':
                pos = i
                break
        if pos == -1:
            raise BadReqError("Authentication: Malformed request body")

        username = tr_data[0:pos]
        password = tr_data[pos + 1:]
        logger.info("Trying to login with " \
                    "(username = {0}, password = {1})" \
                .format(username, password))

        session = self.Session()
        try:
            user = session.query(UserModel) \
                .filter(UserModel.username == username).one()
        except NoResultFound:
            logger.info("No such user: {0}".format(username))
            return struct.pack("!LBBL32s",  UserAuthHandler \
                                                ._user_auth_response_size,
                                            _OptCode.user_auth, 
                                            _StatusCode.failure,
                                            0,
                                            bytes('\x00' * 32))

        except MultipleResultsFound:
            raise DBCorruptedError()

        uauth = user.auth
        if uauth is None:
            raise DBCorruptedError()
        if not uauth.check_password(password):
            logger.info("Incorrect password: {0}".format(username))
            return struct.pack("!LBBL32s",  UserAuthHandler \
                                                ._user_auth_response_size,
                                            _OptCode.user_auth,
                                            _StatusCode.failure,
                                            0,
                                            bytes('\x00' * 32))
        else:
            logger.info("Logged in sucessfully: {0}".format(username))
            uauth.regen_token()
            session.commit()
            print "new token generated: " + get_hex(uauth.token)
            return struct.pack("!LBBL32s", UserAuthHandler \
                                                ._user_auth_response_size,
                                            _OptCode.user_auth,
                                            _StatusCode.sucess,
                                            user.id,
                                            uauth.token)


class LocationUpdateHandler(RequestHandler):

#    _location_update_size = \
#            _SectionSize.AUTH_HEAD + \
#            _SectionSize.LATITUDE + \
#            _SectionSize.LONGITUDE

    _location_update_response_size = \
            _SectionSize.LENGTH + \
            _SectionSize.OPT_ID + \
            _SectionSize.STATUS

    def handle(self, tr_data):
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

        session = self.Session()
        uauth = RequestHandler.get_uauth(token, username, session)
        # Authentication failure
        if uauth is None:
            logger.warning("Authentication failure")
            return struct.pack("!LBB",  LocationUpdateHandler \
                                            ._location_update_response_size,
                                        _OptCode.location_update,
                                        _StatusCode.failure)

        ulocation = uauth.user.location
        ulocation.lat = lat
        ulocation.lng = lng
        session.commit()

        logger.info("Location is updated sucessfully")
        return struct.pack("!LBB",  LocationUpdateHandler \
                                        ._location_update_response_size,
                                    _OptCode.location_update,
                                    _StatusCode.sucess)

class LocationRequestHandler(RequestHandler):

#    _location_request_size = \
#            _SectionSize.AUTH_HEAD + \
#            _SectionSize.GROUP_ID

    @classmethod
    def _location_request_response_size(cls, item_num):
        return _SectionSize.LENGTH + \
                _SectionSize.OPT_ID + \
                _SectionSize.STATUS + \
                _SectionSize.ENTRY_CNT + \
                _SectionSize.LOCATION_ENTRY * item_num

    def handle(self, tr_data):
        logger.info("Reading location request data..")

        try:
            token, = struct.unpack("!32s", tr_data[:32])
            username, tail = RequestHandler.trunc_padding(tr_data[32:])
            if username is None:
                raise struct.error
            gid, = struct.unpack("!L", tail)
        except struct.error:
            raise BadReqError("Location request: Malformed request body")

        logger.info("Trying to request locatin with " \
                    "(token = {0}, gid = {1})" \
            .format(get_hex(token), gid))

        session = self.Session()
        uauth = RequestHandler.get_uauth(token, username, session)
        # Auth failure
        if uauth is None:
            logger.warning("Authentication failure")
            return struct.pack("!LBBL", LocationRequestHandler \
                                            ._location_request_response_size(0),
                                        _OptCode.location_request,
                                        _StatusCode.failure,
                                       0)

        ulist = session.query(UserModel).filter(UserModel.gid == gid).all()
        reply = struct.pack(
                "!LBBL", 
                LocationRequestHandler._location_request_response_size(len(ulist)),
                _OptCode.location_request, 
                _StatusCode.sucess,
                len(ulist))

        for user in ulist:
            loc = user.location
            reply += struct.pack("!Ldd", user.id, loc.lat, loc.lng)

        return reply
        
handlers = [UserAuthHandler,
            LocationUpdateHandler,
            LocationRequestHandler]

def check_header(header):
    return 0 <= header < len(handlers)

class PTP(Protocol):

    def __init__(self):
        self.buff = bytes()
        self.length = -1

    def connectionMade(self):
        logger.info("A new connection is made")

    def dataReceived(self, data):
        self.buff += data
        print len(self.buff)
        if len(self.buff) > 4:
            try:
                self.length, self.optcode = struct.unpack("!LB", self.buff[:5])
                if not check_header(self.optcode):    # invalid header
                    raise struct.error
            except struct.error:
                logger.warning("Invalid request header")
                raise BadReqError("Malformed request header")
        print self.length
        if len(self.buff) == self.length:
            h = handlers[self.optcode]()
            reply = h.handle(self.buff[5:])
            logger.info("Wrote: %s", get_hex(reply))
            self.transport.write(reply)
            self.transport.loseConnection()

        elif len(self.buff) > self.length:
            self.transport.loseConnection()


    def connectionLost(self, reason):
        logger.info("The connection is lost")

class PTPFactory(Factory):
    def __init__(self):
        pass
    def buildProtocol(self, addr):
        return PTP()

endpoint = TCP4ServerEndpoint(reactor, 9990)
endpoint.listen(PTPFactory())
reactor.run()
