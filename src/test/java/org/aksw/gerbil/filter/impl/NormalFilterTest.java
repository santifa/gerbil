package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.EntityFilter;
import org.aksw.gerbil.filter.EntityResolutionService;
import org.aksw.gerbil.filter.FilterConfiguration;
import org.aksw.gerbil.filter.FilterHolder;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by ratzeputz on 16.11.15.
 */
@RunWith(Parameterized.class)
public class NormalFilterTest {

    private FilterHolder filters;

    private final String serviceUrl = "http://dbpedia.org/sparql";

    private final String cacheLocation = "/tmp/filter";

    private final String[] prefix = new String[] {"foaf:<http://xmlns.com/foaf/0.1/>",
            "rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>", "dbo:<http://dbpedia.org/ontology/>"};

    @Parameterized.Parameters(name = "{index}: {0} expected with {1} for persons and {2} for places.")
    public static Collection<Object[]> data() {
        List<Object[]> testObjects = new ArrayList<>(5);
        testObjects.add(new Object[] { Arrays.asList(
                (Marking) new NamedEntity(0, 5,
                        "http://dbpedia.org/resource/Heidi_Klum"),
                (Marking) new NamedEntity(22, 4,
                        "http://dbpedia.org/resource/Seal_(musician)"),
                (Marking) new NamedEntity(35, 5,
                        "http://dbpedia.org/resource/Las_Vegas")
        ),
        Arrays.asList(
                (Marking) new NamedEntity(0, 5,
                        "http://dbpedia.org/resource/Heidi_Klum"),
                (Marking) new NamedEntity(22, 4,
                        "http://dbpedia.org/resource/Seal_(musician)")
        ),
        Arrays.asList(
                (Marking) new NamedEntity(35, 5,
                        "http://dbpedia.org/resource/Las_Vegas")
        )});
        testObjects.add(new Object[] { Arrays.asList(
                (Marking) new NamedEntity(73, 7,
                        "http://dbpedia.org/resource/Eric_Clapton"),
                (Marking) new NamedEntity(82, 4,
                        "http://dbpedia.org/resource/Jeff_Beck"),
                (Marking) new NamedEntity(92, 4,
                        "http://dbpedia.org/resource/Jimmy_Page")
        ),
        Arrays.asList(
                (Marking) new NamedEntity(73, 7,
                        "http://dbpedia.org/resource/Eric_Clapton"),
                (Marking) new NamedEntity(82, 4,
                        "http://dbpedia.org/resource/Jeff_Beck"),
                (Marking) new NamedEntity(92, 4,
                        "http://dbpedia.org/resource/Jimmy_Page")
        ),
        Arrays.asList(
        )});
        testObjects.add(new Object[] {  Arrays.asList(
                (Marking) new NamedEntity(0, 5,
                        "http://dbpedia.org/resource/Paul_Allen"),
                (Marking) new NamedEntity(18, 3,
                        "http://dbpedia.org/resource/EMP_Museum"),
                (Marking) new NamedEntity(25, 7,
                        "http://dbpedia.org/resource/Seattle"),
                (Marking) new NamedEntity(67, 7,
                        "http://dbpedia.org/resource/Jimi_Hendrix"),
                (Marking) new NamedEntity(79, 5,
                        "http://dbpedia.org/resource/Bob_Dylan")
        ),
        Arrays.asList(
                (Marking) new NamedEntity(0, 5,
                        "http://dbpedia.org/resource/Paul_Allen"),
                (Marking) new NamedEntity(67, 7,
                        "http://dbpedia.org/resource/Jimi_Hendrix"),
                (Marking) new NamedEntity(79, 5,
                        "http://dbpedia.org/resource/Bob_Dylan")
        ),
        Arrays.asList(
                (Marking) new NamedEntity(25, 7,
                        "http://dbpedia.org/resource/Seattle")
        )});
        return testObjects;
    }

    private List<List<Marking>> expectedPerson;
    private List<List<Marking>> expectedPlaces;

    private List<List<Marking>> input;

    public NormalFilterTest(List<Marking> input, List<Marking> expectedPerson, List<Marking> expectedPlaces) {
        this.expectedPerson = new ArrayList<>(1);
        this.expectedPerson.add(expectedPerson);
        this.expectedPlaces = new ArrayList<>(1);
        this.expectedPlaces.add(expectedPlaces);
        this.input = new ArrayList<>(1);
        this.input.add(input);
    }

    @Before
    public void setUp() throws Exception {
        EntityResolutionService service = new SparqlEntityResolution(serviceUrl, prefix);
        service = new CacheEntityResolution(service, cacheLocation);
        EntityFilter filter1 = new NormalFilter(new FilterConfiguration("person", "select distinct ?v where { values ?v {##} ?v rdf:type foaf:Person . }"));
        filter1.setEntityResolution(service);
        EntityFilter filter2 = new NormalFilter(new FilterConfiguration("place", "select distinct ?v where { values ?v {##} ?v rdf:type dbo:Place . }"));
        filter2.setEntityResolution(service);
        this.filters = new FilterHolder(Arrays.asList(filter1, filter2), false);
    }

    @Test
    public void testFilterGoldstandard() throws Exception {
        for (EntityFilter f : filters.getFilterList()) {
            List<List<Marking>> results = f.filterAnnotatorResults(input, "dataset1", "anno1");
            if (f.getConfig().getName().equals("person")) {
                Assert.assertTrue(expectedPerson.equals(results));
            } else {
                Assert.assertTrue(expectedPlaces.equals(results));
            }
        }
    }

    @Test
    public void testFilterAnnotatorResults() throws Exception {
        for (EntityFilter f : filters.getFilterList()) {
            List<List<Marking>> results = f.filterGoldstandard(input, "dataset1");
            if (f.getConfig().getName().equals("person")) {
                Assert.assertTrue(expectedPerson.equals(results));
            } else {
                Assert.assertTrue(expectedPlaces.equals(results));
            }
        }
    }
}