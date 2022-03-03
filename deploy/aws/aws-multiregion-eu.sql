---------------------------------------------------------------
-- ACCOUNT
---------------------------------------------------------------

ALTER TABLE account PARTITION BY LIST (region) (
    PARTITION eu_west_1 VALUES IN ('london','amsterdam','manchester','paris'),
    PARTITION eu_west_2 VALUES IN ('milano','madrid','athens','barcelona'),
    PARTITION eu_central_1 VALUES IN ('frankfurt','stockholm','helsinki','oslo'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

-- Pin partitions to regions
ALTER PARTITION eu_west_1 OF TABLE account CONFIGURE ZONE USING num_replicas=3, constraints='[+region=eu-west-1]';
ALTER PARTITION eu_west_2 OF TABLE account CONFIGURE ZONE USING num_replicas=3, constraints='[+region=eu-west-2]';
ALTER PARTITION eu_central_1 OF TABLE account CONFIGURE ZONE USING num_replicas=3, constraints='[+region=eu-central-1]';

---------------------------------------------------------------
-- TRANSACTION
---------------------------------------------------------------
ALTER TABLE transaction PARTITION BY LIST (region) (
    PARTITION eu_west_1 VALUES IN ('london','amsterdam','manchester','paris'),
    PARTITION eu_west_2 VALUES IN ('milano','madrid','athens','barcelona'),
    PARTITION eu_central_1 VALUES IN ('frankfurt','stockholm','helsinki','oslo'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

ALTER PARTITION eu_west_1 OF TABLE transaction CONFIGURE ZONE USING num_replicas=3, constraints='[+region=eu-west-1]';
ALTER PARTITION eu_west_2 OF TABLE transaction CONFIGURE ZONE USING num_replicas=3, constraints='[+region=eu-west-2]';
ALTER PARTITION eu_central_1 OF TABLE transaction CONFIGURE ZONE USING num_replicas=3, constraints='[+region=eu-central-1]';

---------------------------------------------------------------
-- TRANSACTION_ITEM
---------------------------------------------------------------

ALTER TABLE transaction_item PARTITION BY LIST (transaction_region) (
    PARTITION eu_west_1 VALUES IN ('london','amsterdam','manchester','paris'),
    PARTITION eu_west_2 VALUES IN ('milano','madrid','athens','barcelona'),
    PARTITION eu_central_1 VALUES IN ('frankfurt','stockholm','helsinki','oslo'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

ALTER PARTITION eu_west_1 OF TABLE transaction_item CONFIGURE ZONE USING num_replicas=3, constraints='[+region=eu-west-1]';
ALTER PARTITION eu_west_2 OF TABLE transaction_item CONFIGURE ZONE USING num_replicas=3, constraints='[+region=eu-west-2]';
ALTER PARTITION eu_central_1 OF TABLE transaction_item CONFIGURE ZONE USING num_replicas=3, constraints='[+region=eu-central-1]';

---------------------------------------------------------------
-- System ranges
---------------------------------------------------------------

ALTER RANGE meta CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=eu-west-1: 2, +region=eu-central-1: 3, +region=eu-west-2: 2}';

ALTER RANGE liveness CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=eu-west-1: 2, +region=eu-central-1: 3, +region=eu-west-2: 2}';

ALTER RANGE system CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=eu-west-1: 2, +region=eu-central-1: 3, +region=eu-west-2: 2}';

ALTER DATABASE system CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=eu-west-1: 2, +region=eu-central-1: 3, +region=eu-west-2: 2}';