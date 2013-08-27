Piztor Transmission Protocol v0.4a
----------------------------------

- General 

  - Request

    ::
    
        +---4b---+---1b---+-------?b--------+
        | LENGTH | OPT_ID |  SPECIFIC DATA  |
        +--int---+-uchar--+-----------------+

  - Response

    ::
    
        +---4b---+---1b---+------?b---------+
        | LENGTH | OPT_ID |  SPECIFIC DATA  |
        +--int---+-uchar--+-----------------+

    Notice:

    - In following sections, ``LENGTH`` part is left out for clarity.
    - ``PADDING`` has value ``0``.
    - ``string`` type structure:

      ::

          +-------?b-------+---------+
          | STRING_CONTENT | PADDING |
          +----------------+---------+

    - ``AUTH_HEAD`` structure:

      ::

          +----32b-----+----?b----+
          | USER_TOKEN | USERNAME |
          +----raw-----+--string--+

- Authentication 

  - Request

    :: 

        +--1b---+-----?b---+----?b----+
        | 0x00  | USERNAME | PASSWORD |
        +-uchar-+--string--+--string--+

  - Response

    ::
    
       +--1b---+---1b---+---4b----+----32b-----+
       | 0x00  | STATUS | USER_ID | USER_TOKEN |
       +-uchar-+--uchar-+---int---+----raw-----+

    ``STATUS`` :
    
    - ``0x00`` for success
    - ``0x01`` for failure

- Location Update

  - Request

    ::
    
        +--1b---+-----?b------+----8b------+------8b-----+
        | 0x01  |  AUTH_HEAD  |  LATITUDE  |  LONGITUDE  |
        +-uchar-+-------------+---double---+---double----+

  - Response

    ::

        +--1b---+---1b---+
        | 0x01  | STATUS |
        +-uchar-+--uchar-+

    ``STATUS`` :

    - ``0x00`` for success
    - ``0x01`` for invalid token

- Location Information

  - Request

    ::
    
        +--1b---+------?b------+------4b-----+
        | 0x02  |  AUTH_HEAD   |  GROUP_ID   |
        +-uchar-+--------------+-----int-----+

  - Response

    ::

        +--1b---+---1b---+------20b-------+-----+
        | 0x02  | STATUS | LOCATION_ENTRY | ... |
        +-uchar-+-uchar--+----------------+-----+
        
    ``LOCATION_ENTRY`` :

    :: 

        +---4b----+----8b----+-----8b----+
        | USER_ID | LATITUDE | LONGITUDE |
        +---int---+--double--+--double---+

- User Information

  - Request

    ::

        +--1b---+------?b------+------4b-----+
        | 0x03  |  AUTH_HEAD   |   USER_ID   |
        +-uchar-+--------------+-----int-----+

  - Response 

    ::

        +--1b---+---1b---+------?b-----+-----+
        | 0x03  | STATUS | UINFO_ENTRY | ... |
        +-uchar-+-uchar--+-------------+-----+

    ``UINFO_ENTRY`` : 
    
    ::

        +----1b----+-----?b-----+
        | INFO_KEY | INFO_VALUE |
        +--uchar---+------------+

    ``INFO_KEY`` :

    :``0x00``: gid (value is a 4-byte ``long int``)
    :``0x01``: sex (value is a 1-byte ``boolean``: ``0x01`` for male, ``0x00`` for female)

- User Logout

  - Request

  ::

      +--1b--+-----?b------+
      | 0x04 |  AUTH_HEAD  |
      +------+-------------+

  - Response

  ::

      +--1b--+---1b---+
      | 0x04 | STATUS |
      +------+--------+

