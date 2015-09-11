#!/bin/bash

# this script is run with root user.
# it setup mesos master/slave according to
# https://www.digitalocean.com/community/tutorials/how-to-configure-a-production-ready-mesosphere-cluster-on-ubuntu-14-04

export LOG=/vagrant/prepareMaster.log

echo args: $1 &> $LOG

/vagrant/prepareCommon.sh

apt-get install mesosphere -y >> $LOG 2>&1

master1Ip=$1
echo zk://$master1Ip:2181/marathon &> /etc/mesos/zk >> $LOG 2>&1
echo 1 &> /etc/zookeeper/conf/myid >> $LOG 2>&1
echo server.1=$master1Ip:2888:3888 &> /etc/zookeeper/conf/zoo.cfg >> $LOG 2>&1
echo 1 &> /etc/mesos-master/quorum >> $LOG 2>&1

: <<'END'

echo $master1Ip | sudo tee /etc/mesos-master/ip >> $LOG 2>&1
cp /etc/mesos-master/ip /etc/mesos-master/hostname >> $LOG 2>&1
mkdir -p /etc/marathon/conf >> $LOG 2>&1
cp /etc/mesos-master/hostname /etc/marathon/conf >> $LOG 2>&1
cp /etc/mesos/zk /etc/marathon/conf/master >> $LOG 2>&1
cp /etc/marathon/conf/master /etc/marathon/conf/zk >> $LOG 2>&1
stop mesos-slave >> $LOG 2>&1
echo manual | sudo tee /etc/init/mesos-slave.override >> $LOG 2>&1
restart zookeeper >> $LOG 2>&1
start mesos-master >> $LOG 2>&1
start marathon >> $LOG 2>&1


# http://stackoverflow.com/questions/1988249/how-do-i-use-su-to-execute-the-rest-of-the-bash-script-as-that-user
echo "vagrant ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers

echo get ip  >> $LOG 2>&1
IP=$(. /vagrant/lib.sh && getIP)
echo IP: $IP  >> $LOG 2>&1
END