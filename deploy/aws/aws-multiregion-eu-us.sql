SET enable_multiregion_placement_policy=on;

ALTER DATABASE roach_bank PRIMARY REGION "eu-central-1";
ALTER DATABASE roach_bank ADD REGION "eu-west-1";
ALTER DATABASE roach_bank ADD REGION "us-east-1";

ALTER DATABASE roach_bank PLACEMENT RESTRICTED;
-- ALTER DATABASE roach_bank PLACEMENT DEFAULT;

ALTER TABLE region SET locality GLOBAL;

ALTER TABLE account ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('dublin','belfast','liverpool','manchester','glasgow') THEN 'eu-west-1'
        WHEN city IN ('london','birmingham','leeds','amsterdam','rotterdam','antwerp','hague','ghent','brussels') THEN 'eu-west-1'
        WHEN city IN ('berlin','hamburg','munich','frankfurt','dusseldorf','leipzig','dortmund','essen','stuttgart','stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'eu-central-1'
        WHEN city IN ('new york','boston','washington dc','miami','charlotte') THEN 'us-east-1'
        WHEN city IN ('phoenix','minneapolis','chicago','detroit','atlanta') THEN 'us-east-1'
        WHEN city IN ('seattle','san francisco','los angeles','portland','las vegas') THEN 'us-east-1'
        ELSE 'eu-central-1'
        END
    ) STORED NOT NULL;

ALTER TABLE account SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('dublin','belfast','liverpool','manchester','glasgow') THEN 'eu-west-1'
        WHEN city IN ('london','birmingham','leeds','amsterdam','rotterdam','antwerp','hague','ghent','brussels') THEN 'eu-west-1'
        WHEN city IN ('berlin','hamburg','munich','frankfurt','dusseldorf','leipzig','dortmund','essen','stuttgart','stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'eu-central-1'
        WHEN city IN ('new york','boston','washington dc','miami','charlotte') THEN 'us-east-1'
        WHEN city IN ('phoenix','minneapolis','chicago','detroit','atlanta') THEN 'us-east-1'
        WHEN city IN ('seattle','san francisco','los angeles','portland','las vegas') THEN 'us-east-1'
        ELSE 'eu-central-1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction_item ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('dublin','belfast','liverpool','manchester','glasgow') THEN 'eu-west-1'
        WHEN city IN ('london','birmingham','leeds','amsterdam','rotterdam','antwerp','hague','ghent','brussels') THEN 'eu-west-1'
        WHEN city IN ('berlin','hamburg','munich','frankfurt','dusseldorf','leipzig','dortmund','essen','stuttgart','stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'eu-central-1'
        WHEN city IN ('new york','boston','washington dc','miami','charlotte') THEN 'us-east-1'
        WHEN city IN ('phoenix','minneapolis','chicago','detroit','atlanta') THEN 'us-east-1'
        WHEN city IN ('seattle','san francisco','los angeles','portland','las vegas') THEN 'us-east-1'
        ELSE 'eu-central-1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction_item SET LOCALITY REGIONAL BY ROW AS region;

