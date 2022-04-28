-- RoachBank DDL for PostgreSQL

-- For error 'function gen_random_uuid() does not exist' run:
-- CREATE extension pgcrypto;

create type account_type as enum ('A', 'L', 'E', 'R', 'C');
create type transaction_type as enum ('GEN');

create table region
(
    name   string not null,
    cities string not null,

    primary key (name)
);

----------------------
-- Metadata
----------------------

create table region
(
    name   string not null,
    cities string not null,

    primary key (name)
);

----------------------
-- Main tables
----------------------

create table account
(
    id             uuid,
    city           varchar(64)    not null,
    balance        numeric(19, 2) not null,
    currency       varchar(64)  not null,
    name           varchar(128)   not null,
    description    varchar(256),
    type           varchar(1)     not null,
    closed         boolean        not null,
    allow_negative integer        not null,
    updated        timestamptz    not null,

    primary key (id)
);

create table transaction
(
    id               uuid             not null,
    city             varchar(64)      not null,
    booking_date     date             null,
    transfer_date    date             not null,
    transaction_type transaction_type not null,

    primary key (id)
);

create table transaction_item
(
    transaction_id   uuid           not null,
    transaction_city varchar(64)    not null,
    account_id       uuid           not null,
    amount           numeric(19, 2) not null,
    currency         varchar(64)  not null,
    note             varchar(255),
    running_balance  numeric(19, 2) not null,

    primary key (transaction_id, account_id)
);

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
