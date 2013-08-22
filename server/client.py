import socket
import sys
from struct import *

HOST, PORT = "localhost", 9999
data = ""

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

#data = pack("!B", 0)
#data += "hello"
#data += "\0"
#data += "world"

data = pack("!BLL", 1, 1234, 5678)
data += "hello, world!"

#data = pack("!BLdd", 2, 1234, 123.123, 12323.23222)
print data

try:

    sock.connect((HOST, PORT))
    sock.sendall(data)
    sock.shutdown(socket.SHUT_WR)
    received = sock.recv(1024)
finally:
    sock.close()

print "Sent:  {}".format(data)
print "Received: {}".format(received[0])
