#!/bin/bash
set -e

sbt clean coverage +coreJVM/test +akka/test +circeJVM/test +asynchttpclient/test coverageReport
sbt +validateJS
sbt +example/compile +exampleJS/compile
