from random import random
from time import sleep

from sys import argv
from ptp_send import *

username = "hello"
password = "world"
#username = "1234567890123456789012"
#password = "world12345678901234567890"

if len(argv) == 2:
    host = argv[1]

if len(argv) == 3:
    username = argv[1]
    password = argv[2]

token = user_auth(username, password)
open_push_tunnel(token, username)
