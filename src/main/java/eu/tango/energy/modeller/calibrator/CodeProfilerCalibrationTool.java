/**
 * Copyright 2018 University of Leeds
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
import eu.tango.energymodeller.datasourceclient.HostDataSource;
import eu.tango.energymodeller.datasourceclient.WattsUpMeterDataSourceAdaptor;
import eu.tango.energymodeller.types.energyuser.Host;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The aim of this is to induce load on the local host and measure the response.
 * The results will then be written into the Energy Modellers database
 *
 * @author Richard Kavanagh
 */
public class CodeProfilerCalibrationTool implements CompletedListener {

    private boolean working = false;
    private final Host host;
    private HostDataSource source;
    private CalibrationRunManager runManager;
    private static final String DEFAULT_DATA_SOURCE_PACKAGE = "eu.tango.energymodeller.datasourceclient";

    /**
     * Creates a calibration tool for the ASECTiC energy modeller
     *
     * @param hostname The host to calibrate
     */
    public CodeProfilerCalibrationTool(String hostname) {
        source = WattsUpMeterDataSourceAdaptor.getInstance();
        host = source.getHostByName(hostname);
    }

    /**
     * Creates a calibration tool for the ASECTiC energy modeller
     *
     * @param hostname The host to calibrate
     * @param datasource This allows the data source to be changed to a named
     * other from the default of ZabbixDirectDBDatasourceAdaptor.
     */
    public CodeProfilerCalibrationTool(String hostname, String datasource) {
        setDataSource(datasource);
        host = source.getHostByName(hostname);
    }

    /**
     * This performs the calibration of a host.
     *
     * @param args The first argument should be the host name after this there
     * are several optional arguments can be passed namely: halt-on-calibrated
     * which prevents a host from been re-calibrated. benchmark-only which
     * prevents calibration from running. and use-watts-up-meter which means a
     * watts up meter is used locally for measurements.
     *
     */
    public static void main(String[] args) {
        CodeProfilerCalibrationTool instance;
            ResourceExtractor.extractSigar();
            instance = new CodeProfilerCalibrationTool("localhost", 
                    DEFAULT_DATA_SOURCE_PACKAGE + ".WattsUpMeterDataSourceAdaptor");
            instance.induceLoad();
    }

    /**
     * This allows the data source to be set
     *
     * @param dataSource The name of the data source to use for the calibration
     */
    public final void setDataSource(String dataSource) {
        try {
            if (!dataSource.startsWith(DEFAULT_DATA_SOURCE_PACKAGE)) {
                dataSource = DEFAULT_DATA_SOURCE_PACKAGE + "." + dataSource;
            }
            /**
             * This is a special case that requires it to be loaded under the
             * singleton design pattern.
             */
            String wattsUpMeter = DEFAULT_DATA_SOURCE_PACKAGE + ".WattsUpMeterDataSourceAdaptor";
            if (wattsUpMeter.equals(dataSource)) {
                source = WattsUpMeterDataSourceAdaptor.getInstance();
            } else {
                source = (HostDataSource) (Class.forName(dataSource).newInstance());
            }
        } catch (ClassNotFoundException ex) {
            if (source == null) {
                source = WattsUpMeterDataSourceAdaptor.getInstance();
            }
            Logger.getLogger(StandaloneCalibrationTool.class.getName()).log(Level.WARNING, "The data source specified was not found");
        } catch (InstantiationException | IllegalAccessException ex) {
            if (source == null) {
                source = WattsUpMeterDataSourceAdaptor.getInstance();
            }
            Logger.getLogger(StandaloneCalibrationTool.class.getName()).log(Level.WARNING, "The data source did not work", ex);
        }
    }

    /**
     * This induces load on the physical host
     *
     * @return If the executor has finished or not.
     */
    public boolean induceLoad() {
        if (!working) {
            runManager = new CalibrationRunManager(this, source, null, host);
            working = true;
        }
        return working;
    }

    /**
     * This indicates if it has finished inducing load on the web server
     *
     * @return If the executor has finished or not.
     */
    public boolean currentlyWorking() {
        return working;
    }

    @Override
    public void finished() {
        working = false;
    }

}
