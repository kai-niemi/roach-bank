# Import Notes

To import, first connect to a client node:

     roachprod sql $CLUSTER:N

Then pick the appropriate t-shirt:

    -- 100K accounts
    import into account(id,region,balance,currency,name,description,type,closed,allow_negative,updated)
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
    
    
    -- 100M accounts (about 10GiB)
    import into account(id,region,balance,currency,name,description,type,closed,allow_negative,updated)
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
    
    import into account(id,region,balance,currency,name,description,type,closed,allow_negative,updated)
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


## Generating CSV files

Connect to bank client:

    ./bank-client

Commands to generate CSVs for all regions:

      gen-csv --accounts 100_000 --destination .data/10k
      gen-csv --accounts 500_000 --destination .data/500k
      gen-csv --accounts 1_000_000 --destination .data/1m
      gen-csv --accounts 25_000_000 --destination .data/25m
      gen-csv --accounts 50_000_000 --destination .data/50m
      gen-csv --accounts 100_000_000 --destination .data/100m
      gen-csv --accounts 250_000_000 --destination .data/25m

US-only regions:

      gen-csv --accounts 100_000 --destination .data/10k --suffix us --cities us_west,us_central,us_east
      gen-csv --accounts 500_000 --destination .data/500k --suffix us --cities us_west,us_central,us_east
      gen-csv --accounts 1_000_000 --destination .data/1m --suffix us --cities us_west,us_central,us_east
      gen-csv --accounts 25_000_000 --destination .data/25m --suffix us --cities us_west,us_central,us_east
      gen-csv --accounts 50_000_000 --destination .data/50m --suffix us --cities us_west,us_central,us_east
      gen-csv --accounts 100_000_000 --destination .data/100m --suffix us --cities us_west,us_central,us_east
      gen-csv --accounts 250_000_000 --destination .data/25m --suffix us --cities us_west,us_central,us_east

## NFS/Local import
              
CockroachDB supports bulk importing from local files also, see 
[import considerations](https://www.cockroachlabs.com/docs/v20.2/use-cloud-storage-for-bulk-operations.html#considerations) 
for guidance.

On a single-host, you would typically symlink the `extern` directory of each node to a shared directory 
with these files.

    ln -s <base>/importfiles/ <base>/datafiles/n1/extern
    ln -s <base>/importfiles/ <base>/datafiles/n2/extern
    ln -s <base>/importfiles/ <base>/datafiles/n3/extern
    ..

Then use following SQL:

    import into account(id,region,balance,currency,name,description,type,closed,allow_negative,updated)
    CSV DATA (
    'nodelocal://1/account-1.csv',
    'nodelocal://1/account-2.csv',
    'nodelocal://1/account-3.csv',
    'nodelocal://1/account-4.csv',
    'nodelocal://1/account-5.csv',
    'nodelocal://1/account-6.csv',
    'nodelocal://1/account-7.csv',
    'nodelocal://1/account-8.csv',
    'nodelocal://1/account-9.csv',
    'nodelocal://1/account-10.csv'
    );
