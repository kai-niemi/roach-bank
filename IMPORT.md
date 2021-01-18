# Import Notes

These import files (in S3) contains fake accounts and transactions for testing purposes. 
Each bucket contains accounts in 25 regions (cities) around the globe. 

First connect to any node:

    ./cockroach sql --insecure --database roach_bank

Clear current account plan and transactions before importing (optional):

     TRUNCATE TABLE transaction_item CASCADE;
     TRUNCATE TABLE transaction CASCADE;
     TRUNCATE TABLE account CASCADE;

Copy SQL from one of following T-shirt sized import files:

- [X-small](roach-bank-server/src/main/resources/db/import/import-25k.sql) - 1K accounts per region 25K total
- [Small](roach-bank-server/src/main/resources/db/import/import-250k.sql) - 10K accounts per region 250K total
- [Medium](roach-bank-server/src/main/resources/db/import/import-2500k.sql) - 100K accounts per region 2.5M total
- [Large](roach-bank-server/src/main/resources/db/import/import-25m.sql) - 1M accounts per region 25M total
- [X-large](roach-bank-server/src/main/resources/db/import/import-125m.sql) - 5M accounts per region 125M total (~20 GiB DB)
- [X-large US](roach-bank-server/src/main/resources/db/import/import-120m-us.sql) - 10M accounts per 12 US regions 120M total
- [2X-large](roach-bank-server/src/main/resources/db/import/import-250m.sql) - 10M accounts per region 250M total

## Generate CSVs

To generate custom import CSV files, start the client and execute:

    help gen-csv
