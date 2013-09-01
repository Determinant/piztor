from random import random
from time import sleep

from sys import argv
from ptp_send import *

username = "yg"
password = "yg"
mesg = "niu x push!"
#username = "1234567890123456789012"
#password = "world12345678901234567890"

if len(argv) == 2:
    host = argv[1]

if len(argv) == 4:
    username = argv[1]
    password = argv[2]
    mesg = argv[3]


token = user_auth(username, password)
#update_location(token, username, 31.028616, 121.434661)
for i in xrange(100):
    update_location(token, username, 31.028616, 121.434661)
    set_marker(token, username, 10.028716, 121.545661, 0x7fffffff)
    send_text_mesg(token, username, mesg)
#send_text_mesg(token, username, "a")
#send_text_mesg(token, username, "the last")
#update_sub(token, username, [(0, 0)])
logout(token, username)
