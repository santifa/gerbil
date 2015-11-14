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

import org.aksw.gerbil.annotator.*;
import org.aksw.gerbil.annotator.decorator.ErrorCountingAnnotatorDecorator;
import org.aksw.gerbil.annotator.decorator.TimeMeasuringAnnotatorDecorator;
import org.aksw.gerbil.database.ExperimentDAO;
import org.aksw.gerbil.database.ResultNameToIdMapping;
import org.aksw.gerbil.dataset.Dataset;
import org.aksw.gerbil.datatypes.ErrorTypes;
import org.aksw.gerbil.datatypes.ExperimentTaskConfiguration;
import org.aksw.gerbil.datatypes.ExperimentTaskResult;
import org.aksw.gerbil.datatypes.ExperimentTaskState;
import org.aksw.gerbil.evaluate.*;
import org.aksw.gerbil.evaluate.impl.FMeasureCalculator;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.filter.EntityFilter;
import org.aksw.gerbil.semantic.sameas.DatasetBasedSameAsRetriever;
import org.aksw.gerbil.semantic.sameas.MultipleSameAsRetriever;
import org.aksw.gerbil.semantic.sameas.SameAsRetriever;
import org.aksw.gerbil.transfer.nif.*;
import org.aksw.gerbil.transfer.nif.data.TypedNamedEntity;
import org.aksw.simba.topicmodeling.concurrent.tasks.Task;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a single experiment designed as {@link Task} to be able to run
 * several tasks in parallel.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class ExperimentTask implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentTask.class);

    private ExperimentDAO experimentDAO;
    private ExperimentTaskConfiguration configuration;
    private int experimentTaskId;
    private EvaluatorFactory evFactory;
    private ExperimentTaskState taskState = null;
    private AnnotatorOutputWriter annotatorOutputWriter = null;
    private SameAsRetriever globalRetriever = null;
    private EntityFilter filter = null;

    //TODO think about a filter per task
    public ExperimentTask(int experimentTaskId, ExperimentDAO experimentDAO, SameAsRetriever globalRetriever,
                          EvaluatorFactory evFactory, ExperimentTaskConfiguration configuration, EntityFilter filter) {
        this.experimentDAO = experimentDAO;
        this.configuration = configuration;
        this.experimentTaskId = experimentTaskId;
        this.evFactory = evFactory;
        this.globalRetriever = globalRetriever;
        this.filter = filter;
    }

    @Override
    public void run() {
        LOGGER.info("Task started " + configuration.toString());
        Annotator annotator = null;
        try {
            // Create dataset
            Dataset dataset = configuration.datasetConfig.getDataset(configuration.type);
            if (dataset == null) {
                throw new GerbilException("dataset=\"" + configuration.datasetConfig.getName() + "\" experimentType=\""
                        + configuration.type.name() + "\".", ErrorTypes.DATASET_DOES_NOT_SUPPORT_EXPERIMENT);
            }

            // Create annotator
            annotator = configuration.annotatorConfig.getAnnotator(configuration.type);
            if (annotator == null) {
                throw new GerbilException("annotator=\"" + configuration.annotatorConfig.getName()
                        + "\" experimentType=\"" + configuration.type.name() + "\".",
                        ErrorTypes.ANNOTATOR_DOES_NOT_SUPPORT_EXPERIMENT);
            }
            Annotator decoratedAnnotator = annotator;
            // Add decroatoring evaluators
            TimeMeasuringAnnotatorDecorator timeMeasurer = TimeMeasuringAnnotatorDecorator
                    .createDecorator(configuration.type, decoratedAnnotator);
            decoratedAnnotator = timeMeasurer;
            ErrorCountingAnnotatorDecorator errorCounter = ErrorCountingAnnotatorDecorator
                    .createDecorator(configuration.type, decoratedAnnotator, dataset.size());
            decoratedAnnotator = errorCounter;

            List<Evaluator<?>> evaluators = new ArrayList<Evaluator<?>>();
            evFactory.addEvaluators(evaluators, configuration, dataset);
            evaluators.add(timeMeasurer);
            evaluators.add(errorCounter);

            // Prepare dataset for the experiment
            prepareDataset(dataset);

            taskState = new ExperimentTaskState(dataset.size());
            // perform experiment
            EvaluationResult result = runExperiment(dataset, decoratedAnnotator, evaluators, taskState);

            // create result object
            // FIXME Fix this workaround
            ExperimentTaskResult expResult = new ExperimentTaskResult(configuration, new double[6],
                    ExperimentDAO.TASK_FINISHED, 0);
            transformResults(result, expResult);

            // store result
            experimentDAO.setExperimentTaskResult(experimentTaskId, expResult);
            LOGGER.info("Task Finished " + configuration.toString());
        } catch (GerbilException e) {
            LOGGER.error("Got an error while running the task. Storing the error code in the db...", e);
            // store error
            experimentDAO.setExperimentState(experimentTaskId, e.getErrorType().getErrorCode());
        } catch (Exception e) {
            LOGGER.error("Error while trying to execute experiment.", e);
        } finally {
            IOUtils.closeQuietly(annotator);
        }
    }

    /**
     * Prepares the given dataset for the experiment, i.e., performs a sameAs
     * retrieval if it is needed for the experiment type.
     * 
     * @param dataset
     */
    @SuppressWarnings("deprecation")
    protected void prepareDataset(Dataset dataset) {
        switch (configuration.type) {
        case A2KB:// falls through
        case C2KB:
        case D2KB:
        case Rc2KB:
        case Sa2KB:
        case Sc2KB:
        case OKE_Task1: // falls through
        case OKE_Task2:
        case ETyping: {
            SameAsRetriever retriever = DatasetBasedSameAsRetriever.create(dataset);
            if (retriever != null) {
                if (globalRetriever != null) {
                    retriever = new MultipleSameAsRetriever(retriever, globalRetriever);
                }
            } else {
                retriever = globalRetriever;
            }
            if (retriever != null) {
                for (Document document : dataset.getInstances()) {
                    for (Meaning meaning : document.getMarkings(Meaning.class)) {
                        retriever.addSameURIs(meaning.getUris());
                    }
                }
            }
            return;
        }
        case ERec:// falls through
        default:
            // nothing to do
            return;
        }
    }

    /**
     * Prepares the given annotator results for the evaluation, i.e., performs a
     * sameAs retrieval if it is needed for the experiment type.
     *
     */
    @SuppressWarnings("deprecation")
    protected void prepareAnnotatorResults(List<? extends List<? extends Meaning>> results,
            SameAsRetriever annotatorSameAsRetriever) {
        switch (configuration.type) {
        case A2KB:// falls through
        case C2KB:
        case D2KB:
        case Rc2KB:
        case Sa2KB:
        case Sc2KB:
        case OKE_Task1: // falls through
        case OKE_Task2:
        case ETyping: {
            if (annotatorSameAsRetriever != null) {
                for (List<? extends Meaning> result : results) {
                    for (Meaning meaning : result) {
                        annotatorSameAsRetriever.addSameURIs(meaning.getUris());
                    }
                }
            }
            return;
        }
        case ERec:// falls through
        default:
            // nothing to do
            return;
        }
    }

    protected void transformResults(EvaluationResult result, ExperimentTaskResult expResult) {
        if (result instanceof SubTaskResult) {
            ExperimentTaskResult subTask = new ExperimentTaskResult(((SubTaskResult) result).getConfiguration(),
                    new double[6], ExperimentDAO.TASK_FINISHED, 0);
            List<EvaluationResult> tempResults = ((EvaluationResultContainer) result).getResults();
            for (EvaluationResult tempResult : tempResults) {
                transformResults(tempResult, subTask);
            }
            expResult.addSubTask(subTask);
        } else if (result instanceof EvaluationResultContainer) {
            List<EvaluationResult> tempResults = ((EvaluationResultContainer) result).getResults();
            for (EvaluationResult tempResult : tempResults) {
                transformResults(tempResult, expResult);
            }
        } else if (result instanceof DoubleEvaluationResult) {
            switch (result.getName()) {
            case FMeasureCalculator.MACRO_F1_SCORE_NAME: {
                expResult.results[ExperimentTaskResult.MACRO_F1_MEASURE_INDEX] = ((DoubleEvaluationResult) result)
                        .getValueAsDouble();
                return;
            }
            case FMeasureCalculator.MACRO_PRECISION_NAME: {
                expResult.results[ExperimentTaskResult.MACRO_PRECISION_INDEX] = ((DoubleEvaluationResult) result)
                        .getValueAsDouble();
                return;
            }
            case FMeasureCalculator.MACRO_RECALL_NAME: {
                expResult.results[ExperimentTaskResult.MACRO_RECALL_INDEX] = ((DoubleEvaluationResult) result)
                        .getValueAsDouble();
                return;
            }
            case FMeasureCalculator.MICRO_F1_SCORE_NAME: {
                expResult.results[ExperimentTaskResult.MICRO_F1_MEASURE_INDEX] = ((DoubleEvaluationResult) result)
                        .getValueAsDouble();
                return;
            }
            case FMeasureCalculator.MICRO_PRECISION_NAME: {
                expResult.results[ExperimentTaskResult.MICRO_PRECISION_INDEX] = ((DoubleEvaluationResult) result)
                        .getValueAsDouble();
                return;
            }
            case FMeasureCalculator.MICRO_RECALL_NAME: {
                expResult.results[ExperimentTaskResult.MICRO_RECALL_INDEX] = ((DoubleEvaluationResult) result)
                        .getValueAsDouble();
                return;
            }
            default: {
                int id = ResultNameToIdMapping.getInstance().getResultId(result.getName());
                if (id == ResultNameToIdMapping.UKNOWN_RESULT_TYPE) {
                    LOGGER.error("Got an unknown additional result \"" + result.getName() + "\". Discarding it.");
                } else {
                    expResult.addAdditionalResult(id, ((DoubleEvaluationResult) result).getValueAsDouble());
                }
            }
            }
            return;
        } else if (result instanceof IntEvaluationResult) {
            if (result.getName().equals(ErrorCountingAnnotatorDecorator.ERROR_COUNT_RESULT_NAME)) {
                expResult.errorCount = ((IntEvaluationResult) result).getValueAsInt();
                return;
            }
            int id = ResultNameToIdMapping.getInstance().getResultId(result.getName());
            if (id == ResultNameToIdMapping.UKNOWN_RESULT_TYPE) {
                LOGGER.error("Got an unknown additional result \"" + result.getName() + "\". Discarding it.");
            } else {
                expResult.addAdditionalResult(id, ((IntEvaluationResult) result).getValueAsInt());
            }
        }
    }

    @SuppressWarnings({ "deprecation" })
    protected EvaluationResult runExperiment(Dataset dataset, Annotator annotator,
            List<Evaluator<? extends Marking>> evaluators, ExperimentTaskState state) throws GerbilException {
        EvaluationResult evalResult = null;
        switch (configuration.type) {
        case D2KB: {
            try {
                List<List<MeaningSpan>> results = new ArrayList<List<MeaningSpan>>(dataset.size());
                List<List<MeaningSpan>> goldStandard = new ArrayList<List<MeaningSpan>>(dataset.size());
                D2KBAnnotator linker = ((D2KBAnnotator) annotator);

                //TODO insert fix filter loop
                for (Document document : dataset.getInstances()) {
                    // reduce the document to a text and a list of Spans
                    results.add(linker.performD2KBTask(DocumentInformationReducer.reduceToTextAndSpans(document)));
                    goldStandard.add(document.getMarkings(MeaningSpan.class));
                    taskState.increaseExperimentStepCount();
                }
                if (annotatorOutputWriter != null) {
                    annotatorOutputWriter.storeAnnotatorOutput(configuration, results, dataset.getInstances());
                }
                prepareAnnotatorResults(results, globalRetriever);
                evalResult = evaluate(evaluators, results, goldStandard);
            } catch (GerbilException e) {
                throw e;
            } catch (Exception e) {
                throw new GerbilException(e, ErrorTypes.UNEXPECTED_EXCEPTION);
            }
            break;
        }
        case Sa2KB:
        case A2KB: {
            try {
                List<List<MeaningSpan>> results = new ArrayList<List<MeaningSpan>>(dataset.size());
                List<List<MeaningSpan>> goldStandard = new ArrayList<List<MeaningSpan>>(dataset.size());
                A2KBAnnotator extractor = ((A2KBAnnotator) annotator);
                for (Document document : dataset.getInstances()) {
                    // reduce the document to a single text
                    results.add(extractor.performA2KBTask(DocumentInformationReducer.reduceToPlainText(document)));
                    goldStandard.add(document.getMarkings(MeaningSpan.class));
                    taskState.increaseExperimentStepCount();
                }
                if (annotatorOutputWriter != null) {
                    annotatorOutputWriter.storeAnnotatorOutput(configuration, results, dataset.getInstances());
                }
                prepareAnnotatorResults(results, globalRetriever);
                evalResult = evaluate(evaluators, results, goldStandard);
            } catch (GerbilException e) {
                throw e;
            } catch (Exception e) {
                throw new GerbilException(e, ErrorTypes.UNEXPECTED_EXCEPTION);
            }
            break;
        }
        case C2KB: {
            try {
                List<List<Meaning>> results = new ArrayList<List<Meaning>>(dataset.size());
                List<List<Meaning>> goldStandard = new ArrayList<List<Meaning>>(dataset.size());
                C2KBAnnotator c2KBAnnotator = ((C2KBAnnotator) annotator);

                for (Document document : dataset.getInstances()) {
                    // reduce the document to a text and a list of Spans
                    results.add(c2KBAnnotator.performC2KB(DocumentInformationReducer.reduceToPlainText(document)));
                    goldStandard.add(document.getMarkings(Meaning.class));
                    taskState.increaseExperimentStepCount();
                }
                if (annotatorOutputWriter != null) {
                    annotatorOutputWriter.storeAnnotatorOutput(configuration, results, dataset.getInstances());
                }
                prepareAnnotatorResults(results, globalRetriever);
                evalResult = evaluate(evaluators, results, goldStandard);
            } catch (GerbilException e) {
                throw e;
            } catch (Exception e) {
                throw new GerbilException(e, ErrorTypes.UNEXPECTED_EXCEPTION);
            }
            break;
        }
        case Sc2KB: // Falls through
        case Rc2KB: {
            throw new GerbilException(ErrorTypes.UNEXPECTED_EXCEPTION);
        }
        case ERec: {
            try {
                List<List<Span>> results = new ArrayList<List<Span>>(dataset.size());
                List<List<Span>> goldStandard = new ArrayList<List<Span>>(dataset.size());
                EntityRecognizer recognizer = ((EntityRecognizer) annotator);
                for (Document document : dataset.getInstances()) {
                    // reduce the document to a single text
                    results.add(recognizer.performRecognition(DocumentInformationReducer.reduceToPlainText(document)));
                    goldStandard.add(document.getMarkings(Span.class));
                    taskState.increaseExperimentStepCount();
                }
                if (annotatorOutputWriter != null) {
                    annotatorOutputWriter.storeAnnotatorOutput(configuration, results, dataset.getInstances());
                }
                evalResult = evaluate(evaluators, results, goldStandard);
            } catch (GerbilException e) {
                throw e;
            } catch (Exception e) {
                throw new GerbilException(e, ErrorTypes.UNEXPECTED_EXCEPTION);
            }
            break;
        }
        case ETyping: {
            try {
                List<List<TypedSpan>> results = new ArrayList<List<TypedSpan>>(dataset.size());
                List<List<TypedSpan>> goldStandard = new ArrayList<List<TypedSpan>>(dataset.size());
                EntityTyper typer = ((EntityTyper) annotator);

                for (Document document : dataset.getInstances()) {
                    // reduce the document to a text and a list of Spans
                    results.add(typer.performTyping(DocumentInformationReducer.reduceToTextAndSpans(document)));
                    goldStandard.add(document.getMarkings(TypedSpan.class));
                    taskState.increaseExperimentStepCount();
                }
                if (annotatorOutputWriter != null) {
                    annotatorOutputWriter.storeAnnotatorOutput(configuration, results, dataset.getInstances());
                }
                evalResult = evaluate(evaluators, results, goldStandard);
            } catch (GerbilException e) {
                throw e;
            } catch (Exception e) {
                throw new GerbilException(e, ErrorTypes.UNEXPECTED_EXCEPTION);
            }
            break;
        }
        case OKE_Task1: {
            try {
                List<List<TypedNamedEntity>> results = new ArrayList<List<TypedNamedEntity>>(dataset.size());
                List<List<TypedNamedEntity>> goldStandard = new ArrayList<List<TypedNamedEntity>>(dataset.size());
                OKETask1Annotator okeTask1Annotator = ((OKETask1Annotator) annotator);

                for (Document document : dataset.getInstances()) {
                    // reduce the document to a text and a list of Spans
                    results.add(
                            okeTask1Annotator.performTask1(DocumentInformationReducer.reduceToTextAndSpans(document)));
                    goldStandard.add(document.getMarkings(TypedNamedEntity.class));
                    taskState.increaseExperimentStepCount();
                }
                if (annotatorOutputWriter != null) {
                    annotatorOutputWriter.storeAnnotatorOutput(configuration, results, dataset.getInstances());
                }
                prepareAnnotatorResults(results, globalRetriever);
                evalResult = evaluate(evaluators, results, goldStandard);
            } catch (GerbilException e) {
                throw e;
            } catch (Exception e) {
                throw new GerbilException(e, ErrorTypes.UNEXPECTED_EXCEPTION);
            }
            break;
        }
        case OKE_Task2: {
            try {
                List<List<TypedNamedEntity>> results = new ArrayList<List<TypedNamedEntity>>(dataset.size());
                List<List<TypedNamedEntity>> goldStandard = new ArrayList<List<TypedNamedEntity>>(dataset.size());
                OKETask2Annotator okeTask2Annotator = ((OKETask2Annotator) annotator);

                for (Document document : dataset.getInstances()) {
                    // reduce the document to a text and a list of Spans
                    results.add(okeTask2Annotator
                            .performTask2(DocumentInformationReducer.reduceToTextAndEntities(document)));
                    goldStandard.add(document.getMarkings(TypedNamedEntity.class));
                    taskState.increaseExperimentStepCount();
                }
                if (annotatorOutputWriter != null) {
                    annotatorOutputWriter.storeAnnotatorOutput(configuration, results, dataset.getInstances());
                }
                prepareAnnotatorResults(results, globalRetriever);
                evalResult = evaluate(evaluators, results, goldStandard);
            } catch (GerbilException e) {
                throw e;
            } catch (Exception e) {
                throw new GerbilException(e, ErrorTypes.UNEXPECTED_EXCEPTION);
            }
            break;
        }
        default:
            throw new GerbilException("This experiment type isn't implemented yet. Sorry for this.",
                    ErrorTypes.UNEXPECTED_EXCEPTION);
        }
        return evalResult;

    }

    @SuppressWarnings("unchecked")
    protected <T extends Marking> EvaluationResult evaluate(List<Evaluator<? extends Marking>> evaluators,
            List<List<T>> annotatorResults, List<List<T>> goldStandard) {
        EvaluationResultContainer evalResults = new EvaluationResultContainer();
        for (Evaluator<? extends Marking> e : evaluators) {
            ((Evaluator<T>) e).evaluate(annotatorResults, goldStandard, evalResults);
        }
        return evalResults;
    }

    @Override
    public String getId() {
        return configuration.toString();
    }

    @Override
    public String getProgress() {
        if (taskState != null) {
            return (taskState.getExperimentTaskProcess() * 100.0) + "% of dataset";
        } else {
            return null;
        }
    }

    public void setAnnotatorOutputWriter(AnnotatorOutputWriter annotatorOutputWriter) {
        this.annotatorOutputWriter = annotatorOutputWriter;
    }
}
