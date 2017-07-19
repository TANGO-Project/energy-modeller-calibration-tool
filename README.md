# Tango Energy Modeller Calibration Tool

&copy; University of Leeds 2016

Tango Energy Modeller Calibration Tool (EMC) is a component of the European Project TANGO (http://tango-project.eu).

EMC is distributed under a [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Description

The Calibrator performs the initial experimentation that allows the energy model to predict future power consumption based upon workload. 

## Installation Guide

This guide it is divided into two different guides, one specific to compilation of the Calibrator and the second on how to run and configure the Calibrator.

### Compilation

#### Requirements

The Energy Modeller's primary two prerequisites are:

* Java
* Maven

#### Installation and configuration procedure


#### Build status from Travis-CI

[![Build Status](https://travis-ci.org/TANGO-Project/energy-modeller-calibration-tool.svg?branch=master)](https://travis-ci.org/TANGO-Project/energy-modeller-calibration-tool)

#### Sonar Cloud reports:
The Sonar Cloud reports for this project are available at: https://sonarcloud.io/dashboard?id=eu.tango%3Aenergy-modeller-standalone-calibration-tool

### Installation for running the service

In this case, we are going to detail how to perform a calibration run.

#### Configuring the service

TODO

## Usage Guide

TODO

## Relation to other TANGO components

The energy modeller calibrator works with the following components:

* **Energy Modeller** - The calibrator provides the initial data needed to create models regarding power and energy consumption.
* **Device Supervisor** - The SAM can directly interface with the device supervisor as a means of using it as both a datasource for monitoring the environment and for invoking adaptation.
* **Monitoring Infrastructure** - The SAM can interface with the monitoring infrastructure as a means of using it as a datasource for monitoring the environment..
