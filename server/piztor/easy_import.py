from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from model import *

path = "root:helloworld@localhost/piztor"

class UserData:
    def __init__(self, username, nickname, password, 
                        comp_no, sec_no, sex, perm, sub):
        self.username = username
        self.nickname = nickname
        self.password = password
        self.comp_no = int(comp_no)
        self.sec_no = int(sec_no)
        self.sex = int(sex)
        self.perm = perm
        self.sub = sub

def create_database():
    engine = create_engine('mysql://' + path, echo = True)
    Base.metadata.drop_all(engine)
    Base.metadata.create_all(engine)

def find_or_create_group(comp_no, sec_no, session):
    gid = UserModel.to_gid(comp_no, sec_no)
    q = session.query(GroupInfo).filter(GroupInfo.id == gid)
    entry = q.first()
    if not entry:
        entry = GroupInfo(gid = gid)
    return entry


def import_user_data(data):
    engine = create_engine('mysql://' + path, echo = True)
    Session = sessionmaker(bind = engine)
    session = Session()

    for user in data: 
        um = UserModel(username = user.username, 
                        nickname = user.nickname,
                        sex = user.sex,
                        comp_no = user.comp_no,
                        sec_no = user.sec_no,
                        perm = user.perm)
    
        for cn, sn in user.sub:
            print cn, sn
            g = find_or_create_group(int(cn), int(sn), session)
            um.sub.append(g)
        um.auth = UserAuth(user.password)
        um.location = LocationInfo(lat = NOT_A_LAT, lng = NOT_A_LNG)
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
            idx = 0
            comp_no = line[3]
            sec_no = line[4]
            sub = [ (comp_no, sec_no), (comp_no, 0xff) ]
            data.append(UserData(username = line[0], 
                                nickname = line[1],
                                password = line[0],
                                comp_no = comp_no, 
                                sec_no = sec_no,
                                sex = line[2],
                                perm = line[5],
                                sub = sub))


    create_database()
    import_user_data(data)
