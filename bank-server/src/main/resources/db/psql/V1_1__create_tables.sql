-- RoachBank DDL for PostgreSQL 10+
CREATE EXTENSION if not exists pgcrypto;

CREATE
FUNCTION gateway_region() RETURNS text
    AS $$ select 'europe_west1' $$
    LANGUAGE SQL
    IMMUTABLE
    RETURNS NULL ON NULL INPUT;

drop type if exists account_type;
create type account_type as enum ('A', 'L', 'E', 'R', 'C');

drop type if exists transaction_type;
create type transaction_type as enum ('GEN','TMP','PAY');

----------------------
-- Metadata
----------------------

create table region
(
    cloud  varchar(256) not null,
    name   varchar(256) not null,
    cities varchar(256) not null,

    primary key (name)
);

----------------------
-- Main tables
----------------------

create table account
(
    id             uuid           not null default gen_random_uuid(),
    city           varchar(256)   not null,
    balance        decimal(19, 2) not null,
    currency       varchar(3)     not null default 'USD',
    name           varchar(128)   not null,
    description    varchar(512)   null,
    type           account_type   not null,
    closed         boolean        not null default false,
    allow_negative integer        not null default 0,
    updated_at     timestamptz    not null default clock_timestamp(),

    primary key (id)
);

create index on account (city);

create table transaction
(
    id               uuid             not null default gen_random_uuid(),
    city             varchar(256)     not null,
    booking_date     date             not null default CURRENT_DATE,
    transfer_date    date             not null default CURRENT_DATE,
    transaction_type transaction_type not null,

    primary key (id)
);

create table transaction_item
(
    transaction_id  uuid           not null,
    account_id      uuid           not null,
    city            varchar(256)   not null,
    amount          decimal(19, 2) not null,
    currency        varchar(256)   not null default 'USD',
    note            varchar(512),
    running_balance decimal(19, 2) not null,

    primary key (transaction_id, account_id)
);

create table outbox
(
    id             uuid         not null default gen_random_uuid(),
    crated_at      timestamptz  not null default clock_timestamp(),
    aggregate_type varchar(256) not null,
    aggregate_id   varchar(256) not null,
    event_type     varchar(256) not null,
    payload        jsonb        not null,

    primary key (id)
);

----------------------
-- Contraints
----------------------

alter table account
    add constraint check_account_allow_negative check (allow_negative between 0 and 1);
alter table account
    add constraint check_account_positive_balance check (balance * abs(allow_negative - 1) >= 0);

alter table transaction_item
    add constraint fk_txn_item_ref_transaction
        foreign key (transaction_id) references transaction (id);
alter table transaction_item
    add constraint fk_txn_item_ref_account
        foreign key (account_id) references account (id);