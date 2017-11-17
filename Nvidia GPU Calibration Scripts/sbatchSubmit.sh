#!/bin/bash
#
#SBATCH --job-name=GPU-Bench-Test
#SBATCH --output=BenchTestOutput.txt
#
#SBATCH --ntasks=1
#SBATCH --time=25:00
#SBATCH --mem-per-cpu=100
#SBATCH  -N 1
#SBATCH --profile=Energy,Task 
#SBATCH --acctg-freq=Energy=5,Task=5 
#SBATCH -w ns50
#SBATCH --exclusive
#SBATCH --gres=gpu:1


CORECOUNT=24

hostname
nvidia-smi
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/cuda-8.0/lib64/
./gather_calibration_data.sh

INCREMENT=1
RUN=1
MAXLOOPCOUNT=20
while [ $RUN -le $MAXLOOPCOUNT ]; do
echo "Running iteration:" $CORECOUNT
    ./stress $CORECOUNT 256;
    RUN=$((RUN + INCREMENT));
done
stop_gathering.sh

