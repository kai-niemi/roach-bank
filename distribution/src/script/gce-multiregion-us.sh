#!/bin/bash
# Script for setting up a multi-region Roach Bank cluster using roachprod in either AWS or GCE.

# Configuration
########################

title="CockroachDB 3-region US deployment"
# CRDB release version
releaseversion="v21.1.2"
# Number of node instances in total including clients
nodes="12"
# Nodes hosting CRDB
crdbnodes="1-9"
# Array of client nodes (must match size of regions)
clients=(10 11 12)
# Array of client localities (must match partition names)
localities=('us_east1' 'us_central' 'us_east4')
# Array of regions localities (must match zone names)
regions=('us-east1' 'us-central1' 'us-east4')
# AWS/GCE cloud (aws|gce)
cloud="gce"
# AWS/GCE region zones (must align with nodes count)
zones="\
us-east1-b,\
us-east1-c,\
us-east1-d,\
us-central1-a,\
us-central1-b,\
us-central1-c,\
us-east4-a,\
us-east4-b,\
us-east4-c,\
us-east1-b,\
us-central1-a,\
us-east4-a"
# AWS/GCE machine types
machinetypes="n2-standard-16"
# SQL file for geo-partitioning
partitionsqlfile="sql/partition-gce-us.sql"

# DO NOT EDIT BELOW THIS LINE
#############################

source "fn_util.sh"
source "fn_deploy.sh"

exit 0