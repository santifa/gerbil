package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.SparqlEntityResolution;
import org.aksw.gerbil.filter.EntityResolutionService;
import org.aksw.gerbil.filter.FilterConfiguration;
import org.aksw.gerbil.filter.cache.FilterCache;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ratzeputz on 16.11.15.
 */
public class NormalFilterTest {

    private NormalFilter filter;

    private final String serviceUrl = "http://dbpedia.org/sparql";

    private final String[] prefix = new String[] {"foaf:<http://xmlns.com/foaf/0.1/>",
            "rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"};


    private static final List<List<Marking>> entities = Arrays.asList(
            Arrays.asList((Marking) new NamedEntity(22, 14, "http://dbpedia.org/resource/James_Carville"),
                    (Marking) new NamedEntity(57, 17, "http://dbpedia.org/resource/Political_consulting"),
                    (Marking) new NamedEntity(78, 12, "http://dbpedia.org/resource/Bill_Clinton"),
                    (Marking) new NamedEntity(96, 13, "http://dbpedia.org/resource/Donna_Brazile"),
                    (Marking) new NamedEntity(115, 16, "http://dbpedia.org/resource/Campaign_manager"),
                    (Marking) new NamedEntity(184, 7, "http://dbpedia.org/resource/Al_Gore")),
            Arrays.asList((Marking) new NamedEntity(49, 19,
                                                    "http://dbpedia.org/resource/Columbia_University")));

    private static final List<List<Marking>> expected = Arrays.asList(
            Arrays.asList((Marking) new NamedEntity(22, 14, "http://dbpedia.org/resource/James_Carville"),
                    (Marking) new NamedEntity(78, 12, "http://dbpedia.org/resource/Bill_Clinton"),
                    (Marking) new NamedEntity(96, 13, "http://dbpedia.org/resource/Donna_Brazile"),
                    (Marking) new NamedEntity(184, 7, "http://dbpedia.org/resource/Al_Gore")),
            new ArrayList<Marking>());

    @Before
    public void setUp() throws Exception {
        EntityResolutionService service = new SparqlEntityResolution(serviceUrl);
        service.initCache(FilterCache.getInstance());
        service.setPrefixSet(prefix);
        filter = new NormalFilter(new FilterConfiguration("person", "?v rdf:type foaf:Person . }"));
        filter.setEntityResolution(service);
    }

    @Test
    public void testFilterGoldstandard() throws Exception {
        System.out.println(filter);
        List<List<Marking>> results = filter.filterGoldstandard(entities, "dataset1");
        Assert.assertTrue(expected.equals(results));
    }

    @Test
    public void testFilterAnnotatorResults() throws Exception {
        System.out.println(filter);
        List<List<Marking>> results = filter.filterGoldstandard(entities, "dataset1");
        Assert.assertTrue(expected.equals(results));
    }
}