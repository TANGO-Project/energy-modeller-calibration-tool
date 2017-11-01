# Tango Energy Modeller Calibration Tool
  
  &copy; University of Leeds 2017

Tango Energy Modeller Calibration Tool (EMC) is a component of the European Project TANGO (http://tango-project.eu).

Energy Modeller Calibration Tool is distributed under a [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Description

The Calibrator performs the initial experimentation that allows the energy model to predict future power consumption based upon workload. 

## Installation Guide

These scripts should be made executable and executed via SLURM.
  
  ## Usage Guide

The standalone calibration tool is designed to calibrate the model that is used for physical hosts.

These scripts are intended to be used on a SLURM based environment. They are to be used as follows:

submit a calibration run job using:

```
sbatch ./sbatchSubmit.sh
```

Make a note of its job id, then run the following command once it has completed:

```
extract_profiling.sh <job_id>
```

This extracts data from SLURM ready for processing. The extracted data can then be merged into a single dataset:

```
./merge.sh <core_count>
```

The count of cores the processor has is an optional extension to this command. The default is 16.
The next command is used to prepare the dataset for loading into the database of the energy modeller.

```
./tosql.sh <filter_time>
```

The filter time indicates how many seconds of data should be removed from the start of each load period. This is variable, but is recommended to be changed based upon the stability of the numbers gained. This may require graphing or other examination of variance to understand if the values obtained are undergoing changes, which may be due to averaging windows for example.

## Relation to other TANGO components

The energy modeller calibrator works with the following components:

* **Energy Modeller** - The calibrator provides the initial data needed to create models regarding power and energy consumption.
* **Device Supervisor** - The calibrator can directly interface with the device supervisor as a means of using it as a datasource for monitoring the environment.
* **Monitoring Infrastructure** - The calibrator can interface with the monitoring infrastructure as a means of using it as a datasource for monitoring the environment.
