--
-- Metadata used for bank transactions and client account selection
--

INSERT INTO transaction_type (id, name)
VALUES ('GEN', 'Generic'),
       ('DPO', 'Deposit'),
       ('PYO', 'Payout')
;

INSERT INTO region_group
VALUES ('us_west'),
       ('us_central'),
       ('us_east'),
       ('eu_west'),
       ('eu_central'),
       ('eu_south'),
       ('apac')
;

INSERT INTO region_config
VALUES ('seattle', 'USD', 'us_west'),
       ('san francisco', 'USD', 'us_west'),
       ('los angeles', 'USD', 'us_west'),
       ('phoenix', 'USD', 'us_west'),
       ('minneapolis', 'USD', 'us_central'),
       ('chicago', 'USD', 'us_central'),
       ('detroit', 'USD', 'us_central'),
       ('atlanta', 'USD', 'us_central'),
       ('new york', 'USD', 'us_east'),
       ('boston', 'USD', 'us_east'),
       ('washington dc', 'USD', 'us_east'),
       ('miami', 'USD', 'us_east'),

       ('stockholm', 'SEK', 'eu_central'),
       ('helsinki', 'EUR', 'eu_central'),
       ('oslo', 'NOK', 'eu_central'),
       ('frankfurt', 'EUR', 'eu_central'),
       ('london', 'GBP', 'eu_west'),
       ('amsterdam', 'EUR', 'eu_west'),
       ('paris', 'EUR', 'eu_west'),
       ('milano', 'EUR', 'eu_south'),
       ('madrid', 'EUR', 'eu_south'),
       ('athens', 'EUR', 'eu_south'),

       ('singapore', 'SGD', 'apac'),
       ('hong kong', 'HKD', 'apac'),
       ('sydney', 'AUD', 'apac')
;
