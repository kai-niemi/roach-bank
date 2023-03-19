INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated_at) VALUES
    ('ea4bde66-de55-4e3f-bc00-1b2b8fa22bfe', 'stockholm', '100.00', 'SEK', 'test:1', 'A', false, 0, clock_timestamp()),
    ('3f50fdd1-97a2-407c-95da-e00bf0cae97b', 'stockholm', '100.00', 'SEK', 'test:2', 'A', false, 0, clock_timestamp()),
    ('ea4bde66-de55-4e3f-bc00-1b2b8fa22bfc', 'stockholm', '100.00', 'SEK', 'test:3', 'L', false, 1, clock_timestamp()),
    ('3f50fdd1-97a2-407c-95da-e00bf0cae97d', 'stockholm', '100.00', 'SEK', 'test:4', 'L', false, 1, clock_timestamp())
;

UPDATE account SET balance = account.balance + data_table.balance, updated_at=clock_timestamp()
FROM (SELECT
     unnest(ARRAY['ea4bde66-de55-4e3f-bc00-1b2b8fa22bfe'::UUID,'3f50fdd1-97a2-407c-95da-e00bf0cae97b'::UUID]) as id,
     unnest(ARRAY[50,-50]) as balance) as data_table
WHERE account.id=data_table.id AND account.closed=false
  AND (account.balance + data_table.balance) * abs(account.allow_negative-1) >= 0;

SELECT unnest(ARRAY['ea4bde66-de55-4e3f-bc00-1b2b8fa22bfe','3f50fdd1-97a2-407c-95da-e00bf0cae97b']);
SELECT unnest(ARRAY[50,-50]);

select id,balance,allow_negative from account
    where id in('ea4bde66-de55-4e3f-bc00-1b2b8fa22bfe','3f50fdd1-97a2-407c-95da-e00bf0cae97b');

update account set allow_negative=0 where id='ea4bde66-de55-4e3f-bc00-1b2b8fa22bfe';
update account set allow_negative=0 where id='3f50fdd1-97a2-407c-95da-e00bf0cae97b';

update account set balance=100.00 where id='ea4bde66-de55-4e3f-bc00-1b2b8fa22bfe';
update account set balance=100.00 where id='3f50fdd1-97a2-407c-95da-e00bf0cae97b';

--

UPDATE account SET balance = account.balance + data_table.balance, updated_at=clock_timestamp()
FROM (SELECT
          unnest(ARRAY['ea4bde66-de55-4e3f-bc00-1b2b8fa22bfc'::UUID,'3f50fdd1-97a2-407c-95da-e00bf0cae97d'::UUID]) as id,
          unnest(ARRAY[50,-50]) as balance) as data_table
WHERE account.id=data_table.id AND account.closed=false
  AND (account.balance + data_table.balance) * abs(account.allow_negative-1) >= 0;

select id,balance,allow_negative from account
    where id in ('ea4bde66-de55-4e3f-bc00-1b2b8fa22bfc','3f50fdd1-97a2-407c-95da-e00bf0cae97d');
select sum(balance) from account
    where id in ('ea4bde66-de55-4e3f-bc00-1b2b8fa22bfc','3f50fdd1-97a2-407c-95da-e00bf0cae97d'); -- 200