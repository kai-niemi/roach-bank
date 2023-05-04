--
-- Region to city mapping metadata
--
-- select array_cat_agg(city_names) from city_group where name in ('us-west-1','us-west-2');
DELETE  from city_group where 1=1;
DELETE  from region where 1=1;

INSERT into city_group
VALUES ('us-east-1', string_to_array('new york,boston,washington dc,miami,charlotte,atlanta', ',')),
       ('us-east-2', string_to_array('chicago,st louis,indianapolis,nashville,dallas,houston,detroit',',')),
       ('us-west-1', string_to_array('san francisco,los angeles,san diego,portland,las vegas,salt lake city',',')),
       ('us-west-2', string_to_array('seattle,tacoma,portland,salem,bend,eugene',',')),
       ('us-central-1', string_to_array('phoenix,minneapolis,chicago,detroit,atlanta',',')),

       ('ca-central-1', string_to_array('calgary,edmonton,winnipeg,regina,brandon,dryden',',')),

       ('eu-north-1', string_to_array('stockholm,copenhagen,helsinki,oslo,riga,tallinn',',')),
       ('eu-west-1', string_to_array('dublin,belfast,london,liverpool,manchester,glasgow,birmingham,leeds',',')),
       ('eu-west-2', string_to_array('london,birmingham,leeds,amsterdam,rotterdam,antwerp,hague,ghent,brussels',',')),
       ('eu-west-3', string_to_array('lyon,lisbon,toulouse,paris,cologne,seville,marseille',',')),
       ('eu-south-1', string_to_array('rome,milan,naples,turin,valencia,palermo',',')),
       ('eu-south-2', string_to_array('madrid,barcelona,sintra,lisbon',',')),
       ('eu-central-1', string_to_array('berlin,hamburg,munich,frankfurt,dusseldorf,leipzig,dortmund,essen,stuttgart',',')),
       ('eu-central-2', string_to_array('zurich,krakov,zagraeb,zaragoza,lodz,athens,bratislava,prague,sofia,bucharest,vienna,warsaw,budapest',',')),

       ('ap-northeast-1', string_to_array('hong kong,beijing,shanghai,tokyo',',')),
       ('ap-southeast-2', string_to_array('singapore,jakarta,sydney,melbourne',',')),

       ('sa-east-1', string_to_array('sao paulo,rio de janeiro,salvador,buenos aires',',')),

       ('af-south-1', string_to_array('cape town,durban,johannesburg,pretoria,grahamstown,kimberley',','))
;

INSERT into region
VALUES
       ('default', string_to_array('eu-north-1,eu-west1,eu-west-2,eu-west-3,eu-south-1,eu-south-2,eu-central-1,eu-central-2',',')), -- Default region if --locality flag doesnt match
       ('aws-us-east-1', string_to_array('us-east-1',',')),
       ('aws-us-east-2', string_to_array('us-east-2',',')),
       ('aws-us-west-1', string_to_array('us-west-1',',')),
       ('aws-us-west-2', string_to_array('us-west-2',',')),
       ('aws-ca-central-1', string_to_array('ca-central-1',',')),
       ('aws-eu-north-1', string_to_array('eu-north-1',',')),
       ('aws-eu-west-1', string_to_array('eu-west-1',',')),
       ('aws-eu-west-2', string_to_array('eu-west-2',',')),
       ('aws-eu-west-3', string_to_array('eu-south-1',',')),
       ('aws-eu-south-1', string_to_array('eu-south-1',',')),
       ('aws-eu-south-2', string_to_array('eu-south-1',',')),
       ('aws-eu-central-1', string_to_array('eu-central-1',',')),
       ('aws-eu-central-2', string_to_array('eu-central-2',',')),
       ('aws-ap-northeast-1', string_to_array('ap-northeast-1',',')),
       ('aws-ap-northeast-2', string_to_array('ap-northeast-1',',')),
       ('aws-ap-northeast-3', string_to_array('ap-northeast-1',',')),
       ('aws-ap-southeast-1', string_to_array('ap-southeast-2',',')),
       ('aws-ap-southeast-2', string_to_array('ap-southeast-2',',')),
       ('aws-ap-east-1', string_to_array('ap-northeast-1',',')),
       ('aws-ap-south-1', string_to_array('ap-southeast-2',',')),
       ('aws-ap-south-2', string_to_array('ap-southeast-2',',')),
       ('aws-sa-east-1', string_to_array('sa-east-1',',')),
       ('aws-af-south-1', string_to_array('af-south-1',',')),

       ('azure-eastasia', string_to_array('ap-northeast-1',',')),
       ('azure-southeastasia', string_to_array('ap-southeast-2',',')),
       ('azure-centralus', string_to_array('us-central-1',',')),
       ('azure-eastus', string_to_array('us-east-1',',')),
       ('azure-eastus2', string_to_array('us-east-1',',')),
       ('azure-westus', string_to_array('us-west-2',',')),
       ('azure-northcentralus', string_to_array('us-central-1',',')),
       ('azure-southcentralus', string_to_array('us-central-1',',')),
       ('azure-northeurope', string_to_array('eu-north-1',',')),
       ('azure-westeurope', string_to_array('eu-west-1',',')),
       ('azure-japanwest', string_to_array('ap-northeast-1',',')),
       ('azure-japaneast', string_to_array('ap-northeast-1',',')),
       ('azure-brazilsouth', string_to_array('sa-east-1',',')),
       ('azure-australiaeast', string_to_array('ap-southeast-2',',')),
       ('azure-australiasoutheast', string_to_array('ap-southeast-2',',')),
       ('azure-southindia', string_to_array('ap-southeast-2',',')),
       ('azure-centralindia', string_to_array('ap-southeast-2',',')),
       ('azure-westindia', string_to_array('ap-southeast-2',',')),
       ('azure-canadacentral', string_to_array('us-central-1',',')),
       ('azure-canadaeast', string_to_array('us-east-1',',')),
       ('azure-uksouth', string_to_array('eu-west-1',',')),
       ('azure-ukwest', string_to_array('eu-west-2',',')),
       ('azure-westcentralus', string_to_array('us-central-1',',')),
       ('azure-westus2', string_to_array('us-west-2',',')),
       ('azure-koreacentral', string_to_array('ap-northeast-1',',')),
       ('azure-koreasouth', string_to_array('ap-northeast-1',',')),
       ('azure-francecentral', string_to_array('eu-south-1',',')),
       ('azure-francesouth', string_to_array('eu-south-1',',')),

       ('gcp-us-east1', string_to_array('us-east-1',',')),
       ('gcp-us-east4', string_to_array('us-east-1',',')),
       ('gcp-us-central1', string_to_array('us-central-1',',')),
       ('gcp-us-west1', string_to_array('us-west-2',',')),
       ('gcp-northamerica-northeast1', string_to_array('us-east-1',',')),
       ('gcp-europe-west1', string_to_array('eu-north-1',',')),
       ('gcp-europe-west2', string_to_array('eu-west-1',',')),
       ('gcp-europe-west3', string_to_array('eu-central-1',',')),
       ('gcp-europe-west4', string_to_array('eu-south-1',',')),
       ('gcp-europe-west6', string_to_array('eu-central-2',',')),
       ('gcp-europe-west8', string_to_array('eu-central-2',',')),
       ('gcp-asia-east1', string_to_array('ap-northeast-1',',')),
       ('gcp-asia-east2', string_to_array('ap-northeast-1',',')),
       ('gcp-asia-northeast1', string_to_array('ap-northeast-1',',')),
       ('gcp-asia-southeast1', string_to_array('ap-southeast-2',',')),
       ('gcp-asia-southeast2', string_to_array('ap-southeast-2',',')),
       ('gcp-asia-southeast3', string_to_array('ap-southeast-2',',')),
       ('gcp-australia-southeast1', string_to_array('ap-southeast-2',',')),
       ('gcp-asia-south1', string_to_array('ap-southeast-2',',')),
       ('gcp-southamerica-east1', string_to_array('sa-east-1',',')),
       ('gcp-us-west2', string_to_array('us-west-2',','))
;
