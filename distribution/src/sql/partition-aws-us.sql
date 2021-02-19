---------------------------------------------------------------
-- ACCOUNT
---------------------------------------------------------------

ALTER TABLE account PARTITION BY LIST (region) (
    PARTITION us_west VALUES IN ('seattle','san francisco','los angeles','phoenix'),
    PARTITION us_central VALUES IN ('minneapolis','chicago','detroit','atlanta'),
    PARTITION us_east VALUES IN ('new york','boston','washington dc','miami'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

-- Pin partitions to regions
ALTER PARTITION us_west OF TABLE account CONFIGURE ZONE USING num_replicas=3, constraints='[+region=us-west-2]';
ALTER PARTITION us_central OF TABLE account CONFIGURE ZONE USING num_replicas=3, constraints='[+region=us-east-2]';
ALTER PARTITION us_east OF TABLE account CONFIGURE ZONE USING num_replicas=3, constraints='[+region=us-east-1]';

---------------------------------------------------------------
-- TRANSACTION
---------------------------------------------------------------
ALTER TABLE transaction PARTITION BY LIST (region) (
    PARTITION us_west VALUES IN ('seattle','san francisco','los angeles','phoenix'),
    PARTITION us_central VALUES IN ('minneapolis','chicago','detroit','atlanta'),
    PARTITION us_east VALUES IN ('new york','boston','washington dc','miami'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

ALTER PARTITION us_west OF TABLE transaction CONFIGURE ZONE USING num_replicas=3, constraints='[+region=us-west-2]';
ALTER PARTITION us_central OF TABLE transaction CONFIGURE ZONE USING num_replicas=3, constraints='[+region=us-east-2]';
ALTER PARTITION us_east OF TABLE transaction CONFIGURE ZONE USING num_replicas=3, constraints='[+region=us-east-1]';

---------------------------------------------------------------
-- TRANSACTION_ITEM
---------------------------------------------------------------
ALTER TABLE transaction_item PARTITION BY LIST (transaction_region) (
    PARTITION us_west VALUES IN ('seattle','san francisco','los angeles','phoenix'),
    PARTITION us_central VALUES IN ('minneapolis','chicago','detroit','atlanta'),
    PARTITION us_east VALUES IN ('new york','boston','washington dc','miami'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

ALTER PARTITION us_west OF TABLE transaction_item CONFIGURE ZONE USING num_replicas=3,constraints='[+region=us-west-2]';
ALTER PARTITION us_central OF TABLE transaction_item CONFIGURE ZONE USING num_replicas=3, constraints='[+region=us-east-2]';
ALTER PARTITION us_east OF TABLE transaction_item CONFIGURE ZONE USING num_replicas=3, constraints='[+region=us-east-1]';

---------------------------------------------------------------
-- System ranges
---------------------------------------------------------------

ALTER RANGE meta CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=us-east-1: 2, +region=us-east-2: 3, +region=us-west-1: 2}';

ALTER RANGE liveness CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=us-east-1: 2, +region=us-east-2: 3, +region=us-west-1: 2}';

ALTER RANGE system CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=us-east-1: 2, +region=us-east-2: 3, +region=us-west-1: 2}';

ALTER DATABASE system CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=us-east-1: 2, +region=us-east-2: 3, +region=us-west-1: 2}';