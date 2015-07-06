#!/bin/sh
exec scala -nocompdaemon -savecompiled -Dfile.encoding=UTF-8 "$0" $@
!#

import sys.process._

val containerName = "andrew"
val ip="http://10.141.141.10:8080"
val containerJson = s"${containerName}.json"

// destroy any existing andrew container before redeploy
val destroyContainer = s"curl -X DELETE $ip/v2/apps/$containerName"
println(destroyContainer.!!)

// need the async destroying finish its job
Thread.sleep(5000)

val deploy = Seq("curl", "-X", "POST", s"$ip/v2/apps", "-H", "Content-Type: application/json", "-d", s"@$containerJson")
println(deploy.!!)
