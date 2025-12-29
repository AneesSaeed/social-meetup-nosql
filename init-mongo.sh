#!/bin/bash
echo "Waiting for MongoDB to start..."
sleep 10

docker exec mongodb mongosh --eval "rs.initiate()"

echo "MongoDB replica set initialized!"