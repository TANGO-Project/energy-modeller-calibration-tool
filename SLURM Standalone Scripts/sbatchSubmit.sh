#!/bin/bash
#
#SBATCH --job-name=RK-Bench-Test
#SBATCH --output=RKBenchTest1.txt
#
#SBATCH --ntasks=1
#SBATCH --time=15:00
#SBATCH --mem-per-cpu=100
#SBATCH  -N 1
#SBATCH --profile=Energy,Task 
#SBATCH --acctg-freq=Energy=1,Task=1 
#SBATCH -w nd32

CORES=16

INCREMENT=10
PERCENTAGE=10
while [ $PERCENTAGE -le 100 ]; do
  echo "Running stress test with $PERCENTAGE% CPU Usage"
  stress -c $CORES --backoff 1000000 &
  PID=$!
  sleep 0.5
  CPIDS=$(pidof -o $PID stress)
  echo "Stress PIDs are: $CPIDS"
  CORE=0
  for CPID in $CPIDS; do
    cpulimit -p $CPID -l $PERCENTAGE &
    taskset -cp $CORE $CPID
    let "CORE++"
  done
  sleep 120;
  killall -s KILL stress
  PERCENTAGE=$((PERCENTAGE + INCREMENT))
done
