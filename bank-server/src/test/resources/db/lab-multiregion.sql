ALTER DATABASE roach_bank PRIMARY REGION "eu-central-1";
ALTER DATABASE roach_bank ADD REGION "eu-west-1";
ALTER DATABASE roach_bank ADD REGION "eu-west-2";
ALTER DATABASE roach_bank ADD REGION "us-east-1";
ALTER DATABASE roach_bank ADD REGION "us-east-2";
ALTER DATABASE roach_bank ADD REGION "us-west-1";

-- Pin replicas to regions by disabling NVRs (fast stale reads out of primary region)
-- SET enable_multiregion_placement_policy=on;
-- ALTER DATABASE roach_bank PLACEMENT RESTRICTED;
-- ALTER DATABASE roach_bank PLACEMENT DEFAULT;

ALTER TABLE city SET locality GLOBAL;
ALTER TABLE region SET locality GLOBAL;

ALTER TABLE account ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'eu-central-1'
        WHEN city IN ('dublin','belfast','london','liverpool','manchester','glasgow','birmingham','leeds') THEN 'eu-west-1'
        WHEN city IN ('madrid','barcelona','sintra','rome','milan','lyon','lisbon','toulouse','paris','cologne','seville','marseille','naples','turin','valencia','palermo') THEN 'eu-west-2'
        ELSE 'eu-central-1'
        END
    ) STORED NOT NULL;

-- INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated_at) VALUES
--     (gen_random_uuid(), 'madrid', '100.00', 'SEK', 'test', 'A', false, 0, clock_timestamp()) returning id;

SHOW RANGE FROM TABLE account FOR ROW ('eu-west-2','0707ffe1-1d62-4946-9061-f9a5608ae702');

ALTER TABLE account SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'eu-central-1'
        WHEN city IN ('dublin','belfast','london','liverpool','manchester','glasgow','birmingham','leeds') THEN 'eu-west-1'
        WHEN city IN ('madrid','barcelona','sintra','rome','milan','lyon','lisbon','toulouse','paris','cologne','seville','marseille','naples','turin','valencia','palermo') THEN 'eu-west-2'
        ELSE 'eu-central-1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction_item ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'eu-central-1'
        WHEN city IN ('dublin','belfast','london','liverpool','manchester','glasgow','birmingham','leeds') THEN 'eu-west-1'
        WHEN city IN ('madrid','barcelona','sintra','rome','milan','lyon','lisbon','toulouse','paris','cologne','seville','marseille','naples','turin','valencia','palermo') THEN 'eu-west-2'
        ELSE 'eu-central-1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction_item SET LOCALITY REGIONAL BY ROW AS region;
