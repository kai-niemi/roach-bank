#!/bin/bash
# Script for setting up a multi-region Roach Bank cluster using roachprod in either AWS or GCE.

# Configuration
########################

title="CockroachDB 3-region US deployment"
# CRDB release version
releaseversion="v21.1.1"
# Number of node instances in total including clients
nodes="12"
# Nodes hosting CRDB
crdbnodes="1-9"
# Array of client nodes (must match size of regions)
clients=(10 11 12)
# Array of client localities (must match partition names)
localities=('us_west' 'us_central' 'us_east')
# Array of regions localities (must match zone names)
regions=('us-west-2' 'us-east-2' 'us-east-1')
# AWS/GCE cloud (aws|gce)
cloud="aws"
# AWS/GCE region zones (must align with nodes count)
zones="\
us-west-2a,\
us-west-2b,\
us-west-2c,\
us-east-2a,\
us-east-2b,\
us-east-2c,\
us-east-1a,\
us-east-1b,\
us-east-1c,\
us-west-2a,\
us-east-2a,\
us-east-1a"
# AWS/GCE machine types
machinetypes="c5d.4xlarge"
# SQL file for geo-partitioning
partitionsqlfile="sql/partition-aws-us.sql"

# DO NOT EDIT BELOW THIS LINE
#############################

source "fn_util.sh"
source "fn_deploy.sh"

exit 0