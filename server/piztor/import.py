from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from model import *

path = "root:helloworld@localhost/piztor"

class UserData:
    def __init__(self, username, password, comp_id, sec_id, sex):
        self.username = username
        self.password = password
        self.comp_id = comp_id
        self.sec_id = sec_id
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
        um = UserModel(username = user.username, 
                        comp_id = user.comp_id,
                        sec_id = user.sec_id, 
                        sex = user.sex)
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
            data.append(UserData(username = line[0], 
                                password = line[1], 
                                comp_id = line[2], 
                                sec_id = line[3],
                                sex = line[4]))

    create_database()
    import_user_data(data)
