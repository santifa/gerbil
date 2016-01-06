package org.aksw.gerbil.filter;

import org.aksw.gerbil.filter.impl.FileFilter;
import org.aksw.gerbil.filter.wrapper.FilterWrapper;
import org.aksw.gerbil.filter.wrapper.FilterWrapperImpl;
import org.aksw.gerbil.filter.impl.SparqlFilter;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static java.util.Collections.EMPTY_LIST;

/**
 * Test the so called normal filters which uses plain sparql queries against a knowledge base for entity filtering.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@RunWith(Parameterized.class)
public class NormalFilterTest {

    private final String SERVICE_URL = "http://dbpedia.org/sparql";

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
                        "http://dbpedia.org/resource/Bob_Dylan"),
                (Marking) new NamedEntity(79, 5,
                        "http://www.org/resource/Bob_Dylan")
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
        this.expectedPerson = Collections.singletonList(expectedPerson);
        this.expectedPlaces = Collections.singletonList(expectedPlaces);
        this.input = Collections.singletonList(input);
    }

    @Test
    public void testFilterAnnotatorResultsPlace() throws Exception {
        FilterWrapper f = new FilterWrapperImpl(new SparqlFilter(new FilterDefinition("place",
                "select distinct ?v where { values ?v {##} ?v rdf:type dbo:Place . }",
                EMPTY_LIST, SERVICE_URL, 20), prefix));

        List<List<Marking>> results = f.filterAnnotatorResults(input, "dataset1", "anno1");
        Assert.assertTrue(expectedPlaces.equals(results));
    }

    @Test
    public void testFilterAnnotatorResultsPerson() throws Exception {
        FilterWrapper f = new FilterWrapperImpl(new SparqlFilter(new FilterDefinition("person",
                "select distinct ?v where { values ?v {##} ?v rdf:type foaf:Person . }",
                Collections.singletonList("http://dbpedia.org/"), SERVICE_URL, 20), prefix));

        List<List<Marking>> results = f.filterAnnotatorResults(input, "dataset1", "anno1");
        Assert.assertTrue(expectedPerson.equals(results));
    }

    @Test
    public void testFilterGoldstandardPerson() throws Exception {
        FilterWrapper f = new FilterWrapperImpl(new SparqlFilter(new FilterDefinition("person",
                "select distinct ?v where { values ?v {##} ?v rdf:type foaf:Person . }",
                Collections.singletonList("http://dbpedia.org/"), SERVICE_URL, 20), prefix));

        List<List<Marking>> results = f.filterGoldstandard(input, "dataset1");
        Assert.assertTrue(expectedPerson.equals(results));
    }

    @Test
    public void testFilterGoldstandardPlace() throws Exception {
        FilterWrapper f = new FilterWrapperImpl(new SparqlFilter(new FilterDefinition("place",
                "select distinct ?v where { values ?v {##} ?v rdf:type dbo:Place . }",
                EMPTY_LIST, SERVICE_URL, 20), prefix));

         List<List<Marking>> results = f.filterGoldstandard(input, "dataset1");
         Assert.assertTrue(expectedPlaces.equals(results));
    }

    @Test
    public void testFileFilter() throws InstantiationException {
        // TODO Currently NOT TESTED
        FilterWrapper filter3 = new FilterWrapperImpl(new FileFilter(new FilterDefinition("pop",
                "select distinct ?v ?pagerank WHERE { values ?v {##} ?v dbo:wikiPageRank ?pagerank . } ORDER BY DESC (?pagerank)", new ArrayList<String>(),
                "gerbil_data/resources/filter/sources/pagerank_scores_reduced_en_2015.ttl", 20), prefix));

    }
}