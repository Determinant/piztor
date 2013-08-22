Piztor Transmission Protocol v0.1
---------------------------------

- General 

  - Request

    ::
    
        +---1b---+-------?b--------+
        | OPT ID |  SPECIFIC DATA  |
        +--------+-----------------+

  - Response

    ::
    
        +---1b---+------?b---------+
        | OPT ID |  SPECIFIC DATA  |
        +--------+-----------------+

- Authentication 

  - Request

    :: 

        +--1b--+-----?b------+-----?b-----+
        | 0x00 |   USERNAME  |  PASSWORD  |
        +------+-------------+------------+

  - Response

    ::
    
       +--1b--+-----4b-----+
       | 0x00 | USER_TOKEN |
       +------+------------+

- Message Sending 

  - Request

    ::
    
        +--1b--+------4b------+------4b-----+
        | 0x01 | SENDER_TOKEN | RECEIVER_ID |
        +------+--------------+-------------+

  - Response
        
    ::

        +--1b--+----?b----+
        | 0x01 | RESERVED |
        +------+----------+

- Location Update

  - Request

    ::
    
        +--1b--+------4b------+-----8b-----+------8b-----+
        | 0x02 | SENDER_TOKEN |  LATITUDE  |  LONGITUDE  |
        +------+--------------+------------+-------------+

  - Response

    ::

        +--1b--+----?b----+
        | 0x02 | RESERVED |
        +------+----------+

- Location Information

  - Request

    ::
    
        +--2b--+-----4b-------+------4b-----+
        | 0x03 | SENDER_TOKEN |  GROUP_ID   |
        +------+--------------+-------------+

  - Response

    ::

        +--2b--+-----20b--------+-----+
        | 0x03 | LOCATION_ENTRY | ... |
        +------+----------------+-----+
        
    Location Entry:

    :: 

        +---4b----+----8b----+-----8b----+
        | USER_ID | LATITUDE | LONGITUDE |
        +---------+----------+-----------+

