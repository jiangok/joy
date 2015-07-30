#!/bin/bash

# this script is run with root user.
# it setup mesos master/slave according to
# https://www.digitalocean.com/community/tutorials/how-to-configure-a-production-ready-mesosphere-cluster-on-ubuntu-14-04

ifconfig eth1 | grep inet | grep -v inet6 | awk '{print $2}'

export COMMON_SCRIPT=/vagrant/prepareCommon.sh

$COMMON_SCRIPT

export LOG=/vagrant/prepareMaster.log

apt-get install mesosphere -y &> $LOG

IP=$(bash $COMMON_SCRIPT getIP) >> $LOG 2>&1
echo $IP  >> $LOG 2>&1

: <<'END'
# http://stackoverflow.com/questions/1988249/how-do-i-use-su-to-execute-the-rest-of-the-bash-script-as-that-user
echo "vagrant ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers
END