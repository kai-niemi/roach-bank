--
-- Region to city mapping metadata
--
DELETE  from region where 1=1;

INSERT into region
VALUES ('aws','us-east-1', 'new york,boston,washington dc,miami,charlotte,atlanta'),
       ('aws','us-east-2', 'chicago,st louis,indianapolis,nashville,dallas,houston'),
       ('aws','us-west-1', 'san francisco,los angeles,san diego,portland,las vegas,salt lake city'),
       ('aws','us-west-2', 'seattle,tacoma,portland,salem,bend,eugene'),
       ('aws','ca-central-1', 'calgary,edmonton,winnipeg,regina,brandon,dryden'),
       ('aws','eu-west-1', 'dublin,belfast,liverpool,manchester,glasgow'),
       ('aws','eu-west-2', 'london,birmingham,leeds,amsterdam,rotterdam,antwerp,hague,ghent,brussels'),
       ('aws','eu-west-3', 'madrid,barcelona,sintra,rome,milan,lyon,lisbon,toulouse,paris,cologne,seville,marseille,naples,turin,valencia,palermo'),
       ('aws','eu-central-1', 'berlin,hamburg,munich,frankfurt,dusseldorf,leipzig,dortmund,essen,stuttgart'),
       ('aws','ap-northeast-1', 'hong kong,beijing,shanghai,tokyo'),
       ('aws','ap-northeast-2', 'hong kong,beijing,shanghai,tokyo'),
       ('aws','ap-northeast-3', 'hong kong,beijing,shanghai,tokyo'),
       ('aws','ap-southeast-1', 'singapore,jakarta,sydney,melbourne'),
       ('aws','ap-southeast-2', 'singapore,jakarta,sydney,melbourne'),
       ('aws','ap-south-1', 'singapore,jakarta,sydney,melbourne'),
       ('aws','sa-east-1', 'sao paulo,rio de janeiro,salvador,buenos aires'),
       ('aws','eu-north-1', 'stockholm,copenhagen,helsinki,oslo,riga,tallinn'),

       ('azure','eastasia', 'hong kong,beijing,shanghai,tokyo'),
       ('azure','southeastasia', 'singapore,jakarta,sydney,melbourne'),
       ('azure','centralus', 'phoenix,minneapolis,chicago,detroit,atlanta'),
       ('azure','eastus', 'new york,boston,washington dc,miami,charlotte'),
       ('azure','eastus2', 'new york,boston,washington dc,miami,charlotte'),
       ('azure','westus', 'seattle,san francisco,los angeles,portland,las vegas'),
       ('azure','northcentralus', 'phoenix,minneapolis,chicago,detroit,atlanta'),
       ('azure','southcentralus', 'phoenix,minneapolis,chicago,detroit,atlanta'),
       ('azure','northeurope', 'stockholm,copenhagen,helsinki,oslo,riga,tallinn'),
       ('azure','westeurope', 'dublin,belfast,london,liverpool,manchester,glasgow,birmingham,leeds'),
       ('azure','japanwest', 'hong kong,beijing,shanghai,tokyo'),
       ('azure','japaneast', 'hong kong,beijing,shanghai,tokyo'),
       ('azure','brazilsouth', 'sao paulo,rio de janeiro,salvador,buenos aires'),
       ('azure','australiaeast', 'singapore,jakarta,sydney,melbourne'),
       ('azure','australiasoutheast', 'singapore,jakarta,sydney,melbourne'),
       ('azure','southindia', 'singapore,jakarta,sydney,melbourne'),
       ('azure','centralindia', 'singapore,jakarta,sydney,melbourne'),
       ('azure','westindia', 'singapore,jakarta,sydney,melbourne'),
       ('azure','canadacentral', 'phoenix,minneapolis,chicago,detroit,atlanta'),
       ('azure','canadaeast', 'new york,boston,washington dc,miami,charlotte'),
       ('azure','uksouth', 'dublin,belfast,london,liverpool,manchester,glasgow,birmingham,leeds'),
       ('azure','ukwest', 'dublin,belfast,london,liverpool,manchester,glasgow,birmingham,leeds'),
       ('azure','westcentralus', 'phoenix,minneapolis,chicago,detroit,atlanta'),
       ('azure','westus2', 'seattle,san francisco,los angeles,portland,las vegas'),
       ('azure','koreacentral', 'hong kong,beijing,shanghai,tokyo'),
       ('azure','koreasouth', 'hong kong,beijing,shanghai,tokyo'),
       ('azure','francecentral', 'madrid,barcelona,sintra,rome,milan,lyon,lisbon,toulouse,paris,cologne,seville,marseille,naples,turin,valencia,palermo'),
       ('azure','francesouth', 'madrid,barcelona,sintra,rome,milan,lyon,lisbon,toulouse,paris,cologne,seville,marseille,naples,turin,valencia,palermo'),

       ('gcp','us-east1', 'new york,boston,washington dc,miami,charlotte'),
       ('gcp','us-east4', 'new york,boston,washington dc,miami,charlotte'),
       ('gcp','us-central1', 'phoenix,minneapolis,chicago,detroit,atlanta'),
       ('gcp','us-west1', 'seattle,san francisco,los angeles,portland,las vegas'),
       ('gcp','northamerica-northeast1', 'new york,boston,washington dc,miami,charlotte'),
       ('gcp','europe-west1', 'stockholm,copenhagen,helsinki,oslo,riga,tallinn'),
       ('gcp','europe-west2', 'dublin,belfast,london,liverpool,manchester,glasgow,birmingham,leeds'),
       ('gcp','europe-west3', 'amsterdam,rotterdam,antwerp,hague,ghent,brussels,berlin,hamburg,munich,frankfurt,dusseldorf,leipzig,dortmund,essen,stuttgart'),
       ('gcp','europe-west4', 'madrid,barcelona,sintra,rome,milan,lyon,lisbon,toulouse,paris,cologne,seville,marseille,naples,turin,valencia,palermo'),
       ('gcp','europe-west6', 'krakov,zagraeb,zaragoza,lodz,athens,bratislava,prague,sofia,bucharest,vienna,warsaw,budapest'),
       ('gcp','asia-east1', 'hong kong,beijing,shanghai,tokyo'),
       ('gcp','asia-east2', 'hong kong,beijing,shanghai,tokyo'),
       ('gcp','asia-northeast1', 'hong kong,beijing,shanghai,tokyo'),
       ('gcp','asia-southeast1', 'singapore,jakarta,sydney,melbourne'),
       ('gcp','australia-southeast1', 'singapore,jakarta,sydney,melbourne'),
       ('gcp','asia-south1', 'singapore,jakarta,sydney,melbourne'),
       ('gcp','southamerica-east1', 'sao paulo,rio de janeiro,salvador,buenos aires'),
       ('gcp','gcp-europe-west1', 'stockholm,copenhagen,helsinki,oslo,riga,tallinn'),
       ('gcp','gcp-europe-west2', 'dublin,belfast,london,liverpool,manchester,glasgow,birmingham,leeds'),
       ('gcp','gcp-europe-west3', 'amsterdam,rotterdam,antwerp,hague,ghent,brussels,berlin,hamburg,munich,frankfurt,dusseldorf,leipzig,dortmund,essen,stuttgart'),
       ('gcp','gcp-europe-west4', 'madrid,barcelona,sintra,rome,milan,lyon,lisbon,toulouse,paris,cologne,seville,marseille,naples,turin,valencia,palermo'),
       ('gcp','gcp-europe-west6', 'krakov,zagraeb,zaragoza,lodz,athens,bratislava,prague,sofia,bucharest,vienna,warsaw,budapest'),
       ('gcp','gcp-europe-west8', 'krakov,zagraeb,zaragoza,lodz,athens,bratislava,prague,sofia,bucharest,vienna,warsaw,budapest'),
       ('gcp','gcp-us-west2', 'seattle,san francisco,los angeles,portland,las vegas'),
       ('gcp','gcp-australia-southeast1', 'singapore,jakarta,sydney,melbourne'),
       ('gcp','asia-southeast-1', 'singapore,jakarta,sydney,melbourne'),
       ('gcp','asia-southeast-2', 'singapore,jakarta,sydney,melbourne'),
       ('gcp','asia-southeast-3', 'singapore,jakarta,sydney,melbourne');
