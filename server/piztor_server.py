import sqlalchemy
import SocketServer, socket, select
import struct

from sqlalchemy import create_engine, Column, Integer, String
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

engine = create_engine('sqlite:///t.sqlite', echo = True)
Base = declarative_base()
Session = sessionmaker(bind=engine)

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
        super(ReqInvalidError, self).__init__("Invalid token")

class DataManager(object):
    pass

class UserManager(DataManager):
    class User(Base):
        __tablename__ = 'users'
        id = Column(Integer, primary_key = True)
        username = Column(String)
        password = Column(String)

    def get_uid_by_token(self, token):
        try:
            return self.active_sessions[token]
        except:
            raise TokenInvalidError()

    def __init__(self):
        Base.metadata.create_all(engine)
        self.active_sessions = dict()

    def authentication_handle(self, opt_type, data, srv):
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
        q = session.query(User).filter(User.username == username)
        entry = q.first()
        if entry.password != password:  # Auth failed
            return struct.pack("!BL"
        
        return struct.pack("!BL", 0, 1234)
        

class MesgManager(DataManager):
    def mesg_sending_handle(self, opt_type, data, srv):
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
    def location_update_handle(self, opt_type, data, srv):
        print "Parsing Loc Data"
        try:
            if len(data) < 8:
                raise ReqInvalidError()
            sender_token, lat, lont = struct.unpack("!Ldd", data)
            print (sender_token, lat, lont)
            return struct.pack("!B", 2)
        except struct.error:
            raise ReqInvalidError()



class PiztorServer():

    user_mgr = UserManager()
    mesg_mgr = MesgManager()
    location_mgr = LocationManager()

    mgr_map = [ user_mgr.authentication_handle, 
                mesg_mgr.mesg_sending_handle, 
                location_mgr.location_update_handle ] 

    class GenericHandler(SocketServer.StreamRequestHandler):

        def handle(self):
            sock = self.request
            sock.setblocking(0)
            data = ""
            while True:
                ready = select.select([sock], [], [], 1)
                if not ready[0]:
                    raise ReqReadError()
                buff = sock.recv(4096)
                if len(buff) == 0:
                    break   # terminated
                else:
                    data += buff
            sock.shutdown(socket.SHUT_RD)

            print "Got the data:"
            print data
            print "===="

            if len(data) < 1: 
                raise ReqInvalidError()
            opt_id = struct.unpack("!B", data[0])[0]
            reply = PiztorServer.mgr_map[opt_id](opt_id, data[1:], self)
            sock.sendall(reply)
            sock.close()

    def __init__(self, host, port):
        srv = SocketServer.TCPServer((host, port), 
                                    PiztorServer.GenericHandler)
        srv.timeout = 2
        self.server = srv


    def run(self):
        try:
            self.server.serve_forever()
        except KeyboardInterrupt:
            print "Exiting..."
            self.server.shutdown() 
            print "Server shutdown"

if __name__ == "__main__":
    
    ps = PiztorServer("localhost", 9999)
    ps.run()
