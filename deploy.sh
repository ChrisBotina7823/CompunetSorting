#!/bin/bash

var=21
psResult=$(ps -ef | grep libs/server.jar)
echo $psResult > temp.txt
pid=$(awk '$8 ~ /java/ {print $2}' temp.txt)
rm temp.txt
echo $pid
if [ -z "$pid" ];then
    #kill -9 $pid
    nohup java -jar server/build/libs/server.jar >> out.txt &
    echo "success"
fi
