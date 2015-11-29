package org.aksw.gerbil.filter;

import org.aksw.gerbil.filter.impl.CacheFilter;
import org.aksw.gerbil.filter.impl.SparqlFilter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the {@link SparqlFilter}
 *
 * Created by Henrik Jürges on 08.11.15.
 */
public class SparqlFilterTest {

    private final String cacheLocation = "/tmp/filter";

    private final String service = "http://dbpedia.org/sparql";

    private final String prefix = "foaf:<http://xmlns.com/foaf/0.1/>";

    private final List<String> entitiesPerson = Arrays.asList("http://dbpedia.org/resource/Victoria_Beckham",
            "http://dbpedia.org/resource/John_P._Kennedy");

    private final FilterDefinition conf1 = new FilterDefinition("person-filter",
            "select ?v where { values ?v {##} . ?v a foaf:Person . }", new ArrayList<String>(), service);

    private final String annotatorName = "testAnno";

    private final String datasetName = "testData";

    @Test
    public void testResolveEntitiesAnnotator() throws Exception {
       Filter resolution = new SparqlFilter(conf1, new String[] { prefix });
       resolution = new CacheFilter(resolution, cacheLocation);
       List<String> result = resolution.resolveEntities(entitiesPerson, datasetName, annotatorName);
        assertEquals(entitiesPerson, result);
    }

    @Test
    public void testResolveEntitiesGoldstandard() throws Exception {
        Filter resolution = new SparqlFilter(conf1, new String[] { prefix });
        resolution = new CacheFilter(resolution, cacheLocation);
        List<String> result = resolution.resolveEntities(entitiesPerson, datasetName);
        assertTrue(result.size() == 2);
    }
}