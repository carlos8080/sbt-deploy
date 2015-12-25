#!/bin/bash
# Script to deploy SBT package to server
# Takes 4 parameters:
# $1: the instance address
# $2: the key PEM file
# $3: the HTTP port to start the app
# $4: the NOHUP file

keyFile=$2
packageFolder="$WORKSPACE/target/universal/"
latestPackage="$(ls -rt "$packageFolder" | tail -1)"
package=$packageFolder$latestPackage
destination="ubuntu@$1"
destinationFolder="/home/ubuntu/"
destinationPackage=$destinationFolder$latestPackage

echo "Deploying" $package "to" $destination":"$destinationFolder

scp -i $keyFile "$package" $destination":"$destinationFolder
ssh -i $keyFile $destination "unzip -o $destinationPackage -d $destinationFolder"
ssh -i $keyFile $destination "rm $destinationPackage"

echo "Deployment successful! Restarting server..."

temp=$destinationFolder$latestPackage
appFolder="${temp%.*}/"
pidFile=$appFolder"RUNNING_PID"

if ssh -i $keyFile $destination stat $pidFile \> /dev/null 2\>\&1
  then
    echo "Stopping application before proceeding..."
    pid=$(ssh -i $keyFile $destination "cat $pidFile")
    ssh -i $keyFile $destination "sudo kill $pid"
  else
    echo "Application is not running... Proceeding to startup"
fi

echo "Starting server..."
filename=$(ssh -i $keyFile $destination "ls $appFolder"bin/" | head -1")
app=$appFolder"bin/"$filename
#[  -z "$4" ] && nohupFile=$appFolder"nohup.out" || nohupFile=$4
ssh -i $keyFile $destination "sudo nohup $app -Dhttp.port=$3 > $appFolder"nohup.out" 2>&1 &"

echo "Play server restarted!"
