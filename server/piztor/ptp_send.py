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

host = "202.120.7.4" #"localhost"
port = 2223

def pack_data(optcode, data):
    return pack("!LB", _SectionSize.LENGTH + \
                            _SectionSize.OPT_ID + \
                            len(data), optcode) + data

def gen_auth(username, password):
    return pack_data(0x00, username + chr(0) +  \
                            password + chr(0))

def gen_update_location(token, username, lat, lng):
    data = pack("!32s", token)
    data += username
    data += chr(0)
    data += pack("!dd", lat, lng)
    return pack_data(0x01, data)

def gen_user_info(token, username, gid):
    data = pack("!32s", token)
    data += username
    data += chr(0)
    data += pack("!H", gid)
    return pack_data(0x02, data)

def gen_update_sub(token, username, sub):
    data = pack("!32s", token)
    data += username
    data += chr(0)
    for gid in sub:
        data += pack("!H", gid)
    data += chr(0)
    return pack_data(0x03, data)

def gen_logout(token, username):
    data = pack("!32s", token)
    data += username
    data += chr(0)
    return pack_data(0x04, data)

def gen_open_push_tunnel(token, username):
    data = pack("!32s", token)
    data += username
    data += chr(0)
    return pack_data(0x05, data)

def gen_send_text_mesg(token, username, mesg):
    data = pack("!32s", token)
    data += username
    data += chr(0)
    data += mesg
    data += chr(0)
    return pack_data(0x06, data)

def gen_set_marker(token, username, lat, lng, deadline):
    data = pack("!32s", token)
    data += username
    data += chr(0)
    data += pack("!ddL", lat, lng, deadline)
    return pack_data(0x07, data)

def send(data):
    received = bytes()
    from time import time
    begin = time()
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((host, port))
        sock.sendall(data)
        while True:
            rd, wr, err = select([sock], [], [])
            if rd:
                buff = sock.recv(4096)
                if len(buff) == 0:
                    break
                received += buff
            else:
                break
    finally:
        print "closing"
        sock.shutdown(1)
        sock.close()
    print "Waited for {} seconds".format(str(time() - begin))
    return received

def user_auth(username, password):
    resp = send(gen_auth(username, password))
    try:
        pl, optcode, status, token = unpack("!LBB32s", resp[:38])
        if pl != len(resp):
            logger.error("User authentication: incorrect packet length")
        print "status: " + str(status)
#        print "token: " + get_hex(token)
    except error:
        logger.error("User authentication: can not parse the response")
        print get_hex(resp)

    return token

def update_location(token, username, lat, lng):
    resp = send(gen_update_location(token, username, lat, lng)) 
    try:
        pl, optcode, status = unpack("!LBB", resp[:6])
        if pl != len(resp):
            logger.error("Request location: incorrect packet length")
        print "status: " + str(status)
    except error:
        logger.error("Request location: can not parse the response")


def user_info(token, username, comp_no, sec_no):
    resp = send(gen_user_info(token, username, comp_no * 256 + sec_no))
    try:
        pl, optcode, status = unpack("!LBB", resp[:6])
        if pl != len(resp):
            logger.error("Request location: incorrect packet length")
        print get_hex(resp[6:])
    except error:
        logger.error("Request location: can not parse the response")


def update_sub(token, username, sub):
    sub = map(lambda t: t[0] * 256 + t[1], sub)
    resp = send(gen_update_sub(token, username, sub))
    try:
        pl, optcode, status = unpack("!LBB", resp[:6])
        if pl != len(resp):
            logger.error("Request user info: incorrect packet length")
        print "status: " + str(status)
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

def set_marker(token, username, lat, lng, deadline):
    resp = send(gen_set_marker(token, username, lat, lng, deadline))
    try:
        pl, optcode, status = unpack("!LBB", resp)
        if pl != len(resp):
            logger.error("Set marker: incorrect packet length")
        print "status: " + str(status)
    except error:
        logger.error("Set marker: can not parse the response")

def open_push_tunnel(token, username):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((host, port))
    sock.sendall(gen_open_push_tunnel(token, username))
    print get_hex(sock.recv(6))
    
    while True:
        received = bytes()
        length = -1
        while True:
            if len(received) > 4:
                length, optcode = unpack("!LB", received[:5])

            if len(received) == length:
                break
            rd, wr, err = select([sock], [], [])
            if rd:
                buff = sock.recv(1)
                if len(buff) == 0:
                    break
                received += buff
            else:
                break
        print "received: " + str(len(received))
        pl, optcode, fingerprint = unpack("!LB32s", received[:37])
        mesg = received[37:]
        logger.info("Received a push: %s", get_hex(mesg))
        sock.sendall(pack("!LB32s", 37, optcode, fingerprint))
