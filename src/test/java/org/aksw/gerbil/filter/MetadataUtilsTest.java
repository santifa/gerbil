package org.aksw.gerbil.filter;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.filter.impl.PopularityFilter;
import org.aksw.gerbil.filter.impl.SparqlFilter;
import org.aksw.gerbil.web.config.AdapterList;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the metadata creation.
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

        HashMap<String, Integer> expected = new HashMap<>(13);
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

        HashMap<String, Integer> result = reduceMap(utils.getAmountOfEntitiesPerFilter());
        assertTrue(expected.equals(result));
    }

    private HashMap<String, Integer> reduceMap(HashMap<String, HashMap<String, Integer>> result) {
        HashMap<String, Integer> reducedResult = new HashMap<>();

        for (String filter : result.keySet()) {
            HashMap<String, Integer> datasets = result.get(filter);
            reducedResult.put(filter, datasets.get("Kore50-reduced"));
        }
        return reducedResult;
    }
}