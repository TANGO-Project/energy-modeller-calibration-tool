# Tango Energy Modeller Calibration Tool

&copy; University of Leeds 2016

Tango Energy Modeller Calibration Tool (EMC) is a component of the European Project TANGO (http://tango-project.eu).

EMC is distributed under a [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Description

The Calibrator performs the initial experimentation that allows the energy model to predict future power consumption based upon workload. 

## Installation Guide

This guide it is divided into two parts, one specific to compilation of the Calibrator and the second on how to run and configure the Calibrator.

### Compilation

#### Requirements

The Energy Modeller's primary two prerequisites are:

* Java
* Maven

#### Installation and configuration procedure

1. Generate the jar energy modeller standalone calibration tool jar using the command: mvn clean package (executed  in the standalone calibration tool directory)
2. Install the energy-modeller-standalone-calibration-tool on each host that is to be calibrated.

A configuration file called “Apps.csv” can now be specified. This file providing details about the application/s used to induce the training load for the host.

An example is provided within the source code and the headers as part of a default file are written out to disk if the apps.csv file is not found. A test application has also been provided under utils\ascetic-load-generator-app. This file specifies the following: The start time the application should run, the standard out and error files to redirect output to, the applications working directory and if output should also be redirected to the screen or not.

#### Build status from Travis-CI

[![Build Status](https://travis-ci.org/TANGO-Project/energy-modeller-calibration-tool.svg?branch=master)](https://travis-ci.org/TANGO-Project/energy-modeller-calibration-tool)

#### Sonar Cloud reports:
The Sonar Cloud reports for this project are available at: https://sonarcloud.io/dashboard?id=eu.tango%3Aenergy-modeller-standalone-calibration-tool

## Usage Guide

Its usage is as follows: 

```
java –jar energy-modeller-standalone-calibration-tool-0.0.1-SNAPSHOT.jar [hostname] [halt-on-calibrated] [benchmark-only] [no-benchmark] [use-watts-up-meter]
```

[hostname]: This is an non-optional argument that states which host to emulate the Watt meter for. If no hostname is specified the tool will work for all calibrated hosts.

[halt-on-calibrated]: The halt-on-calibrated flag will prevent calibration in cases where the data has already been gathered.

[benchmark-only]: The benchmark-only flag skips the calibration run and performs a benchmark run only.

[no-benchmark]: The no-benchmark flag skips the benchmarking.

[use-watts-up-meter]: The use-watts-up-meter flag can be used so that Zabbix is not used for calibration but local measurements are performed instead. This requires a Watts Up Meter.

## Relation to other TANGO components

The energy modeller calibrator works with the following components:

* **Energy Modeller** - The calibrator provides the initial data needed to create models regarding power and energy consumption.
* **Device Supervisor** - The calibrator can directly interface with the device supervisor as a means of using it as a datasource for monitoring the environment.
* **Monitoring Infrastructure** - The calibrator can interface with the monitoring infrastructure as a means of using it as a datasource for monitoring the environment.
