ALTER DATABASE roach_bank PRIMARY REGION "us_west";
ALTER DATABASE roach_bank ADD REGION "us_central";
ALTER DATABASE roach_bank ADD REGION "us_east";

ALTER TABLE account ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('seattle','san francisco','los angeles','phoenix','stockholm','helsinki','oslo','london','paris','manchester') THEN 'us_west'
        WHEN city IN ('minneapolis','chicago','detroit','atlanta','frankfurt','amsterdam','milano','madrid','athens','barcelona') THEN 'us_central'
        WHEN city IN ('new york','boston','washington dc','miami','singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador') THEN 'us_east'
        END
    ) STORED NOT NULL;

ALTER TABLE account SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('seattle','san francisco','los angeles','phoenix','stockholm','helsinki','oslo','london','paris','manchester') THEN 'us_west'
        WHEN city IN ('minneapolis','chicago','detroit','atlanta','frankfurt','amsterdam','milano','madrid','athens','barcelona') THEN 'us_central'
        WHEN city IN ('new york','boston','washington dc','miami','singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador') THEN 'us_east'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction_item ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN transaction_city IN ('seattle','san francisco','los angeles','phoenix','stockholm','helsinki','oslo','london','paris','manchester') THEN 'us_west'
        WHEN transaction_city IN ('minneapolis','chicago','detroit','atlanta','frankfurt','amsterdam','milano','madrid','athens','barcelona') THEN 'us_central'
        WHEN transaction_city IN ('new york','boston','washington dc','miami','singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador') THEN 'us_east'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction_item SET LOCALITY REGIONAL BY ROW AS region;
