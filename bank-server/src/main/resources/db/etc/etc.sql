SHOW ZONE CONFIGURATION FOR RANGE meta;
SHOW ZONE CONFIGURATION FOR RANGE default;
SHOW ZONE CONFIGURATION FOR PARTITION europe_west1 OF TABLE account;

SHOW RANGES FROM TABLE account;
SHOW RANGES from index account@primary;

SELECT region FROM [SHOW regions];
SELECT * FROM [SHOW RANGES FROM TABLE account] WHERE "start_key" NOT LIKE '%Prefix%';
SELECT * FROM crdb_internal.ranges;

EXPLAIN ANALYZE SELECT * FROM account WHERE city='stockholm' and id='02fde064-10b1-4568-be1c-9012f97cd448';

SHOW RANGE FROM TABLE account FOR ROW ('europe-west1','02fde064-10b1-4568-be1c-9012f97cd448');

SELECT * FROM system.replication_constraint_stats WHERE violating_ranges > 0;