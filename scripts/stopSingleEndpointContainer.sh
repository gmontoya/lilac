#!/bin/bash

federation=$1

name=${federation}SingleEndpoint
docker stop ${name}

sleep 10
