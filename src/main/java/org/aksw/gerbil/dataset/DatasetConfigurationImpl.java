/**
 * This file is part of General Entity Annotator Benchmark.
 *
 * General Entity Annotator Benchmark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * General Entity Annotator Benchmark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with General Entity Annotator Benchmark.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.aksw.gerbil.dataset;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import org.aksw.gerbil.datatypes.AbstractAdapterConfiguration;
import org.aksw.gerbil.datatypes.ErrorTypes;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;

public class DatasetConfigurationImpl extends AbstractAdapterConfiguration implements DatasetConfiguration {

    protected Constructor<? extends Dataset> constructor;
    protected Object constructorArgs[];

    public DatasetConfigurationImpl(String datasetName, boolean couldBeCached,
            Constructor<? extends Dataset> constructor, Object constructorArgs[],
            ExperimentType applicableForExperiment) {
        super(datasetName, couldBeCached, applicableForExperiment);
        this.constructor = constructor;
        this.constructorArgs = constructorArgs;
    }

    @Override
    public Dataset getDataset(ExperimentType experimentType) throws GerbilException {
        // for (int i = 0; i < applicableForExperiments.length; ++i) {
        // if (applicableForExperiments[i].equalsOrContainsType(experimentType))
        if (applicableForExperiment.equalsOrContainsType(experimentType)) {
            try {
                return loadDataset();
            } catch (GerbilException e) {
                throw e;
            } catch (Exception e) {
                throw new GerbilException(e, ErrorTypes.DATASET_LOADING_ERROR);
            }
        }
        return null;
    }

    protected Dataset loadDataset() throws Exception {
        Dataset instance = constructor.newInstance(constructorArgs);
        instance.setName(this.getName());
        // If this dataset should be initialized
        if (instance instanceof InitializableDataset) {
            ((InitializableDataset) instance).init();
        }
        return instance;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(\"");
        builder.append(name);
        builder.append("\",cached=");
        builder.append(couldBeCached);
        // builder.append(",expTypes={");
        // for (int i = 0; i < applicableForExperiments.length; ++i) {
        // if (i > 0) {
        // builder.append(',');
        // }
        // builder.append(applicableForExperiments[i].name());
        // }
        builder.append(",expType={");
        builder.append(applicableForExperiment.name());
        builder.append("},constr.=");
        builder.append(constructor);
        builder.append(",args=");
        builder.append(Arrays.toString(constructorArgs));
        builder.append(')');
        return builder.toString();
    }
}