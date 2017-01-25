#!/bin/bash
set -e

git config --global user.email "jl.garhdez@gmail.com"
git config --global user.name "pepegar"
git config --global push.default simple

sbt docs/publishMicrosite
