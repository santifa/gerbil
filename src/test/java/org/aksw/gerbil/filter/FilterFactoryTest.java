package org.aksw.gerbil.filter;

import org.aksw.gerbil.dataset.Dataset;
import org.aksw.gerbil.dataset.TestDataset;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.filter.cache.FilterCache;
import org.aksw.gerbil.filter.impl.NormalFilter;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.junit.Assert;
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
                            "http://de.dbpedia.org/resource/Bernhard_Langer_(Golfer)"), (Marking) new NamedEntity(0, 8,
                            "http://de.dbpedia.org/resource/Hamburg"), (Marking) new NamedEntity(42, 4,
                            "http://de.dbpedia.org/resource/Venice_(Louisiana)"))),
                    (Document) new DocumentImpl(
                            "McDonald’s Corp., which replaced its chief executive officer last week, saw U.S. sales drop 4 percent in February after a short-lived recovery in its domestic market sputtered.",
                            "http://www.aksw.org/gerbil/test-document-2", Arrays.asList((Marking) new NamedEntity(0,
                                    16, "http://de.dbpedia.org/resource/CNN"),
                            (Marking) new NamedEntity(76, 4, "http://de.dbpedia.org/resource/Tiger_Woods"))));


    @BeforeClass
    public static void setUp() throws IOException {
        service = new SparqlEntityResolution(serviceUrl);
        service.initCache(FilterCache.getInstance("/tmp/filter"));
    }

    @Test
    public void testRegisterFilter() throws Exception {
        final FilterConfiguration conf = new FilterConfiguration("name filter", "?v rdf:type foaf:Person . }");
        final NormalFilter expected = new NormalFilter(conf);
        expected.setEntityResolution(service);
        FilterFactory factory = new FilterFactory(service);
        factory.registerFilter(NormalFilter.class, new FilterFactory.ConfigResolver<FilterConfiguration>() {
            @Override
            int resolve(int counter, List<FilterConfiguration> result) {
                result.add(conf);
                return -1;
            }
        });
        FilterHolder holder = factory.getFilters();
        Assert.assertTrue(holder.getFilterList().size() == 2);
        Assert.assertEquals(expected, holder.getFilterList().get(1));

    }

    @Test
    public void testPrecache() throws Exception {
        FilterFactory factory = new FilterFactory(service);
        factory.registerFilter(NormalFilter.class, new FilterFactory.ConfigResolver<FilterConfiguration>() {
            @Override
            int resolve(int counter, List<FilterConfiguration> result) {
                result.add(new FilterConfiguration("place filter", "?v rdf:type dbo:Place . }"));
                return -1;
            }
        });
        Dataset test = new TestDataset(INSTANCES, ExperimentType.ERec);
        factory.getFilters().cacheGoldstandard(test.getInstances(), "test");
    }
}