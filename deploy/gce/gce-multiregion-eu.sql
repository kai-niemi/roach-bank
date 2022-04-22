ALTER DATABASE roach_bank PRIMARY REGION "europe-west1";
ALTER DATABASE roach_bank ADD REGION "europe-west2";
ALTER DATABASE roach_bank ADD REGION "europe-west3";

-- Pin replicas to regions by disabling NVRs (fast stale reads out of primary region)
SET enable_multiregion_placement_policy=on;
-- ALTER DATABASE roach_bank PLACEMENT RESTRICTED;
-- ALTER DATABASE roach_bank PLACEMENT DEFAULT;

ALTER TABLE region_map SET locality GLOBAL;

ALTER TABLE account ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester') THEN 'europe-west1'
        WHEN city IN ('seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami') THEN 'europe-west2'
        WHEN city IN ('singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador') THEN 'europe-west3'
        END
    ) STORED NOT NULL;

ALTER TABLE account SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester') THEN 'europe-west1'
        WHEN city IN ('seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami') THEN 'europe-west2'
        WHEN city IN ('singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador') THEN 'europe-west3'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction_item ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN transaction_city IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester') THEN 'europe-west1'
        WHEN transaction_city IN ('seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami') THEN 'europe-west2'
        WHEN transaction_city IN ('singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador') THEN 'europe-west3'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction_item SET LOCALITY REGIONAL BY ROW AS region;