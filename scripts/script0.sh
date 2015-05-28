#!/bin/bash

address=`$fedrahome/scripts/getHost.sh 3040`

oarsh $address "$fedrahome/scripts/script.sh"


