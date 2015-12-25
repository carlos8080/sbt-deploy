#!/bin/bash
# Script to deploy Node package to production server
# Takes 3 parameters:
# $1: the instance address
# $2: the key PEM file
# $3: the ZIP file
# $4: the node server file

keyFile=$2
package="$WORKSPACE/target/$3"
destination="ubuntu@$1"
destinationFolder="/home/ubuntu/"
destinationPackage=$destinationFolder"$3"

echo "Deploying" $package "to" $destination":"$destinationFolder

scp -i $keyFile "$package" $destination":"$destinationFolder
ssh -i $keyFile $destination "unzip -o $destinationPackage -d $destinationFolder"
ssh -i $keyFile $destination "rm $destinationPackage"

echo "Deployment successful! Restarting server..."

process=$(ssh -i $keyFile $destination "forever list | grep 'No forever processes running'")
if [ ! "$process" ];then
   #There are processes running
   ssh -i $keyFile $destination "forever restart --spinSleepTime 10000 $4"
fi
if [ "$process" ];then
   #There are NO processes running
   ssh -i $keyFile $destination "forever start --spinSleepTime 10000 $4"
fi

echo "Node server restarted!"
