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
package org.aksw.gerbil.web;

import com.google.common.base.Splitter;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.aksw.gerbil.annotator.AnnotatorConfiguration;
import org.aksw.gerbil.database.ExperimentDAO;
import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.datatypes.ExperimentTaskResult;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.filter.FilterFactory;
import org.aksw.gerbil.filter.wrapper.IdentityWrapper;
import org.aksw.gerbil.matching.Matching;
import org.aksw.gerbil.utils.DatasetMetaData;
import org.aksw.gerbil.utils.DatasetMetaDataMapping;
import org.aksw.gerbil.utils.PearsonsSampleCorrelationCoefficient;
import org.aksw.gerbil.web.config.AdapterList;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Controller
public class ExperimentOverviewController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentOverviewController.class);

	private static final double NOT_AVAILABLE_SENTINAL = -2;
	private static final int MIN_NUMBER_OF_VALUES_FOR_CORR_CALC = 5;
	private static final String CORRELATION_TABLE_COLUMN_HEADINGS[] = { "number of documents", "avg. document length",
			"number of entities", "entities per document", "entities per token", "amount of persons",
			"amount of organizations", "amount of locations", "amount of others"/*
																				 * ,
																				 * "corr. based on # datasets"
																				 */};

	@Autowired
	@Qualifier("experimentDAO")
	private ExperimentDAO dao;

	@Autowired
	@Qualifier("annotators")
	private AdapterList<AnnotatorConfiguration> annotators;

	@Autowired
	@Qualifier("datasets")
	private AdapterList<DatasetConfiguration> datasets;

    @Autowired
    private FilterFactory filterFactory;

	@RequestMapping("/experimentoverview")
	public @ResponseBody String experimentoverview(@RequestParam(value = "experimentType") String experimentType,
			@RequestParam(value = "matching") String matchingString, @RequestParam(required = false, value = "filter") String filterName) {
		LOGGER.debug("Got request on /experimentoverview(experimentType={}, matching={}, filter={}", experimentType,
				matchingString, filterName);
		Matching matching = MainController.getMatching(matchingString);
		ExperimentType eType = ExperimentType.valueOf(experimentType);

		String annotatorNames[] = loadAnnotators(eType);
		String datasetNames[] = loadDatasets(eType);

        // prevent loading of wrong data; only the identity filter is applicable for this experiments
        if (isFilteredExperiment(filterName, eType)) {
            filterName = IdentityWrapper.CONF.getName();
        }

		double results[][] = loadLatestResults(eType, matching, annotatorNames, datasetNames, filterName);
		double correlations[][] = calculateCorrelations(results, datasetNames);
		return generateJson(results, correlations, annotatorNames, datasetNames);

	}

    @RequestMapping("/filtermetadata")
    public @ResponseBody String filtermetadata() {
        LOGGER.debug("Got request on /filtermetadata");
        File metadata = new File("gerbil_data/resources/filter/metadata");

        if (metadata.exists() && metadata.isFile()) {
            List<String> values = new ArrayList<>();

            try {
                BufferedReader reader = new BufferedReader(new FileReader(metadata));
                String line;
                while ((line = reader.readLine()) != null) {
                    values.add(line);
                }
                reader.close();

                return generateMetadataJson(filterFactory.getRegisteredFilterNames(), values);
            } catch (IOException e) {
                LOGGER.error("Could not fetch filter metadata; Returning empty metadata. " + e.getMessage(), e);
                return  generateMetadataJson(filterFactory.getRegisteredFilterNames(), new ArrayList<String>());
            }

        } else {
            return generateMetadataJson(filterFactory.getRegisteredFilterNames(), new ArrayList<String>());
        }
    }

    private boolean isFilteredExperiment(String filterName, ExperimentType eType) {
        return eType.equalsOrContainsType(ExperimentType.ERec)
                || eType.equalsOrContainsType(ExperimentType.ETyping)
                || eType.equalsOrContainsType(ExperimentType.OKE_Task1)
                || eType.equalsOrContainsType(ExperimentType.OKE_Task2)
                || StringUtils.isEmpty(filterName);
    }

    private double[][] loadLatestResults(ExperimentType experimentType, Matching matching, String[] annotatorNames,
			String[] datasetNames, String filterName) {
		Map<String, Integer> annotator2Index = new HashMap<String, Integer>();
		for (int i = 0; i < annotatorNames.length; ++i) {
			annotator2Index.put(annotatorNames[i], i);
		}
		Map<String, Integer> dataset2Index = new HashMap<String, Integer>();
		for (int i = 0; i < datasetNames.length; ++i) {
			dataset2Index.put(datasetNames[i], i);
		}

		List<ExperimentTaskResult> expResults = dao.getLatestResultsOfExperiments(experimentType.name(),
				matching.name(), filterName);
		double results[][] = new double[annotatorNames.length][datasetNames.length];
		for (int i = 0; i < results.length; ++i) {
			Arrays.fill(results[i], NOT_AVAILABLE_SENTINAL);
		}
		int row, col;
		for (ExperimentTaskResult result : expResults) {
			if (annotator2Index.containsKey(result.annotator) && dataset2Index.containsKey(result.dataset)) {
				row = annotator2Index.get(result.annotator);
				col = dataset2Index.get(result.dataset);
				if (result.state == ExperimentDAO.TASK_FINISHED) {
					results[row][col] = result.getMicroF1Measure();
				} else {
					results[row][col] = result.state;
				}
			}
		}
		return results;
	}

	private String[] loadAnnotators(ExperimentType eType) {
		Set<String> annotatorNames = annotators.getAdapterNamesForExperiment(eType);
		String annotatorNameArray[] = annotatorNames.toArray(new String[annotatorNames.size()]);
		Arrays.sort(annotatorNameArray);
		return annotatorNameArray;
	}

	private String[] loadDatasets(ExperimentType eType) {
		Set<String> datasetNames = datasets.getAdapterNamesForExperiment(eType);
		String datasetNameArray[] = datasetNames.toArray(new String[datasetNames.size()]);
		Arrays.sort(datasetNameArray);
		return datasetNameArray;
	}

	private double[][] calculateCorrelations(double[][] results, String datasetNames[]) {
		DatasetMetaDataMapping mapping = DatasetMetaDataMapping.getInstance();
		DatasetMetaData metadata[] = new DatasetMetaData[datasetNames.length];
		for (int i = 0; i < datasetNames.length; ++i) {
			metadata[i] = mapping.getMetaData(datasetNames[i]);
		}
		double correlations[][] = new double[results.length][CORRELATION_TABLE_COLUMN_HEADINGS.length];
		DoubleArrayList annotatorResults = new DoubleArrayList(datasetNames.length);
		DoubleArrayList numberOfDocuments = new DoubleArrayList(datasetNames.length);
		DoubleArrayList avgDocumentLength = new DoubleArrayList(datasetNames.length);
		DoubleArrayList numberOfEntities = new DoubleArrayList(datasetNames.length);
		DoubleArrayList entitiesPerDoc = new DoubleArrayList(datasetNames.length);
		DoubleArrayList entitiesPerToken = new DoubleArrayList(datasetNames.length);
		DoubleArrayList amountOfPersons = new DoubleArrayList(datasetNames.length);
		DoubleArrayList amountOfOrganizations = new DoubleArrayList(datasetNames.length);
		DoubleArrayList amountOfLocations = new DoubleArrayList(datasetNames.length);
		DoubleArrayList amountOfOthers = new DoubleArrayList(datasetNames.length);
		double annotatorResultsAsArray[];
		int elementCount;
		for (int i = 0; i < correlations.length; ++i) {
			Arrays.fill(correlations[i], NOT_AVAILABLE_SENTINAL);
			// load the values for this annotator
			annotatorResults.clear();
			numberOfDocuments.clear();
			avgDocumentLength.clear();
			numberOfEntities.clear();
			entitiesPerDoc.clear();
			entitiesPerToken.clear();
			amountOfPersons.clear();
			amountOfOrganizations.clear();
			amountOfLocations.clear();
			amountOfOthers.clear();
			for (int j = 0; j < results[i].length; ++j) {
				if ((metadata[j] != null) && (results[i][j] >= 0)) {
					annotatorResults.add(results[i][j]);
					numberOfDocuments.add(metadata[j].numberOfDocuments);
					avgDocumentLength.add(metadata[j].avgDocumentLength);
					numberOfEntities.add(metadata[j].numberOfEntities);
					entitiesPerDoc.add(metadata[j].entitiesPerDoc);
					entitiesPerToken.add(metadata[j].entitiesPerToken);
					amountOfPersons.add(metadata[j].amountOfPersons);
					amountOfOrganizations.add(metadata[j].amountOfOrganizations);
					amountOfLocations.add(metadata[j].amountOfLocations);
					amountOfOthers.add(metadata[j].amountOfOthers);
				}
			}
			// If we have enough datasets with metadata and results of the
			// current annotator for these datasets
			elementCount = annotatorResults.size();
			if (elementCount > MIN_NUMBER_OF_VALUES_FOR_CORR_CALC) {
				annotatorResultsAsArray = annotatorResults.toArray(new double[elementCount]);
				correlations[i][0] = PearsonsSampleCorrelationCoefficient.calculateRankCorrelation(
						annotatorResultsAsArray, numberOfDocuments.toArray(new double[elementCount]));
				correlations[i][1] = PearsonsSampleCorrelationCoefficient.calculateRankCorrelation(
						annotatorResultsAsArray, avgDocumentLength.toArray(new double[elementCount]));
				correlations[i][2] = PearsonsSampleCorrelationCoefficient.calculateRankCorrelation(
						annotatorResultsAsArray, numberOfEntities.toArray(new double[elementCount]));
				correlations[i][3] = PearsonsSampleCorrelationCoefficient.calculateRankCorrelation(
						annotatorResultsAsArray, entitiesPerDoc.toArray(new double[elementCount]));
				correlations[i][4] = PearsonsSampleCorrelationCoefficient.calculateRankCorrelation(
						annotatorResultsAsArray, entitiesPerToken.toArray(new double[elementCount]));
				correlations[i][5] = PearsonsSampleCorrelationCoefficient.calculateRankCorrelation(
						annotatorResultsAsArray, amountOfPersons.toArray(new double[elementCount]));
				correlations[i][6] = PearsonsSampleCorrelationCoefficient.calculateRankCorrelation(
						annotatorResultsAsArray, amountOfOrganizations.toArray(new double[elementCount]));
				correlations[i][7] = PearsonsSampleCorrelationCoefficient.calculateRankCorrelation(
						annotatorResultsAsArray, amountOfLocations.toArray(new double[elementCount]));
				correlations[i][8] = PearsonsSampleCorrelationCoefficient.calculateRankCorrelation(
						annotatorResultsAsArray, amountOfOthers.toArray(new double[elementCount]));
				// correlations[i][9] = annotatorResultsAsArray.length;
			}
		}

		return correlations;
	}

    private int computeAmountOfEntities(String filterName, List<String> values) {
        int result = 0;
        for (String value : values) {
            List<String> lst = Splitter.on(' ').splitToList(value);
            if (StringUtils.equalsIgnoreCase(filterName, lst.get(0).replaceAll("_", " "))) {
                result += Integer.valueOf(lst.get(2));
            }
        }
        return result;
    }

    private String getMetadataAsJson(String filterName, List<String> values) {
        StringBuilder datasets = new StringBuilder();
        StringBuilder amount = new StringBuilder();
        datasets.append('[');
        amount.append('[');

        String prefix = "";
        for (String value : values) {
            List<String> lst = Splitter.on(' ').splitToList(value);
            if (StringUtils.equalsIgnoreCase(filterName, lst.get(0).replaceAll("_", " "))) {
                datasets.append(prefix);
                amount.append(prefix);
                prefix = ",";
                datasets.append('"').append(lst.get(1)).append('"');
                amount.append((lst.get(2)));
            }
        }

        datasets.append(']');
        amount.append(']');
        return "\"datasets\": " + datasets.toString() + ",\n\"values\": " + amount.toString();
    }

    private String generateMetadataJson(String[] filterNames, List<String> values) {
        StringBuilder jsonBuilder = new StringBuilder();
        if (values.isEmpty()) {
            jsonBuilder.append('[');
            String prefix = "";
            for (String name : filterNames) {
                jsonBuilder.append(prefix);
                prefix = ",\n";
                jsonBuilder.append("{ \"filter\": ").append('"').append(name).append('"').append(",\n");
                jsonBuilder.append("\"amount\": 0,\n\"datasets\": [],\n\"values\": []\n}");
            }
            return jsonBuilder.append(']').toString();
        }

        jsonBuilder.append('[');
        String prefix = "";
        for (String name : filterNames) {
            jsonBuilder.append(prefix);
            prefix = ",\n";
            jsonBuilder.append("{ \"filter\": ").append('"').append(name).append('"').append(",\n");
            jsonBuilder.append("\"amount\": ").append(computeAmountOfEntities(name, values)).append(",\n");
            jsonBuilder.append(getMetadataAsJson(name, values));
            jsonBuilder.append("}");
        }
        return jsonBuilder.append(']').toString();
    }

	private String generateJson(double[][] results, double[][] correlations, String annotatorNames[],
			String datasetNames[]) {
		StringBuilder jsonBuilder = new StringBuilder();
		// jsonBuilder.append("results=");
		jsonBuilder.append('[');
		jsonBuilder.append(generateJSonTableString(results, datasetNames, annotatorNames, "Micro F1-measure"));
		jsonBuilder.append(',');
		jsonBuilder.append(generateJSonTableString(correlations, CORRELATION_TABLE_COLUMN_HEADINGS, annotatorNames,
				"Correlations"));
		jsonBuilder.append(']');
		return jsonBuilder.toString();
	}

	private String generateJSonTableString(double values[][], String columnHeadings[], String lineHeadings[],
			String tableName) {
		StringBuilder dataBuilder = new StringBuilder();
		dataBuilder.append("[[\"");
		dataBuilder.append(tableName);
		for (int i = 0; i < columnHeadings.length; ++i) {
			dataBuilder.append("\",\"");
			dataBuilder.append(columnHeadings[i]);
		}
		for (int i = 0; i < lineHeadings.length; ++i) {
			dataBuilder.append("\"],\n[\"");
			dataBuilder.append(lineHeadings[i]);
			for (int j = 0; j < columnHeadings.length; ++j) {
				dataBuilder.append("\",\"");
				// if this is a real result
				if (values[i][j] > NOT_AVAILABLE_SENTINAL) {
					dataBuilder.append(String.format(Locale.US, "%.3f", values[i][j]));
				} else {
					// if this value is simply missing
					if (values[i][j] == NOT_AVAILABLE_SENTINAL) {
						dataBuilder.append("n.a.");
					} else {
						// this is an error value
						dataBuilder.append("error (");
						dataBuilder.append((int) values[i][j]);
						dataBuilder.append(')');
					}
				}
			}
		}
		dataBuilder.append("\"]]");
		return dataBuilder.toString();
	}
}
