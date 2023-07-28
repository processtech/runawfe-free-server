#!/bin/bash
cd `dirname $0`
mvn compile exec:java -Dexec.args="$1"
