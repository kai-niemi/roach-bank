-- SET CLUSTER SETTING kv.rangefeed.enabled = false;
-- SET CLUSTER SETTING kv.range_merge.queue_enabled = false;
SET CLUSTER SETTING sql.metrics.statement_details.plan_collection.period = '1m';
SET CLUSTER SETTING server.remote_debugging.mode = 'any';
SET CLUSTER SETTING server.time_until_store_dead = '2m';
SET CLUSTER SETTING server.consistency_check.interval = '1024h';
SET CLUSTER SETTING diagnostics.reporting.enabled = false;

-- SET CLUSTER SETTING kv.dist_sender.concurrency_limit = 2016;
-- SET CLUSTER SETTING kv.snapshot_rebalance.max_rate = '256 MiB';
-- SET CLUSTER SETTING kv.snapshot_recovery.max_rate = '256 MiB';