#!/bin/bash

docker pull mtiessler/kg_repository:latest

docker stop kg_repository
docker rm kg_repository

docker run -d --name kg_repository -p 3003:3003 mtiessler/kg_repository:latest
