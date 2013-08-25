import sqlalchemy
import SocketServer, socket, select
import struct
import os

from sqlalchemy import create_engine
from sqlalchemy import Column, Integer, String, Float
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from random import randint

engine = create_engine('sqlite:///t.sqlite', echo = False)
Base = declarative_base()
Session = sessionmaker(bind=engine)

def get_hex(data):
    return "".join([hex(ord(c))[2:].zfill(2) for c in data])

class PiztorError(Exception):
    def __init__(self, msg):
        self.err_msg = msg
    def __str__(self, msg):
        return self.err_msg

class ConnectionError(PiztorError):
    pass

class ReqReadError(ConnectionError):
    def __init__(self):
        super(ReqReadError, self).__init__("Error while reading request")

class ReqInvalidError(ConnectionError):
    def __init__(self):
        super(ReqInvalidError, self).__init__("Invalid request")

class TokenInvalidError(ConnectionError):
    def __init__(self):
        super(TokenInvalidError, self).__init__("Invalid token")

class DataManager(object):
    def __init__(self, piz_srv):
        self.piz_srv = piz_srv

class UserManager(DataManager):

    class User(Base):
        __tablename__ = 'users'
        id = Column(Integer, primary_key = True)
        gid = Column(Integer)
        username = Column(String)
        password = Column(String)
        token = Column(Integer)

    def get_user_by_token(self, token):
        session = Session()
        User = UserManager.User
        entries = session.query(User).filter(User.token == token).all()
        if len(entries) == 0:
            raise TokenInvalidError()
        return entries[0]

    def authentication_handle(self, opt_type, data):
        print "Parsing User Data"
        pos = -1
        for i in xrange(0, len(data)):
            if data[i] == '\0':
                print i
                if pos != -1:
                    raise ReqInvalidError()
                pos = i
                break
        if pos == -1:
            raise ReqInvalidError()
        username = data[0:pos]  
        password = data[pos + 1:]

        print "Trying to login with following info:"
        print (username, password)
        
        session = Session()
        entries = session.query(UserManager.User). \
            filter(UserManager.User.username == username).all()
        if len(entries) == 0:
            return struct.pack("!BLB", 0, 0, 1)
        entry = entries[0]
        if entry.password != password:  # Auth failed
            print "Login failed!"
            return struct.pack("!BLB", 0, 0, 1)
        else:                           # Succeeded
            print "Logged in sucessfully!"
            entry.token = randint(0, 2147483647)
            session.commit()
            return struct.pack("!BLB", 0, entry.token, 0)
        

class MesgManager(DataManager):
    def mesg_sending_handle(self, opt_type, data):
        print "Parsing Mesg Data"
        try:
            if len(data) < 8:
                raise ReqInvalidError()
            sender_token, recv_id = struct.unpack("!LL", data[:8])
            msg = data[8:]
            print (sender_token, recv_id, msg)
            return struct.pack("!B", 1)
        except struct.error:
            raise ReqInvalidError()

class LocationManager(DataManager):

    class LocationInfo(Base):
        __tablename__ = "location_info"
        uid = Column(Integer, primary_key = True)
        lat = Column(Float(precesion = 64))
        lng = Column(Float(precesion = 64))
        # More: last_update

    def location_update_handle(self, opt_type, data):
        print "Parsing a Location Update"
        try:
            if len(data) < 8:
                raise ReqInvalidError()
            sender_token, lat, lng = struct.unpack("!Ldd", data)
            print "Updating location data with following info:"
            print (sender_token, lat, lng)

            user = self.piz_srv. \
                    user_mgr.get_user_by_token(sender_token)
            session = Session()
            LInfo = LocationManager.LocationInfo
            q = session.query(LInfo).filter(LInfo.uid == user.id)
            entry = q.first()
            entry.lat = lat
            entry.lng = lng
            session.commit()
            print "Location update succeeded!"
            return struct.pack("!BB", 2, 0)
        except TokenInvalidError:
            print "Location update failed!"
            return struct.pack("!BB", 2, 1)
        except struct.error:
            raise ReqInvalidError()

    def location_request_handle(self, opt_type, data):
        print "Parsing a Location Request"
        try:
            if len(data) != 8:
                raise ReqInvalidError()
            sender_token, gid = struct.unpack("!LL", data)
            print "Requesting location data with following info:"
            print (sender_token, gid)
            session = Session()
            UInfo = UserManager.User
            LInfo = LocationManager.LocationInfo
            user_list = session.query(UInfo).filter(UInfo.gid == gid).all()
            reply = struct.pack("!BL", 3, len(user_list))
            for user in user_list:
                loc = session.query(LInfo).filter(LInfo.uid == user.id).first()
                reply += struct.pack("!Ldd", user.id, loc.lat, loc.lng)
            print get_hex(reply)
            return reply
        except struct.error:
            raise ReqInvalidError()

class PiztorServer():


    class GenericHandler(SocketServer.StreamRequestHandler):

        def handle(self):
            print self.piz_srv
            sock = self.request
            sock.settimeout(100)
#           sock.setblocking(0)
            data = ""
            try:
                while True:
#                   ready = select.select([sock], [], [], 10)
#                   if not ready[0]:
#                       raise ReqReadError()
                    buff = sock.recv(4096)
                    if len(buff) == 0:
                        break   # terminated
                    else:
                        data += buff
                sock.shutdown(socket.SHUT_RD)
    
                print "Got the data:" + get_hex(data)
    
                if len(data) < 1: 
                    print "invalid length"
                    raise ReqInvalidError()
                opt_id = struct.unpack("!B", data[0])[0]
                print opt_id
                reply = self.piz_srv.mgr_map[opt_id](opt_id, data[1:])
                sock.sendall(reply)
            finally:
                sock.close()

    class ForkingEchoServer(SocketServer.ForkingMixIn, SocketServer.TCPServer):
        pass

    def __init__(self, host, port):
        PiztorServer.GenericHandler.piz_srv = self
        srv = PiztorServer.ForkingEchoServer((host, port), 
                                            PiztorServer.GenericHandler)
        srv.request_queue_size = 100
#        srv.timeout = 2
        self.server = srv

        self.user_mgr = UserManager(self)
        self.mesg_mgr = MesgManager(self)
        self.location_mgr = LocationManager(self)
    
        self.mgr_map = [ self.user_mgr.authentication_handle, 
                    self.mesg_mgr.mesg_sending_handle, 
                    self.location_mgr.location_update_handle,
                    self.location_mgr.location_request_handle] 
    
        Base.metadata.create_all(engine)


    def run(self):
        try:
            self.server.serve_forever()
        except KeyboardInterrupt:
            print "Exiting..."
            self.server.shutdown() 
            print "Server shutdown"

if __name__ == "__main__":
    
    ps = PiztorServer("localhost", 9990)
    ps.run()
