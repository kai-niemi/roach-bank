-- RoachBank DDL for CockroachDB

----------------------
-- Types
----------------------

drop type if exists account_type;
drop type if exists transaction_type;
drop type if exists currency_code;

create type account_type as enum ('A', 'L', 'E', 'R', 'C');
create type transaction_type as enum ('GEN');
create type currency_code as enum ('USD', 'SEK', 'EUR', 'NOK', 'GBP','SGD','HKD','AUD','JPY','BRL');

-- create type city_name as enum (
--     'stockholm','helsinki','oslo','london','frankfurt','amsterdam','milano','madrid','athens','barcelona','paris','manchester',
--     'seattle','san francisco','los angeles','phoenix','minneapolis','chicago','detroit','atlanta','new york','boston','washington dc','miami',
--     'singapore','hong kong','sydney','tokyo','sao paulo','rio de janeiro','salvador'
-- );

----------------------
-- Metadata
----------------------

create table region
(
    name   string not null,
    cities string not null,

    primary key (name)
);

create table city
(
    name     string        not null,
    currency currency_code not null,

    primary key (name)
);

----------------------
-- Main tables
----------------------

create table account
(
    id             uuid           not null default gen_random_uuid(),
    city           string         not null,
    balance        decimal(19, 2) not null,
    currency       currency_code  not null,
    name           string(128) not null,
    description    string(256) null,
    type           account_type   not null,
    closed         boolean        not null default false,
    allow_negative integer        not null default 0,
    updated        timestamptz    not null default clock_timestamp(),

    primary key (id)
);

-- Breaks CDC
-- family         update_often(balance, updated),
--     family         update_rarely(currency, name, description, type, closed, allow_negative),

create table transaction
(
    id               uuid             not null default gen_random_uuid(),
    city             string           not null,
    booking_date     date             not null default current_date(),
    transfer_date    date             not null default current_date(),
    transaction_type transaction_type not null,

    primary key (id)
);

create table transaction_item
(
    transaction_id   uuid           not null,
    transaction_city string         not null,
    account_id       uuid           not null,
    amount           decimal(19, 2) not null,
    currency         currency_code  not null,
    note             string,
    running_balance  decimal(19, 2) not null,

    primary key (transaction_id, account_id)
);

create table outbox
(
    id             uuid        not null default gen_random_uuid(),
    create_time    timestamptz not null default clock_timestamp(),
    aggregate_type string      not null,
    aggregate_id   string      not null,
    event_type     string      not null,
    payload        jsonb       not null,

    primary key (id)
);

----------------------
-- Contraints
----------------------

alter table account
    add constraint check_account_allow_negative check (allow_negative between 0 and 1);
alter table account
    add constraint check_account_positive_balance check (balance * abs(allow_negative - 1) >= 0);

-- alter table transaction_item
--     add constraint fk_txn_item_ref_transaction
--         foreign key (transaction_id) references transaction (id);
-- alter table transaction_item
--     add constraint fk_txn_item_ref_account
--         foreign key (account_id) references account (id);
