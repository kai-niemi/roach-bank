connect http://localhost:8090/api
set-thread-pool-size --size 400
set-pool-size --size 600
transfer --concurrency 20 --limit 1000
balance --followerReads --concurrency 5 --limit 1000