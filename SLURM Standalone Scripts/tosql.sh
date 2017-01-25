#!/bin/bash

TIME=30

#take an input parameter a the time from the start and end that should be cut from the experimental data
if [ $# -eq 0 ]
then
TIME=30
else
TIME=$1
fi

awk -v time=$TIME 'FS="," {print (NR>1 && $7>0 && $9>=time && $9<120.5-time ) ? "INSERT INTO host_calibration_data ( host_id, cpu, memory, power ) Values ("substr($1,3)","$7/100",0,"$8");": ""  }' calibration_data.csv | awk 'NF' > calibration_data.sql
