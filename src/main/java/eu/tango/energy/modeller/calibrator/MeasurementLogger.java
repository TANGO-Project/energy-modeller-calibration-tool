/**
 * Copyright 2014 University of Leeds
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

import eu.ascetic.ioutils.io.GenericLogger;
import eu.ascetic.ioutils.io.ResultsStore;
import eu.tango.energymodeller.datasourceclient.Measurement;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

/**
 * This measurement logger writes out data taken during calibration to disk.
 * @author Richard Kavanagh
 */
public class MeasurementLogger extends GenericLogger<Measurement> {

    ArrayList<String> metricNames = new ArrayList<>();

    public MeasurementLogger(File file, boolean overwrite) {
        super(file, overwrite);
        saveFile.setDelimeter("\t");
    }

    /**
     * This writes a measurement out to disk for the purpose of auditing what is
     * going on. It by default overwrites the previous file
     *
     * @param store The results store to save data to
     * @param measurements The measurement to write to file
     */
    @Override
    public void saveToDisk(ResultsStore store, Collection<Measurement> measurements) {
        try {
            if ((!store.getResultsFile().exists() || metricNames.isEmpty()) && (!measurements.isEmpty())) {
                /**
                 * Write out the header if the file does not exist or is if the
                 * file is being appended to for the first time. i.e. the headers
                 * may be in a different order.
                 * The list of headers comes from the measurements collection so make sure
                 * it is also not empty.
                 */
                    Measurement measurement = (Measurement) (measurements.toArray())[0];
                    metricNames.addAll(measurement.getMetricNameList());
                    writeHeader(store);

            }
            for (Measurement measurement : measurements) {
                writebody(measurement, store);
                store.saveMemoryConservative();
            }
        } catch (Exception ex) {
            //logging is important but should not stop the main thread from running!
            java.util.logging.Logger.getLogger(GenericLogger.class.getName()).log(Level.SEVERE, "An error occurred when saving an item to disk", ex);
        }
    }

    @Override
    public void writeHeader(ResultsStore store) {
        store.add("Time Stamp Data");
        for (String name : metricNames) {
            store.append(name);
        }
    }

    @Override
    public void writebody(Measurement item, ResultsStore store) {
        store.add(item.getClock());
        for (String name : metricNames) {
            store.append(item.getMetric(name).getValueAsString());
        }
    }
}
