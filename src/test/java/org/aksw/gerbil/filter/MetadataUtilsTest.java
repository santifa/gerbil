package org.aksw.gerbil.filter;

import com.google.common.collect.Table;
import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.filter.impl.PopularityFilter;
import org.aksw.gerbil.filter.impl.SparqlFilter;
import org.aksw.gerbil.filter.wrapper.FilterWrapper;
import org.aksw.gerbil.web.config.AdapterList;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test parts of the metadata creation.
 * Mostly simply printouts, due to complexity of resulting data.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class MetadataUtilsTest {

    private static final DatasetConfiguration REDUCED_GLD_STD = new NIFFileDatasetConfig("Kore50-reduced",
            "src/test/resources/filter_example_data/kore50-reduced-nif.ttl", false, ExperimentType.A2KB);


    @Test
    public void testMetadataCreation() {
        AdapterList<DatasetConfiguration> configurations = new AdapterList<>(Collections.singletonList(REDUCED_GLD_STD));
        FilterFactory factory = new FilterFactory(false);
        factory.registerFilter(SparqlFilter.class, factory.getBasicFilterResolver());
        factory.registerFilter(PopularityFilter.class, factory.getPopularityFilterResolver());

        MetadataUtils utils = new MetadataUtils(configurations, factory.getFilters());
        assertEquals(14, utils.getAmountOfEntities());

        Map<String, Integer> expected = new HashMap<>(13);
        expected.put("Filter Persons", 9);
        expected.put("Filter Organizations", 1);
        expected.put("Filter Pagerank 10%-55%", 8);
        expected.put("Filter Hitsscore 55%-100%", 2);
        expected.put("nofilter", 14);
        expected.put("Filter Hitsscore 10%", 7);
        expected.put("Filter Places", 1);
        expected.put("Filter Pagerank 55%-100%", 3);
        expected.put("Filter Pagerank 10%", 3);
        expected.put("Filter Hitsscore 10%-55%", 5);

        Map<String, Integer> result = reduceMap(utils.getAmountOfEntitiesPerFilter());
        assertTrue(expected.equals(result));
    }

    @Test
    public void testDensityCreation() {
        AdapterList<DatasetConfiguration> configurations = new AdapterList<>(Collections.singletonList(REDUCED_GLD_STD));
        MetadataUtils utils = new MetadataUtils(configurations, new FilterHolder(new ArrayList<FilterWrapper>()));
        Map<String, Double> expected = new HashMap<>(3);
        expected.put("All Datasets", 0.2153846153846154);
        expected.put("Kore50-reduced", 0.2153846153846154);

        Map<String, Double> annotaionsPerWords = utils.getAnnotationsPerWord();
        assertTrue(expected.equals(annotaionsPerWords));
    }

    @Test
    public void testAmbiguityCreation() {
        AdapterList<DatasetConfiguration> configurations = new AdapterList<>(Collections.singletonList(REDUCED_GLD_STD));
        MetadataUtils utils = new MetadataUtils(configurations, new FilterHolder(new ArrayList<FilterWrapper>()));
        Table<String, String, Integer> ambiguityEntities = utils.getAmbiguityOfEntities();
        Table<String, String, Integer> ambiguitySurface = utils.getAmbiguityOfSurface();

        System.out.println("Entity Ambiguity");
        for (String row : ambiguityEntities.column("Kore50-reduced").keySet()) {
            System.out.println(row + ": " + ambiguityEntities.row(row));
        }

        System.out.println("Surface Form Ambiguity");
        for (String row : ambiguitySurface.column("Kore50-reduced").keySet()) {
            System.out.println(row + ": " + ambiguitySurface.row(row));
        }

        System.out.println(utils.getAmbiguityOfEntitiesAsJson());
        System.out.println(utils.getAmbiguityOfSurfaceAsJson());
    }

    private HashMap<String, Integer> reduceMap(Map<String, Map<String, Integer>> result) {
        HashMap<String, Integer> reducedResult = new HashMap<>();

        for (String filter : result.keySet()) {
            Map<String, Integer> datasets = result.get(filter);
            reducedResult.put(filter, datasets.get("Kore50-reduced"));
        }
        return reducedResult;
    }
}