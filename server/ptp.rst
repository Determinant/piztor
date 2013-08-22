Piztor Transmission Protocol v0.1
---------------------------------

- General 

  - Request

    ::
    
        +---1b---+-------?b--------+
        | OPT ID |  SPECIFIC DATA  |
        +-uchar--+-----------------+

  - Response

    ::
    
        +---1b---+------?b---------+
        | OPT ID |  SPECIFIC DATA  |
        +-uchar--+-----------------+

- Authentication 

  - Request

    :: 

        +--1b---+-----?b------+-----?b-----+
        | 0x00  |   USERNAME  |  PASSWORD  |
        +-uchar-+-------------+------------+

  - Response

    ::
    
       +--1b---+-----4b-----+
       | 0x00  | USER_TOKEN |
       +-uchar-+----uint----+

- Message Sending 

  - Request

    ::
    
        +--1b---+------4b------+------4b-----+
        | 0x01  | SENDER_TOKEN | RECEIVER_ID |
        +-uchar-+-----uint-----+----uint-----+

  - Response
        
    ::

        +--1b---+----?b----+
        | 0x01  | RESERVED |
        +-uchar-+----------+

- Location Update

  - Request

    ::
    
        +--1b---+------4b------+-----8b-----+------8b-----+
        | 0x02  | SENDER_TOKEN |  LATITUDE  |  LONGITUDE  |
        +-uchar-+-----uint-----+---double---+---double----+

  - Response

    ::

        +--1b---+----?b----+
        | 0x02  | RESERVED |
        +-uchar-+----------+

- Location Information

  - Request

    ::
    
        +--1b---+-----4b-------+------4b-----+
        | 0x03  | SENDER_TOKEN |  GROUP_ID   |
        +-uchar-+----uint------+----uint-----+

  - Response

    ::

        +--1b---+-----20b--------+-----+
        | 0x03  | LOCATION_ENTRY | ... |
        +-uchar-+----------------+-----+
        
    Location Entry:

    :: 

        +---4b----+----8b----+-----8b----+
        | USER_ID | LATITUDE | LONGITUDE |
        +--uint---+--double--+--double---+

