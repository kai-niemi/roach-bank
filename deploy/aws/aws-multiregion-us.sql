-- SET enable_multiregion_placement_policy=on;
-- ALTER DATABASE roach_bank PLACEMENT RESTRICTED;
-- ALTER DATABASE roach_bank PLACEMENT DEFAULT;

ALTER DATABASE roach_bank PRIMARY REGION "us-east-1";
ALTER DATABASE roach_bank ADD REGION "us-east-2";
ALTER DATABASE roach_bank ADD REGION "us-west-2";

-- Pin replicas to regions by disabling NVRs (fast stale reads out of primary region)
-- SET enable_multiregion_placement_policy=on;
-- ALTER DATABASE roach_bank PLACEMENT RESTRICTED;
-- ALTER DATABASE roach_bank PLACEMENT DEFAULT;

ALTER TABLE region SET locality GLOBAL;

ALTER TABLE account ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('new york','boston','washington dc','miami','charlotte') THEN 'us-east-1'
        WHEN city IN ('phoenix','minneapolis','chicago','detroit','atlanta') THEN 'us-east-2'
        WHEN city IN ('seattle','san francisco','los angeles','portland','las vegas') THEN 'us-west-2'
        ELSE 'us-east-1'
        END
    ) STORED NOT NULL;

ALTER TABLE account SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('new york','boston','washington dc','miami','charlotte') THEN 'us-east-1'
        WHEN city IN ('phoenix','minneapolis','chicago','detroit','atlanta') THEN 'us-east-2'
        WHEN city IN ('seattle','san francisco','los angeles','portland','las vegas') THEN 'us-west-2'
        ELSE 'us-east-1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction_item ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN transaction_city IN ('new york','boston','washington dc','miami','charlotte') THEN 'us-east-1'
        WHEN transaction_city IN ('phoenix','minneapolis','chicago','detroit','atlanta') THEN 'us-east-2'
        WHEN transaction_city IN ('seattle','san francisco','los angeles','portland','las vegas') THEN 'us-west-2'
        ELSE 'us-east-1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction_item SET LOCALITY REGIONAL BY ROW AS region;
