-- RoachBank DDL for CockroachDB

----------------------
-- Metadata
----------------------

create table region_group
(
    name string not null,
    primary key (name)
);

create table region_config
(
    name       string    not null,
    currency   string(3) not null,
    group_name string    not null,

    constraint fk_region_group foreign key (group_name) references region_group (name),
    primary key (name, currency, group_name)
);

----------------------
-- Main tables
----------------------

create table account
(
    id             uuid           not null default gen_random_uuid(),
    region         string         not null default crdb_internal.locality_value('city'),
    balance        decimal(19, 2) not null,
    currency       string(3)      not null,
    name           string(128)    not null,
    description    string(256)    null,
    type           string(1)      not null,
    closed         boolean        not null default false,
    allow_negative integer        not null default 0,
    updated        timestamptz    not null default clock_timestamp(),

    family         update_often(balance, updated),
    family         update_never(currency, name, description, type, closed, allow_negative),

    primary key (region, id)
);

create table transaction_type
(
    id   string(3) not null,
    name string    not null,

    primary key (id)
);

create table transaction
(
    id               uuid      not null default gen_random_uuid(),
    region           string    not null default crdb_internal.locality_value('city'),
    booking_date     date      not null default current_date(),
    transfer_date    date      not null default current_date(),
    transaction_type string(3) not null,

    primary key (region, id)
);

create table transaction_item
(
    transaction_id     uuid           not null,
    transaction_region string         not null,
    account_id         uuid           not null,
    account_region     string         not null,
    amount             decimal(19, 2) not null,
    currency           string(3)      not null,
    note               string,
    running_balance    decimal(19, 2) not null,

    primary key (transaction_region, transaction_id, account_region, account_id)
);

--interleave in parent transaction (transaction_region, transaction_id);

------------------------------------------------
-- Constraints on account
------------------------------------------------

alter table account
    add constraint check_account_type check (type in ('A', 'L', 'E', 'R', 'C'));
alter table account
    add constraint check_account_allow_negative check (allow_negative between 0 and 1);
alter table account
    add constraint check_account_positive_balance check (balance * abs(allow_negative - 1) >= 0);

------------------------------------------------
-- Constraints on transaction
------------------------------------------------

-- create index idx_transaction_type on transaction (transaction_type);
-- alter table transaction
--     add constraint fk_transaction_type_reftype
--         foreign key (transaction_type)
--             references transaction_type;

------------------------------------------------
-- Constraints on transaction_item
------------------------------------------------

-- alter table transaction_item
--     add constraint fk_region_ref_transaction
--         foreign key (transaction_region, transaction_id) references transaction (region, id);

-- alter table transaction_item
--     add constraint fk_region_ref_account
--         foreign key (account_region, account_id) references account (region, id);
