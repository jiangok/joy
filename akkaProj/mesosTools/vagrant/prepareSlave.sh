#!/bin/bash

# this script is run with root user.
# it setup mesos master/slave according to
# https://www.digitalocean.com/community/tutorials/how-to-configure-a-production-ready-mesosphere-cluster-on-ubuntu-14-04

export LOG=/vagrant/prepareSlave.log

echo args: $1 &> $LOG

/vagrant/prepareCommon.sh

apt-get install mesos -y &> $LOG

master1Ip=$1
echo write to zk: zk://$master1Ip:2181/mesos >> $LOG 2>&1

echo zk://$master1Ip:2181/mesos &> /etc/mesos/zk

stop zookeeper

echo manual | sudo tee /etc/init/zookeeper.override

echo manual | sudo tee /etc/init/mesos-master.override

stop mesos-master


echo get ip  >> $LOG 2>&1
IP=$(. /vagrant/lib.sh && getIP)
echo IP: $IP  >> $LOG 2>&1
echo $IP | sudo tee /etc/mesos-slave/ip


cp /etc/mesos-slave/ip /etc/mesos-slave/hostname

start mesos-slave


: <<'END'
# http://stackoverflow.com/questions/1988249/how-do-i-use-su-to-execute-the-rest-of-the-bash-script-as-that-user
echo "vagrant ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers

echo get ip  >> $LOG 2>&1
IP=$(. /vagrant/lib.sh && getIP)
echo IP: $IP  >> $LOG 2>&1
END