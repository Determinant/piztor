from struct import *
import socket, logging
from select import select
from time import sleep

FORMAT = "%(asctime)-15s %(message)s"
logging.basicConfig(format = FORMAT)
logger = logging.getLogger('piztor_server')
logger.setLevel(logging.INFO)

def get_hex(data):
    return "".join([hex(ord(c))[2:].zfill(2) for c in data])

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

host = "localhost" #"localhost"
port = 2222

def gen_auth(username, password):
    length = _SectionSize.LENGTH + \
                _SectionSize.OPT_ID + \
                len(username) + \
                _SectionSize.PADDING + \
                len(password) + \
                _SectionSize.PADDING

    data = pack("!LB", length, 0x00)
    data += username
    data += "\0"
    data += password
    data += "\0"
    return data

def gen_auth_head_length(token, username):
    return _SectionSize.USER_TOKEN + \
                 len(username) + \
                _SectionSize.PADDING


def gen_update_location(token, username, lat, lng):
    length = _SectionSize.LENGTH + \
                _SectionSize.OPT_ID + \
                gen_auth_head_length(token, username) + \
                _SectionSize.LATITUDE + \
                _SectionSize.LONGITUDE

    data = pack("!LB32s", length, 0x01, token)
    data += username
    data += chr(0)
    data += pack("!dd", lat, lng)
    return data

def gen_request_location(token, username, comp_id, sec_id):
    length = _SectionSize.LENGTH + \
                _SectionSize.OPT_ID + \
                gen_auth_head_length(token, username) + \
                _SectionSize.GROUP_ID

    data = pack("!LB32s", length, 0x02, token)
    data += username
    data += chr(0)
    data += pack("!BB", comp_id, sec_id)
    return data


def gen_request_user_info(token, username, uid):
    length = _SectionSize.LENGTH + \
                _SectionSize.OPT_ID + \
                gen_auth_head_length(token, username) + \
                _SectionSize.USER_ID

    data = pack("!LB32s", length, 0x03, token)
    data += username
    data += chr(0)
    data += pack("!L", uid)
    return data

def gen_logout(token, username):
    length = _SectionSize.LENGTH + \
                _SectionSize.OPT_ID + \
                gen_auth_head_length(token, username)
    data = pack("!LB32s", length, 0x04, token)
    data += username
    data += chr(0)
    return data

def gen_open_push_tunnel(token, username):
    length = _SectionSize.LENGTH + \
                _SectionSize.OPT_ID + \
                gen_auth_head_length(token, username)
    data = pack("!LB32s", length, 0x05, token)
    data += username
    data += chr(0)
    return data

def gen_send_text_mesg(token, username, mesg):
    length = _SectionSize.LENGTH + \
                _SectionSize.OPT_ID + \
                gen_auth_head_length(token, username) + \
                len(mesg) + \
                _SectionSize.PADDING
    data = pack("!LB32s", length, 0x06, token)
    data += username
    data += chr(0)
    data += mesg
    data += chr(0)
    return data

def send(data):
    received = bytes()
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((host, port))
        sock.sendall(data)
        while True:
            rd, wr, err = select([sock], [], [], 10)
            if rd:
                buff = sock.recv(4096)
                if len(buff) == 0:
                    break
                received += buff
            else:
                break
    finally:
        sock.close()
    return received

def request_location(token, username, comp_id, sec_id):
    resp = send(gen_request_location(token, username, comp_id, sec_id))
    try:
        pl, optcode, status = unpack("!LBB", resp[:6])
        if pl != len(resp):
            logger.error("Request location: incorrect packet length")
        idx = 6
        while idx < pl:
            uid, lat, lng = unpack("!Ldd", resp[idx:idx + 20])
            idx += 20
            print (uid, lat, lng)
    except error:
        logger.error("Request location: can not parse the response")

def user_auth(username, password):
    resp = send(gen_auth(username, password))
    try:
        pl, optcode, status, uid, token = unpack("!LBBL32s", resp)
        if pl != len(resp):
            logger.error("User authentication: incorrect packet length")
        print "status: " + str(status)
        print "uid: " + str(uid)
        print "token: " + get_hex(token)
    except error:
        logger.error("User authentication: can not parse the response")

    return uid, token

def update_location(token, username, lat, lng):
    resp = send(gen_update_location(token, username, lat, lng)) 
    print get_hex(resp)
    try:
        pl, optcode, status = unpack("!LBB", resp[:6])
        if pl != len(resp):
            logger.error("Request location: incorrect packet length")
        print "status: " + str(status)
    except error:
        logger.error("Request location: can not parse the response")


def request_user_info(token, username, uid):
    resp = send(gen_request_user_info(token, username, uid))
    try:
        pl, optcode, status = unpack("!LBB", resp[:6])
        if pl != len(resp):
            logger.error("Request user info: incorrect packet length")
    
        idx = 6
        comp_id = None
        sec_id = None
        sex = None
        while idx < pl:
            info_key, = unpack("!B", resp[idx:idx + 1])
            idx += 1
            if info_key == 0x00:
                comp_id, sec_id = unpack("!BB", resp[idx:idx + 2])
                idx += 2
            elif info_key == 0x01:
                sex, = unpack("!B", resp[idx:idx + 1])
                idx += 1
        return comp_id, sec_id, sex
    except error:
        logger.error("Request user info: can not parse the response")

def logout(token, username):
    resp = send(gen_logout(token, username))
    try:
        pl, optcode, status = unpack("!LBB", resp)
        if pl != len(resp):
            logger.error("Logout: incorrect packet length")
        print "status: " + str(status)
    except error:
        logger.error("Logout: can not parse the response")

def send_text_mesg(token, username, mesg):
    resp = send(gen_send_text_mesg(token, username, mesg))
    try:
        pl, optcode, status = unpack("!LBB", resp)
        if pl != len(resp):
            logger.error("Send text mesg: incorrect packet length")
        print "status: " + str(status)
    except error:
        logger.error("Send text mesg: can not parse the response")

def open_push_tunnel(token, username):

    length = _SectionSize.LENGTH + \
                _SectionSize.OPT_ID + \
                gen_auth_head_length(token, username)
    data = pack("!LB32s", length, 0x05, token)
    data += username
    data += chr(0)

    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((host, port))
    sock.sendall(data)
    sock.recv(4096)
    
    length = -1
    while True:
        received = bytes()
        while True:
            if len(received) > 4:
                length, optcode = unpack("!LB", received[:5])

            if len(received) == length:
                break
            rd, wr, err = select([sock], [], [])
            if rd:
                buff = sock.recv(4096)
                if len(buff) == 0:
                    break
                received += buff
            else:
                break
        print len(received)
        pl, optcode, fingerprint = unpack("!LB32s", received[:37])
        mesg = received[37:-1]
        logger.info("Received a push: %s", mesg)
        sock.sendall(pack("!LB32s", 37, 0x00, fingerprint))
