OUNT=`nvidia-smi --query-gpu=gpu_name --format=csv,noheader | wc -l`
let GPU_COUNT-=1

#Note: using the parameter -f ./GPU-stats.csv forces the script to run in the foreground. Redirecting standard out i.e. >> ./GPU-stats.csv does not have this limitation.
for ((GPU=0;GPU <= $GPU_COUNT; GPU++)); do
   kill $(cat "$GPU".pid)
done
