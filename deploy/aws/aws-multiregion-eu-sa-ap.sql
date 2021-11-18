---------------------------------------------------------------
-- ACCOUNT
---------------------------------------------------------------

ALTER TABLE account PARTITION BY LIST (region) (
    PARTITION eu VALUES IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester'),
    PARTITION sa VALUES IN ('sao paulo','rio de janeiro','salvador','seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami'),
    PARTITION ap VALUES IN ('singapore','hong kong','sydney','tokyo'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

-- Pin partitions to regions
ALTER PARTITION eu OF TABLE account CONFIGURE ZONE USING num_replicas=3, constraints='[+region=eu-central-1]';
ALTER PARTITION sa OF TABLE account CONFIGURE ZONE USING num_replicas=3, constraints='[+region=sa-east-1]';
ALTER PARTITION ap OF TABLE account CONFIGURE ZONE USING num_replicas=3, constraints='[+region=ap-northeast-1]';

---------------------------------------------------------------
-- TRANSACTION
---------------------------------------------------------------
ALTER TABLE transaction PARTITION BY LIST (region) (
    PARTITION eu VALUES IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester'),
    PARTITION sa VALUES IN ('sao paulo','rio de janeiro','salvador','seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami'),
    PARTITION ap VALUES IN ('singapore','hong kong','sydney','tokyo'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

ALTER PARTITION eu OF TABLE transaction CONFIGURE ZONE USING num_replicas=3, constraints='[+region=eu-central-1]';
ALTER PARTITION sa OF TABLE transaction CONFIGURE ZONE USING num_replicas=3, constraints='[+region=sa-east-1]';
ALTER PARTITION ap OF TABLE transaction CONFIGURE ZONE USING num_replicas=3, constraints='[+region=ap-northeast-1]';

---------------------------------------------------------------
-- TRANSACTION_ITEM
---------------------------------------------------------------

ALTER TABLE transaction_item PARTITION BY LIST (transaction_region) (
    PARTITION eu VALUES IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester'),
    PARTITION sa VALUES IN ('sao paulo','rio de janeiro','salvador','seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami'),
    PARTITION ap VALUES IN ('singapore','hong kong','sydney','tokyo'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

ALTER PARTITION eu OF TABLE transaction_item CONFIGURE ZONE USING num_replicas=3, constraints='[+region=eu-central-1]';
ALTER PARTITION sa OF TABLE transaction_item CONFIGURE ZONE USING num_replicas=3, constraints='[+region=sa-east-1]';
ALTER PARTITION ap OF TABLE transaction_item CONFIGURE ZONE USING num_replicas=3, constraints='[+region=ap-northeast-1]';

---------------------------------------------------------------
-- System ranges
---------------------------------------------------------------

ALTER RANGE meta CONFIGURE ZONE USING num_replicas = 9,
    constraints = '{+region=sa-east-1: 2, +region=eu-central-1: 3, +region=ap-northeast-1: 2}';

ALTER RANGE liveness CONFIGURE ZONE USING num_replicas = 9,
    constraints = '{+region=sa-east-1: 2, +region=eu-central-1: 3, +region=ap-northeast-1: 2}';

ALTER RANGE system CONFIGURE ZONE USING num_replicas = 9,
    constraints = '{+region=sa-east-1: 2, +region=eu-central-1: 3, +region=ap-northeast-1: 2}';

ALTER DATABASE system CONFIGURE ZONE USING num_replicas = 9,
    constraints = '{+region=sa-east-1: 2, +region=eu-central-1: 3, +region=ap-northeast-1: 2}';
