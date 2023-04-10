SHOW REGIONS;

ALTER DATABASE roach_bank PRIMARY REGION "eu-central-1";
ALTER DATABASE roach_bank ADD REGION "us-west-1";
ALTER DATABASE roach_bank ADD REGION "eu-west-2";
ALTER DATABASE roach_bank ADD REGION "eu-west-1";
ALTER DATABASE roach_bank ADD REGION "us-east-1";
ALTER DATABASE roach_bank ADD REGION "us-east-2";

-- Pin replicas to regions by disabling NVRs (fast stale reads out of primary region)
-- SET enable_multiregion_placement_policy=on;
-- ALTER DATABASE roach_bank PLACEMENT RESTRICTED;
-- ALTER DATABASE roach_bank PLACEMENT DEFAULT;

ALTER TABLE region SET locality GLOBAL;

ALTER TABLE account ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'eu-north-1'
        WHEN city IN ('dublin','belfast','london','liverpool','manchester','glasgow','birmingham','leeds') THEN 'eu-west-3'
        WHEN city IN ('madrid','barcelona','sintra','rome','milan','lyon','lisbon','toulouse','paris','cologne','seville','marseille','naples','turin','valencia','palermo') THEN 'eu-central-1'
        ELSE 'eu-north-1'
        END
    ) STORED NOT NULL;

ALTER TABLE account SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'eu-north-1'
        WHEN city IN ('dublin','belfast','london','liverpool','manchester','glasgow','birmingham','leeds') THEN 'eu-west-3'
        WHEN city IN ('madrid','barcelona','sintra','rome','milan','lyon','lisbon','toulouse','paris','cologne','seville','marseille','naples','turin','valencia','palermo') THEN 'eu-central-1'
        ELSE 'eu-north-1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction_item ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'eu-north-1'
        WHEN city IN ('dublin','belfast','london','liverpool','manchester','glasgow','birmingham','leeds') THEN 'eu-west-3'
        WHEN city IN ('madrid','barcelona','sintra','rome','milan','lyon','lisbon','toulouse','paris','cologne','seville','marseille','naples','turin','valencia','palermo') THEN 'eu-central-1'
        ELSE 'eu-north-1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction_item SET LOCALITY REGIONAL BY ROW AS region;

-----------------

SHOW ZONE CONFIGURATION FOR RANGE meta;
SHOW ZONE CONFIGURATION FOR RANGE default;
SHOW ZONE CONFIGURATION FOR PARTITION europe_west1 OF TABLE account;

SHOW RANGES FROM TABLE account;
SHOW RANGES from index account@primary;
SHOW RANGE FROM TABLE account FOR ROW ('eu-north-1','02fde064-10b1-4568-be1c-9012f97cd448');

SELECT region FROM [SHOW regions];
SELECT * FROM [SHOW RANGES FROM TABLE account] WHERE "start_key" NOT LIKE '%Prefix%';
SELECT * FROM crdb_internal.ranges;
SELECT * FROM system.replication_constraint_stats WHERE violating_ranges > 0;

EXPLAIN ANALYZE SELECT * FROM account WHERE city='stockholm' and id='02fde064-10b1-4568-be1c-9012f97cd448';
