import socket
from struct import *
from random import random
from select import select
from time import sleep

def get_hex(data):
    return "".join([hex(ord(c))[2:].zfill(2) for c in data])

host = "localhost"
port = 2222

def gen_auth(username, password):
    length = 4 + 1 + len(username) + 1 + len(password) + 1
    data = pack("!LB", length, 0x00)
    data += username
    data += "\0"
    data += password
    data += "\0"
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


def gen_request_user_info(token, username, uid):
    length = 4 + 1 + 32 + len(username) + 1 + 4
    data = pack("!LB32s", length, 0x03, token)
    data += username
    data += chr(0)
    data += pack("!L", uid)
    return data

def gen_logout(token, username):
    length = 4 + 1 + 32 + len(username) + 1
    data = pack("!LB32s", length, 0x04, token)
    data += username
    data += chr(0)
    return data

def send(data):
    received = bytes()
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((host, port))
        #print len(data)
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

username = "hello"
password = "world"
#username = "1234567890123456789012"
#password = "world12345678901234567890"
failed_cnt = 0

if len(argv) == 2:
    host = argv[1]

if len(argv) == 3:
    username = argv[1]
    password = argv[2]

for i in xrange(10):
    resp = send(gen_auth(username, password))
    try:
        pl, optcode, status, uid, token = unpack("!LBBL32s", resp)
    except:
        print "fuck1"
        failed_cnt += 1
        continue
    if pl != len(resp): print "God!"
    print "size: " + str((pl, len(resp)))
    print "opt: " + str(optcode)
    print "status: " + str(status)
    print "uid: " + str(uid)
    print "token: " + get_hex(token)
    
    resp = send(gen_update_location(token, username, random(), random()))
    try:
        pl, optcode, status = unpack("!LBB", resp)
    except:
        print "fuck2"
    if pl != len(resp): print "God!"
    print "size: " + str((pl, len(resp)))
    print "opt: " + str(optcode)
    print "status: " + str(status)

    resp = send(gen_request_user_info(token, username, uid))
    try:
        pl, optcode, status = unpack("!LBB", resp[:6])
    except:
        print "fuck5"
    if pl != len(resp): print "God!"
    print "size: " + str((pl, len(resp)))
    
    idx = 6
    try:
        while idx < pl:
            info_key, = unpack("!B", resp[idx:idx + 1])
            idx += 1
            if info_key == 0x00:
                gid, = unpack("!L", resp[idx:idx + 4])
                idx += 4
                print "gid: {}".format(str(gid))
            elif info_key == 0x01:
                sex, = unpack("!B", resp[idx:idx + 1])
                idx += 1
                print "sex: {}".format(str(sex))
    except:
        print "fuck6"
    
    resp = send(gen_request_location(token, username, gid))
    try:
        pl, optcode, status = unpack("!LBB", resp[:6])
    except:
        print "fuck3"
    if pl != len(resp): print "God!"
    print "size: " + str((pl, len(resp)))
    idx = 6
    print "length: " + str(len(resp[6:]))
    try:
        while idx < pl:
            print len(resp[idx:idx + 20])
            uid, lat, lng = unpack("!Ldd", resp[idx:idx + 20])
            idx += 20
            print (uid, lat, lng)
    except:
        print "fuck4"
    

    resp = send(gen_logout(token, username))
    try:
        pl, optcode, status = unpack("!LBB", resp)
    except:
        print "fuck7"
    if pl != len(resp): print "God!"
    print "size: " + str((pl, len(resp)))
    print "opt: " + str(optcode)
    print "status: " + str(status)
    sleep(10)

print failed_cnt
