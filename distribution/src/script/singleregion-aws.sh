#!/bin/bash
# Script for setting up a single-region Roach Bank cluster using roachprod in either AWS or GCE.

# Configuration
########################

title="CockroachDB single region deployment (AWS)"
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
#cloud="aws"
cloud="gce"
# AWS/GCE region prefix
region="eu-central-1"
region1="eu-central-1a"
region2="eu-central-1b"
region3="eu-central-1c"
# AWS/GCE region zones (must align with nodes size)
zones="\
${region}a,\
${region}a,\
${region}a,\
${region}b,\
${region}b,\
${region}b,\
${region}c,\
${region}c,\
${region}c,\
${region}a,\
${region}b,\
${region}c"
# AWS/GCE machine types
machinetypes="c5d.9xlarge"
# Dry-run mode on|off
dryrun=off

# Bootstrap
# DO NOT EDIT BELOW THIS LINE
#############################

source "fn_util.sh"
source "fn_deploy.sh"

exit 0