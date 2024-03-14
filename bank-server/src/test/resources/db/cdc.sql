SET CLUSTER SETTING kv.rangefeed.enabled = true;
SET CLUSTER SETTING kv.range_merge.queue_enabled = false;

INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated_at) VALUES
    (gen_random_uuid(), 'stockholm', '100.00', 'SEK', 'test', 'A', false, 0, clock_timestamp());
-- delete from account where id='18955dc6-400d-4bb9-96c0-125bbe95e4ab';

CANCEL JOBS (SELECT job_id FROM [SHOW JOBS] where job_type='CHANGEFEED');

INSERT INTO outbox (id, aggregate_type, aggregate_id, event_type, payload)
VALUES (gen_random_uuid(), 'transaction', gen_random_uuid()::string, 'TransactionCreatedEvent', '[
    {
        "abc": false
    },
    {
        "def": true
    }
]');

ALTER TABLE outbox SET (ttl_expire_after = '1 hour');

SELECT * FROM [SHOW JOBS] WHERE job_type = 'ROW LEVEL TTL';