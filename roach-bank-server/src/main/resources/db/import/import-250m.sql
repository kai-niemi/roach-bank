import into account(id,region,balance,currency,name,description,type,closed,allow_negative,updated)
    CSV DATA (
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/account-1.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/account-2.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/account-3.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/account-4.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/account-5.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/account-6.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/account-7.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/account-8.csv'
    );

import into transaction(id,region,booking_date,transfer_date,transaction_type)
    CSV DATA (
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/transaction-1.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/transaction-2.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/transaction-3.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/transaction-4.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/transaction-5.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/transaction-6.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/transaction-7.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/transaction-8.csv'
    );

import into transaction_item(transaction_id,transaction_region,account_id,account_region,amount,currency,note,running_balance)
    CSV DATA (
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/transaction-item-1.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/transaction-item-2.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/transaction-item-3.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/transaction-item-4.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/transaction-item-5.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/transaction-item-6.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/transaction-item-7.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250M/transaction-item-8.csv'
    );