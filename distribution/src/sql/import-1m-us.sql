import into account(id,region,balance,currency,name,description,type,closed,allow_negative,updated)
    CSV DATA (
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-1-us.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-2-us.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-3-us.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-4-us.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-5-us.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-6-us.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-7-us.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-8-us.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-9-us.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/1m/account-10-us.csv'
    );

-- import into transaction(id,region,booking_date,transfer_date,transaction_type)
--     CSV DATA (
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-1-us.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-2-us.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-3-us.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-4-us.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-5-us.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-6-us.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-7-us.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-8-us.csv'
--     );
--
-- import into transaction_item(transaction_id,transaction_region,account_id,account_region,amount,currency,note,running_balance)
--     CSV DATA (
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-item-1-us.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-item-2-us.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-item-3-us.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-item-4-us.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-item-5-us.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-item-6-us.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-item-7-us.csv',
--     'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/2500K/transaction-item-8-us.csv'
--     );
