-- SET enable_multiregion_placement_policy=on;
-- ALTER DATABASE roach_bank PLACEMENT RESTRICTED;
-- ALTER DATABASE roach_bank PLACEMENT DEFAULT;

ALTER DATABASE roach_bank PRIMARY REGION "us-east";
ALTER DATABASE roach_bank ADD REGION "us-central";
ALTER DATABASE roach_bank ADD REGION "us-west";

-- Pin replicas to regions by disabling NVRs (fast stale reads out of primary region)
-- SET enable_multiregion_placement_policy=on;
-- ALTER DATABASE roach_bank PLACEMENT RESTRICTED;
-- ALTER DATABASE roach_bank PLACEMENT DEFAULT;

ALTER TABLE region SET locality GLOBAL;

ALTER TABLE account ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('new york','boston','washington dc','miami','charlotte') THEN 'us-east'
        WHEN city IN ('phoenix','minneapolis','chicago','detroit','atlanta') THEN 'us-central'
        WHEN city IN ('seattle','san francisco','los angeles','portland','las vegas') THEN 'us-west'
        ELSE 'us-east'
        END
    ) STORED NOT NULL;

ALTER TABLE account SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('new york','boston','washington dc','miami','charlotte') THEN 'us-east'
        WHEN city IN ('phoenix','minneapolis','chicago','detroit','atlanta') THEN 'us-central'
        WHEN city IN ('seattle','san francisco','los angeles','portland','las vegas') THEN 'us-west'
        ELSE 'us-east'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction_item ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN transaction_city IN ('new york','boston','washington dc','miami','charlotte') THEN 'us-east'
        WHEN transaction_city IN ('phoenix','minneapolis','chicago','detroit','atlanta') THEN 'us-central'
        WHEN transaction_city IN ('seattle','san francisco','los angeles','portland','las vegas') THEN 'us-west'
        ELSE 'us-east'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction_item SET LOCALITY REGIONAL BY ROW AS region;

-- Reduce regions to the ones that are relevant

DELETE from region where 1=1;

INSERT into region
VALUES ('us-east', 'new york,boston,washington dc,miami,charlotte'),
       ('us-central', 'phoenix,minneapolis,chicago,detroit,atlanta'),
       ('us-west', 'seattle,san francisco,los angeles,portland,las vegas');
