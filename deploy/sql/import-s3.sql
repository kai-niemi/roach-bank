-- 100K accounts
import into account(id,city,balance,currency,name,description,type,closed,allow_negative,updated)
    CSV DATA (
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100k/account-1.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100k/account-2.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100k/account-3.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100k/account-4.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100k/account-5.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100k/account-6.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100k/account-7.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100k/account-8.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100k/account-9.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100k/account-10.csv'
    );

-- 1M accounts
import into account(id,city,balance,currency,name,description,type,closed,allow_negative,updated)
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

-- 10M accounts
import into account(id,city,balance,currency,name,description,type,closed,allow_negative,updated)
    CSV DATA (
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-1.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-2.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-3.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-4.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-5.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-6.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-7.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-8.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-9.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/10m/account-10.csv'
    );

-- 100M accounts (about 10GiB)
import into account(id,city,balance,currency,name,description,type,closed,allow_negative,updated)
    CSV DATA (
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100m/account-1.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100m/account-2.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100m/account-3.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100m/account-4.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100m/account-5.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100m/account-6.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100m/account-7.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100m/account-8.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100m/account-9.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/100m/account-10.csv'
    );

import into account(id,city,balance,currency,name,description,type,closed,allow_negative,updated)
    CSV DATA (
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250m/account-1.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250m/account-2.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250m/account-3.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250m/account-4.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250m/account-5.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250m/account-6.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250m/account-7.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250m/account-8.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250m/account-9.csv',
    'https://roach-bank-demo.s3.eu-central-1.amazonaws.com/250m/account-10.csv'
    );
