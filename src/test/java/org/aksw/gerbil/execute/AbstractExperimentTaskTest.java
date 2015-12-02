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
package org.aksw.gerbil.execute;

import org.aksw.gerbil.database.ExperimentDAO;
import org.aksw.gerbil.database.SimpleLoggingResultStoringDAO4Debugging;
import org.aksw.gerbil.datatypes.ExperimentTaskConfiguration;
import org.aksw.gerbil.datatypes.ExperimentTaskResult;
import org.aksw.gerbil.evaluate.EvaluatorFactory;
import org.aksw.gerbil.filter.FilterWrapper;
import org.aksw.gerbil.filter.FilterHolder;
import org.aksw.gerbil.filter.wrapper.IdentityWrapper;
import org.aksw.gerbil.semantic.sameas.SameAsRetriever;
import org.aksw.gerbil.web.config.RootConfig;
import org.aksw.simba.topicmodeling.concurrent.overseers.Overseer;
import org.aksw.simba.topicmodeling.concurrent.overseers.simple.SimpleOverseer;
import org.aksw.simba.topicmodeling.concurrent.reporter.LogReporter;
import org.aksw.simba.topicmodeling.concurrent.reporter.Reporter;
import org.aksw.simba.topicmodeling.concurrent.tasks.Task;
import org.aksw.simba.topicmodeling.concurrent.tasks.TaskObserver;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public abstract class AbstractExperimentTaskTest {

    private Throwable testError = null;
    private Semaphore mutex = new Semaphore(0);

    public void runTest(int experimentTaskId, ExperimentDAO experimentDAO, EvaluatorFactory evFactory,
            ExperimentTaskConfiguration configuration, TaskObserver observer) {
        runTest(experimentTaskId, experimentDAO, evFactory, configuration,
                observer, RootConfig.createSameAsRetriever());
    }

    public void runTest(int experimentTaskId, ExperimentDAO experimentDAO, EvaluatorFactory evFactory,
                        ExperimentTaskConfiguration configuration, TaskObserver observer, SameAsRetriever sameAsRetriever) {
        List<FilterWrapper> filter = new ArrayList<>();
        filter.add(new IdentityWrapper());
        FilterHolder holder = new FilterHolder(filter, false);
        Map<ExperimentTaskConfiguration, Integer> filterTask = new HashMap<>();
        filterTask.put(configuration, experimentTaskId);
        runTest(experimentTaskId, experimentDAO, sameAsRetriever, evFactory, configuration, observer, holder, filterTask);

    }

        public void runTest(int experimentTaskId, ExperimentDAO experimentDAO, SameAsRetriever sameAsRetriever,
            EvaluatorFactory evFactory, ExperimentTaskConfiguration configuration, TaskObserver observer,
                            FilterHolder holder, Map<ExperimentTaskConfiguration, Integer> filterTask) {

        ExperimentTask task = new ExperimentTask(experimentTaskId, experimentDAO, sameAsRetriever, evFactory,
                configuration, holder, filterTask);
        Overseer overseer = new SimpleOverseer();
        overseer.addObserver(observer);
        @SuppressWarnings("unused")
        Reporter reporter = new LogReporter(overseer);
        overseer.startTask(task);
        // wait for the task to end
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertNull("Got an exception: " + testError, testError);
    }

    protected static abstract class AbstractJUnitTestTaskObserver implements TaskObserver {

        private static final Logger LOGGER = LoggerFactory
                .getLogger(AbstractExperimentTaskTest.AbstractJUnitTestTaskObserver.class);

        private AbstractExperimentTaskTest testInstance;

        public AbstractJUnitTestTaskObserver(AbstractExperimentTaskTest testInstance) {
            this.testInstance = testInstance;
        }

        @Override
        public void reportTaskThrowedException(Task task, Throwable t) {
            testInstance.testError = t;
            LOGGER.error("Got an unexpected exception.", t);
            // If there was an error we have to release the mutex here
            testInstance.mutex.release();
        }

        @Override
        public void reportTaskFinished(Task task) {
            testTaskResults(task);
            // If there was no error we have to release the mutex here
            testInstance.mutex.release();
        }

        protected abstract void testTaskResults(Task task);

    }

    protected static class F1MeasureTestingObserver extends AbstractJUnitTestTaskObserver {

        public static int MACRO_PREC_INDEX = 0;
        public static int MACRO_REC_INDEX = 1;
        public static int MACRO_F1_INDEX = 2;
        public static int MICRO_PREC_INDEX = 3;
        public static int MICRO_REC_INDEX = 4;
        public static int MICRO_F1_INDEX = 5;
        public static int ERROR_COUNT_INDEX = 6;

        private static final double DELTA = 0.0000001;

        private int experimentTaskId;
        private SimpleLoggingResultStoringDAO4Debugging experimentDAO;
        private double expectedResults[];

        public F1MeasureTestingObserver(AbstractExperimentTaskTest testInstance, int experimentTaskId,
                SimpleLoggingResultStoringDAO4Debugging experimentDAO, double expectedResults[]) {
            super(testInstance);
            this.experimentTaskId = experimentTaskId;
            this.experimentDAO = experimentDAO;
            this.expectedResults = expectedResults;
        }

        @Override
        protected void testTaskResults(Task task) {
            Assert.assertEquals(ExperimentDAO.TASK_FINISHED, experimentDAO.getExperimentState(experimentTaskId));
            ExperimentTaskResult result = experimentDAO.getTaskResult(experimentTaskId);
            Assert.assertEquals(expectedResults[MACRO_PREC_INDEX], result.getMacroPrecision(), DELTA);
            Assert.assertEquals(expectedResults[MACRO_REC_INDEX], result.getMacroRecall(), DELTA);
            Assert.assertEquals(expectedResults[MACRO_F1_INDEX], result.getMacroF1Measure(), DELTA);
            Assert.assertEquals(expectedResults[MICRO_PREC_INDEX], result.getMicroPrecision(), DELTA);
            Assert.assertEquals(expectedResults[MICRO_REC_INDEX], result.getMicroRecall(), DELTA);
            Assert.assertEquals(expectedResults[MICRO_F1_INDEX], result.getMicroF1Measure(), DELTA);
            Assert.assertEquals(expectedResults[ERROR_COUNT_INDEX], result.getErrorCount(), DELTA);
        }
    }
}
