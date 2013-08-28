from random import random
from time import sleep

from sys import argv
from ptp_send import *

username = "a"
password = "a"
#username = "1234567890123456789012"
#password = "world12345678901234567890"

if len(argv) == 2:
    host = argv[1]

if len(argv) == 3:
    username = argv[1]
    password = argv[2]


for i in xrange(10):

    uid, token = user_auth(username, password)
    update_location(token, username, random(), random()) 
    
    comp_id, sec_id, sex = request_user_info(token, username, uid)
    if comp_id:
        request_location(token, username, comp_id, sec_id)    
        request_location(token, username, comp_id, 0xff)

        logout(token, username)

    sleep(10)
