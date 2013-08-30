from twisted.internet.protocol import Protocol
from twisted.internet.protocol import Factory
from twisted.internet.endpoints import TCP4ServerEndpoint
from twisted.protocols.policies import TimeoutMixin

from sqlalchemy import create_engine, and_
from sqlalchemy.orm import sessionmaker
from sqlalchemy.orm.exc import NoResultFound, MultipleResultsFound

from collections import deque

import struct
import os
import logging

from exc import *
from model import *

def get_hex(data):
    return "".join([hex(ord(c))[2:].zfill(2) for c in data])

db_path = "root:helloworld@localhost/piztor2"
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
    LATITUDE = 8
    LONGITUDE = 8
    PADDING = 1

_MAX_AUTH_HEAD_SIZE = _SectionSize.USER_TOKEN + \
                      MAX_USERNAME_SIZE + \
                      _SectionSize.PADDING
_HEADER_SIZE = _SectionSize.LENGTH + \
                _SectionSize.OPT_ID

_MAX_TEXT_MESG_SIZE = 1024

_MAX_PENDING_PUSH = 100

class _OptCode:
    user_auth =             0x00
    update_location =       0x01
    user_info =             0x02
    update_subscription =   0x03
    user_logout =           0x04
    open_push_tunnel =      0x05
    send_text_mesg =        0x06

class _StatusCode:
    sucess = 0x00
    failure = 0x01

class PushData(object):
    from hashlib import sha256
    def pack(self, optcode, data):
        self.finger_print = sha256(data).digest()
        buff = struct.pack("!B32s", optcode, self.finger_print)
        buff += data
        buff = struct.pack("!L", _SectionSize.LENGTH + len(buff)) + buff
        self.data = buff

class PushTextMesgData(PushData):
    def __init__(self, mesg): 
        self.pack(0x00, mesg + chr(0))

class PushLocationData(PushData):
    def __init__(self, uid, lat, lng):
        self.pack(0x01, struct.pack("!Ldd", uid, lat, lng))
                

class PushTunnel(object):
    def __init__(self):
        self.pending = deque()
        self.conn = None
        self.blocked = False

    def close(self):
        if self.conn:
            self.conn.transport.loseConnection()

    def add(self, pdata):
        logger.info("-- Push data enqued --")
        self.pending.append(pdata)
        if len(self.pending) > _MAX_PENDING_PUSH:
            logger.info("-- Push queue is full, discarded an obsolete push --")
            self.pending.popleft()  # discard old push

    def on_receive(self, data):
        front = self.pending.popleft()
        length, optcode, fingerprint = struct.unpack("!LB32s", data)
        if front.finger_print != fingerprint:
            raise PiztorError
        logger.info("-- Push data confirmed by client --")
        self.blocked = False
        self.push()

    def push(self):
        if self.blocked:
            return
        print "Pushing via " + str(self)
        print "Pending size: " + str(len(self.pending))
        logger.info("Pushing...")
        if (self.conn is None) or len(self.pending) == 0:
            return
        front = self.pending.popleft()
        self.pending.appendleft(front)
        self.conn.transport.write(front.data)
        logger.info("-- Wrote push: %s --", get_hex(front.data))
        self.blocked = True

    def connect(self, conn):
        conn.tunnel = self
        if self.conn:   # only one long-connection per user
            self.conn.transport.loseConnection()
        self.conn = conn

    def on_connection_lost(self, conn):
        if conn == self.conn:
            self.conn = None

def pack_uid(user):
    return struct.pack("!L", user.id)

def pack_username(user):
    buff = user.username
    buff += chr(0)
    return buff

def pack_nickname(user):
    buff = user.nickname
    buff += chr(0)
    return buff

def pack_sex(user):
    return struct.pack("!B", 0x01 if user.sex else 0x00)

def pack_gid(user):
    return struct.pack("!H", user.sec_id)

def pack_lat(user):
    return struct.pack("!d", user.location.lat)

def pack_lng(user):
    return struct.pack("!d", user.location.lng)

class RequestHandler(object):
    push_tunnels = dict()
    def __init__(self):
        Session = sessionmaker(bind = engine)
        self.session = Session()

    def __del__(self):
        self.session.close()

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
            uid = uauth.uid
            if not cls.push_tunnels.has_key(uid):
                cls.push_tunnels[uid] = PushTunnel()
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

    def pack(self, data):
        return struct.pack("!LB", 
                                _SectionSize.LENGTH + \
                                _SectionSize.OPT_ID + \
                                len(data), self._optcode) + data

    _code_map = { 0x01 : pack_uid,
                    0x02 : pack_username,
                    0x03 : pack_nickname,
                    0x04 : pack_sex,
                    0x05 : pack_gid,
                    0x06 : pack_lat,
                    0x07 : pack_lng }

    @classmethod
    def pack_info_entry(cls, user, entry_code):
        pack_method = cls._code_map[entry_code]
        info_key = entry_code
        return struct.pack("!B", info_key) + pack_method(user)

    @classmethod
    def pack_user_entry(cls, user):
        buff = bytes()
        for entry_code in _code_map:
            buff += cls.pack_info_entry(user, entry_code)
        buff += chr(0)
        return buff

    @classmethod
    def pack_sub_list(cls, user):
        buff = bytes()
        for grp in user.sub:
            buff += struct.pack("!H", grp.id)
        buff += chr(0)
        return buff

    @classmethod
    def unpack_sub_list(cls, data):
        res = list()
        idx = 0
        end = len(data) - 1
        while idx < end:
            res.append(struct.unpack("!H", 
                        data[idx:idx + _SectionSize.GROUP_ID]))
        return res


class UserAuthHandler(RequestHandler):

    _optcode = _OptCode.user_auth
    _max_tr_data_size = MAX_USERNAME_SIZE + \
                        _SectionSize.PADDING + \
                        MAX_PASSWORD_SIZE + \
                        _SectionSize.PADDING

    _failed_response = \
            lambda : self.pack(struct.pack("!B", _StatusCode.failure))


    def handle(self, tr_data, conn):
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
            return self._failed_response()

        except MultipleResultsFound:
            raise DBCorruptionError()

        uauth = user.auth
        if uauth is None:
            raise DBCorruptionError()
        if not uauth.check_password(password):
            logger.info("Incorrect password: {0}".format(password))
            return self._failed_response()
        else:
            logger.info("Logged in sucessfully: {0}".format(username))
            uauth.regen_token()
            #logger.info("New token generated: " + get_hex(uauth.token))
            self.session.commit()
            # STATUS |  USER_TOKEN
            reply = struct.pack("!B32s",  _StatusCode.sucess, uauth.token)
            # USER_ENTRY
            reply += RequestHandler.pack_user_entry(user)
            reply += RequestHandler.pack_sub_list(user)
            return self.pack(reply)


class UpdateLocationHandler(RequestHandler):

    _optcode = _OptCode.update_location
    _max_tr_data_size = _MAX_AUTH_HEAD_SIZE + \
                        _SectionSize.LATITUDE + \
                        _SectionSize.LONGITUDE

    def handle(self, tr_data, conn):
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
            return self.pack(struct.pack("!B", _StatusCode.failure))

        loc = uauth.user.location
        loc.lat = lat
        loc.lng = lng

        self.session.commit()
        logger.info("Location is updated sucessfully")

        pt = RequestHandler.push_tunnels
        u = uauth.user
        comp = self.session.query(GroupInfo) \
                .filter(GroupInfo.id == u.comp_id).one()
        sec = self.session.query(GroupInfo) \
                .filter(GroupInfo.id == u.sec_id).one()

        pdata = PushLocationData(u.id, lat, lng)
        for user in comp.subscribers:
            uid = user.id
            if uid == uauth.uid: continue
            if pt.has_key(uid):
                tunnel = pt[uid]
                tunnel.add(pdata)
                tunnel.push()

        for user in sec.subscribers:
            uid = user.id
            if uid == uauth.uid: continue
            if pt.has_key(uid):
                tunnel = pt[uid]
                tunnel.add(pdata)
                tunnel.push()

        return self.pack(struct.pack("!B", _StatusCode.sucess))


class UserInfoHandler(RequestHandler):

    _optcode = _OptCode.user_info
    _max_tr_data_size = _MAX_AUTH_HEAD_SIZE + \
                        _SectionSize.USER_ID

    _failed_response = \
            lambda : self.pack(struct.pack("!B", _StatusCode.failure))


    def handle(self, tr_data, conn):
        self.check_size(tr_data)
        logger.info("Reading user info request data...")
        try:
            token, = struct.unpack("!32s", tr_data[:32])
            username, tail = RequestHandler.trunc_padding(tr_data[32:])
            if username is None:
                raise struct.error
            gid, = struct.unpack("!H", tail)
        except struct.error:
            raise BadReqError("User info request: Malformed request body")

        logger.info("Trying to user info with " \
                    "(token = {0}, gid = {1})" \
            .format(get_hex(token), gid))

        uauth = RequestHandler.get_uauth(token, username, self.session)
        # Auth failure
        if uauth is None:
            logger.warning("Authentication failure")
            return self._failed_response()
        # TODO: check the relationship between user and quser
        u = uauth.user 

        grp = self.session.query(UserModel) \
                        .filter(UserModel.sec_id == gid)
        grp += self.session.query(UserModel) \
                        .filter(UserModel.comp_id == gid)

        reply = struct.pack("!B", _StatusCode.sucess)
        for user in grp:
            reply += RequestHandler.pack_user_entry(user)
        return self.pack(reply)

class UpdateSubscription(RequestHandler):

    _optcode = _OptCode.update_subscription
    _max_tr_data_size = _MAX_AUTH_HEAD_SIZE + \
                        _SectionSize.LATITUDE + \
                        _SectionSize.LONGITUDE

    def _find_or_create_group(self, gid, session):
        q = self.session.query(GroupInfo).filter(GroupInfo.id == gid)
        entry = q.first()
        if not entry:
            entry = GroupInfo(gid = gid)
        self.session.commit()
        return entry

    def handle(self, tr_data, conn):
        self.check_size(tr_data)
        logger.info("Reading update subscription data...")
        try:
            token, = struct.unpack("!32s", tr_data[:32])
            username, tail = RequestHandler.trunc_padding(tr_data[32:])
            if username is None: 
                raise struct.error
            sub_list = RequestHandler.unpack_sub_list(tail)
        except struct.error:
            raise BadReqError("Location update: Malformed request body")

        logger.info("Trying to update location with "
                    "(token = {0}, username = {1}, lat = {2}, lng = {3})"\
                .format(get_hex(token), username, lat, lng))

        uauth = RequestHandler.get_uauth(token, username, self.session)
        # Authentication failure
        if uauth is None:
            logger.warning("Authentication failure")
            return self.pack(struct.pack("!B", _StatusCode.failure))

        uauth.user.sub = map(self._find_or_create_group, sub_list)
        self.session.commit()
        logger.info("Subscription is updated sucessfully")

        return self.pack(struct.pack("!B", _StatusCode.sucess))

class UserLogoutHandler(RequestHandler):

    _optcode = _OptCode.user_logout
    _max_tr_data_size = _MAX_AUTH_HEAD_SIZE

    def handle(self, tr_data, conn):
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
            return self.pack(struct.pack("!B", _StatusCode.failure))
        pt = RequestHandler.push_tunnels
        uid = uauth.uid
        pt[uid].close()
        del pt[uid]
        uauth.regen_token()
        logger.info("User Logged out successfully!")
        self.session.commit()
        return self.pack(struct.pack("!B",  _StatusCode.sucess))

class OpenPushTunnelHandler(RequestHandler):

    _optcode = _OptCode.open_push_tunnel
    _max_tr_data_size = _MAX_AUTH_HEAD_SIZE

    def handle(self, tr_data, conn):
        self.check_size(tr_data)
        logger.info("Reading open push tunnel data...")
        try:
            token, = struct.unpack("!32s", tr_data[:32])
            username, tail = RequestHandler.trunc_padding(tr_data[32:])
            if username is None: 
                raise struct.error
        except struct.error:
            raise BadReqError("Open push tunnel: Malformed request body")

        logger.info("Trying to open push tunnel with "
                    "(token = {0}, username = {1})"\
                .format(get_hex(token), username))

        uauth = RequestHandler.get_uauth(token, username, self.session)
        # Authentication failure
        if uauth is None:
            logger.warning("Authentication failure")
            return self.pack(struct.pack("!B", _StatusCode.failure))

        tunnel = RequestHandler.push_tunnels[uauth.uid]
        pt = RequestHandler.push_tunnels
        uid = uauth.uid
        if pt.has_key(uid):
            tunnel = pt[uid]
            tunnel.connect(conn)

        logger.info("Push tunnel opened successfully!")
        return self.pack(struct.pack("!B", _StatusCode.sucess))

class SendTextMessageHandler(RequestHandler):

    _optcode = _OptCode.send_text_mesg
    _max_tr_data_size = _MAX_AUTH_HEAD_SIZE + \
                        _MAX_TEXT_MESG_SIZE + \
                        _SectionSize.PADDING

    def handle(self, tr_data, conn):
        self.check_size(tr_data)
        logger.info("Reading send text mesg data...")
        try:
            token, = struct.unpack("!32s", tr_data[:32])
            username, tail = RequestHandler.trunc_padding(tr_data[32:])
            mesg = tail[:-1]
            if username is None: 
                raise struct.error
        except struct.error:
            raise BadReqError("Send text mesg: Malformed request body")

        logger.info("Trying to send text mesg with "
                    "(token = {0}, username = {1})"\
                .format(get_hex(token), username))

        uauth = RequestHandler.get_uauth(token, username, self.session)
        # Authentication failure
        if uauth is None:
            logger.warning("Authentication failure")
            return self.pack(struct.pack("!B", _StatusCode.failure))

        pt = RequestHandler.push_tunnels
        u = uauth.user
        ulist = self.session.query(UserModel) \
                .filter(UserModel.sec_id == u.sec_id).all()

        for user in ulist:
            uid = user.id
            if uid == uauth.uid: continue
            if pt.has_key(uid):
                tunnel = pt[uid]
                tunnel.add(PushTextMesgData(mesg))
                tunnel.push()
        logger.info("Sent text mesg successfully!")
        return self.pack(struct.pack("!B", _StatusCode.sucess))


class PTP(Protocol, TimeoutMixin):

    handlers = [UserAuthHandler,
                UpdateLocationHandler,
                UserInfoHandler,
                UpdateSubscription,
                UserLogoutHandler,
                OpenPushTunnelHandler,
                SendTextMessageHandler]

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
        self.tunnel = None

    def timeoutConnection(self):
        logger.info("The connection times out")
        self.transport.loseConnection()

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

            if len(self.buff) >= self.length:
                buff = self.buff[:self.length]
                self.buff = self.buff[self.length:]
                if self.tunnel:   # received push response
                    self.tunnel.on_receive(buff)
                    self.length = -1
                    return
                h = PTP.handlers[self.optcode]()
                reply = h.handle(buff[5:], self)
                logger.info("Wrote: %s", get_hex(reply))
                self.transport.write(reply)
                if self.tunnel:
                    logger.info("Blocking the client...")
                    self.tunnel.push()
                    self.length = -1
                    self.setTimeout(None)
                    return
                self.transport.loseConnection()
        except BadReqError as e:
            logger.warn("Rejected a bad request: %s", str(e))
            self.transport.loseConnection()
        except DBCorruptionError:
            logger.error("*** Database corruption ***")
            self.transport.loseConnection()
        if self.tunnel is None:
            self.transport.loseConnection()

    def connectionLost(self, reason):
        if self.tunnel:
            self.tunnel.on_connection_lost(self)
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
reactor.listenTCP(2223, f)
logger.warning("The server is lanuched")
reactor.run()
