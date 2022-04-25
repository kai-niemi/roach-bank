import into account(id,city,balance,currency,name,description,type,closed,allow_negative,updated)
    CSV DATA (
                 'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-1.csv',
                 'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-2.csv',
                 'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-3.csv',
                 'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-4.csv',
                 'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-5.csv',
                 'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-6.csv',
                 'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-7.csv',
                 'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-8.csv',
                 'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-9.csv',
                 'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-10.csv'
             );

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
ALTER TABLE city SET locality GLOBAL;

ALTER TABLE account ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester') THEN 'europe-west1'
        WHEN city IN ('seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami') THEN 'europe-west2'
        WHEN city IN ('singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador') THEN 'europe-west3'
        ELSE 'europe-west1'
        END
    ) STORED NOT NULL;

ALTER TABLE account SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester') THEN 'europe-west1'
        WHEN city IN ('seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami') THEN 'europe-west2'
        WHEN city IN ('singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador') THEN 'europe-west3'
        ELSE 'europe-west1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction_item ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN transaction_city IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester') THEN 'europe-west1'
        WHEN transaction_city IN ('seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami') THEN 'europe-west2'
        WHEN transaction_city IN ('singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador') THEN 'europe-west3'
        ELSE 'europe-west1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction_item SET LOCALITY REGIONAL BY ROW AS region;
