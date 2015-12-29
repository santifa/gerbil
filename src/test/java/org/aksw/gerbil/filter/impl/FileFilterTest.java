package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.FilterDefinition;
import org.aksw.gerbil.filter.wrapper.FilterWrapper;
import org.aksw.gerbil.filter.wrapper.FilterWrapperImpl;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

/**
 * Test the filters that uses rdf files as knowledge base.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@RunWith(Parameterized.class)
public class FileFilterTest {

    private FilterWrapper f;

    private final String[] prefix = new String[]{"foaf:<http://xmlns.com/foaf/0.1/>",
            "rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>", "dbo:<http://dbpedia.org/ontology/>"};

    @Parameterized.Parameters(name = "{index}: {0} expected with {1} for persons and {2} for places.")
    public static Collection<Object[]> data() {
        List<Object[]> testObjects = new ArrayList<>(5);
        testObjects.add(new Object[]{Arrays.asList(
                (Marking) new NamedEntity(0, 5,
                        "http://dbpedia.org/resource/Heidi_Klum"),
                (Marking) new NamedEntity(22, 4,
                        "http://dbpedia.org/resource/Seal_(musician)"),
                (Marking) new NamedEntity(35, 5,
                        "http://dbpedia.org/resource/Las_Vegas")
        ), Arrays.asList()});

        testObjects.add(new Object[]{Arrays.asList(
                (Marking) new NamedEntity(73, 7,
                        "http://dbpedia.org/resource/Eric_Clapton"),
                (Marking) new NamedEntity(82, 4,
                        "http://dbpedia.org/resource/Jeff_Beck"),
                (Marking) new NamedEntity(92, 4,
                        "http://dbpedia.org/resource/Jimmy_Page")
        ), Arrays.asList(
                (Marking) new NamedEntity(73, 7,
                        "http://dbpedia.org/resource/Eric_Clapton")
        )});

        testObjects.add(new Object[]{Arrays.asList(
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
        ), Arrays.asList(
                (Marking) new NamedEntity(25, 7,
                        "http://dbpedia.org/resource/Seattle"),
                (Marking) new NamedEntity(67, 7,
                        "http://dbpedia.org/resource/Jimi_Hendrix"),
                (Marking) new NamedEntity(79, 5,
                        "http://dbpedia.org/resource/Bob_Dylan")
        )});
        return testObjects;
    }

    @Before
    public void setUp() {
        this.f = new FilterWrapperImpl(new FileFilter(new FilterDefinition("pop",
                "select distinct ?v ?pagerank WHERE { values ?v {##} ?v dbo:wikiPageRank ?pagerank . } ORDER BY DESC (?pagerank)", new ArrayList<String>(),
                "gerbil_data/resources/filter/sources/pagerank_scores_en_2015.ttl"), prefix));
    }

    private List<List<Marking>> expectedPop;

    private List<List<Marking>> input;

    public FileFilterTest(List<Marking> input, List<Marking> expectedPop) {
        this.expectedPop = Collections.singletonList(expectedPop);
        this.input = Collections.singletonList(input);
    }

    @Test
    public void testFilterAnnotatorResults() throws Exception {
        List<List<Marking>> results = f.filterAnnotatorResults(input, "dataset1", "anno1");
        Assert.assertTrue(expectedPop.equals(results));
    }

    @Test
    public void testFilterGoldstandard() throws Exception {
        List<List<Marking>> results = f.filterGoldstandard(input, "dataset1");
        Assert.assertTrue(expectedPop.equals(results));
    }
}