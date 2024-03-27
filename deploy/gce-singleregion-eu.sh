#!/bin/bash
# Script for setting up a multi-region Roach Bank cluster using roachprod in either AWS or GCE.

# Configuration
########################

title="CockroachDB single-region EU deployment"
# CRDB release version
releaseversion="v23.2.2"
# Number of node instances in total including clients
nodes="4"
# Nodes hosting CRDB
crdbnodes="1-3"
# Array of client nodes (must match size of regions)
clients=(4)
# Array of regions localities (must match zone names)
regions=('europe-west3')
# AWS/GCE cloud (aws|gce)
cloud="gce"
# AWS/GCE region zones (must align with nodes count)
zones="\
europe-west3-a,\
europe-west3-b,\
europe-west3-c,\
europe-west3-a"
# AWS/GCE machine types
machinetypes="n2d-standard-8"
1
# DO NOT EDIT BELOW THIS LINE
#############################

functionsdir="./common"

source "${functionsdir}/core_functions.sh"

main.sh

exit 0