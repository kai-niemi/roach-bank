-- Geo-partitioning schema configuration for:
--   us-east-1 (N. Virginia)
--   eu-central-1 (Frankfurt)
--   ap-southeast-1 (Singapore)

---------------------------------------------------------------
-- TRANSACTION_TYPE
---------------------------------------------------------------

-- Create secondary indexes for other regions
CREATE INDEX idx_us on transaction_type (id) STORING (name);
CREATE INDEX idx_ap on transaction_type (id) STORING (name);

--  Pin primary index lease holder to EU
ALTER INDEX transaction_type@primary CONFIGURE ZONE USING
    num_replicas = 3,
    constraints = '{"+region=eu-central-1":1}',
    lease_preferences = '[[+region=eu-central-1]]';

--  Pin secondary index lease holder to US
ALTER INDEX transaction_type@idx_us CONFIGURE ZONE USING
    num_replicas = 3,
    constraints = '{"+region=us-east-1":1}',
    lease_preferences = '[[+region=us-east-1]]';

--  Pin secondary index lease holder to APAC
ALTER INDEX transaction_type@idx_ap CONFIGURE ZONE USING
    num_replicas = 3,
    constraints = '{"+region=ap-southeast-1":1}',
    lease_preferences = '[[+region=ap-southeast-1]]';

-- show ranges from index transaction_type@primary;
-- show ranges from index transaction_type@idx_us;
-- show ranges from index transaction_type@idx_ap;

---------------------------------------------------------------
-- ACCOUNT
---------------------------------------------------------------

ALTER TABLE account PARTITION BY LIST (region) (
    PARTITION us VALUES IN ('seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami'),
    PARTITION eu VALUES IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester'),
    PARTITION ap VALUES IN ('singapore','hong kong','sydney','tokyo'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

-- Pin partitions to regions
ALTER
PARTITION us OF
TABLE account
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=us-east-1]';

ALTER
PARTITION eu OF
TABLE account
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=eu-central-1]';

ALTER
PARTITION ap OF
TABLE account
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=ap-southeast-1]';

---------------------------------------------------------------
-- TRANSACTION
---------------------------------------------------------------
ALTER TABLE transaction PARTITION BY LIST (region) (
    PARTITION us VALUES IN ('seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami'),
    PARTITION eu VALUES IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester'),
    PARTITION ap VALUES IN ('singapore','hong kong','sydney','tokyo'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

ALTER
PARTITION us OF
TABLE transaction
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=us-east-1]';

ALTER
PARTITION eu OF
TABLE transaction
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=eu-central-1]';

ALTER
PARTITION ap OF
TABLE transaction
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=ap-southeast-1]';

---------------------------------------------------------------
-- TRANSACTION_ITEM
---------------------------------------------------------------

ALTER TABLE transaction_item PARTITION BY LIST (transaction_region) (
    PARTITION us VALUES IN ('seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami'),
    PARTITION eu VALUES IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester'),
    PARTITION ap VALUES IN ('singapore','hong kong','sydney','tokyo'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

ALTER
PARTITION us OF
TABLE transaction_item
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=us-east-1]';

ALTER
PARTITION eu OF
TABLE transaction_item
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=eu-central-1]';

ALTER
PARTITION ap OF
TABLE transaction_item
CONFIGURE ZONE USING
    num_replicas=3,
    constraints='[+region=ap-southeast-1]';

---------------------------------------------------------------
-- System ranges
---------------------------------------------------------------

ALTER
RANGE meta CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=us-east-1: 2, +region=eu-central-1: 3, +region=ap-southeast-1: 2}';

ALTER
RANGE liveness CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=us-east-1: 2, +region=eu-central-1: 3, +region=ap-southeast-1: 2}';

ALTER
RANGE system CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=us-east-1: 2, +region=eu-central-1: 3, +region=ap-southeast-1: 2}';

ALTER DATABASE system CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=us-east-1: 2, +region=eu-central-1: 3, +region=ap-southeast-1: 2}';