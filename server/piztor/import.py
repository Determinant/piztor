from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from model import *

path = "root:helloworld@localhost/piztor"

class UserData:
    def __init__(self, username, password, gid, sex):
        self.username = username
        self.password = password
        self.gid = gid
        self.sex = sex

def create_database():
    engine = create_engine('mysql://' + path, echo = True)
    Base.metadata.drop_all(engine)
    Base.metadata.create_all(engine)

def import_user_data(data):
    engine = create_engine('mysql://' + path, echo = True)
    Session = sessionmaker(bind = engine)
    session = Session()
    for user in data: 
        um = UserModel(username = user.username, gid = user.gid, sex = user.sex)
        um.auth = UserAuth(user.password)
        um.location = LocationInfo(lat = 0, lng = 0)
        session.add(um)
        session.commit()

if __name__ == '__main__':

    from sys import argv, exit
    if len(argv) != 2:
        print "Usage: " + argv[0] + " FILE"
        exit(0) 

    data = list()
    with open(argv[1], 'r') as f:
        while True:
            line = f.readline().split()
            if len(line) == 0: break
            data.append(UserData(line[0], line[1], line[2], line[3]))

    create_database()
    import_user_data(data)
