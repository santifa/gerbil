package org.aksw.gerbil.filter;

import org.aksw.gerbil.filter.cache.FilterCache;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the {@link SparqlEntityResolution}
 *
 * Created by Henrik Jürges on 08.11.15.
 */
public class SparqlEntityResolutionTest {

    private final String cacheLocation = "/tmp/filter";

    private final String service = "http://dbpedia.org/sparql";

    private final String prefix = "foaf:<http://xmlns.com/foaf/0.1/>";

    private final String[] entitiesPerson = new String[] {"http://dbpedia.org/resource/Victoria_Beckham",
            "http://dbpedia.org/resource/John_P._Kennedy"};

    private final FilterConfiguration conf1 = new FilterConfiguration("person-filter", "select ?v where { values ?v {##} . ?v a foaf:Person . }");

    private final FilterConfiguration conf2 = new FilterConfiguration("person-agent", "select ?v where { values ?v {##} . ?v a foaf:Agent . }");

    private final String annotatorName = "testAnno";

    private final String datasetName = "testData";

    @Test
    public void testResolveEntitiesAnnotator() throws Exception {
       SparqlEntityResolution resolution = new SparqlEntityResolution(service);
       resolution.initCache(FilterCache.getInstance(cacheLocation));
       resolution.setPrefixSet(new String[] { prefix });
       String[] result = resolution.resolveEntities(entitiesPerson, conf1, datasetName, annotatorName);
        assertArrayEquals(entitiesPerson, result);
    }

    @Test
    public void testResolveEntitiesGoldstandard() throws Exception {
        SparqlEntityResolution resolution = new SparqlEntityResolution(service);
        resolution.initCache(FilterCache.getInstance(cacheLocation));
        resolution.setPrefixSet(new String[] { prefix });
        String[] result = resolution.resolveEntities(entitiesPerson, conf1, datasetName);
        System.out.println(Arrays.toString(result));
        assertTrue(result.length == 2);
    }
}