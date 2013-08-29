from sqlalchemy import Table, Column
from sqlalchemy import Integer, String, Float, ForeignKey, Boolean
from sqlalchemy.dialects.mysql import BLOB, TINYINT
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship, backref

Base = declarative_base()

_SALT_LEN = 16
_TOKEN_LEN = 16
MAX_USERNAME_SIZE = 20
MAX_PASSWORD_SIZE = 20

_table_typical_settings = {
        'mysql_engine' : 'InnoDB',
        'mysql_charset' : 'utf8',
        'mysql_auto_increment' : '1'}

class _TableName:   # avoid typoes
    UserModel = 'users'
    LocationInfo = 'location_info'
    UserAuth = 'user_auth'
    CompanyInfo = 'comp_info'
    SectionInfo = 'sec_info'
    CompanySub = 'comp_sub'
    SectionSub = 'sec_sub'

comp_sub_assoc = Table(_TableName.CompanySub, Base.metadata,
        Column('uid', Integer, ForeignKey(_TableName.UserModel + '.id')),
        Column('comp_id', Integer, ForeignKey(_TableName.CompanyInfo + '.id')))

sec_sub_assoc = Table(_TableName.SectionSub, Base.metadata,
        Column('uid', Integer, ForeignKey(_TableName.UserModel + '.id')),
        Column('sec_id', Integer, ForeignKey(_TableName.SectionInfo + '.id')))

class UserModel(Base):
    __tablename__ = _TableName.UserModel
    __table_args__ = _table_typical_settings
    id = Column(Integer, primary_key = True)
    username = Column(String(MAX_USERNAME_SIZE),
                    unique = True, nullable = False)

    sex = Column(Boolean, nullable = False)
    sec_no = Column(TINYINT)
    comp_id = Column(TINYINT, ForeignKey(_TableName.CompanyInfo + '.id'))
    sec_id = Column(TINYINT, ForeignKey(_TableName.SectionInfo + '.id'))

    comp_sub = relationship(_TableName.CompanyInfo,
                            secondary = comp_sub_assoc,
                            backref = "subscribers")
    sec_sub = relationship(_TableName.SectionInfo,
                            secondary = sec_sub_assoc,
                            backref = "subscribers")

    location = None
    auth = None
    sec = None

class LocationInfo(Base):
    __tablename__ = _TableName.LocationInfo
    __table_args__ = _table_typical_settings

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

class CompanyInfo(Base):
    __tablename__ = _TableName.CompanyInfo
    __table_args__ = _table_typical_settings

    id = Column(TINYINT, primary_key = True)
    marker_lat = Column(Float(precesion = 64), nullable = False)
    market_lng = Column(Float(precesion = 64), nullable = False)

    subscribers = None

class SectionInfo(Base):
    __tablename__ = _TableName.SectionInfo
    __table_args__ = _table_typical_settings

    id = Column(TINYINT, primary_key = True)
    marker_lat = Column(Float(precesion = 64), nullable = False)
    market_lng = Column(Float(precesion = 64), nullable = False)

    subscribers = None
