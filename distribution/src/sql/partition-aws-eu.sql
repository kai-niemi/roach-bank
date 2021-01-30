-- Geo-partitioning schema configuration for:
-- region1="eu-central-1"
-- region2="eu-west-1"
-- region2="eu-west-2"
-- region3="eu-west-3"

---------------------------------------------------------------
-- TRANSACTION_TYPE
---------------------------------------------------------------

-- Create secondary indexes for other regions
CREATE INDEX idx_eu_west on transaction_type (id) STORING (name);
CREATE INDEX idx_eu_south on transaction_type (id) STORING (name);

--  Pin primary index lease holder
ALTER INDEX transaction_type@primary CONFIGURE ZONE USING
    num_replicas = 3,
    constraints = '{"+region=eu-central-1":1}',
    lease_preferences = '[[+region=eu-central-1]]';

ALTER INDEX transaction_type@idx_eu_west CONFIGURE ZONE USING
    num_replicas = 3,
    constraints = '{"+region=eu-west-2":1}',
    lease_preferences = '[[+region=eu-west-2]]';

ALTER INDEX transaction_type@idx_eu_south CONFIGURE ZONE USING
    num_replicas = 3,
    constraints = '{"+region=eu-west-3":1}',
    lease_preferences = '[[+region=eu-west-3]]';

---------------------------------------------------------------
-- ACCOUNT
---------------------------------------------------------------

ALTER TABLE account PARTITION BY LIST (region) (
    PARTITION eu_west VALUES IN ('london','amsterdam','manchester','paris'),
    PARTITION eu_south VALUES IN ('milano','madrid','athens','barcelona'),
    PARTITION eu_central VALUES IN ('frankfurt','stockholm','helsinki','oslo'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

-- Pin partitions to regions
ALTER
PARTITION eu_west OF
TABLE account
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=eu-west-2]';

ALTER
PARTITION eu_south OF
TABLE account
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=eu-west-3]';

ALTER
PARTITION eu_central OF
TABLE account
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=eu-central-1]';

---------------------------------------------------------------
-- TRANSACTION
---------------------------------------------------------------
ALTER TABLE transaction PARTITION BY LIST (region) (
    PARTITION eu_west VALUES IN ('london','amsterdam','manchester','paris'),
    PARTITION eu_south VALUES IN ('milano','madrid','athens','barcelona'),
    PARTITION eu_central VALUES IN ('frankfurt','stockholm','helsinki','oslo'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

ALTER
PARTITION eu_west OF
TABLE transaction
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=eu-west-2]';

ALTER
PARTITION eu_south OF
TABLE transaction
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=eu-west-3]';

ALTER
PARTITION eu_central OF
TABLE transaction
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=eu-central-1]';

---------------------------------------------------------------
-- TRANSACTION_ITEM
---------------------------------------------------------------

ALTER TABLE transaction_item PARTITION BY LIST (transaction_region) (
    PARTITION eu_west VALUES IN ('london','amsterdam','manchester','paris'),
    PARTITION eu_south VALUES IN ('milano','madrid','athens','barcelona'),
    PARTITION eu_central VALUES IN ('frankfurt','stockholm','helsinki','oslo'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

ALTER
PARTITION eu_west OF
TABLE transaction_item
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=eu-west-2]';

ALTER
PARTITION eu_south OF
TABLE transaction_item
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=eu-west-3]';

ALTER
PARTITION eu_central OF
TABLE transaction_item
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=eu-central-1]';

---------------------------------------------------------------
-- System ranges
---------------------------------------------------------------

ALTER
RANGE meta CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=eu-west-2: 2, +region=eu-central-1: 3, +region=eu-west-3: 2}';

ALTER
RANGE liveness CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=eu-west-2: 2, +region=eu-central-1: 3, +region=eu-west-3: 2}';

ALTER
RANGE system CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=eu-west-2: 2, +region=eu-central-1: 3, +region=eu-west-3: 2}';

ALTER DATABASE system CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=eu-west-2: 2, +region=eu-central-1: 3, +region=eu-west-3: 2}';