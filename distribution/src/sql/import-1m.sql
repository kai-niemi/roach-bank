import into account(id,region,balance,currency,name,description,type,closed,allow_negative,updated)
    CSV DATA (
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-1.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-2.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-3.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-4.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-5.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-6.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-7.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-8.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-9.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-10.csv'
    );

-- import into transaction(id,region,booking_date,transfer_date,transaction_type)
--     CSV DATA (
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-1.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-2.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-3.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-4.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-5.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-6.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-7.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-8.csv'
--     );
--
-- import into transaction_item(transaction_id,transaction_region,account_id,account_region,amount,currency,note,running_balance)
--     CSV DATA (
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-item-1.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-item-2.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-item-3.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-item-4.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-item-5.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-item-6.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-item-7.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-item-8.csv'
--     );
