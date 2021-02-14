SHOW ZONE CONFIGURATION FOR RANGE meta;
SHOW ZONE CONFIGURATION FOR RANGE default;
SHOW ZONE CONFIGURATION FOR PARTITION us_east OF TABLE account;
SHOW RANGES FROM TABLE account;
show ranges from index account@primary;

SELECT * FROM [SHOW RANGES FROM TABLE account] WHERE "start_key" NOT LIKE '%Prefix%';
SELECT * FROM crdb_internal.ranges;

CREATE CHANGEFEED FOR TABLE account INTO 'kafka://localhost:9092' with updated, resolved='5s';
CREATE CHANGEFEED FOR TABLE account INTO 'experimental-http://localhost:8090/api/changefeed/account' with updated, resolved='5s';
