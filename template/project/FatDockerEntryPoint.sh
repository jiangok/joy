#!/bin/bash

# to ssh to this docker running on ubuntuDev: ssh root@172.17.42.1 -p 20022

java -jar /app/fatapp.jar &

/usr/sbin/sshd -D


