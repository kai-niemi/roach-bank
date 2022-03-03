
ALTER TABLE account PARTITION BY LIST (region) (
    PARTITION europe_west1 VALUES IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester'),
    PARTITION europe_west2 VALUES IN ('seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami'),
    PARTITION europe_west3 VALUES IN ('singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

ALTER TABLE transaction PARTITION BY LIST (region) (
    PARTITION europe_west1 VALUES IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester'),
    PARTITION europe_west2 VALUES IN ('seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami'),
    PARTITION europe_west3 VALUES IN ('singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

ALTER TABLE transaction_item PARTITION BY LIST (transaction_region) (
    PARTITION europe_west1 VALUES IN ('stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester'),
    PARTITION europe_west2 VALUES IN ('seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami'),
    PARTITION europe_west3 VALUES IN ('singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador'),
    PARTITION DEFAULT VALUES IN (DEFAULT)
    );

-- ALTER RANGE meta CONFIGURE ZONE USING num_replicas = 7;
-- ALTER RANGE liveness CONFIGURE ZONE USING num_replicas = 7;
-- ALTER RANGE system CONFIGURE ZONE USING num_replicas = 7;
-- ALTER DATABASE system CONFIGURE ZONE USING num_replicas = 7;


-- Pin partitions to regions
ALTER PARTITION europe_west1 OF TABLE account CONFIGURE ZONE USING num_replicas=3, constraints='[+region=europe-west1]';
ALTER PARTITION europe_west2 OF TABLE account CONFIGURE ZONE USING num_replicas=3, constraints='[+region=europe-west2]';
ALTER PARTITION europe_west3 OF TABLE account CONFIGURE ZONE USING num_replicas=3, constraints='[+region=europe-west3]';

ALTER PARTITION europe_west1 OF TABLE transaction CONFIGURE ZONE USING num_replicas=3, constraints='[+region=europe-west1]';
ALTER PARTITION europe_west2 OF TABLE transaction CONFIGURE ZONE USING num_replicas=3, constraints='[+region=europe-west2]';
ALTER PARTITION europe_west3 OF TABLE transaction CONFIGURE ZONE USING num_replicas=3, constraints='[+region=europe-west3]';

ALTER PARTITION europe_west1 OF TABLE transaction_item CONFIGURE ZONE USING num_replicas=3, constraints='[+region=europe-west1]';
ALTER PARTITION europe_west2 OF TABLE transaction_item CONFIGURE ZONE USING num_replicas=3, constraints='[+region=europe-west2]';
ALTER PARTITION europe_west3 OF TABLE transaction_item CONFIGURE ZONE USING num_replicas=3, constraints='[+region=europe-west3]';

