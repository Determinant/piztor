import socket
import sys
from struct import *

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
        sock.sendall(data)
        sock.shutdown(socket.SHUT_WR)
        received = sock.recv(1024)
    finally:
        print "adf"
        sock.close()

    print "Sent:  {}".format(data)
    print "Received: {}".format(received)
    return received

rec = send(gen_auth("hello", "world"))
opt, token, status = unpack("!BLB", rec)
token = 1
send(gen_update_location(token, 23.33, -54.44))
