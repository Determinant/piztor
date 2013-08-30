Piztor Transmission Protocol v2.0a
----------------------------------

- Pull 

  - General 
  
    - Request
  
      ::
      
          +---4b---+---1b---+-------?b--------+
          | LENGTH | OPT_ID |  SPECIFIC_DATA  |
          +--int---+-uchar--+-----------------+
  
    - Response
  
      ::
      
          +---4b---+---1b---+------?b---------+
          | LENGTH | OPT_ID |  SPECIFIC_DATA  |
          +--int---+-uchar--+-----------------+
  
      Notice:
  
      - In following sections, ``LENGTH`` and ``OPT_ID`` are left out for clarity.
      - ``PADDING``:

        ::

            +--1b--+
            | 0x00 |
            +------+

      - ``string`` type: 
  
        ::
  
            +-------?b-------+---------+
            | STRING_CONTENT | PADDING |
            +----------------+---------+
  
      - ``AUTH_HEAD`` : 
  
        ::
  
            +----32b-----+----?b----+
            | USER_TOKEN | USERNAME |
            +----raw-----+--string--+

      - ``GROUP_ID`` :
  
      ::
  
          +---1b----+---1b---+
          | COMP_ID | SEC_ID |
          +--uchar--+-uchar--+

      - ``USER_ENTRY`` :

      ::
  
          +------?b-----+-----+---------+
          | INFO_ENTRY  | ... | PADDING |
          +-------------+-----+---------+

      - ``INFO_ENTRY`` : 
      
      ::
  
          +----1b----+-----?b-----+
          | INFO_KEY | INFO_VALUE |
          +--uchar---+------------+
  
      - ``INFO_KEY`` :
  
        :``0x01``: uid (value is a 4-byte ``integer``)
        :``0x02``: username (value is a ``string``)
        :``0x03``: nickname (value is a ``string``)
        :``0x04``: sex (value is a 1-byte ``boolean``: ``0x01`` for male, ``0x00`` for female)
        :``0x05``: gid (value is a 2-byte ``GROUP_ID``)
        :``0x06``: latitude (value is a 8-byte ``double`` )
        :``0x07``: longtitude( value is a 8-byte ``double`` )

      - ``SUB_LIST`` :

      ::

          +----------+-----+---------+
          | GROUP_ID | ... | PADDING |
          +----------+-----+---------+
 
      - If ``STATUS`` appears to be a failure, the client should ignore the rest part of the datagram

  - Authentication ``0x00``
  
    - Request
  
      :: 
  
          +-----?b---+----?b----+
          | USERNAME | PASSWORD |
          +--string--+--string--+
  
    - Response
  
      ::
      
          +---1b---+----32b-----+------------+----------+
          | STATUS | USER_TOKEN | USER_ENTRY | SUB_LIST |
          +--uchar-+----raw-----+------------+----------+
  
      ``STATUS`` :
      
      - ``0x00`` for success
      - ``0x01`` for failure
  
  - Location Update ``0x01``
  
    - Request
  
      ::
      
          +-------------+----8b------+------8b-----+
          |  AUTH_HEAD  |  LATITUDE  |  LONGITUDE  |
          +-------------+---double---+---double----+
  
    - Response
  
      ::
  
          +--------+
          | STATUS |
          +--uchar-+
  
      ``STATUS`` :
  
      - ``0x00`` for success
      - ``0x01`` for invalid token
  
  
  - User Information (by group) ``0x02``
  
    - Request
  
      ::
  
          +--------------+-------------+
          |  AUTH_HEAD   |  GROUP_ID   |
          +--------------+-----int-----+
  
    - Response 
  
      ::
  
          +--------+------?b-----+-----+
          | STATUS | USER_ENTRY  | ... |
          +-uchar--+-------------+-----+


  - Update Subscription ``0x03``

    - Request

      ::

        +-----------+----------+
        | AUTH_HEAD | SUB_LIST |
        +-----------+----------+

    - Response

      ::

        +--------+
        | STATUS |
        +--------+
  
  - User Logout ``0x04``
  
    - Request
  
      ::
  
        +-----------+
        | AUTH_HEAD |
        +-----------+
  
    - Response
  
      ::
  
        +--------+
        | STATUS |
        +--------+
  
  - Open Push Tunnel ``0x05``
  
    - Request
  
      ::
  
        +-----------+
        | AUTH_HEAD |
        +-----------+
  
    - Response
  
      ::
  
        +--------+
        | STATUS |
        +--------+
  
  - Send Text Message ``0x06``
  
    - Request
  
      ::
  
        +-----------+----?b----+
        | AUTH_HEAD | MESSAGE  |
        +-----------+--string--+
  
    - Response
  
      ::
  
        +--------+
        | STATUS |
        +--------+

  - Set Marker ``0x07``

    - Request

      ::
          +-------------+----8b------+------8b-----+----4b----+
          |  AUTH_HEAD  |  LATITUDE  |  LONGITUDE  | DEADLINE |
          +-------------+---double---+---double----+---int----+

    - Response

      ::

          +--------+
          | STATUS |
          +--------+

- Push Notification

  - General Request

    ::

        +---1b---+-------32b--------+-------?b------+
        | OPT_ID | PUSH_FINGERPRINT | SPECIFIC_DATA |
        +--------+------------------+---------------+

  - Acknowledgement

    ::

        +---1b---+-------32b---------+
        | OPT_ID | PUSH_FINGERPRINT  |
        +--------+-------------------+
 
  - Text Message 

    ::
    
      ----+----?b----+
      ... | MESSAGE  |
      ----+--string--+

  - Location Update

    ::

      ----+---4b----+----8b----+----8b-----+
      ... | USER_ID | LATITUDE | LONGITUDE |
      ----+---------+----------+-----------+

  - Marker Push

    ::

      ----+----8b----+----8b-----+----4b----+
      ... | LATITUDE | LONGITUDE | DEADLINE |
      ----+----------+-----------+----int---+

