ALTER DATABASE roach_bank PRIMARY REGION "eu";
ALTER DATABASE roach_bank ADD REGION "us";
ALTER DATABASE roach_bank ADD REGION "sa";

-- drop table account;
-- ALTER TABLE account DROP COLUMN region;

ALTER TABLE account ADD COLUMN region crdb_internal_region AS (
  CASE
    WHEN city IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester') THEN 'eu'
    WHEN city IN ('seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami') THEN 'us'
    WHEN city IN ('singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador') THEN 'sa'
  END
) STORED NOT NULL;

ALTER TABLE account SET LOCALITY REGIONAL BY ROW AS region;

-- INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES
--     ('18955dc6-400d-4bb9-96c0-125bbe95e4ab', 'stockholm', '100.00', 'SEK', 'test', 'A', false, 0, clock_timestamp());

ALTER TABLE transaction ADD COLUMN region crdb_internal_region AS (
  CASE
    WHEN city IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester') THEN 'eu'
    WHEN city IN ('seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami') THEN 'us'
    WHEN city IN ('singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador') THEN 'sa'
  END
) STORED NOT NULL;

ALTER TABLE transaction SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction_item ADD COLUMN region crdb_internal_region AS (
  CASE
    WHEN transaction_city IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester') THEN 'eu'
    WHEN transaction_city IN ('seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami') THEN 'us'
    WHEN transaction_city IN ('singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador') THEN 'sa'
  END
) STORED NOT NULL;

ALTER TABLE transaction_item SET LOCALITY REGIONAL BY ROW AS region;

SHOW CREATE TABLE account;
SHOW CREATE TABLE transaction;
SHOW CREATE TABLE transaction_item;