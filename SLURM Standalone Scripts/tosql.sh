#!/bin/bash

AGREEABLE=5

#take an input parameter a count of how many times a power value is needed before the values are considered consistant and correct
if [ $# -eq 0 ]
then
AGREEABLE=5
else
AGREEABLE=$1
fi

awk -v agreeable=$AGREEABLE 'FS="," {print (NR>1 && $7>0 && $9>=agreeable ) ? "INSERT INTO host_calibration_data ( host_id, cpu, memory, power ) Values ("substr($1,3)","$7/100",0,"$8");": ""  }' calibration_data.csv | awk 'NF' > calibration_data.sql
