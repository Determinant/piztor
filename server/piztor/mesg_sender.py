from random import random
from time import sleep

from sys import argv
from ptp_send import *

username = "hello"
password = "world"
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
send_text_mesg(token, username, mesg)
send_text_mesg(token, username, "a")
update_location(token, username, 1.2345, 2.468)
update_location(token, username, 1.2345, 2.468)
send_text_mesg(token, username, "the last")
logout(token, username)
