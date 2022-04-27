-- SET enable_multiregion_placement_policy=on;
-- ALTER DATABASE roach_bank PLACEMENT RESTRICTED;
-- ALTER DATABASE roach_bank PLACEMENT DEFAULT;

ALTER DATABASE roach_bank PRIMARY REGION "us-west";
ALTER DATABASE roach_bank ADD REGION "us-central";
ALTER DATABASE roach_bank ADD REGION "us-east";

ALTER TABLE region SET locality GLOBAL;
ALTER TABLE city SET locality GLOBAL;

ALTER TABLE account ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('seattle','san francisco','los angeles','phoenix','stockholm','helsinki','oslo','london','paris','manchester') THEN 'us-west'
        WHEN city IN ('minneapolis','chicago','detroit','atlanta','frankfurt','amsterdam','milano','madrid','athens','barcelona') THEN 'us-central'
        WHEN city IN ('new york','boston','washington dc','miami','singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador') THEN 'us-east'
        default 'us-west'
        END
    ) STORED NOT NULL;

ALTER TABLE account SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('seattle','san francisco','los angeles','phoenix','stockholm','helsinki','oslo','london','paris','manchester') THEN 'us-west'
        WHEN city IN ('minneapolis','chicago','detroit','atlanta','frankfurt','amsterdam','milano','madrid','athens','barcelona') THEN 'us-central'
        WHEN city IN ('new york','boston','washington dc','miami','singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador') THEN 'us-east'
        default 'us-west'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction_item ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN transaction_city IN ('seattle','san francisco','los angeles','phoenix','stockholm','helsinki','oslo','london','paris','manchester') THEN 'us-west'
        WHEN transaction_city IN ('minneapolis','chicago','detroit','atlanta','frankfurt','amsterdam','milano','madrid','athens','barcelona') THEN 'us-central'
        WHEN transaction_city IN ('new york','boston','washington dc','miami','singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador') THEN 'us-east'
        default 'us-west'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction_item SET LOCALITY REGIONAL BY ROW AS region;
