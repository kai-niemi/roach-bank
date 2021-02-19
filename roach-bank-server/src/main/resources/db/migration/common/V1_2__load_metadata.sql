--
-- Metadata used for bank transactions and client account selection
--

-- TRUNCATE TABLE region_group CASCADE;
-- TRUNCATE TABLE region_config CASCADE;

INSERT INTO region_group
VALUES ('us_west'),
       ('us_central'),
       ('us_east'),
       ('us'),
       ('eu_west'),
       ('eu_central'),
       ('eu_south'),
       ('eu'),
       ('apac')
;

INSERT INTO region_config
VALUES ('seattle', 'USD', 'us_west'),
       ('seattle', 'USD', 'us'),
       ('san francisco', 'USD', 'us_west'),
       ('san francisco', 'USD', 'us'),
       ('los angeles', 'USD', 'us_west'),
       ('los angeles', 'USD', 'us'),
       ('phoenix', 'USD', 'us_west'),
       ('phoenix', 'USD', 'us'),

       ('minneapolis', 'USD', 'us_central'),
       ('minneapolis', 'USD', 'us'),
       ('chicago', 'USD', 'us_central'),
       ('chicago', 'USD', 'us'),
       ('detroit', 'USD', 'us_central'),
       ('detroit', 'USD', 'us'),
       ('atlanta', 'USD', 'us_central'),
       ('atlanta', 'USD', 'us'),

       ('new york', 'USD', 'us_east'),
       ('new york', 'USD', 'us'),
       ('boston', 'USD', 'us_east'),
       ('boston', 'USD', 'us'),
       ('washington dc', 'USD', 'us_east'),
       ('washington dc', 'USD', 'us'),
       ('miami', 'USD', 'us_east'),
       ('miami', 'USD', 'us'),

       ('stockholm', 'SEK', 'eu_central'),
       ('stockholm', 'SEK', 'eu'),
       ('helsinki', 'EUR', 'eu_central'),
       ('helsinki', 'EUR', 'eu'),
       ('oslo', 'NOK', 'eu_central'),
       ('oslo', 'NOK', 'eu'),
       ('frankfurt', 'EUR', 'eu_central'),
       ('frankfurt', 'EUR', 'eu'),

       ('london', 'GBP', 'eu_west'),
       ('london', 'GBP', 'eu'),
       ('amsterdam', 'EUR', 'eu_west'),
       ('amsterdam', 'EUR', 'eu'),
       ('paris', 'EUR', 'eu_west'),
       ('paris', 'EUR', 'eu'),
       ('manchester', 'GBP', 'eu_west'),
       ('manchester', 'GBP', 'eu'),

       ('milano', 'EUR', 'eu_south'),
       ('milano', 'EUR', 'eu'),
       ('madrid', 'EUR', 'eu_south'),
       ('madrid', 'EUR', 'eu'),
       ('athens', 'EUR', 'eu_south'),
       ('athens', 'EUR', 'eu'),
       ('barcelona', 'EUR', 'eu_south'),
       ('barcelona', 'EUR', 'eu'),

       ('singapore', 'SGD', 'apac'),
       ('hong kong', 'HKD', 'apac'),
       ('sydney', 'AUD', 'apac'),
       ('tokyo', 'JPY', 'apac')
;
