from random import randint

def get_rand_gid():
    return randint(1, 2)

def get_rand_sex():
    return randint(0, 1)

for i in xrange(100):
    print i, i, get_rand_gid(), get_rand_sex()
