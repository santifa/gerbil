package org.aksw.gerbil.filter;

import org.aksw.gerbil.dataset.Dataset;
import org.aksw.gerbil.dataset.TestDataset;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.filter.impl.*;
import org.aksw.gerbil.filter.wrapper.FilterWrapperImpl;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test the creation of {@link FilterWrapper} and caching of the goldstandard instances.
 *
 * Created by Henrik Jürges on 12.11.15.
 */
public class FilterFactoryTest {

    private static Filter expectedService;

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


    final FilterDefinition conf = new FilterDefinition("name filter",
            "select ?v where { values ?v {##} ?v rdf:type dbo:Place . }", new ArrayList<String>(), serviceUrl);

    @Before
    public void setUp() throws IOException {
        expectedService = new SparqlFilter(conf, new String[] {"dbo:<http://dbpedia.org/ontology/>",
                "foaf:<http://xmlns.com/foaf/0.1/>",
                "rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"});
        //expectedService = new CacheFilter(expectedService, "/tmp/filter");
        //expectedService = new UriCleaner(expectedService);
    }

    @Test
    public void testRegisterFilter() throws Exception {
        final FilterWrapperImpl expected = new FilterWrapperImpl(expectedService);

        FilterFactory factory = new FilterFactory(false);
        factory.registerFilter(SparqlFilter.class, new FilterFactory.ConfigResolver<FilterDefinition>() {
            @Override
            int resolve(int counter, List<FilterDefinition> result) {
                result.add(conf);
                return -1;
            }
        });

        FilterHolder holder = factory.getFilters();
        Assert.assertTrue(holder.getFilterList().size() == 2);
        // IdentityWrapper is trivial
        Assert.assertEquals(expected, holder.getFilterList().get(1));

    }

    @Test
    public void testPrecache() throws Exception {
        FilterFactory factory = new FilterFactory(false);
        factory.registerFilter(SparqlFilter.class, new FilterFactory.ConfigResolver<FilterDefinition>() {
            @Override
            int resolve(int counter, List<FilterDefinition> result) {
                result.add(new FilterDefinition("place filter",
                        "select ?v where { values ?v {##} ?v rdf:type dbo:Place . }", new ArrayList<String>(), serviceUrl));
                return -1;
            }
        });
        Dataset test = new TestDataset(INSTANCES, ExperimentType.ERec);
        factory.getFilters().cacheGoldstandard(test.getInstances(), "test");
    }

    // ugly quick loading from config test
    @Test
    public void testConfigLoading() throws Exception {
        FilterFactory factory = new FilterFactory(false);
        factory.registerFilter(SparqlFilter.class, factory.getBasicFilterResolver());
        factory.registerFilter(FileFilter.class, factory.getFileFilterResolver());
        factory.registerFilter(PopularityFilter.class, factory.getPopularityFilterResolver());
        System.out.println(factory.getFilters());
    }
}