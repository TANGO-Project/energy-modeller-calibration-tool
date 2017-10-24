#!/bin/bash
#
#SBATCH --job-name=Bench-Test
#SBATCH --output=BenchTestOutput.txt
#
#SBATCH --ntasks=1
#SBATCH --time=25:00
#SBATCH --mem-per-cpu=100
#SBATCH  -N 1
#SBATCH --profile=Energy,Task 
#SBATCH --acctg-freq=Energy=5,Task=5 
#SBATCH -w ns51
#SBATCH --exclusive

CORES=16

echo "Running stress test with 1% CPU Usage"
stress -c $CORES --backoff 1000000 &
PID=$!
sleep 0.5
CPIDS=$(pidof -o $PID stress)
echo "Stress PIDs are: $CPIDS"
CORE=0
for CPID in $CPIDS; do
  cpulimit -p $CPID -l 1 &
  taskset -cp $CORE $CPID
  let "CORE++"
done
sleep 120;
#killall -s KILL stress
pkill -9 stress

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
#  killall -s KILL stress
  pkill -9 stress
  PERCENTAGE=$((PERCENTAGE + INCREMENT))
done
