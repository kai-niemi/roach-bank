SET CLUSTER SETTING kv.rangefeed.enabled = true;
SET CLUSTER SETTING kv.range_merge.queue_enabled = false;

CREATE CHANGEFEED FOR TABLE account INTO 'kafka://localhost:9092' with updated, resolved='5s';
CREATE CHANGEFEED FOR TABLE account INTO 'experimental-http://localhost:8090/api/cdc/cloud/account' with updated, resolved='5s';
CREATE CHANGEFEED FOR TABLE account INTO 'webhook-https://localhost:8443/api/cdc/webhook/account?insecure_tls_skip_verify=true' with updated, resolved='5s';

CREATE CHANGEFEED FOR TABLE account INTO 'webhook-https://192.168.1.113:8443/api/cdc/webhook/account?insecure_tls_skip_verify=true' with updated, resolved='5s';

INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES
    (gen_random_uuid(), 'stockholm', '100.00', 'SEK', 'test', 'A', false, 0, clock_timestamp());
-- delete from account where id='18955dc6-400d-4bb9-96c0-125bbe95e4ab';

CANCEL JOBS (SELECT job_id FROM [SHOW JOBS] where job_type='CHANGEFEED');

CREATE CHANGEFEED FOR TABLE outbox INTO 'kafka://localhost:9092' with updated, resolved='5s';

INSERT INTO outbox (id, aggregate_type, aggregate_id, event_type, payload)
VALUES (gen_random_uuid(), 'transaction', gen_random_uuid()::string, 'TransactionCreatedEvent', '[
    {
        "abc": false
    },
    {
        "def": true
    }
]');
