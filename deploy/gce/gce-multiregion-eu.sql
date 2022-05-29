-- europe-west1 belgium
-- europe-west2 london
-- europe-west3 frankfurt

ALTER DATABASE roach_bank PRIMARY REGION "europe-west1";
ALTER DATABASE roach_bank ADD REGION "europe-west2";
ALTER DATABASE roach_bank ADD REGION "europe-west3";

-- Pin replicas to regions by disabling NVRs (fast stale reads out of primary region)
-- SET enable_multiregion_placement_policy=on;
-- ALTER DATABASE roach_bank PLACEMENT RESTRICTED;
-- ALTER DATABASE roach_bank PLACEMENT DEFAULT;

ALTER TABLE region SET locality GLOBAL;

ALTER TABLE account ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'europe-west1'
        WHEN city IN ('dublin','belfast','london','liverpool','manchester','glasgow','birmingham','leeds') THEN 'europe-west2'
        WHEN city IN ('amsterdam','rotterdam','antwerp','hague','ghent','brussels','berlin','hamburg','munich','frankfurt','dusseldorf','leipzig','dortmund','essen','stuttgart') THEN 'europe-west3'
        ELSE 'europe-west1'
        END
    ) STORED NOT NULL;

ALTER TABLE account SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'europe-west1'
        WHEN city IN ('dublin','belfast','london','liverpool','manchester','glasgow','birmingham','leeds') THEN 'europe-west2'
        WHEN city IN ('amsterdam','rotterdam','antwerp','hague','ghent','brussels','berlin','hamburg','munich','frankfurt','dusseldorf','leipzig','dortmund','essen','stuttgart') THEN 'europe-west3'
        ELSE 'europe-west1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction_item ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'europe-west1'
        WHEN city IN ('dublin','belfast','london','liverpool','manchester','glasgow','birmingham','leeds') THEN 'europe-west2'
        WHEN city IN ('amsterdam','rotterdam','antwerp','hague','ghent','brussels','berlin','hamburg','munich','frankfurt','dusseldorf','leipzig','dortmund','essen','stuttgart') THEN 'europe-west3'
        ELSE 'europe-west1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction_item SET LOCALITY REGIONAL BY ROW AS region;

