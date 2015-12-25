#!/bin/sh

keyFile=/var/lib/jenkins/.ssh/rte-back-staging.pem
destination=ubuntu@ec2-54-207-62-7.sa-east-1.compute.amazonaws.com

process=$(ssh -i $keyFile $destination "forever list | grep 'No forever processes running'")
#process=$(forever list | grep "No forever processes running")

if [ ! "$process" ];then
   echo There are processes running
fi
if [ "$process" ];then
   echo There are NO processes running
fi
