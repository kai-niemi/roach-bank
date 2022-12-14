-- For error 'function gen_random_uuid() does not exist' run:
-- CREATE extension pgcrypto;

drop type if exists account_type;
create type account_type as enum ('A', 'L', 'E', 'R', 'C');

drop sequence if exists account_name_sequence;
create sequence if not exists account_name_sequence
    start 1 increment by 64 cache 64;

drop type if exists transaction_type;
create type transaction_type as enum ('GEN');

----------------------
-- Metadata
----------------------

create table region
(
    cloud  varchar(255) not null,
    name   varchar(255) not null,
    cities varchar(255) not null,

    primary key (name)
);

----------------------
-- Main tables
----------------------

create table account
(
    id             uuid           not null default gen_random_uuid(),
    city           varchar(255)   not null,
    balance        decimal(19, 2) not null,
    currency       varchar(255)   not null default 'USD',
    name           varchar(128)   not null,
    description    varchar(256)   null,
    type           account_type   not null,
    closed         boolean        not null default false,
    allow_negative integer        not null default 0,
    updated        timestamptz    not null default clock_timestamp(),

    primary key (id)
);

create index idx_account_city on account (city);

create table transaction
(
    id               uuid             not null default gen_random_uuid(),
    city             varchar(255)     not null,
    booking_date     date             not null default CURRENT_DATE,
    transfer_date    date             not null default CURRENT_DATE,
    transaction_type transaction_type not null,

    primary key (id)
);

create table transaction_item
(
    transaction_id  uuid           not null,
    account_id      uuid           not null,
    city            varchar(255)   not null,
    amount          decimal(19, 2) not null,
    currency        varchar(255)   not null default 'USD',
    note            varchar(255),
    running_balance decimal(19, 2),

    primary key (transaction_id, account_id)
);

create table outbox
(
    id             uuid         not null default gen_random_uuid(),
    create_time    timestamptz  not null default clock_timestamp(),
    aggregate_type varchar(255) not null,
    aggregate_id   varchar(255) not null,
    event_type     varchar(255) not null,
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
