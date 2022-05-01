ALTER DATABASE roach_bank PRIMARY REGION "gcp-europe-west1";
ALTER DATABASE roach_bank ADD REGION "gcp-europe-west2";
ALTER DATABASE roach_bank ADD REGION "gcp-europe-west4";

-- Pin replicas to regions by disabling NVRs (fast stale reads out of primary region)
-- SET enable_multiregion_placement_policy=on;
-- ALTER DATABASE roach_bank PLACEMENT RESTRICTED;
-- ALTER DATABASE roach_bank PLACEMENT DEFAULT;

ALTER TABLE region SET locality GLOBAL;

ALTER TABLE account ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'gcp-europe-west1'
        WHEN city IN ('dublin','belfast','london','liverpool','manchester','glasgow','birmingham','leeds') THEN 'gcp-europe-west2'
        WHEN city IN ('madrid','barcelona','sintra','rome','milan','lyon','lisbon','toulouse','paris','cologne','seville','marseille','naples','turin','valencia','palermo') THEN 'gcp-europe-west4'
        ELSE 'gcp-europe-west1'
        END
    ) STORED NOT NULL;

ALTER TABLE account SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'gcp-europe-west1'
        WHEN city IN ('dublin','belfast','london','liverpool','manchester','glasgow','birmingham','leeds') THEN 'gcp-europe-west2'
        WHEN city IN ('madrid','barcelona','sintra','rome','milan','lyon','lisbon','toulouse','paris','cologne','seville','marseille','naples','turin','valencia','palermo') THEN 'gcp-europe-west4'
        ELSE 'gcp-europe-west1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction_item ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN transaction_city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'gcp-europe-west1'
        WHEN transaction_city IN ('dublin','belfast','london','liverpool','manchester','glasgow','birmingham','leeds') THEN 'gcp-europe-west2'
        WHEN transaction_city IN ('madrid','barcelona','sintra','rome','milan','lyon','lisbon','toulouse','paris','cologne','seville','marseille','naples','turin','valencia','palermo') THEN 'gcp-europe-west4'
        ELSE 'gcp-europe-west1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction_item SET LOCALITY REGIONAL BY ROW AS region;
