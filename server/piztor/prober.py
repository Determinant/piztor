from random import random
from time import sleep

from sys import argv
from ptp_send import *

username = "yg"
password = "yg"
#username = "12345678901234567890"
#password = "world123456789012345"

if len(argv) == 2:
    host = argv[1]

if len(argv) == 3:
    username = argv[1]
    password = argv[2]


token = user_auth(username, password)
game_start(token, username)
#check_in(token, username, 0x23)
#change_password(token, username, "ddd", "haha")
#print "Client: " + username + "logged in"
#open_push_tunnel(token, username)
#for i in xrange(100):
#    print "Client: " + username  + " updateing"
#    update_location(token, username, 123.456, 123.456)
##    sleep(5)
