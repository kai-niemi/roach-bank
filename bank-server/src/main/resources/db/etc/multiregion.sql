create database roach_bank;

set enable_multiregion_placement_policy=on;
alter database roach_bank placement restricted;
alter database roach_bank placement default;

set enable_super_regions = 'on';
alter role all set enable_super_regions = on;
alter database roach_bank add super region "eu" values "eu-north-1","eu-central-1","eu-west-1";
alter database roach_bank add super region "us" values "us-east-1","us-east-2","us-west-1";
alter database roach_bank survive region failure;

show super regions from database roach_bank;
show partitions from table account;
show zone configuration for table account;
-- show range from table account for row (..);