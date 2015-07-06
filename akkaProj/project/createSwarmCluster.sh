#!/bin/sh
exec scala -nocompdaemon -savecompiled -Dfile.encoding=UTF-8 "$0" $@
!#

# this script and deleteSwarmCluster.sh is to create/delete swarm cluster on local machine.
# they have been tested on OSX.
# the workflow is:
# 1. ./createSwarmCluster.sh   // create a swarm cluster
# 2. eval $(docker-machine env --swarm swarm-master // set docker env to the newly created swarm cluster
# 3. docker-compose up   // deploy containers
# 4. docker-compose up --no-deps -d andrew // redeploy container andrew
# 5. ./deleteSwarmCluster.sh  // delete the cluster after test.

# I stopped exploring this technology and focus on mesosphere for better service discovery and other goodies.

import sys.process._

val token = "41305b3342e57f88f62e1924f52ab32c"

val createMaster = s"docker-machine create -d virtualbox --swarm --swarm-master --swarm-discovery token://$token swarm-master"

println(createMaster.!!)

// TODO: have trouble to run below command. eval is a builtin command.
// need to use Seq("sh", "-c", "eval $(docker-machine env --swarm swarm-master").!!
// but it seems to create a new shell instead of working for current shell.
println("Please run 'eval $(docker-machine env --swarm swarm-master' to set current the cluster as the current docker env")

// To redeploy andrew container
// docker-compose up --no-deps -d andrew



