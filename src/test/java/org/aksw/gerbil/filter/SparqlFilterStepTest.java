package org.aksw.gerbil.filter;

import org.aksw.gerbil.filter.impl.CacheFilterStep;
import org.aksw.gerbil.filter.impl.SparqlFilterStep;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the {@link SparqlFilterStep}
 *
 * Created by Henrik Jürges on 08.11.15.
 */
public class SparqlFilterStepTest {

    private final String cacheLocation = "/tmp/filter";

    private final String service = "http://dbpedia.org/sparql";

    private final String prefix = "foaf:<http://xmlns.com/foaf/0.1/>";

    private final String[] entitiesPerson = new String[] {"http://dbpedia.org/resource/Victoria_Beckham",
            "http://dbpedia.org/resource/John_P._Kennedy"};

    private final FilterConfiguration conf1 = new FilterConfiguration("person-filter", "select ?v where { values ?v {##} . ?v a foaf:Person . }");

    private final String annotatorName = "testAnno";

    private final String datasetName = "testData";

    @Test
    public void testResolveEntitiesAnnotator() throws Exception {
       FilterStep resolution = new SparqlFilterStep(service, new String[] { prefix });
       resolution = new CacheFilterStep(resolution, cacheLocation);
       String[] result = resolution.resolveEntities(entitiesPerson, conf1, datasetName, annotatorName);
        assertArrayEquals(entitiesPerson, result);
    }

    @Test
    public void testResolveEntitiesGoldstandard() throws Exception {
        FilterStep resolution = new SparqlFilterStep(service, new String[] { prefix });
        resolution = new CacheFilterStep(resolution, cacheLocation);
        String[] result = resolution.resolveEntities(entitiesPerson, conf1, datasetName);
        assertTrue(result.length == 2);
    }
}