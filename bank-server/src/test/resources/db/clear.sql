drop table if exists transaction_item cascade;
drop table if exists transaction cascade;
drop table if exists account cascade;
drop table if exists region cascade;
drop table if exists outbox cascade;

TRUNCATE TABLE transaction_item CASCADE;
TRUNCATE TABLE transaction CASCADE;
TRUNCATE TABLE account CASCADE;
TRUNCATE TABLE region CASCADE;

