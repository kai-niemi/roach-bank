IMPORT INTO account(id,city,balance,currency, name,description, type,closed,allow_negative,updated)
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
