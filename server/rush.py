from subprocess import Popen
procs = []

try:
    for i in xrange(10):
        p = Popen(["python", "client.py", str(i)])
        procs.append(p)
        #p.wait()
    print "done"

except KeyboardInterrupt:
    print "killing"
    for p in procs:
        p.kill()
