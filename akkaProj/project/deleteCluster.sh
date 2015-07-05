#!/bin/sh
exec scala -nocompdaemon -savecompiled -Dfile.encoding=UTF-8 "$0" $@
!#

import sys.process._

val discoverDockerMachines = "docker-machine ls"

val output = discoverDockerMachines !!

val machineNames = for(
    (row, i) <- output.split('\n').zipWithIndex if i > 0;
    (column, j) <- row.split(" +").zipWithIndex if j == 0
    ) yield column

machineNames.map({ m =>
    println(s"docker-machine stop $m".!!)
    println(s"docker-machine rm $m".!!)
})



