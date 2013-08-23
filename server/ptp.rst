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
    
       +--1b---+-----4b-----+---1b---+
       | 0x00  | USER_TOKEN | STATUS |
       +-uchar-+-----int----+--uchar-+

- Message Sending 

  - Request

    ::
    
        +--1b---+------4b------+------4b-----+
        | 0x01  | SENDER_TOKEN | RECEIVER_ID |
        +-uchar-+------int-----+-----int-----+

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
        +-uchar-+------int-----+---double---+---double----+

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
        +-uchar-+-----int------+-----int-----+

  - Response

    ::

        +--1b---+-----4b----+------20b-------+-----+
        | 0x03  | ENTRY_CNT | LOCATION_ENTRY | ... |
        +-uchar-+---int-----+----------------+-----+
        
    Location Entry:

    :: 

        +---4b----+----8b----+-----8b----+
        | USER_ID | LATITUDE | LONGITUDE |
        +---int---+--double--+--double---+

