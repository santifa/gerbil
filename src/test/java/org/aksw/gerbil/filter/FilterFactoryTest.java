package org.aksw.gerbil.filter;

import org.aksw.gerbil.dataset.Dataset;
import org.aksw.gerbil.dataset.TestDataset;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.filter.cache.FilterCache;
import org.aksw.gerbil.filter.impl.SparqlFilter;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Test the creation of {@link EntityFilter} and caching of the goldstandard instances.
 *
 * Created by Henrik Jürges on 12.11.15.
 */
public class FilterFactoryTest {

    private static EntityResolutionService service;

    private static final String serviceUrl = "http://dbpedia.org/sparql";

    private static final List<Document> INSTANCES = Arrays
            .asList((Document) new DocumentImpl(
                            "Angelina, her father Jon, and her partner Brad never played together in the same movie.",
                            "http://www.aksw.org/gerbil/test-document-1", Arrays.asList((Marking) new NamedEntity(21, 3,
                            "http://www.aksw.org/gerbil/test-document/Jon"), (Marking) new NamedEntity(0, 8,
                            "http://www.aksw.org/gerbil/test-document/Angelina"), (Marking) new NamedEntity(42, 4,
                            "http://www.aksw.org/gerbil/test-document/Brad"))),
                    (Document) new DocumentImpl(
                            "McDonald’s Corp., which replaced its chief executive officer last week, saw U.S. sales drop 4 percent in February after a short-lived recovery in its domestic market sputtered.",
                            "http://www.aksw.org/gerbil/test-document-2", Arrays.asList((Marking) new NamedEntity(0,
                                    16, "http://www.aksw.org/gerbil/test-document/McDonaldsCorp"),
                            (Marking) new NamedEntity(76, 4, "http://www.aksw.org/gerbil/test-document/US"))));


    @BeforeClass
    public static void setUp() throws IOException {
        service = new DbpediaEntityResolution(serviceUrl);
        service.initCache(FilterCache.getInstance());
    }

    @Test
    public void testRegisterFilter() throws Exception {
        FilterFactory factory = new FilterFactory(service);
        factory.registerFilter(SparqlFilter.class, new FilterFactory.ConfigResolver<FilterConfiguration>() {
            @Override
            int resolve(int counter, List<FilterConfiguration> result) {
                result.add(new FilterConfiguration("name filter", "v? values?v rdf:type foaf:Person . }"));
                return -1;
            }
        });


    }

    @Test
    public void testPrecache() throws Exception {
        FilterFactory factory = new FilterFactory(service);
        Dataset test = new TestDataset(INSTANCES, ExperimentType.ERec);
        factory.precache(test.getInstances());

    }
}