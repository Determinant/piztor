from sqlalchemy import Column, Integer, String, Float, ForeignKey, LargeBinary, Boolean
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship, backref

Base = declarative_base()

_SALT_LEN = 16
_TOKEN_LEN = 16
MAX_USERNAME_SIZE = 20
MAX_PASSWORD_SIZE = 20

class _TableName:   # avoid typoes
    UserModel = 'users'
    LocationInfo = 'location_info'
    UserAuth = 'user_auth'

class UserModel(Base):
    __tablename__ = _TableName.UserModel
    __table_args__ = {
        'mysql_engine' : 'InnoDB', 
        'mysql_charset' : 'utf8', 
        'mysql_auto_increment' : '1'}

    id = Column(Integer, primary_key = True)
    gid = Column(Integer, nullable = False)
    username = Column(String(MAX_USERNAME_SIZE), 
                    unique = True, nullable = False)
    sex = Column(Boolean, nullable = False)
    location = None
    auth = None

class LocationInfo(Base):
    __table_args__ = {
        'mysql_engine' : 'InnoDB', 
        'mysql_charset' : 'utf8', 
        'mysql_auto_increment' : '1'}

    __tablename__ = _TableName.LocationInfo

    uid = Column(Integer, ForeignKey(_TableName.UserModel + '.id'), 
                primary_key = True)
    lat = Column(Float(precesion = 64), nullable = False)
    lng = Column(Float(precesion = 64), nullable = False)
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
    __table_args__ = {
        'mysql_engine' : 'InnoDB', 
        'mysql_charset' : 'utf8', 
        'mysql_auto_increment' : '1'}

    __tablename__ = _TableName.UserAuth

    uid = Column(Integer, ForeignKey(_TableName.UserModel + '.id'), primary_key = True)
    password = Column(LargeBinary)
    salt = Column(LargeBinary)
    token = Column(LargeBinary)

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
