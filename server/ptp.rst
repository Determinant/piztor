Piztor Transmission Protocol v0.2
---------------------------------

- General 

  - Request

    ::
    
        +---4b---+---1b---+-------?b--------+
        | LENGTH | OPT ID |  SPECIFIC DATA  |
        +--int---+-uchar--+-----------------+

  - Response

    ::
    
        +---4b---+---1b---+------?b---------+
        | LENGTH | OPT ID |  SPECIFIC DATA  |
        +--int---+-uchar--+-----------------+

    Notice that in following sections, ``LENGTH`` part is left out for clarity.

- Authentication 

  - Request

    :: 

        +--1b---+-----?b------+-----?b-----+
        | 0x00  |   USERNAME  |  PASSWORD  |
        +-uchar-+-------------+------------+

  - Response

    ::
    
       +--1b---+---1b---+---4b----+----16b-----+
       | 0x00  | STATUS | USER_ID | USER_TOKEN |
       +-uchar-+--uchar-+---int---+----raw-----+

    ``STATUS`` :
    
    - 0x00 for success
    - 0x01 for failure

- Location Update

  - Request

    ::
    
        +--1b---+-----16b------+-----8b-----+------8b-----+
        | 0x02  |  USER_TOKEN  |  LATITUDE  |  LONGITUDE  |
        +-uchar-+------raw-----+---double---+---double----+

  - Response

    ::

        +--1b---+---1b---+
        | 0x02  | STATUS |
        +-uchar-+--uchar-+

    ``STATUS`` :

    - 0x00 for success
    - 0x01 for invalid token

- Location Information

  - Request

    ::
    
        +--1b---+-----16b------+------4b-----+
        | 0x03  |  USER_TOKEN  |  GROUP_ID   |
        +-uchar-+-----raw------+-----int-----+

  - Response

    ::

        +--1b---+---1b---+-----4b----+------20b-------+-----+
        | 0x03  | STATUS | ENTRY_CNT | LOCATION_ENTRY | ... |
        +-uchar-+-uchar--+----int----+----------------+-----+
        
    ``LOCATION_ENTRY`` :

    :: 

        +---4b----+----8b----+-----8b----+
        | USER_ID | LATITUDE | LONGITUDE |
        +---int---+--double--+--double---+

