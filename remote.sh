#!/bin/sh

host=$1
folder="testDespliegue"
./gradlew build
ssh $host mkdir -p $folder
scp  server/build/libs/*.jar $host:$folder
ssh $host java -jar $folder/server.jar
sshpass