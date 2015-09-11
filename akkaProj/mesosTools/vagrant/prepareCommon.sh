#!/bin/bash

export LOG=/vagrant/prepareCommon.log
export GOPATH=/home/vagrant/go
export PATH=$PATH:$GOPATH/bin:$GOROOT/bin:/usr/local/go/bin

cp /vagrant/.bashrc_vagrant /home/vagrant/.bashrc &> $LOG

apt-get install git -y >> $LOG 2>&1

#
echo install go  >> $LOG 2>&1
#
apt-get install golang -y >> $LOG 2>&1
tar -C /usr/local -xzf /vagrant/go1.4.2.linux-amd64.tar.gz >> $LOG 2>&1
apt-get purge golang* -y >> $LOG 2>&1

#
echo install godep  >> $LOG 2>&1
#
mkdir $GOPATH  >> $LOG 2>&1
cd $GOPATH  >> $LOG 2>&1
go get github.com/tools/godep  >> $LOG 2>&1

#
echo install docker  >> $LOG 2>&1
#
apt-get install lxc wget bsdtar curl -y >> $LOG 2>&1
apt-get install linux-image-extra-$(uname -r) -y >> $LOG 2>&1
modprobe aufs -y >> $LOG 2>&1
wget -qO- https://get.docker.com/ | sh >> $LOG 2>&1

#
echo preparing mesosphere and mesos installation  >> $LOG 2>&1
#
apt-key adv --keyserver keyserver.ubuntu.com --recv E56151BF >> $LOG 2>&1
DISTRO=$(lsb_release -is | tr '[:upper:]' '[:lower:]') >> $LOG 2>&1
CODENAME=$(lsb_release -cs) >> $LOG 2>&1
echo "deb http://repos.mesosphere.io/${DISTRO} ${CODENAME} main" | tee /etc/apt/sources.list.d/mesosphere.list >> $LOG 2>&1

# required for mesosphere install.
# use "tail -f prepareCommon.log" to monitor the output.
# otherwise, the command looks stuck since it takes long time.
apt-get update

