#!/bin/bash

SERVERPORT=80
MONGOPORT=27017:27017

cd ~/chatterDirServer
git pull
mvn clean
mvn package
sudo docker run -p ${MONGOPORT} --name mongo-db -d mongo
sudo java -jar ./target/ChatterDirectoryServer*.jar --server.port=${SERVERPORT} &