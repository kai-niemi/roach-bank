SHOW ZONE CONFIGURATION FOR RANGE meta;
SHOW ZONE CONFIGURATION FOR RANGE default;
SHOW ZONE CONFIGURATION FOR PARTITION europe_west1 OF TABLE account;
SHOW RANGES FROM TABLE account;
show ranges from index account@primary;

SELECT * FROM [SHOW RANGES FROM TABLE account] WHERE "start_key" NOT LIKE '%Prefix%';
SELECT * FROM crdb_internal.ranges;

CREATE CHANGEFEED FOR TABLE account INTO 'kafka://localhost:9092' with updated, resolved='5s';
CREATE CHANGEFEED FOR TABLE account INTO 'experimental-http://localhost:8090/api/changefeed/account' with updated, resolved='5s';

select gen_random_uuid();

BEGIN;
INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES
('18955dc6-400d-4bb9-96c0-125bbe95e4ab', 'stockholm', '100.00', 'SEK', 'test', 'A', false, 0, clock_timestamp());
delete from account where id='18955dc6-400d-4bb9-96c0-125bbe95e4ab';
COMMIT;

CANCEL JOBS (SELECT job_id FROM [SHOW JOBS] where job_type='CHANGEFEED');

CREATE CHANGEFEED FOR TABLE outbox INTO 'kafka://localhost:9092' with updated, resolved='5s';

insert into outbox (id, aggregate_type, aggregate_id, event_type, payload)
values (gen_random_uuid(), 'transaction', gen_random_uuid()::string, 'TransactionCreatedEvent', '[
    {
        "abc": false
    },
    {
        "def": true
    }
]');
