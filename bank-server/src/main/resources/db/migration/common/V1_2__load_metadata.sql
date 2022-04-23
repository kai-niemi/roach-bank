--
-- Metadata used for bank transactions and client account selection
--

-- TRUNCATE TABLE region CASCADE;

INSERT into region
VALUES ('us-east-1', 'new york,boston,washington dc,miami'),
       ('us-east-2', 'new york,boston,washington dc,miami'),
       ('us-west-1', 'seattle,san francisco,los angeles'),
       ('us-west-2', 'seattle,san francisco,los angeles'),
       ('ca-central-1', 'phoenix,minneapolis,chicago,detroit,atlanta'),
       ('eu-central-1', 'stockholm,helsinki,oslo,london,frankfurt,amsterdam,milano,madrid,athens,barcelona,paris,manchester'),
       ('eu-west-1', 'stockholm,helsinki,oslo,london,frankfurt,amsterdam,milano,madrid,athens,barcelona,paris,manchester'),
       ('eu-west-2', 'stockholm,helsinki,oslo,london,frankfurt,amsterdam,milano,madrid,athens,barcelona,paris,manchester'),
       ('eu-west-3', 'stockholm,helsinki,oslo,london,frankfurt,amsterdam,milano,madrid,athens,barcelona,paris,manchester'),
       ('ap-northeast-1', 'singapore,hong kong,sydney,tokyo'),
       ('ap-northeast-2', 'singapore,hong kong,sydney,tokyo'),
       ('ap-northeast-3', 'singapore,hong kong,sydney,tokyo'),
       ('ap-southeast-1', 'singapore,hong kong,sydney,tokyo'),
       ('ap-southeast-2', 'singapore,hong kong,sydney,tokyo'),
       ('ap-south-1', 'singapore,hong kong,sydney,tokyo'),
       ('sa-east-1', 'sao paulo,rio de janeiro,salvador'),
       ('eastasia', 'singapore,hong kong,sydney,tokyo'),
       ('southeastasia', 'singapore,hong kong,sydney,tokyo'),
       ('centralus', 'phoenix,minneapolis,chicago,detroit,atlanta'),
       ('eastus', 'new york,boston,washington dc,miami'),
       ('eastus2', 'new york,boston,washington dc,miami'),
       ('westus', 'seattle,san francisco,los angeles'),
       ('northcentralus', 'phoenix,minneapolis,chicago,detroit,atlanta'),
       ('southcentralus', 'phoenix,minneapolis,chicago,detroit,atlanta'),
       ('northeurope', 'stockholm,helsinki,oslo,london,frankfurt,amsterdam,milano,madrid,athens,barcelona,paris,manchester'),
       ('westeurope', 'stockholm,helsinki,oslo,london,frankfurt,amsterdam,milano,madrid,athens,barcelona,paris,manchester'),
       ('japanwest', 'singapore,hong kong,sydney,tokyo'),
       ('japaneast', 'singapore,hong kong,sydney,tokyo'),
       ('brazilsouth', 'sao paulo,rio de janeiro,salvador'),
       ('australiaeast', 'singapore,hong kong,sydney,tokyo'),
       ('australiasoutheast', 'singapore,hong kong,sydney,tokyo'),
       ('southindia', 'singapore,hong kong,sydney,tokyo'),
       ('centralindia', 'singapore,hong kong,sydney,tokyo'),
       ('westindia', 'singapore,hong kong,sydney,tokyo'),
       ('canadacentral', 'phoenix,minneapolis,chicago,detroit,atlanta'),
       ('canadaeast', 'new york,boston,washington dc,miami'),
       ('uksouth', 'stockholm,helsinki,oslo,london,frankfurt,amsterdam,milano,madrid,athens,barcelona,paris,manchester'),
       ('ukwest', 'stockholm,helsinki,oslo,london,frankfurt,amsterdam,milano,madrid,athens,barcelona,paris,manchester'),
       ('westcentralus', 'phoenix,minneapolis,chicago,detroit,atlanta'),
       ('westus2', 'seattle,san francisco,los angeles'),
       ('koreacentral', 'singapore,hong kong,sydney,tokyo'),
       ('koreasouth', 'singapore,hong kong,sydney,tokyo'),
       ('francecentral', 'stockholm,helsinki,oslo,london,frankfurt,amsterdam,milano,madrid,athens,barcelona,paris,manchester'),
       ('francesouth', 'stockholm,helsinki,oslo,london,frankfurt,amsterdam,milano,madrid,athens,barcelona,paris,manchester'),
       ('us-east1', 'new york,boston,washington dc,miami'),
       ('us-east4', 'new york,boston,washington dc,miami'),
       ('us-central1', 'phoenix,minneapolis,chicago,detroit,atlanta'),
       ('us-west1', 'seattle,san francisco,los angeles'),
       ('northamerica-northeast1', 'new york,boston,washington dc,miami'),
       ('europe-west1', 'stockholm,helsinki,oslo,london,frankfurt,amsterdam,milano,madrid,athens,barcelona,paris,manchester'),
       ('europe-west2', 'stockholm,helsinki,oslo,london,frankfurt,amsterdam,milano,madrid,athens,barcelona,paris,manchester'),
       ('europe-west3', 'stockholm,helsinki,oslo,london,frankfurt,amsterdam,milano,madrid,athens,barcelona,paris,manchester'),
       ('europe-west4', 'stockholm,helsinki,oslo,london,frankfurt,amsterdam,milano,madrid,athens,barcelona,paris,manchester'),
       ('europe-west6', 'stockholm,helsinki,oslo,london,frankfurt,amsterdam,milano,madrid,athens,barcelona,paris,manchester'),
       ('asia-east1', 'singapore,hong kong,sydney,tokyo'),
       ('asia-east2', 'singapore,hong kong,sydney,tokyo'),
       ('asia-northeast1', 'singapore,hong kong,sydney,tokyo'),
       ('asia-southeast1', 'singapore,hong kong,sydney,tokyo'),
       ('australia-southeast1', 'singapore,hong kong,sydney,tokyo'),
       ('asia-south1', 'singapore,hong kong,sydney,tokyo'),
       ('southamerica-east1', 'sao paulo,rio de janeiro,salvador'),
       ('gcp-europe-west4', 'stockholm,helsinki,oslo,london,frankfurt,amsterdam,milano,madrid,athens,barcelona,paris,manchester'),
       ('gcp-us-west2', 'seattle,san francisco,los angeles'),
       ('gcp-australia-southeast1', 'singapore,hong kong,sydney,tokyo'),
       ('asia-southeast-1', 'singapore,hong kong,sydney,tokyo'),
       ('asia-southeast-2', 'singapore,hong kong,sydney,tokyo'),
       ('asia-southeast-3', 'singapore,hong kong,sydney,tokyo');

INSERT INTO city
VALUES ('seattle', 'USD'),
        ('san francisco', 'USD'),
        ('los angeles', 'USD'),
        ('phoenix', 'USD'),
        ('minneapolis', 'USD'),
        ('chicago', 'USD'),
        ('detroit', 'USD'),
        ('atlanta', 'USD'),
        ('new york', 'USD'),
        ('boston', 'USD'),
        ('washington dc', 'USD'),
        ('miami', 'USD'),
        ('stockholm', 'SEK'),
        ('helsinki', 'EUR'),
        ('oslo', 'NOK'),
        ('frankfurt', 'EUR'),
        ('london', 'GBP'),
        ('amsterdam', 'EUR'),
        ('paris', 'EUR'),
        ('manchester', 'GBP'),
        ('milano', 'EUR'),
        ('madrid', 'EUR'),
        ('athens', 'EUR'),
        ('barcelona', 'EUR'),
        ('singapore', 'SGD'),
        ('hong kong', 'HKD'),
        ('sydney', 'AUD'),
        ('tokyo', 'JPY'),
        ('sao paulo', 'BRL'),
        ('rio de janeiro', 'BRL'),
        ('salvador', 'BRL');
