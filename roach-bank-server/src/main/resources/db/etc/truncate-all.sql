--
-- Not executed by liquibase
--

TRUNCATE TABLE transaction_item CASCADE;
TRUNCATE TABLE transaction CASCADE;
TRUNCATE TABLE account CASCADE;

TRUNCATE TABLE transaction_type CASCADE;
TRUNCATE TABLE region_group CASCADE;
TRUNCATE TABLE region_config CASCADE;
