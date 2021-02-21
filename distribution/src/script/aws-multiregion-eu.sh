#!/bin/bash
# Script for setting up a multi-region Roach Bank cluster using roachprod in either AWS or GCE.

# Configuration
########################

title="CockroachDB 3-region EU deployment"
# CRDB release version
releaseversion="v20.2.5"
# Number of node instances in total including clients
nodes="12"
# Nodes hosting CRDB
crdbnodes="1-9"
# Array of client nodes (must match size of regions)
clients=(10 11 12)
# Array of client localities (must match partition names)
localities=('eu_west' 'eu_south' 'eu_central')
# Array of regions localities (must match zone names)
regions=('eu-west-2' 'eu-west-3' 'eu-central-1')
# AWS/GCE cloud (aws|gce)
cloud="aws"
# AWS/GCE region zones (must align with nodes count)
zones="\
eu-west-2a,\
eu-west-2a,\
eu-west-2a,\
eu-west-3a,\
eu-west-3a,\
eu-west-3a,\
eu-central-1a,\
eu-central-1a,\
eu-central-1a,\
eu-west-2a,\
eu-west-3a,\
eu-central-1a"
# AWS/GCE machine types
machinetypes="c5d.4xlarge"
# SQL file for geo-partitioning
partitionsqlfile="sql/partition-aws-eu.sql"

# DO NOT EDIT BELOW THIS LINE
#############################

source "fn_util.sh"
source "fn_deploy.sh"

exit 0