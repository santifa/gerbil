package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.FilterDefinition;
import org.aksw.gerbil.filter.FilterWrapper;
import org.aksw.gerbil.filter.wrapper.FilterWrapperImpl;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Test the popularity filters.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@RunWith(Parameterized.class)
public class PopularityFilterTest {

    @Parameterized.Parameters(name = "{index}: {0} expected with {1} for persons and {2} for places.")
    public static Collection<Object[]> data() {
        final String[] prefix = new String[] {"foaf:<http://xmlns.com/foaf/0.1/>",
                "rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>", "dbo:<http://dbpedia.org/ontology/>"};

        List<Object[]> testObjects = new ArrayList<>(5);
        testObjects.add(new Object[] {
            new FilterWrapperImpl(new PopularityFilter(new FilterDefinition(
                "pop", "0", Collections.singletonList("http://dbpedia.org"),
                "src/test/resources/filter_example_data/ranked_pagerank_scores_reduced_en_2015"), prefix)),
            Arrays.asList(
                (Marking) new NamedEntity(0, 5,
                        "http://dbpedia.org/resource/Secondary_education"),
                (Marking) new NamedEntity(22, 4,
                        "http://dbpedia.org/resource/Academic_degree"),
                (Marking) new NamedEntity(35, 5,
                        "http://dbpedia.org/resource/Las_Vegas")
            ), Arrays.asList(
                (Marking) new NamedEntity(0, 5,
                        "http://dbpedia.org/resource/Secondary_education"),
                (Marking) new NamedEntity(22, 4,
                        "http://dbpedia.org/resource/Academic_degree"),
                (Marking) new NamedEntity(35, 5,
                        "http://dbpedia.org/resource/Las_Vegas")
        )});
        testObjects.add(new Object[] {
            new FilterWrapperImpl(new PopularityFilter(new FilterDefinition(
                "pop", "1", Collections.singletonList("http://dbpedia.org"),
                "src/test/resources/filter_example_data/ranked_pagerank_scores_reduced_en_2015"), prefix)),
            Arrays.asList(
                (Marking) new NamedEntity(73, 7,
                        "http://dbpedia.org/resource/Eric_Clapton"),
                (Marking) new NamedEntity(82, 4,
                        "http://dbpedia.org/resource/Julian_day"),
                (Marking) new NamedEntity(92, 4,
                        "http://dbpedia.org/resource/Jimmy_Page")
            ), Arrays.asList(
                (Marking) new NamedEntity(73, 7,
                        "http://dbpedia.org/resource/Eric_Clapton"),
                (Marking) new NamedEntity(82, 4,
                        "http://dbpedia.org/resource/Julian_day")
        )});
        testObjects.add(new Object[] {
            new FilterWrapperImpl(new PopularityFilter(new FilterDefinition(
                "pop", "2", Collections.singletonList("http://dbpedia.org"),
                "src/test/resources/filter_example_data/ranked_pagerank_scores_reduced_en_2015"), prefix)),
            Arrays.asList(
                (Marking) new NamedEntity(0, 5,
                        "http://dbpedia.org/resource/Julian_day"),
                (Marking) new NamedEntity(18, 3,
                        "http://dbpedia.org/resource/University_of_Cambridge"),
                (Marking) new NamedEntity(25, 7,
                        "http://dbpedia.org/resource/Seattle"),
                (Marking) new NamedEntity(67, 7,
                        "http://dbpedia.org/resource/Jimi_Hendrix"),
                (Marking) new NamedEntity(79, 5,
                        "http://dbpedia.org/resource/Bob_Dylan"),
                (Marking) new NamedEntity(79, 5,
                        "http://www.org/resource/Bob_Dylan")
            ), Arrays.asList(
                (Marking) new NamedEntity(18, 3,
                        "http://dbpedia.org/resource/University_of_Cambridge"),
                (Marking) new NamedEntity(25, 7,
                        "http://dbpedia.org/resource/Seattle"),
                (Marking) new NamedEntity(79, 5,
                        "http://dbpedia.org/resource/Bob_Dylan")
                )});
        testObjects.add(new Object[] {
                new FilterWrapperImpl(new PopularityFilter(new FilterDefinition(
                        "pop", "3", Collections.singletonList("http://dbpedia.org"),
                        "src/test/resources/filter_example_data/ranked_pagerank_scores_reduced_en_2015"), prefix)),
                Arrays.asList(
                        (Marking) new NamedEntity(0, 5,
                                "http://dbpedia.org/resource/East_Africa_Time"),
                        (Marking) new NamedEntity(18, 3,
                                "http://dbpedia.org/resource/Hard_rock"),
                        (Marking) new NamedEntity(25, 7,
                                "http://dbpedia.org/resource/Kazakhstan"),
                        (Marking) new NamedEntity(67, 7,
                                "http://dbpedia.org/resource/Jimi_Hendrix"),
                        (Marking) new NamedEntity(79, 5,
                                "http://dbpedia.org/resource/Bob_Dylan"),
                        (Marking) new NamedEntity(79, 5,
                                "http://www.org/resource/Bob_Dylan")
                ), Arrays.asList(
                (Marking) new NamedEntity(0, 5,
                        "http://dbpedia.org/resource/East_Africa_Time"),
                (Marking) new NamedEntity(18, 3,
                        "http://dbpedia.org/resource/Hard_rock"),
                (Marking) new NamedEntity(25, 7,
                        "http://dbpedia.org/resource/Kazakhstan")
                )});
        testObjects.add(new Object[] {
                new FilterWrapperImpl(new PopularityFilter(new FilterDefinition(
                        "pop", "4", Collections.singletonList("http://dbpedia.org"),
                        "src/test/resources/filter_example_data/ranked_pagerank_scores_reduced_en_2015"), prefix)),
                Arrays.asList(
                        (Marking) new NamedEntity(0, 5,
                                "http://dbpedia.org/resource/Vysočina_Region"),
                        (Marking) new NamedEntity(18, 3,
                                "http://dbpedia.org/resource/Württemberg"),
                        (Marking) new NamedEntity(25, 7,
                                "http://dbpedia.org/resource/Georgia_(country)"),
                        (Marking) new NamedEntity(67, 7,
                                "http://dbpedia.org/resource/Port_Vale_F.C."),
                        (Marking) new NamedEntity(79, 5,
                                "http://dbpedia.org/resource/Bob_Dylan"),
                        (Marking) new NamedEntity(79, 5,
                                "http://www.org/resource/Bob_Dylan")
                ), Arrays.asList(
                (Marking) new NamedEntity(0, 5,
                        "http://dbpedia.org/resource/Vysočina_Region"),
                (Marking) new NamedEntity(18, 3,
                        "http://dbpedia.org/resource/Württemberg"),
                (Marking) new NamedEntity(67, 7,
                        "http://dbpedia.org/resource/Port_Vale_F.C.")
                )});
        testObjects.add(new Object[] {
                new FilterWrapperImpl(new PopularityFilter(new FilterDefinition(
                        "pop", "0,2", Collections.singletonList("http://dbpedia.org"),
                        "src/test/resources/filter_example_data/ranked_pagerank_scores_reduced_en_2015"), prefix)),
                Arrays.asList(
                        (Marking) new NamedEntity(0, 5,
                                "http://dbpedia.org/resource/Secondary_education"),
                        (Marking) new NamedEntity(22, 4,
                                "http://dbpedia.org/resource/Academic_degree"),
                        (Marking) new NamedEntity(0, 5,
                                "http://dbpedia.org/resource/Julian_day"),
                        (Marking) new NamedEntity(18, 3,
                                "http://dbpedia.org/resource/University_of_Cambridge"),
                        (Marking) new NamedEntity(79, 5,
                                "http://dbpedia.org/resource/Bob_Dylan"),
                        (Marking) new NamedEntity(79, 5,
                                "http://www.org/resource/Bob_Dylan")
                ), Arrays.asList(
                (Marking) new NamedEntity(0, 5,
                "http://dbpedia.org/resource/Secondary_education"),
                (Marking) new NamedEntity(22, 4,
                        "http://dbpedia.org/resource/Academic_degree"),
                (Marking) new NamedEntity(18, 3,
                        "http://dbpedia.org/resource/University_of_Cambridge"),
                (Marking) new NamedEntity(79, 5,
                        "http://dbpedia.org/resource/Bob_Dylan")
        )});
        return testObjects;
    }

    private List<List<Marking>> expected;

    private List<List<Marking>> input;

    private FilterWrapper filter;

    public PopularityFilterTest(FilterWrapper filter, List<Marking> input, List<Marking> expected) {
        this.expected = Collections.singletonList(expected);
        this.input = Collections.singletonList(input);
        this.filter = filter;
    }


    @Test
    public void testPopularityFilter() {
        List<List<Marking>> results = filter.filterAnnotatorResults(input, "dataset1", "ano1");
        assertEquals(expected, results);
    }
}