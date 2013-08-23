import socket
import sys
from struct import *
from random import random
from time import sleep

def get_hex(data):
    return "".join([hex(ord(c))[2:].zfill(2) for c in data])

HOST, PORT = "localhost", 9999

def gen_auth(username, password):
    data = pack("!B", 0)
    data += username
    data += "\0"
    data += password
    return data

def gen_update_location(token, lat, lont):
    return pack("!BLdd", 2, token, lat, lont)

def send(data):
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((HOST, PORT))
        print "sent."
        sock.sendall(data)
        sock.shutdown(socket.SHUT_WR)
        received = sock.recv(1024)
    finally:
        print "adf"
        sock.close()

    print "Sent".format(get_hex(data))
    print "Received: {}".format(get_hex(data))
    return received

rec = send(gen_auth("hello", "world"))
opt, token, status = unpack("!BLB", rec)
print "status:" + str(status)

for i in range(10):
    rec = send(gen_update_location(token, random(), random()))
    opc, status = unpack("!BB", rec)
    print "status:" + str(status)
    sleep(10)
