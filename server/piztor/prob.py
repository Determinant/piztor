import socket
from struct import *
from random import random

def get_hex(data):
    return "".join([hex(ord(c))[2:].zfill(2) for c in data])

host = "localhost"
port = 9990

def gen_auth(username, password):
    length = 4 + 1 + len(username) + 1 + len(password)
    data = pack("!LB", length, 0x00)
    data += username
    data += "\0"
    data += password
    return data

def gen_update_location(token, username, lat, lng):
    length = 4 + 1 + 32 + 8 + 8 + len(username) + 1
    data = pack("!LB32s", length, 0x01, token)
    data += username
    data += chr(0)
    data += pack("!dd", lat, lng)
    return data

def gen_request_location(token, username, gid):
    length = 4 + 1 + 32 + 4 + len(username) + 1
    data = pack("!LB32s", length, 0x02, token)
    data += username
    data += chr(0)
    data += pack("!L", gid)
    return data


def send(data):
    received = None
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((host, port))
        print len(data)
        sock.sendall(data)
        received = sock.recv(1024)
    finally:
        sock.close()
    return received

from sys import argv

if len(argv) == 2:
    host = argv[1]

username = "hello"
password = "world"
gid = 1

resp = send(gen_auth(username, password))
pl, optcode, status, uid, token = unpack("!LBBL32s", resp)
print "size: " + str((pl, len(resp)))
print "opt: " + str(optcode)
print "status: " + str(status)
print "uid: " + str(uid)
print "token: " + get_hex(token)

resp = send(gen_update_location(token, username, random(), random()))
pl, optcode, status = unpack("!LBB", resp)
print "size: " + str((pl, len(resp)))
print "opt: " + str(optcode)
print "status: " + str(status)

resp = send(gen_request_location(token, username, gid))
print len(resp)
pl, optcode, status, length = unpack("!LBBL", resp[:10])
print "size: " + str((pl, len(resp)))
idx = 10
print "length: " + str(len(resp[10:]))
for i in xrange(length):
    print len(resp[idx:idx + 20])
    uid, lat, lng = unpack("!Ldd", resp[idx:idx + 20])
    idx += 20
    print (uid, lat, lng)
