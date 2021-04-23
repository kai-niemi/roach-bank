#!/bin/bash

# Configuration
########################

title="CockroachDB single region deployment (AZ)"
# CRDB release version
releaseversion="v20.2.7"
# Number of node instances in total including clients
nodes="4"
# Nodes hosting CRDB
crdbnodes="1-3"
# Array of client nodes (must match size of regions)
clients=(4)
# Array of client localities (must match partition names)
localities=('westeurope' 'westeurope' 'westeurope')
# Array of regions localities (must match zone names)
regions=('westeurope' 'westeurope' 'westeurope')
# AWS/GCE/AZ cloud (aws|gce)
cloud="azure"
# AWS/GCE/AZ region zones (must align with nodes size)
zones="\
westeurope,\
westeurope,\
westeurope,\
westeurope"
# Machine type
machinetypes="Standard_D2_v4"

# DO NOT EDIT BELOW THIS LINE
#############################

source "fn_util.sh"
source "fn_deploy.sh"

exit 0