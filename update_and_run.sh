#!/bin/bash

CONTAINER_NAME="kg_repository"
IMAGE_NAME="mtiessler/kg_repository:latest"

docker pull $IMAGE_NAME


docker stop $CONTAINER_NAME
docker rm $CONTAINER_NAME

docker run -d --name $CONTAINER_NAME -p 3003:3003 $IMAGE_NAME
