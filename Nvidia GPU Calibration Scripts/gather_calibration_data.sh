#!/bin/bash

GPU_COUNT=`nvidia-smi --query-gpu=gpu_name --format=csv,noheader | wc -l`
let GPU_COUNT-=1

#Note: using the parameter -f ./GPU-stats.csv forces the script to run in the foreground. Redirecting standard out i.e. >> ./GPU-stats.csv does not have this limitation.
for ((GPU=0;GPU <= $GPU_COUNT; GPU++)); do
   NAME=`nvidia-smi --query-gpu=name --format=csv,nounits,noheader -i $GPU`
   nvidia-smi --query-gpu=utilization.gpu,utilization.memory,temperature.gpu,clocks.current.graphics,clocks.current.sm,clocks.current.memory,clocks.current.video,power.draw --format=csv,nounits  -i $GPU -l 1 >> ./$HOSTNAME-GPU-"$NAME"-"$GPU"-stats.csv &
   echo "$!" > $GPU.pid
done
