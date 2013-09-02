from sqlalchemy import Table, Column
from sqlalchemy import Integer, String, ForeignKey, Boolean
from sqlalchemy.dialects.mysql import BLOB, TINYINT, DOUBLE
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship, backref
from exc import *

Base = declarative_base()

_SALT_LEN = 16
_TOKEN_LEN = 16
MAX_USERNAME_SIZE = 20
MAX_PASSWORD_SIZE = 20
NOT_A_LAT = NOT_A_LNG = 300

_table_typical_settings = {
        'mysql_engine' : 'InnoDB',
        'mysql_charset' : 'utf8',
        'mysql_auto_increment' : '1'}

class _TableName:   # avoid typoes
    UserModel = 'users'
    LocationInfo = 'location_info'
    UserAuth = 'user_auth'
    GroupInfo = 'group_info'
    GroupSub = 'group_sub'


class GroupInfo(Base):
    __tablename__ = _TableName.GroupInfo
    __table_args__ = _table_typical_settings

    id = Column(Integer, primary_key = True)
    subscribers = None

    def __init__(self, gid):
        self.id = gid

group_sub = Table(_TableName.GroupSub, Base.metadata,
        Column('uid', Integer, ForeignKey(_TableName.UserModel + '.id')),
        Column('gid', Integer, ForeignKey(_TableName.GroupInfo + '.id')),
        mysql_engine = 'InnoDB')


class UserModel(Base):
    __tablename__ = _TableName.UserModel
    __table_args__ = _table_typical_settings
    id = Column(Integer, primary_key = True)
    username = Column(String(MAX_USERNAME_SIZE),
                    unique = True, nullable = False)
    nickname = Column(String(MAX_USERNAME_SIZE),
                    unique = False)
    sex = Column(Boolean, nullable = False)
    comp_id = Column(Integer)
    sec_id = Column(Integer)

    perm = Column(TINYINT, nullable = False)
    sub = relationship("GroupInfo",
                            secondary = group_sub,
                            backref = "subscribers")

    location = None
    auth = None

    @classmethod
    def to_gid(self, comp_no, sec_no):
        return comp_no * 256 + sec_no

    def __init__(self, username, nickname, sex, comp_no, sec_no, perm):
        self.username = username
        self.nickname = nickname
        self.sex = sex
        self.comp_id = UserModel.to_gid(comp_no, 0xff)
        self.sec_id = UserModel.to_gid(comp_no, sec_no)
        self.sub = list()
        self.perm = perm


class LocationInfo(Base):
    __tablename__ = _TableName.LocationInfo
    __table_args__ = _table_typical_settings

    uid = Column(Integer, ForeignKey(_TableName.UserModel + '.id'),
                primary_key = True)
    lat = Column(DOUBLE, nullable = False)
    lng = Column(DOUBLE, nullable = False)
    user = relationship("UserModel", uselist = False,
            backref = backref("location", uselist = False,
                cascade = "all, delete-orphan"))

    # More: last_update

from hashlib import sha256
from os import urandom

def _sprinkle_salt(uauth, passwd):
    data = sha256(uauth.salt)
    data.update(chr(0))
    data.update(passwd)
    return data.digest()

def _random_binary_string(length):
    return urandom(length)

class UserAuth(Base):
    __tablename__ = _TableName.UserAuth
    __table_args__ = _table_typical_settings

    uid = Column(Integer, ForeignKey(_TableName.UserModel + '.id'), primary_key = True)
    password = Column(BLOB)
    salt = Column(BLOB)
    token = Column(BLOB)

    user = relationship("UserModel", uselist = False,
            backref = backref("auth", uselist = False,
                cascade = "all, delete-orphan"))

    def regen_token(self):
        self.token = sha256(_random_binary_string(_TOKEN_LEN)).digest()

    def __init__(self, passwd):
        self.set_password(passwd)

    def set_password(self, passwd):
        self.salt = _random_binary_string(_SALT_LEN)
        self.password = _sprinkle_salt(self, passwd)
        self.regen_token()

    def check_password(self, passwd):
        passwd = _sprinkle_salt(self, passwd)
        return passwd == self.password

    def check_token(self, tk):
        return self.token == tk

    def get_token(self):
        return self.token


