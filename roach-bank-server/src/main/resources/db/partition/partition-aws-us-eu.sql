-- Geo-partitioning schema configuration for:
-- region1="us-west-1"
-- region2="us-east-1"
-- region3="eu-west-1"

---------------------------------------------------------------
-- TRANSACTION_TYPE
---------------------------------------------------------------

-- Create secondary indexes for other regions
CREATE INDEX idx_us_west on transaction_type (id) STORING (name);
CREATE INDEX idx_eu on transaction_type (id) STORING (name);

--  Pin primary index lease holder to us-east-1
ALTER INDEX transaction_type@primary CONFIGURE ZONE USING
    num_replicas = 3,
    constraints = '{"+region=us-east-1":1}',
    lease_preferences = '[[+region=us-east-1]]';

ALTER INDEX transaction_type@idx_us_west CONFIGURE ZONE USING
    num_replicas = 3,
    constraints = '{"+region=us-west-1":1}',
    lease_preferences = '[[+region=us-west-1]]';

ALTER INDEX transaction_type@idx_eu CONFIGURE ZONE USING
    num_replicas = 3,
    constraints = '{"+region=eu-west-1":1}',
    lease_preferences = '[[+region=eu-west-1]]';

---------------------------------------------------------------
-- ACCOUNT
---------------------------------------------------------------

ALTER TABLE account PARTITION BY LIST (region) (
    PARTITION us_west VALUES IN ('seattle','san francisco','los angeles','phoenix','minneapolis'),
    PARTITION us_east VALUES IN ('chicago','detroit','atlanta','new york','boston','washington dc','miami'),
    PARTITION eu VALUES IN ('london','frankfurt','amsterdam','paris','milano','madrid','athens','stockholm','helsinki','oslo'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

-- Pin partitions to regions
ALTER
PARTITION us_west OF
TABLE account
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=us-west-1]';

ALTER
PARTITION us_east OF
TABLE account
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=us-east-1]';

ALTER
PARTITION eu OF
TABLE account
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=eu-west-1]';

---------------------------------------------------------------
-- TRANSACTION
---------------------------------------------------------------
ALTER TABLE transaction PARTITION BY LIST (region) (
    PARTITION us_west VALUES IN ('seattle','san francisco','los angeles','phoenix','minneapolis'),
    PARTITION us_east VALUES IN ('chicago','detroit','atlanta','new york','boston','washington dc','miami'),
    PARTITION eu VALUES IN ('london','frankfurt','amsterdam','paris','milano','madrid','athens','stockholm','helsinki','oslo'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

ALTER
PARTITION us_west OF
TABLE transaction
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=us-west-1]';

ALTER
PARTITION us_east OF
TABLE transaction
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=us-east-1]';

ALTER
PARTITION eu OF
TABLE transaction
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=eu-west-1]';

---------------------------------------------------------------
-- TRANSACTION_ITEM
---------------------------------------------------------------

ALTER TABLE transaction_item PARTITION BY LIST (transaction_region) (
    PARTITION us_west VALUES IN ('seattle','san francisco','los angeles','phoenix','minneapolis'),
    PARTITION us_east VALUES IN ('chicago','detroit','atlanta','new york','boston','washington dc','miami'),
    PARTITION eu VALUES IN ('london','frankfurt','amsterdam','paris','milano','madrid','athens','stockholm','helsinki','oslo'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

ALTER
PARTITION us_west OF
TABLE transaction_item
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=us-west-1]';

ALTER
PARTITION us_east OF
TABLE transaction_item
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=us-east-1]';

ALTER
PARTITION eu OF
TABLE transaction_item
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=eu-west-1]';

---------------------------------------------------------------
-- System ranges
---------------------------------------------------------------

ALTER
RANGE meta CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=us-west-1: 2, +region=us-east-1: 3, +region=eu-west-1: 2}';

ALTER
RANGE liveness CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=us-west-1: 2, +region=us-east-1: 3, +region=eu-west-1: 2}';

ALTER
RANGE system CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=us-west-1: 2, +region=us-east-1: 3, +region=eu-west-1: 2}';

ALTER DATABASE system CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=us-west-1: 2, +region=us-east-1: 3, +region=eu-west-1: 2}';