from subprocess import Popen
procs = []

try:
    for i in xrange(100):
        procs.append(Popen(["python", "client.py"]))
    while True: pass

except KeyboardInterrupt:
    print "killing"
    for p in procs:
        p.kill()
