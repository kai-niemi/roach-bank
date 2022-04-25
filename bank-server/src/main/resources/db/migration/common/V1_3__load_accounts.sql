-- balance per account: 500000.00
-- accounts per region: 10
-- accounts total: 340
-- cities: [boston, stockholm, london, salvador, miami, oslo, paris, barcelona, portland, detroit, seattle, los angeles, chicago, minneapolis, san francisco, amsterdam, helsinki, hong kong, milano, athens, frankfurt, new york, rio de janeiro, las vegas, manchester, charlotte, tokyo, washington dc, singapore, sydney, phoenix, madrid, atlanta, sao paulo]

-- boston | USD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES
    (gen_random_uuid(), 'boston', '500000.00', 'USD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'boston', '500000.00', 'USD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'boston', '500000.00', 'USD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'boston', '500000.00', 'USD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'boston', '500000.00', 'USD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'boston', '500000.00', 'USD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'boston', '500000.00', 'USD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'boston', '500000.00', 'USD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'boston', '500000.00', 'USD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'boston', '500000.00', 'USD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- stockholm | SEK
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'stockholm', '500000.00', 'SEK', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'stockholm', '500000.00', 'SEK', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'stockholm', '500000.00', 'SEK', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'stockholm', '500000.00', 'SEK', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'stockholm', '500000.00', 'SEK', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'stockholm', '500000.00', 'SEK', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'stockholm', '500000.00', 'SEK', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'stockholm', '500000.00', 'SEK', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'stockholm', '500000.00', 'SEK', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'stockholm', '500000.00', 'SEK', 'user:0010', 'A', false, 0, clock_timestamp())
;-- london | GBP
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'london', '500000.00', 'GBP', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'london', '500000.00', 'GBP', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'london', '500000.00', 'GBP', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'london', '500000.00', 'GBP', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'london', '500000.00', 'GBP', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'london', '500000.00', 'GBP', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'london', '500000.00', 'GBP', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'london', '500000.00', 'GBP', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'london', '500000.00', 'GBP', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'london', '500000.00', 'GBP', 'user:0010', 'A', false, 0, clock_timestamp())
;-- salvador | BRL
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'salvador', '500000.00', 'BRL', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'salvador', '500000.00', 'BRL', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'salvador', '500000.00', 'BRL', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'salvador', '500000.00', 'BRL', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'salvador', '500000.00', 'BRL', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'salvador', '500000.00', 'BRL', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'salvador', '500000.00', 'BRL', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'salvador', '500000.00', 'BRL', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'salvador', '500000.00', 'BRL', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'salvador', '500000.00', 'BRL', 'user:0010', 'A', false, 0, clock_timestamp())
;-- miami | USD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'miami', '500000.00', 'USD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'miami', '500000.00', 'USD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'miami', '500000.00', 'USD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'miami', '500000.00', 'USD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'miami', '500000.00', 'USD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'miami', '500000.00', 'USD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'miami', '500000.00', 'USD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'miami', '500000.00', 'USD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'miami', '500000.00', 'USD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'miami', '500000.00', 'USD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- oslo | NOK
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'oslo', '500000.00', 'NOK', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'oslo', '500000.00', 'NOK', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'oslo', '500000.00', 'NOK', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'oslo', '500000.00', 'NOK', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'oslo', '500000.00', 'NOK', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'oslo', '500000.00', 'NOK', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'oslo', '500000.00', 'NOK', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'oslo', '500000.00', 'NOK', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'oslo', '500000.00', 'NOK', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'oslo', '500000.00', 'NOK', 'user:0010', 'A', false, 0, clock_timestamp())
;-- paris | EUR
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'paris', '500000.00', 'EUR', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'paris', '500000.00', 'EUR', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'paris', '500000.00', 'EUR', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'paris', '500000.00', 'EUR', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'paris', '500000.00', 'EUR', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'paris', '500000.00', 'EUR', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'paris', '500000.00', 'EUR', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'paris', '500000.00', 'EUR', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'paris', '500000.00', 'EUR', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'paris', '500000.00', 'EUR', 'user:0010', 'A', false, 0, clock_timestamp())
;-- barcelona | EUR
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'barcelona', '500000.00', 'EUR', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'barcelona', '500000.00', 'EUR', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'barcelona', '500000.00', 'EUR', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'barcelona', '500000.00', 'EUR', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'barcelona', '500000.00', 'EUR', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'barcelona', '500000.00', 'EUR', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'barcelona', '500000.00', 'EUR', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'barcelona', '500000.00', 'EUR', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'barcelona', '500000.00', 'EUR', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'barcelona', '500000.00', 'EUR', 'user:0010', 'A', false, 0, clock_timestamp())
;-- portland | USD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'portland', '500000.00', 'USD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'portland', '500000.00', 'USD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'portland', '500000.00', 'USD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'portland', '500000.00', 'USD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'portland', '500000.00', 'USD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'portland', '500000.00', 'USD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'portland', '500000.00', 'USD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'portland', '500000.00', 'USD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'portland', '500000.00', 'USD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'portland', '500000.00', 'USD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- detroit | USD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'detroit', '500000.00', 'USD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'detroit', '500000.00', 'USD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'detroit', '500000.00', 'USD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'detroit', '500000.00', 'USD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'detroit', '500000.00', 'USD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'detroit', '500000.00', 'USD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'detroit', '500000.00', 'USD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'detroit', '500000.00', 'USD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'detroit', '500000.00', 'USD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'detroit', '500000.00', 'USD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- seattle | USD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'seattle', '500000.00', 'USD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'seattle', '500000.00', 'USD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'seattle', '500000.00', 'USD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'seattle', '500000.00', 'USD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'seattle', '500000.00', 'USD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'seattle', '500000.00', 'USD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'seattle', '500000.00', 'USD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'seattle', '500000.00', 'USD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'seattle', '500000.00', 'USD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'seattle', '500000.00', 'USD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- los angeles | USD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'los angeles', '500000.00', 'USD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'los angeles', '500000.00', 'USD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'los angeles', '500000.00', 'USD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'los angeles', '500000.00', 'USD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'los angeles', '500000.00', 'USD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'los angeles', '500000.00', 'USD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'los angeles', '500000.00', 'USD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'los angeles', '500000.00', 'USD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'los angeles', '500000.00', 'USD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'los angeles', '500000.00', 'USD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- chicago | USD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'chicago', '500000.00', 'USD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'chicago', '500000.00', 'USD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'chicago', '500000.00', 'USD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'chicago', '500000.00', 'USD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'chicago', '500000.00', 'USD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'chicago', '500000.00', 'USD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'chicago', '500000.00', 'USD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'chicago', '500000.00', 'USD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'chicago', '500000.00', 'USD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'chicago', '500000.00', 'USD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- minneapolis | USD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'minneapolis', '500000.00', 'USD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'minneapolis', '500000.00', 'USD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'minneapolis', '500000.00', 'USD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'minneapolis', '500000.00', 'USD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'minneapolis', '500000.00', 'USD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'minneapolis', '500000.00', 'USD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'minneapolis', '500000.00', 'USD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'minneapolis', '500000.00', 'USD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'minneapolis', '500000.00', 'USD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'minneapolis', '500000.00', 'USD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- san francisco | USD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'san francisco', '500000.00', 'USD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'san francisco', '500000.00', 'USD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'san francisco', '500000.00', 'USD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'san francisco', '500000.00', 'USD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'san francisco', '500000.00', 'USD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'san francisco', '500000.00', 'USD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'san francisco', '500000.00', 'USD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'san francisco', '500000.00', 'USD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'san francisco', '500000.00', 'USD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'san francisco', '500000.00', 'USD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- amsterdam | EUR
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'amsterdam', '500000.00', 'EUR', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'amsterdam', '500000.00', 'EUR', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'amsterdam', '500000.00', 'EUR', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'amsterdam', '500000.00', 'EUR', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'amsterdam', '500000.00', 'EUR', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'amsterdam', '500000.00', 'EUR', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'amsterdam', '500000.00', 'EUR', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'amsterdam', '500000.00', 'EUR', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'amsterdam', '500000.00', 'EUR', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'amsterdam', '500000.00', 'EUR', 'user:0010', 'A', false, 0, clock_timestamp())
;-- helsinki | EUR
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'helsinki', '500000.00', 'EUR', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'helsinki', '500000.00', 'EUR', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'helsinki', '500000.00', 'EUR', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'helsinki', '500000.00', 'EUR', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'helsinki', '500000.00', 'EUR', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'helsinki', '500000.00', 'EUR', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'helsinki', '500000.00', 'EUR', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'helsinki', '500000.00', 'EUR', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'helsinki', '500000.00', 'EUR', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'helsinki', '500000.00', 'EUR', 'user:0010', 'A', false, 0, clock_timestamp())
;-- hong kong | HKD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'hong kong', '500000.00', 'HKD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'hong kong', '500000.00', 'HKD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'hong kong', '500000.00', 'HKD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'hong kong', '500000.00', 'HKD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'hong kong', '500000.00', 'HKD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'hong kong', '500000.00', 'HKD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'hong kong', '500000.00', 'HKD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'hong kong', '500000.00', 'HKD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'hong kong', '500000.00', 'HKD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'hong kong', '500000.00', 'HKD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- milano | EUR
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'milano', '500000.00', 'EUR', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'milano', '500000.00', 'EUR', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'milano', '500000.00', 'EUR', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'milano', '500000.00', 'EUR', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'milano', '500000.00', 'EUR', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'milano', '500000.00', 'EUR', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'milano', '500000.00', 'EUR', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'milano', '500000.00', 'EUR', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'milano', '500000.00', 'EUR', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'milano', '500000.00', 'EUR', 'user:0010', 'A', false, 0, clock_timestamp())
;-- athens | EUR
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'athens', '500000.00', 'EUR', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'athens', '500000.00', 'EUR', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'athens', '500000.00', 'EUR', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'athens', '500000.00', 'EUR', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'athens', '500000.00', 'EUR', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'athens', '500000.00', 'EUR', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'athens', '500000.00', 'EUR', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'athens', '500000.00', 'EUR', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'athens', '500000.00', 'EUR', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'athens', '500000.00', 'EUR', 'user:0010', 'A', false, 0, clock_timestamp())
;-- frankfurt | EUR
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'frankfurt', '500000.00', 'EUR', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'frankfurt', '500000.00', 'EUR', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'frankfurt', '500000.00', 'EUR', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'frankfurt', '500000.00', 'EUR', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'frankfurt', '500000.00', 'EUR', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'frankfurt', '500000.00', 'EUR', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'frankfurt', '500000.00', 'EUR', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'frankfurt', '500000.00', 'EUR', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'frankfurt', '500000.00', 'EUR', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'frankfurt', '500000.00', 'EUR', 'user:0010', 'A', false, 0, clock_timestamp())
;-- new york | USD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'new york', '500000.00', 'USD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'new york', '500000.00', 'USD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'new york', '500000.00', 'USD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'new york', '500000.00', 'USD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'new york', '500000.00', 'USD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'new york', '500000.00', 'USD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'new york', '500000.00', 'USD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'new york', '500000.00', 'USD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'new york', '500000.00', 'USD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'new york', '500000.00', 'USD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- rio de janeiro | BRL
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'rio de janeiro', '500000.00', 'BRL', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'rio de janeiro', '500000.00', 'BRL', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'rio de janeiro', '500000.00', 'BRL', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'rio de janeiro', '500000.00', 'BRL', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'rio de janeiro', '500000.00', 'BRL', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'rio de janeiro', '500000.00', 'BRL', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'rio de janeiro', '500000.00', 'BRL', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'rio de janeiro', '500000.00', 'BRL', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'rio de janeiro', '500000.00', 'BRL', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'rio de janeiro', '500000.00', 'BRL', 'user:0010', 'A', false, 0, clock_timestamp())
;-- las vegas | USD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'las vegas', '500000.00', 'USD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'las vegas', '500000.00', 'USD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'las vegas', '500000.00', 'USD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'las vegas', '500000.00', 'USD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'las vegas', '500000.00', 'USD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'las vegas', '500000.00', 'USD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'las vegas', '500000.00', 'USD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'las vegas', '500000.00', 'USD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'las vegas', '500000.00', 'USD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'las vegas', '500000.00', 'USD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- manchester | GBP
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'manchester', '500000.00', 'GBP', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'manchester', '500000.00', 'GBP', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'manchester', '500000.00', 'GBP', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'manchester', '500000.00', 'GBP', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'manchester', '500000.00', 'GBP', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'manchester', '500000.00', 'GBP', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'manchester', '500000.00', 'GBP', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'manchester', '500000.00', 'GBP', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'manchester', '500000.00', 'GBP', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'manchester', '500000.00', 'GBP', 'user:0010', 'A', false, 0, clock_timestamp())
;-- charlotte | USD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'charlotte', '500000.00', 'USD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'charlotte', '500000.00', 'USD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'charlotte', '500000.00', 'USD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'charlotte', '500000.00', 'USD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'charlotte', '500000.00', 'USD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'charlotte', '500000.00', 'USD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'charlotte', '500000.00', 'USD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'charlotte', '500000.00', 'USD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'charlotte', '500000.00', 'USD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'charlotte', '500000.00', 'USD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- tokyo | JPY
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'tokyo', '500000', 'JPY', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'tokyo', '500000', 'JPY', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'tokyo', '500000', 'JPY', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'tokyo', '500000', 'JPY', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'tokyo', '500000', 'JPY', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'tokyo', '500000', 'JPY', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'tokyo', '500000', 'JPY', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'tokyo', '500000', 'JPY', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'tokyo', '500000', 'JPY', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'tokyo', '500000', 'JPY', 'user:0010', 'A', false, 0, clock_timestamp())
;-- washington dc | USD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'washington dc', '500000.00', 'USD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'washington dc', '500000.00', 'USD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'washington dc', '500000.00', 'USD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'washington dc', '500000.00', 'USD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'washington dc', '500000.00', 'USD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'washington dc', '500000.00', 'USD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'washington dc', '500000.00', 'USD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'washington dc', '500000.00', 'USD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'washington dc', '500000.00', 'USD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'washington dc', '500000.00', 'USD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- singapore | SGD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'singapore', '500000.00', 'SGD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'singapore', '500000.00', 'SGD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'singapore', '500000.00', 'SGD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'singapore', '500000.00', 'SGD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'singapore', '500000.00', 'SGD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'singapore', '500000.00', 'SGD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'singapore', '500000.00', 'SGD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'singapore', '500000.00', 'SGD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'singapore', '500000.00', 'SGD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'singapore', '500000.00', 'SGD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- sydney | AUD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'sydney', '500000.00', 'AUD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sydney', '500000.00', 'AUD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sydney', '500000.00', 'AUD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sydney', '500000.00', 'AUD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sydney', '500000.00', 'AUD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sydney', '500000.00', 'AUD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sydney', '500000.00', 'AUD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sydney', '500000.00', 'AUD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sydney', '500000.00', 'AUD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sydney', '500000.00', 'AUD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- phoenix | USD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'phoenix', '500000.00', 'USD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'phoenix', '500000.00', 'USD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'phoenix', '500000.00', 'USD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'phoenix', '500000.00', 'USD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'phoenix', '500000.00', 'USD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'phoenix', '500000.00', 'USD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'phoenix', '500000.00', 'USD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'phoenix', '500000.00', 'USD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'phoenix', '500000.00', 'USD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'phoenix', '500000.00', 'USD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- madrid | EUR
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'madrid', '500000.00', 'EUR', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'madrid', '500000.00', 'EUR', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'madrid', '500000.00', 'EUR', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'madrid', '500000.00', 'EUR', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'madrid', '500000.00', 'EUR', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'madrid', '500000.00', 'EUR', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'madrid', '500000.00', 'EUR', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'madrid', '500000.00', 'EUR', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'madrid', '500000.00', 'EUR', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'madrid', '500000.00', 'EUR', 'user:0010', 'A', false, 0, clock_timestamp())
;-- atlanta | USD
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'atlanta', '500000.00', 'USD', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'atlanta', '500000.00', 'USD', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'atlanta', '500000.00', 'USD', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'atlanta', '500000.00', 'USD', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'atlanta', '500000.00', 'USD', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'atlanta', '500000.00', 'USD', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'atlanta', '500000.00', 'USD', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'atlanta', '500000.00', 'USD', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'atlanta', '500000.00', 'USD', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'atlanta', '500000.00', 'USD', 'user:0010', 'A', false, 0, clock_timestamp())
;-- sao paulo | BRL
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES	(gen_random_uuid(), 'sao paulo', '500000.00', 'BRL', 'user:0001', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sao paulo', '500000.00', 'BRL', 'user:0002', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sao paulo', '500000.00', 'BRL', 'user:0003', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sao paulo', '500000.00', 'BRL', 'user:0004', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sao paulo', '500000.00', 'BRL', 'user:0005', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sao paulo', '500000.00', 'BRL', 'user:0006', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sao paulo', '500000.00', 'BRL', 'user:0007', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sao paulo', '500000.00', 'BRL', 'user:0008', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sao paulo', '500000.00', 'BRL', 'user:0009', 'A', false, 0, clock_timestamp()),
	(gen_random_uuid(), 'sao paulo', '500000.00', 'BRL', 'user:0010', 'A', false, 0, clock_timestamp())
;
