
-- SET enable_multiregion_placement_policy=on;
-- ALTER DATABASE roach_bank PLACEMENT RESTRICTED;
-- ALTER DATABASE roach_bank PLACEMENT DEFAULT;

ALTER DATABASE roach_bank PRIMARY REGION "eu-central-1";
ALTER DATABASE roach_bank ADD REGION "eu-west-1";
ALTER DATABASE roach_bank ADD REGION "eu-west-2";

ALTER TABLE region SET locality GLOBAL;

ALTER TABLE account ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('dublin','belfast','liverpool','manchester','glasgow') THEN 'eu-west-1'
        WHEN city IN ('london','birmingham','leeds','amsterdam','rotterdam','antwerp','hague','ghent','brussels') THEN 'eu-west-2'
        WHEN city IN ('berlin','hamburg','munich','frankfurt','dusseldorf','leipzig','dortmund','essen','stuttgart','stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'eu-central-1'
        ELSE 'eu-central-1'
        END
    ) STORED NOT NULL;

ALTER TABLE account SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('dublin','belfast','liverpool','manchester','glasgow') THEN 'eu-west-1'
        WHEN city IN ('london','birmingham','leeds','amsterdam','rotterdam','antwerp','hague','ghent','brussels') THEN 'eu-west-2'
        WHEN city IN ('berlin','hamburg','munich','frankfurt','dusseldorf','leipzig','dortmund','essen','stuttgart','stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'eu-central-1'
        ELSE 'eu-central-1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction_item ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN transaction_city IN ('dublin','belfast','liverpool','manchester','glasgow') THEN 'eu-west-1'
        WHEN transaction_city IN ('london','birmingham','leeds','amsterdam','rotterdam','antwerp','hague','ghent','brussels') THEN 'eu-west-2'
        WHEN transaction_city IN ('berlin','hamburg','munich','frankfurt','dusseldorf','leipzig','dortmund','essen','stuttgart','stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'eu-central-1'
        ELSE 'eu-central-1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction_item SET LOCALITY REGIONAL BY ROW AS region;

-- Reduce regions to the ones that are relevant

DELETE from region where 1=1;

INSERT into region
VALUES
    ('eu-west-1', 'dublin,belfast,liverpool,manchester,glasgow'),
    ('eu-west-2', 'london,birmingham,leeds,amsterdam,rotterdam,antwerp,hague,ghent,brussels'),
    ('eu-central-1', 'berlin,hamburg,munich,frankfurt,dusseldorf,leipzig,dortmund,essen,stuttgart,stockholm,copenhagen,helsinki,oslo,riga,tallinn');
