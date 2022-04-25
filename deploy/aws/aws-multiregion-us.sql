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

ALTER DATABASE roach_bank PRIMARY REGION "us_west";
ALTER DATABASE roach_bank ADD REGION "us_central";
ALTER DATABASE roach_bank ADD REGION "us_east";

ALTER TABLE region SET locality GLOBAL;
ALTER TABLE city SET locality GLOBAL;

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
