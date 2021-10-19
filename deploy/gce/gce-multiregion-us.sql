-- us-east1 South Carolina
-- us-central1 Iowa
-- us-east4 Virginia

---------------------------------------------------------------
-- System ranges
---------------------------------------------------------------

ALTER RANGE meta CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=us-east1: 2, +region=us-central1: 3, +region=us-east4: 2}';

ALTER RANGE liveness CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=us-east1: 2, +region=us-central1: 3, +region=us-east4: 2}';

ALTER RANGE system CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=us-east1: 2, +region=us-central1: 3, +region=us-east4: 2}';

ALTER DATABASE system CONFIGURE ZONE USING
    num_replicas = 7,
    constraints = '{+region=us-east1: 2, +region=us-central1: 3, +region=us-east4: 2}';