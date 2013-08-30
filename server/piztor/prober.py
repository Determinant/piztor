from random import random
from time import sleep

from sys import argv
from ptp_send import *

username = "haha"
password = "haha"
#username = "12345678901234567890"
#password = "world123456789012345"

if len(argv) == 2:
    host = argv[1]

if len(argv) == 3:
    username = argv[1]
    password = argv[2]


token = user_auth(username, password)

update_sub(token, username, 
        [
            (23, 15), (23, 15), (23, 255), (23, 255),
            (23, 15), (23, 15), (23, 255), (23, 255),
            (23, 15), (23, 15), (23, 255), (23, 255),
            (23, 15), (23, 15), (23, 255), (23, 255),
            (23, 15), (23, 15), 
            ])
user_info(token, username, 23, 15)
