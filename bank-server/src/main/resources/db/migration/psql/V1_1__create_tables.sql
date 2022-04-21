-- RoachBank DDL for PostgreSQL

-- For error 'function gen_random_uuid() does not exist' run:
-- CREATE extension pgcrypto;

----------------------
-- Metadata
----------------------

create table region_map
(
    city     varchar(64) not null,
    currency varchar(3)  not null,
    region   varchar(64) not null,

    primary key (city, currency, region)
);

----------------------
-- Main tables
----------------------

create table account
(
    id             uuid,
    region         varchar(64)    not null,
    balance        numeric(19, 2) not null,
    currency       varchar(3)     not null,
    name           varchar(128)   not null,
    description    varchar(256),
    type           varchar(1)     not null,
    closed         boolean        not null,
    allow_negative integer        not null,
    updated        timestamptz    not null,

    primary key (region, id)
);

alter table account
    add constraint check_account_type check (type in ('A', 'L', 'E', 'R', 'C'));
alter table account
    add constraint check_account_allow_negative check (allow_negative between 0 and 1);
alter table account
    add constraint check_account_positive_balance check (balance * abs(allow_negative - 1) >= 0);

------------------------------------------------
create table transaction
(
    id               uuid        not null,
    region           varchar(64) not null,
    booking_date     date null,
    transfer_date    date        not null,
    transaction_type varchar(3)  not null,
    remark           varchar(255) null,

    primary key (region, id)
);

------------------------------------------------
create table transaction_item
(
    transaction_id     uuid           not null,
    transaction_region varchar(64)    not null,
    account_id         uuid           not null,
    account_region     varchar(64)    not null,
    amount             numeric(19, 2) not null,
    currency           varchar(3)     not null,
    note               varchar(255),
    running_balance    numeric(19, 2) not null,

    primary key (transaction_region, transaction_id, account_region, account_id)
);

alter table transaction_item
    add constraint region_ref_transaction_fk
        foreign key (transaction_region, transaction_id) references transaction (region, id);

alter table transaction_item
    add constraint region_ref_account_fk
        foreign key (account_region, account_id) references account (region, id);

