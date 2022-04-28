DELETE from region where 1=1;

INSERT into region
VALUES
('eu-west-1', 'dublin,belfast,liverpool,manchester,glasgow'),
('eu-west-2', 'london,birmingham,leeds,amsterdam,rotterdam,antwerp,hague,ghent,brussels'),
('eu-west-3', 'madrid,barcelona,sintra,rome,milan,lyon,lisbon,toulouse,paris,cologne,seville,marseille,naples,turin,valencia,palermo'),
('eu-central-1', 'berlin,hamburg,munich,frankfurt,dusseldorf,leipzig,dortmund,essen,stuttgart,stockholm,copenhagen,helsinki,oslo,riga,tallinn'),

('northeurope', 'stockholm,copenhagen,helsinki,oslo,riga,tallinn'),
('westeurope', 'dublin,belfast,london,liverpool,manchester,glasgow,birmingham,leeds'),

('uksouth', 'dublin,belfast,london,liverpool,manchester,glasgow,birmingham,leeds'),
('ukwest', 'dublin,belfast,london,liverpool,manchester,glasgow,birmingham,leeds'),

('europe-west1', 'stockholm,copenhagen,helsinki,oslo,riga,tallinn'),
('europe-west2', 'dublin,belfast,london,liverpool,manchester,glasgow,birmingham,leeds'),
('europe-west3', 'amsterdam,rotterdam,antwerp,hague,ghent,brussels,berlin,hamburg,munich,frankfurt,dusseldorf,leipzig,dortmund,essen,stuttgart'),
('europe-west4', 'madrid,barcelona,sintra,rome,milan,lyon,lisbon,toulouse,paris,cologne,seville,marseille,naples,turin,valencia,palermo'),
('europe-west6', 'krakov,zagraeb,zaragoza,lodz,athens,bratislava,prague,sofia,bucharest,vienna,warsaw,budapest'),

('francecentral', 'madrid,barcelona,sintra,rome,milan,lyon,lisbon,toulouse,paris,cologne,seville,marseille,naples,turin,valencia,palermo'),
('francesouth', 'madrid,barcelona,sintra,rome,milan,lyon,lisbon,toulouse,paris,cologne,seville,marseille,naples,turin,valencia,palermo'),

('gcp-europe-west4', 'krakov,zagraeb,zaragoza,lodz,athens,bratislava,prague,sofia,bucharest,vienna,warsaw,budapest');
