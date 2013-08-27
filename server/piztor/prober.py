import socket, logging
from struct import *
from random import random
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
    GROUP_ID = 4
    ENTRY_CNT = 4
    LATITUDE = 8
    LONGITUDE = 8
    LOCATION_ENTRY = USER_ID + LATITUDE + LONGITUDE
    PADDING = 1

host = "localhost"
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

def gen_request_location(token, username, gid):
    length = _SectionSize.LENGTH + \
                _SectionSize.OPT_ID + \
                gen_auth_head_length(token, username) + \
                _SectionSize.GROUP_ID

    data = pack("!LB32s", length, 0x02, token)
    data += username
    data += chr(0)
    data += pack("!L", gid)
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

from sys import argv

username = "a"
password = "a"
#username = "1234567890123456789012"
#password = "world12345678901234567890"

if len(argv) == 2:
    host = argv[1]

if len(argv) == 3:
    username = argv[1]
    password = argv[2]

def request_location(token, username, gid):
    resp = send(gen_request_location(token, username, gid))
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
        while idx < pl:
            info_key, = unpack("!B", resp[idx:idx + 1])
            idx += 1
            if info_key == 0x00:
                gid, = unpack("!L", resp[idx:idx + 4])
                a, b, comp_id, sec_id = unpack("!BBBB", resp[idx:idx + 4])
                idx += 4
                print "gid: {}".format(str(gid))
            elif info_key == 0x01:
                sex, = unpack("!B", resp[idx:idx + 1])
                idx += 1
                print "sex: {}".format(str(sex))
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


for i in xrange(10):

    uid, token = user_auth(username, password)
    update_location(token, username, random(), random()) 
    
    comp_id, sec_id, sex = request_user_info(token, username, uid)
    request_location(token, username, comp_id * 256 + sec_id)    
    request_location(token, username, comp_id * 256 + 0xff)

    logout(token, username)

    sleep(10)
