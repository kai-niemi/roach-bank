--
-- Region to city mapping metadata
--

-- DELETE from region where 1 = 1;

-- Bank regions
INSERT into region
VALUES ('us-east-1', string_to_array('new york,boston,washington dc,miami,charlotte,atlanta', ',')),
       ('us-east-2', string_to_array('st louis,indianapolis,nashville,dallas,houston,detroit', ',')),
       ('us-west-1', string_to_array('san francisco,los angeles,san diego,las vegas,salt lake city', ',')),
       ('us-west-2', string_to_array('seattle,tacoma,portland,salem,bend,eugene', ',')),
       ('us-central-1', string_to_array('phoenix,minneapolis,chicago,detroit', ',')),

       ('eu-central-1',string_to_array('berlin,hamburg,munich,frankfurt,dusseldorf,leipzig,dortmund,essen,stuttgart', ',')),
       ('eu-central-2', string_to_array('zurich,krakov,zagraeb,zaragoza,lodz,athens,bratislava,prague,sofia,bucharest,vienna,warsaw,budapest',',')),
       ('eu-west-1', string_to_array('dublin,belfast,liverpool,manchester,glasgow,birmingham,leeds', ',')),
       ('eu-west-2', string_to_array('london,amsterdam,rotterdam,antwerp,hague,ghent,brussels', ',')),
       ('eu-west-3', string_to_array('lyon,lisbon,toulouse,paris,cologne,seville,marseille', ',')),
       ('eu-south-1', string_to_array('rome,milan,naples,turin,valencia,palermo', ',')),
       ('eu-south-2', string_to_array('madrid,barcelona,sintra,lisbon', ',')),
       ('eu-north-1', string_to_array('stockholm,copenhagen,helsinki,oslo,riga,tallinn', ',')),

       ('ap-northeast-1', string_to_array('hong kong,beijing,shanghai,tokyo', ',')),
       ('ap-southeast-2', string_to_array('singapore,jakarta,sydney,melbourne', ',')),

       ('ca-central-1', string_to_array('calgary,edmonton,winnipeg,regina,brandon,dryden', ',')),
       ('sa-east-1', string_to_array('sao paulo,rio de janeiro,salvador,buenos aires', ',')),
       ('af-south-1', string_to_array('cape town,durban,johannesburg,pretoria,grahamstown,kimberley', ','))
;

-- CRDB regions mapped to bank regions
insert into region_mapping (crdb_region, region)
values ('aws-eu-central-1', 'eu-central-1'),
       ('aws-eu-central-2', 'eu-central-2'),
       ('aws-eu-west-1', 'eu-west-1'),
       ('aws-eu-west-2', 'eu-west-2'),
       ('aws-eu-west-3', 'eu-west-3'),
       ('aws-eu-south-1', 'eu-south-1'),
       ('aws-eu-south-2', 'eu-south-2'),
       ('aws-eu-north-1', 'eu-north-1'),
-- https://cloud.google.com/compute/docs/regions-zones
       ('gcp-europe-central2', 'eu-central-2'),
       ('gcp-europe-west1', 'eu-west-1'),
       ('gcp-europe-west2', 'eu-west-2'),
       ('gcp-europe-west3', 'eu-central-1'),
       ('gcp-europe-west4', 'eu-west-3'),
       ('gcp-europe-west6', 'eu-central-1'),
       ('gcp-europe-west8', 'eu-south-1'),
       ('gcp-europe-west9', 'eu-south-1'),
       ('gcp-europe-west10', 'eu-central-1'),
       ('gcp-europe-west12', 'eu-south-2'),
       ('gcp-europe-southwest1', 'eu-south-2'),
       ('gcp-europe-southwest2', 'eu-south-1'),
       ('gcp-europe-north1', 'eu-north-1')
;
