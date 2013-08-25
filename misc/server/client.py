import socket
import sys
from struct import *
from random import random
from time import sleep

def get_hex(data):
    return "".join([hex(ord(c))[2:].zfill(2) for c in data])

HOST, PORT = "localhost", 9990

def gen_auth(username, password):
    data = pack("!B", 0)
    data += username
    data += "\0"
    data += password
    return data

def gen_update_location(token, lat, lont):
    return pack("!BLdd", 2, token, lat, lont)

def gen_request_location(token, gid):
    return pack("!BLL", 3, token, gid)

def send(data):
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((HOST, PORT))
#        print "Client " + str(sys.argv[1]) + ": connected"
        sock.sendall(data)
        print get_hex(data)
#        print "Client " + str(sys.argv[1]) + ": sent"
#        sock.shutdown(socket.SHUT_WR)
#        print "Client " + str(sys.argv[1]) + ": shutdown"
        received = sock.recv(1024)
    finally:
        print "adf"
        sock.close()

    print "Sent {}".format(get_hex(data))
    print "Received: {}".format(get_hex(data))
    return received

#print "Client spawned:" + str(sys.argv[1])
rec = send(gen_auth("hello", "world"))
opt, token, status = unpack("!BLB", rec)

rec = send(gen_update_location(token, random(), random()))
opc, status = unpack("!BB", rec)

rec = send(gen_request_location(token, 1))
opc, length = unpack("!BL", rec[:5])
idx = 5
for i in xrange(length):
    uid, lat, lng = unpack("!Ldd", rec[idx:idx + 20])
    print (uid, lat, lng)
    idx += 20
#    sleep(60)
