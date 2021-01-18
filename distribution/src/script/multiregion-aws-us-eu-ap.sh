#!/bin/bash
# Script for setting up a multi-region Roach Bank cluster using roachprod in either AWS or GCE.

# Configuration
########################

title="CockroachDB 3-region US-EU-AP deployment"
# CRDB release version
releaseversion="v20.2.3"
# Number of node instances in total including clients
nodes="12"
# Nodes hosting CRDB
crdbnodes="1-9"
# Nodes hosting LB, client and server apps
clientnodes="10-12"
# Regional client nodes
client1="10"
client2="11"
client3="12"
# AWS/GCE cloud (aws|gce)
cloud="aws"
# US regions
region1="us-east-1"
region2="eu-central-1"
region3="ap-southeast-1"
partitionsqlfile="sql/partition-aws-us-eu-ap.sql"
# AWS/GCE region zones (must align with nodes count)
zones="\
${region1}a,\
${region1}b,\
${region1}c,\
${region2}a,\
${region2}b,\
${region2}c,\
${region3}a,\
${region3}b,\
${region3}c,\
${region1}a,\
${region2}a,\
${region3}a"
# AWS/GCE machine types
machinetypes="c5d.4xlarge"
# Dry-run mode on|off
dryrun=off

# DO NOT EDIT BELOW THIS LINE
#############################

source "fn_util.sh"
source "fn_deploy.sh"

exit 0