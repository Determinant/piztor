from random import randint

def get_rand_gid():
    return randint(1, 2)

def get_rand_sex():
    return randint(0, 1)

for i in xrange(100):
    print i, i, i, 23, 15, get_rand_sex(), 0
    print "23 15"
