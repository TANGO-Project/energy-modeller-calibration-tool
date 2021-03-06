/**
 * Copyright 2015 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.tango.energy.modeller.calibrator;

import eu.ascetic.ioutils.execution.CompletedListener;
import eu.ascetic.ioutils.execution.ManagedProcess;
import eu.ascetic.ioutils.execution.ManagedProcessCommandData;
import eu.ascetic.ioutils.execution.ManagedProcessListener;
import eu.ascetic.ioutils.execution.ManagedProcessLogger;
import eu.ascetic.ioutils.io.ResultsStore;
import eu.ascetic.ioutils.io.Settings;
import eu.tango.energymodeller.datasourceclient.HostDataSource;
import eu.tango.energymodeller.datasourceclient.HostMeasurement;
import static eu.tango.energymodeller.datasourceclient.KpiList.POWER_KPI_NAME;
import eu.tango.energymodeller.datasourceclient.MetricValue;
import eu.tango.energymodeller.datastore.DatabaseConnector;
import eu.tango.energymodeller.types.energyuser.Host;
import eu.tango.energymodeller.types.energyuser.usage.HostEnergyCalibrationData;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The aim of this class is to launch a set of managed processes one after
 * another at the specified time. During the launches of the application
 * calibration data will be taken and at the end of the experiment the data will
 * be written to the database.
 *
 * @author Richard Kavanagh
 */
public class CalibrationRunManager implements ManagedProcessListener {

    private static final String TO_EXECUTE_SETTINGS_FILE = "Apps.csv";
    private String workingDir;
    private final GregorianCalendar startTime = new GregorianCalendar();
    private final ResultsStore appsToExecute;
    private final ArrayList<ManagedProcessCommandData> commandSet = new ArrayList<>();
    private ManagedProcessLogger logger = null;
    private MeasurementLogger measurementLogger = null;
    private Actioner actioner = new Actioner();
    private int counter = 0;
    private final CompletedListener sender;
    private boolean logging = true;
    private final Host host;

    private final ArrayList<HostMeasurement> measurements = new ArrayList<>();
    private final HostDataSource datasource;
    private final DatabaseConnector database;
    private int pollInterval = 2;
    private int gapBeforeMeasurements = 4;
    private boolean simulateCalibrationRun = false;
    private final Settings settings = new Settings("calibration_settings.properties");

    /**
     * Internal state variables ensuring that new values for measurements have
     * changed between measurement intervals
     */
    private long lastClock = 0;
    private double lastPowerValue = 0;

    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public CalibrationRunManager(CompletedListener closeable, HostDataSource datasource, DatabaseConnector database, Host host) {

        workingDir = settings.getString("working_directory", ".");
        if (!workingDir.endsWith("/")) {
            workingDir = workingDir + "/";
        }
        appsToExecute = new ResultsStore(workingDir + TO_EXECUTE_SETTINGS_FILE);
        logging = settings.getBoolean("log_executions", true);
        pollInterval = settings.getInt("poll_interval", pollInterval);
        simulateCalibrationRun = settings.getBoolean("simulate_calibration_run", simulateCalibrationRun);
        gapBeforeMeasurements = settings.getInt("delay_before_taking_measurements", gapBeforeMeasurements);
        if (simulateCalibrationRun) {
            System.out.println("This is a simulated run, no data will be saved!");
        }
        if (settings.isChanged()) {
            settings.save("calibration_settings.properties");
        }
        this.sender = closeable;
        this.datasource = datasource;
        this.database = database;
        this.host = host;
        writeOutDefaults(appsToExecute);
        if (logging) {
            logger = new ManagedProcessLogger(new File(workingDir + "AppLog.csv"), false);
            Thread loggerThread = new Thread(logger);
            loggerThread.setDaemon(true);
            loggerThread.start();
            measurementLogger = new MeasurementLogger(new File(workingDir + "MeasurementsLog.csv"), false);
            Thread measurementLoggerThread = new Thread(measurementLogger);
            measurementLoggerThread.setDaemon(true);
            measurementLoggerThread.start();
        }
        Logger.getLogger(Actioner.class.getName()).log(Level.INFO, "FILE LOCATION: {0}", new File(workingDir + TO_EXECUTE_SETTINGS_FILE).getAbsolutePath());
        getApplicationListFromFile(appsToExecute);
        new Thread(actioner).start();
    }
    
    /**
     * This performs a check to see if the settings file is empty or not. It
     * will write out a blank file if the file is not present and the attempt to
     * write to the database will be stopped.
     *
     * @param appsList The list of apps to execute
     * @return If the defaults settings have been written out to disk or not.
     */
    private boolean writeOutDefaults(ResultsStore appsList) {
        String os = System.getProperty("os.name").toLowerCase();
        boolean answer = false;
        if (!new File(workingDir + TO_EXECUTE_SETTINGS_FILE).exists()) {
            appsList.add("Time From Start");
            appsList.append("Command");
            appsList.append("stdOut");
            appsList.append("stdError");
            appsList.append("Working Directory");
            appsList.append("Output To Screen");
            appsList.append("Time of Task End");
            String pwd = System.getProperty("user.dir");
            if (os.contains("win")) {
                appsList.add(String.format("0,ping 192.0.2.2 -n 1 -w 50000 > nul,%s\\test.out,%s\\error.out,%s\\,TRUE,50", pwd, pwd, pwd));
                appsList.add(String.format("60,%s\\WindowsStress.exe 10 all 60,%s\\test.out,%s\\error.out,%s\\,TRUE,120", pwd, pwd, pwd, pwd));
                appsList.add(String.format("160,%s\\WindowsStress.exe 20 all 60,%s\\test1.out,%s\\error1.out,%s\\,TRUE,220", pwd, pwd, pwd, pwd));
                appsList.add(String.format("260,%s\\WindowsStress.exe 40 all 60,%s\\test2.out,%s\\error2.out,%s\\,TRUE,320", pwd, pwd, pwd, pwd));
                appsList.add(String.format("360,%s\\WindowsStress.exe 60 all 60,%s\\test3.out,%s\\error3.out,%s\\,TRUE,420", pwd, pwd, pwd, pwd));
                appsList.add(String.format("460,%s\\WindowsStress.exe 80 all 60,%s\\test4.out,%s\\error4.out,%s\\,TRUE,520", pwd, pwd, pwd, pwd));
                appsList.add(String.format("560,%s\\WindowsStress.exe 100 all 60,%s\\test5.out,%s\\error5.out,%s\\,TRUE,620", pwd, pwd, pwd, pwd));  
            } else {
                appsList.add(String.format("0,sleep 50,%s/test.out,%s/error.out,%s,TRUE,50", pwd, pwd, pwd));
                appsList.add(String.format("60,%s/run-stress-point.sh 10 4 60,%s/test.out,%s/error.out,%s/,TRUE,120", pwd, pwd, pwd, pwd));
                appsList.add(String.format("160,%s/run-stress-point.sh 20 4 60,%s/test1.out,%s/error1.out,%s/,TRUE,220", pwd, pwd, pwd, pwd));
                appsList.add(String.format("260,%s/run-stress-point.sh 40 4 60,%s/test2.out,%s/error2.out,%s/,TRUE,320", pwd, pwd, pwd, pwd));
                appsList.add(String.format("360,%s/run-stress-point.sh 60 4 60,%s/test3.out,%s/error3.out,%s/,TRUE,420", pwd, pwd, pwd, pwd));
                appsList.add(String.format("460,%s/run-stress-point.sh 80 4 60,%s/test4.out,%s/error4.out,%s/,TRUE,520", pwd, pwd, pwd, pwd));
                appsList.add(String.format("560,%s/run-stress-point.sh 100 4 60,%s/test5.out,%s/error5.out,%s/,TRUE,620", pwd, pwd, pwd, pwd));
            }
            appsList.save();
            answer = true;
            if (logging) {
                logging = false;
                if (logger != null) {
                    logger.stop();
                }
            }
            if (sender != null) {
                sender.finished();
            }
        }
        return answer;
    }

    /**
     * This loads in from file the list of applications to run
     *
     * @param appsList The file on disk that has the list of applications to
     * run.
     * @return The result store after having performed a load operation.
     */
    private ResultsStore getApplicationListFromFile(ResultsStore appsList) {
        appsList.load();
        //ignore the header of the file
        for (int i = 1; i < appsList.size(); i++) {
            ArrayList<String> current = appsList.getRow(i);
            ManagedProcessCommandData data = new ManagedProcessCommandData();
            //Set the start time
            data.setStartTime(Integer.parseInt(current.get(0)));
            GregorianCalendar actionStart = (GregorianCalendar) startTime.clone();
            actionStart.add(Calendar.SECOND, data.getStartTime());
            data.setActualStart(actionStart);

            data.setCommand(current.get(1));
            data.setStdOut(current.get(2));
            data.setStdError(current.get(3));
            data.setWorkingDirectory(new File(current.get(4)));
            data.setOutToScreen(Boolean.parseBoolean(current.get(5)));
            if (current.size() >= 6) {
                data.setEndTime(Integer.parseInt(current.get(6)));
            }
            commandSet.add(data);
        }
        return appsList;
    }

    /**
     * Performs the execution of a command.
     *
     * @param cmd The command to execute.
     */
    private void execute(ManagedProcessCommandData cmd) {
        execute(cmd.getCommand(), cmd.getStdOut(), cmd.getStdError(), cmd.getWorkingDirectory(), cmd.isOutToScreen());
    }

    /**
     * This performs the execution of a given command and registers for the
     * commands completion event.
     *
     * @param command The command to execute
     * @param stdOut The directory for standard out
     * @param stdError The directory for standard error
     * @param workingDirectory The working directory of the application
     * @param toScreen If the output should also be logged to screen or not.
     */
    private void execute(String command, String stdOut, String stdError, File workingDirectory, boolean toScreen) {
        counter = counter + 1;
        ManagedProcess process;
        process = new ManagedProcess(command, stdOut, stdError, workingDirectory, toScreen);
        process.listen(this);
        if (logging) {
            logger.printToFile(process);//prints the starting version of a process
        }
    }

    @Override
    public void receiveProcessFinishedEvent(ManagedProcess process) {
        if (logging) {
            logger.printToFile(process); //prints the finished version of a process
        }
        counter--;
        Logger.getLogger(Actioner.class.getName()).log(Level.INFO, "An action has just finished! Actions still running: {0}", counter);
        if (counter == 0 && actioner == null) {
            if (logging) {
                logger.stop();
            }
            sender.finished();
            Logger.getLogger(Actioner.class.getName()).log(Level.INFO, "Process Exector: Exiting Thread");
        }
    }

    /**
     * This method aims to read the energy data for a host and convert it into
     * the calibration data required for the energy modeller.
     *
     * @param host The host to get the calibration data for
     * @return The updated host
     */
    private HostMeasurement readEnergyDataForHost(Host host) {

        try {
            HostMeasurement dataEntry = datasource.getHostData(host);
            long currentClock = dataEntry.getClock();
            double currentPower = dataEntry.getPower();
            /**
             * The next checks ensure, that at least one metric value has been
             * updated since the last poll interval. Plus it checks that the
             * power and CPU values are within close enough proximity to be
             * still useful. and finally it checks to make sure the dependant
             * and independent variables are within close enough to be still
             * representing the same time period.
             */
            if (currentClock > lastClock
                    && dataEntry.isContemporary(POWER_KPI_NAME,
                            dataEntry.getCpuUtilisationTimeStamp(), 3)
                    && absdifference(currentPower, lastPowerValue) < 0.1) {
                printDataValueToFile(dataEntry, "New Datapoint Generated");
                System.out.println("New Datapoint Generated!");
                return dataEntry;
            } else {
                if (!(currentClock > lastClock)) {
                    printDataValueToFile(dataEntry, "Clock did not increment");
                    System.out.println("No New Datapoint: Clock not incrementing");
                }
                if (!dataEntry.isContemporary(POWER_KPI_NAME,
                        dataEntry.getCpuUtilisationTimeStamp(), 3)) {
                    printDataValueToFile(dataEntry, "CPU and power clock values out of sync");
                    System.out.println("No New Datapoint: CPU and power clock values out of sync");
                }
                if (!(absdifference(currentPower, lastPowerValue) < 0.1)) {
                    printDataValueToFile(dataEntry, "No power values plateau detected");
                    System.out.println("No New Datapoint: No power values plateau detected");
                }
            }
            lastClock = dataEntry.getClock();
            lastPowerValue = dataEntry.getPower();
        } catch (Exception ex) { //This should always try to gather data from the data source.
            Logger.getLogger(CalibrationRunManager.class.getName()).log(Level.SEVERE, "The calibrator's data logger had a problem.", ex);
        }
        return null;
    }

    /**
     * This checks to see if data entry values should be written to disk and
     * then writes them to disk if required.
     *
     * @param dataEntry The data entry to write to disk.
     * @param status The status of the data entry value.
     */
    private void printDataValueToFile(HostMeasurement dataEntry, String status) {
        if (logging == true) {
            dataEntry.addMetric(new MetricValue("Calibration Status", "Calibration Status", status, dataEntry.getClock()));
            measurementLogger.printToFile(dataEntry);
        }
    }

    /**
     * This sets the host calibration data
     *
     * @param measurements The new measurements to convert to calibration data
     * @param host The host to perform the calibration for
     * @return The list of host calibration data values for the named host.
     */
    private ArrayList<HostEnergyCalibrationData> setHostCalibrationData(ArrayList<HostMeasurement> measurements, Host host) {
        ArrayList<HostEnergyCalibrationData> calibrationData = host.getCalibrationData();
        System.out.println("New Calibration Datapoints: " + measurements.size());
        //This gives the opportunity to run a load without saving the result
        if (simulateCalibrationRun == false) {
            calibrationData.addAll(HostEnergyCalibrationData.getCalibrationData(measurements));
            host.setCalibrationData(calibrationData);
            if (database != null && !measurements.isEmpty()) {
                database.setHostCalibrationData(host);
            }
            System.out.println(getCodeProfilerHostCalibrationString(calibrationData));
        } else {
            System.out.println("This was a simulated run! No data has been saved");
        }
        return calibrationData;
    }
    
    /**
     * This prints out the host calibration data string needed to configure the 
     * Code profiler.
     * @param data The calibration data
     * @return The string formatted ready for the code profiler plug-in.
     */
    private String getCodeProfilerHostCalibrationString(ArrayList<HostEnergyCalibrationData> data) {
        String answer = "";
        for (HostEnergyCalibrationData measurement : data) {
            answer = answer + (!answer.isEmpty() ? "," : "") + measurement.getCpuUsage() + "," + measurement.getWattsUsed();
        }
        return answer;
    }

    /**
     * This returns the absolute difference between two values
     *
     * @param value1 The first value
     * @param value2 The second value
     * @return The absolute difference between the two values
     */
    private double absdifference(double value1, double value2) {
        return Math.max(value1, value2) - Math.min(value1, value2);
    }

    /**
     * The actioner looks at the current states event queue and waits to call an
     * action on a worker.
     */
    private class Actioner implements Runnable {

        private boolean stop = false;
        private GregorianCalendar latestMesurementStartTime = null;
        private GregorianCalendar latestMesurementEndTime = null;

        /**
         * This indicates if a measurement should be taken or not. This depends
         * on if an appropriate amount of time has passed since the load was
         * first induced or expected to stop.
         *
         * @return If it is OK to take a measurement or not.
         */
        private boolean shouldTakeMeasurement() {
            GregorianCalendar now = new GregorianCalendar();
            if (now.before(latestMesurementStartTime)) {
                System.out.println("Before Measurement Period");
                return false;
            }
            if (latestMesurementEndTime != null
                    && now.after(latestMesurementEndTime)) {
                System.out.println("After Measurement Period");
                return false;
            }
            return true;
        }

        /**
         * This sets the start and end time for the measurement period. It is
         * designed to avoid measuring at the very start and end of a load
         * period.
         *
         * @param taskStartTime The start time of the load
         * @param startTime The start time of the load, seconds from start of
         * experiment
         * @param endTime The end time of the load, seconds from start of
         * experiment
         */
        private void setLatestStartAndEndTimes(GregorianCalendar taskStartTime, long startTime, long endTime) {
            //Calculated the start time
            latestMesurementStartTime = (GregorianCalendar) taskStartTime.clone();
            latestMesurementStartTime.setTimeInMillis(latestMesurementStartTime.getTimeInMillis() + TimeUnit.SECONDS.toMillis(gapBeforeMeasurements));
            //calculate the end time
            if (endTime == -1) {
                latestMesurementStartTime = null;
            } else {
                latestMesurementEndTime = (GregorianCalendar) taskStartTime.clone();
                //find the end time
                long lastMeasureTime = latestMesurementEndTime.getTimeInMillis()
                        + TimeUnit.SECONDS.toMillis(endTime - startTime) //find the duration
                        - TimeUnit.SECONDS.toMillis(gapBeforeMeasurements);  //find the time a few seconds before that stops the measurements            

                latestMesurementEndTime.setTimeInMillis(lastMeasureTime);
            }
        }

        /**
         * This makes the actioner go through the action queue for actions to
         * perform.
         */
        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            Logger.getLogger(Actioner.class.getName()).log(Level.INFO, "FILE LOCATION: {0}", new File(workingDir + TO_EXECUTE_SETTINGS_FILE).getAbsolutePath());
            Logger.getLogger(Actioner.class.getName()).log(Level.INFO, "Actioner:Starting Thread");
            while (!stop) {
                try {
                    if (commandSet.isEmpty() && counter == 0) {
                        //No more work is to be done thus can terminate.
                        stop = true;
                        continue;
                    }
                    Logger.getLogger(Actioner.class.getName()).log(Level.FINE, "Actioner: current State is not empty");
                    //An action is present check if it is to be fired yet!!

                    ManagedProcessCommandData head = null;
                    if (commandSet.size() > 0) {
                        head = commandSet.get(0);
                    }
                    Logger.getLogger(Actioner.class.getName()).log(Level.FINE, "Actioner: Head null status = {0}", (head != null));

                    if (head != null && new GregorianCalendar().after(head.getActualStart())) {
                        Logger.getLogger(Actioner.class.getName()).log(Level.FINE, "Actioner: Executing: The heads type was {0}", head.getClass());
                        commandSet.remove(0);
                        setLatestStartAndEndTimes(new GregorianCalendar(), head.getStartTime(), head.getEndTime());
                        execute(head);
                    }
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(pollInterval));

                    } catch (InterruptedException ex) {
                        Logger.getLogger(Actioner.class.getName()).log(Level.WARNING, "Actioner: InterruptedException", ex);
                        if (sender != null) {
                            sender.finished();
                        }
                    }
                    //Measurements should only be taken with an application running
                    if (counter >= 1 && shouldTakeMeasurement()) {
                        //Take measurements if a process is currently running
                        HostMeasurement measurement = readEnergyDataForHost(host);
                        if (measurement != null) {
                            measurements.add(measurement);
                        }
                    } else {
                        if (logging == true) {
                            HostMeasurement dataEntry = datasource.getHostData(host);
                            if (counter == 0) {
                                printDataValueToFile(dataEntry, "No Stress Load Induced");
                            } else {
                                printDataValueToFile(dataEntry, "Not in Measurement Period");
                            }
                        }
                    }
                } catch (Exception ex) { //outer most try statement
                    Logger.getLogger(Actioner.class.getName()).log(Level.SEVERE, "Actioner: Exception", ex);
                    if (sender != null) {
                        sender.finished();
                    }
                } //outer most try statement
            } //While not stopped
            Logger.getLogger(Actioner.class.getName()).log(Level.INFO, "Actioner: Exiting Thread");
            actioner = null;
            ArrayList<HostEnergyCalibrationData> data = setHostCalibrationData(measurements, host);
            System.out.println("Data Items saved: " + data.size());
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(20));
                if (data.size() > 350) {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(10));
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Actioner.class.getName()).log(Level.WARNING, "Waiting at the end for DB writes was interupted", ex);
            }
            if (sender != null) {
                sender.finished();
            }
        }
    }

}
