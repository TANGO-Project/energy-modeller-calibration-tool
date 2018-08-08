# Tango Energy Modeller Calibration Tool
  
  &copy; University of Leeds 2017

Tango Energy Modeller Calibration Tool (EMC) is a component of the European Project TANGO (http://tango-project.eu).

Energy Modeller Calibration Tool is distributed under a [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Description

The Calibrator performs the initial experimentation that allows the energy model to predict future power consumption based upon workload. 

## Installation Guide

These scripts should be made executable and then executed on the physical host with the GPU to be profiled.
  
## Usage Guide

The standalone calibration tool is designed to calibrate the model that is used for physical hosts. These scripts aim to work
for gathering calibration data that is suitable for the energy modeller. These scripts may be launched using sbatch sbatchSubmit.sh or more directly as described below:

The first stage is to start the monitoring.

```
./gather_calibration_data.sh
```

The second is to apply a load. The parameter to be passed is how many stream multiprocessors are on the gpu.

```
./gpu-stress.sh <count_of_sm_proccessors>
```

once the load has finished, the data gathering process can be stopped.

```
./stop_gathering.sh
```

Files with the pattern: <Hostname>-<card_name>-<gpu_index>-stats.csv will be produced. Such as: ns51.bullx-GPU-Tesla K20Xm-0-stats.csv 

The file generated shows load being applied, it can be moved into the folder with the energy modeller and renamed.

It should be given the same name as the graphics card (<card_name>.csv) i.e. "GPU-Tesla K20Xm.csv"

## Relation to other TANGO components

The energy modeller calibrator works with the following components:

* **Energy Modeller** - The calibrator provides the initial data needed to create models regarding power and energy consumption.
* **Device Supervisor** - The calibrator can directly interface with the device supervisor as a means of using it as a datasource for monitoring the environment.
* **Monitoring Infrastructure** - The calibrator can interface with the monitoring infrastructure as a means of using it as a datasource for monitoring the environment.
