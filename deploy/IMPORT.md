# Import Notes

The following CSV files contain fake accounts and transactions for testing purposes. 

To import, first connect to any node:

     roachprod sql $CLUSTER:10

Clear current account plan and transactions before importing (optional):

     use roach_bank;
     TRUNCATE TABLE transaction_item CASCADE;
     TRUNCATE TABLE transaction CASCADE;
     TRUNCATE TABLE account CASCADE;

Start the import:
    
    roachprod sql $CLUSTER:10 -e < sql-file
 
where `sql-file` is one of the following T-shirt sizes:

1M accounts:

    roachprod sql $CLUSTER:10 -e < common/sql/import-1m.sql

100M accounts:

    roachprod sql $CLUSTER:10 -e < common/sql/import-100m.sql

250M accounts:

    roachprod sql $CLUSTER:10 -e < common/sql/import-250m.sql

## Generating CSV files

Client commands to generate CSVs for all regions:

      gen-csv --accounts 100_000 --destination .data/10k
      gen-csv --accounts 500_000 --destination .data/500k
      gen-csv --accounts 1_000_000 --destination .data/1m
      gen-csv --accounts 25_000_000 --destination .data/25m
      gen-csv --accounts 50_000_000 --destination .data/50m
      gen-csv --accounts 100_000_000 --destination .data/100m
      gen-csv --accounts 250_000_000 --destination .data/25m

US-only regions:

      gen-csv --accounts 100_000 --destination .data/10k --suffix us --regions us_west,us_central,us_east
      gen-csv --accounts 500_000 --destination .data/500k --suffix us --regions us_west,us_central,us_east
      gen-csv --accounts 1_000_000 --destination .data/1m --suffix us --regions us_west,us_central,us_east
      gen-csv --accounts 25_000_000 --destination .data/25m --suffix us --regions us_west,us_central,us_east
      gen-csv --accounts 50_000_000 --destination .data/50m --suffix us --regions us_west,us_central,us_east
      gen-csv --accounts 100_000_000 --destination .data/100m --suffix us --regions us_west,us_central,us_east
      gen-csv --accounts 250_000_000 --destination .data/25m --suffix us --regions us_west,us_central,us_east

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
