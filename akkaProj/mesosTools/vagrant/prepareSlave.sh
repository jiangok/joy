#!/bin/bash

# this script is run with root user.
# it setup mesos master/slave according to
# https://www.digitalocean.com/community/tutorials/how-to-configure-a-production-ready-mesosphere-cluster-on-ubuntu-14-04

/vagrant/prepareCommon.sh

export LOG=/vagrant/prepareSlave.log

apt-get install mesos -y &> $LOG

