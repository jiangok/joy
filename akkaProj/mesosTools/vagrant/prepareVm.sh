#!/bin/bash

# this script is run with root user.

LOG=/vagrant/prepareVm_root.log
GOPATH=/home/vagrant/go


# http://stackoverflow.com/questions/1988249/how-do-i-use-su-to-execute-the-rest-of-the-bash-script-as-that-user

echo "vagrant ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers


#: <<'END'
#:END

cp /vagrant/.bashrc_vagrant /home/vagrant/.bashrc

apt-get update
apt-get install golang -y
tar -C /usr/local -xzf /vagrant/go1.4.2.linux-amd64.tar.gz
apt-get purge golang* -y

sudo -E -u vagrant -i mkdir $GOPATH; \
    cd $GOPATH; \
    export GOPATH=$GOPATH; \
    export PATH=$PATH:$GOPATH/bin:$GOROOT/bin:/usr/local/go/bin; \
    go get github.com/tools/godep; \
    go get github.com/mesosphere/mesos-dns; \
    cd $GOPATH/src/github.com/mesosphere/mesos-dns; \
    make all; \
    mesos-dns -config=/vagrant/config.json;