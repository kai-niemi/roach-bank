#!/bin/bash

# Configuration
########################

title="CockroachDB 4-region AP-US deployment"
# CRDB release version
releaseversion="v21.1.2"
# Number of node instances in total including clients
nodes="16"
# Nodes hosting CRDB
crdbnodes="1-12"
# Array of client nodes (must match size of regions)
clients=(13 14 15 16)
# Array of client localities (must match partition names)
localities=('ap' 'us')
# Array of regions localities (must match zone names)
regions=('ap-southeast-1' 'us-west-2' 'us-east-2' 'us-east-1' )
# AWS/GCE cloud (aws|gce)
cloud="aws"
# AWS/GCE region zones (must align with nodes count)
zones="\
ap-southeast-1a,\
ap-southeast-1b,\
ap-southeast-1c,\
us-west-2a,\
us-west-2b,\
us-west-2c,\
us-east-2a,\
us-east-2b,\
us-east-2c,\
us-east-1a,\
us-east-1b,\
us-east-1c,\
ap-southeast-1a,\
us-west-2a,\
us-east-2a,\
us-east-1a"
# AWS/GCE machine types
machinetypes="c5d.xlarge"
# SQL file for geo-partitioning
#partitionsqlfile="sql/partition-aws-us-eu-ap.sql"

# DO NOT EDIT BELOW THIS LINE
#############################

source "fn_util.sh"
source "fn_deploy.sh"

exit 0