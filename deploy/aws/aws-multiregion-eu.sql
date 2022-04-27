
-- SET enable_multiregion_placement_policy=on;
-- ALTER DATABASE roach_bank PLACEMENT RESTRICTED;
-- ALTER DATABASE roach_bank PLACEMENT DEFAULT;

ALTER DATABASE roach_bank PRIMARY REGION "eu-central-1";
ALTER DATABASE roach_bank ADD REGION "eu-west-1";
ALTER DATABASE roach_bank ADD REGION "eu-west-2";

ALTER TABLE region SET locality GLOBAL;
ALTER TABLE city SET locality GLOBAL;

ALTER TABLE account ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('dublin','belfast','london','liverpool','manchester','glasgow','birmingham','leeds') THEN 'eu-west-1'
        WHEN city IN ('madrid','barcelona','sintra','rome','milan','lyon','lisbon','toulouse','paris','cologne','seville','marseille','naples','turin','valencia','palermo') THEN 'eu-west-2'
        WHEN city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn','amsterdam','rotterdam','antwerp','hague','ghent','brussels','berlin','hamburg','munich','frankfurt','dusseldorf','leipzig','dortmund','essen','stuttgart','krakov','zagraeb','zaragoza','lodz','athens','bratislava','prague','sofia','bucharest','vienna','warsaw','budapest') THEN 'eu-central-1'
        ELSE 'eu-central-1'
        END
    ) STORED NOT NULL;

ALTER TABLE account SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('dublin','belfast','london','liverpool','manchester','glasgow','birmingham','leeds') THEN 'eu-west-1'
        WHEN city IN ('madrid','barcelona','sintra','rome','milan','lyon','lisbon','toulouse','paris','cologne','seville','marseille','naples','turin','valencia','palermo') THEN 'eu-west-2'
        WHEN city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn','amsterdam','rotterdam','antwerp','hague','ghent','brussels','berlin','hamburg','munich','frankfurt','dusseldorf','leipzig','dortmund','essen','stuttgart','krakov','zagraeb','zaragoza','lodz','athens','bratislava','prague','sofia','bucharest','vienna','warsaw','budapest') THEN 'eu-central-1'
        ELSE 'eu-central-1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction_item ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN transaction_city IN ('dublin','belfast','london','liverpool','manchester','glasgow','birmingham','leeds') THEN 'eu-west-1'
        WHEN transaction_city IN ('madrid','barcelona','sintra','rome','milan','lyon','lisbon','toulouse','paris','cologne','seville','marseille','naples','turin','valencia','palermo') THEN 'eu-west-2'
        WHEN transaction_city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn','amsterdam','rotterdam','antwerp','hague','ghent','brussels','berlin','hamburg','munich','frankfurt','dusseldorf','leipzig','dortmund','essen','stuttgart','krakov','zagraeb','zaragoza','lodz','athens','bratislava','prague','sofia','bucharest','vienna','warsaw','budapest') THEN 'eu-central-1'
        ELSE 'eu-central-1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction_item SET LOCALITY REGIONAL BY ROW AS region;
