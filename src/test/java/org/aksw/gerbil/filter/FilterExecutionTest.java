package org.aksw.gerbil.filter;

import org.aksw.gerbil.annotator.TestA2KBAnnotator;
import org.aksw.gerbil.database.SimpleLoggingResultStoringDAO4Debugging;
import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentTaskConfiguration;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.evaluate.EvaluatorFactory;
import org.aksw.gerbil.execute.AbstractExperimentTaskTest;
import org.aksw.gerbil.filter.impl.PopularityFilter;
import org.aksw.gerbil.filter.impl.SparqlFilter;
import org.aksw.gerbil.filter.wrapper.FilterWrapper;
import org.aksw.gerbil.filter.wrapper.IdentityWrapper;
import org.aksw.gerbil.matching.Matching;
import org.aksw.gerbil.semantic.kb.SimpleWhiteListBasedUriKBClassifier;
import org.aksw.gerbil.semantic.kb.UriKBClassifier;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.web.config.AdapterList;
import org.aksw.gerbil.web.config.RootConfig;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Runs a full fledged filter test. Uses the sparql filters defined in filter.properties.
 * Use this for deeper investigation how the filter subsystem works on datasets.
 * Test the creation of the correct metadata.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@RunWith(Parameterized.class)
public class FilterExecutionTest extends AbstractExperimentTaskTest {

    private static final String TEXTS[] = new String[] {
            "Heidi and her husband Seal live in Vegas.",
            "Three of the greatest guitarists started their career in a single band : Clapton, Beck, and Page.",
            "Allen founded the EMP in Seattle, which featured exhibitions about Hendrix and Dylan, but also about various science fiction movies.",
            "After the death of Steve, the former CEO of Apple, his commencement speech at Stanford was watched thousands of times."};

    private static final DatasetConfiguration REDUCED_GLD_STD = new NIFFileDatasetConfig("Kore50-reduced",
            "src/test/resources/filter_example_data/kore50-reduced-nif.ttl", false, ExperimentType.A2KB);

    private static final UriKBClassifier URI_KB_CLASSIFIER = new SimpleWhiteListBasedUriKBClassifier(
            "http://dbpedia.org/resource/");

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testConfigs = new ArrayList<>();
        testConfigs.add(new Object[] { new Document[] {
                // found everything in the first sentence
                new DocumentImpl(TEXTS[0], "http://www.mpi-inf.mpg.de/yago-naga/aida/download/KORE50.tar.gz/AIDA.tsv/CEL08",
                        Arrays.asList((Marking) new NamedEntity(0, 5,
                                        "http://dbpedia.org/resource/Heidi_Klum"),
                                (Marking) new NamedEntity(22, 4,
                                        "http://dbpedia.org/resource/Seal_(musician)"),
                                (Marking) new NamedEntity(35, 5,
                                        "http://dbpedia.org/resource/Las_Vegas,_Nevada"))),
                // found everything in the second sentence
                new DocumentImpl(TEXTS[1], "http://www.mpi-inf.mpg.de/yago-naga/aida/download/KORE50.tar.gz/AIDA.tsv/MUS03",
                        Arrays.asList(
                                (Marking) new NamedEntity(73, 7,
                                        "http://dbpedia.org/resource/Eric_Clapton"),
                                (Marking) new NamedEntity(82, 4,
                                        "http://dbpedia.org/resource/Jeff_Beck"),
                                (Marking) new NamedEntity(92, 4,
                                        "http://dbpedia.org/resource/Jimmy_Page"))),
                // found everything in the third sentence
                new DocumentImpl(TEXTS[2], "http://www.mpi-inf.mpg.de/yago-naga/aida/download/KORE50.tar.gz/AIDA.tsv/MUS04",
                        Arrays.asList(
                                (Marking) new NamedEntity(0, 5,
                                        "http://dbpedia.org/resource/Paul_Allen"),
                                (Marking) new NamedEntity(18, 3,
                                        "http://dbpedia.org/resource/EMP_Museum"),
                                (Marking) new NamedEntity(25, 7,
                                        "http://dbpedia.org/resource/Seattle"),
                                (Marking) new NamedEntity(67, 7,
                                        "http://dbpedia.org/resource/Jimi_Hendrix"),
                                (Marking) new NamedEntity(79, 5,
                                        "http://dbpedia.org/resource/Bob_Dylan"))),
                new DocumentImpl(TEXTS[3], "http://www.mpi-inf.mpg.de/yago-naga/aida/download/KORE50.tar.gz/AIDA.tsv/BUS01",
                        Arrays.asList(
                        (Marking) new NamedEntity(19, 24,
                                "http://dbpedia.org/resource/Steve_Jobs"),
                        (Marking) new NamedEntity(44, 49,
                                "http://dbpedia.org/resource/Apple_Inc."),
                        (Marking) new NamedEntity(78, 86,
                                "http://dbpedia.org/resource/Stanford_University"))) },
                // (TP=14,FP=0,FN=0,P=1,R=1,F1=1)
                REDUCED_GLD_STD, Matching.WEAK_ANNOTATION_MATCH,
                new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0 } });
        return testConfigs;
    }

    private static FilterHolder holder;

    @BeforeClass
    public static void setUp() {
        FilterFactory factory = new FilterFactory(false);
        factory.registerFilter(SparqlFilter.class, factory.getBasicFilterResolver());
        holder = factory.getFilters();
    }

    private Document annotatorResults[];
    private DatasetConfiguration dataset;
    private double expectedResults[];
    private Matching matching;

    public FilterExecutionTest(Document[] annotatorResults, DatasetConfiguration dataset, Matching matching,
                    double[] expectedResults) {
        this.annotatorResults = annotatorResults;
        this.dataset = dataset;
        this.expectedResults = expectedResults;
        this.matching = matching;
    }

    //@Test
    public void test() throws Exception {
        int experimentTaskId = 1;
        SimpleLoggingResultStoringDAO4Debugging experimentDAO = new SimpleLoggingResultStoringDAO4Debugging();

        int counter = 1;
        for (FilterWrapper f : holder.getFilterList()) {
            HashMap<ExperimentTaskConfiguration, Integer> filterTask = new HashMap<>(6);
            ExperimentTaskConfiguration configuration = new ExperimentTaskConfiguration(
                    new TestA2KBAnnotator(Arrays.asList(annotatorResults)), dataset, ExperimentType.A2KB, matching, f.getConfig());
            filterTask.put(configuration, counter);
            FilterHolder h = new FilterHolder(Collections.singletonList(f));

            runTest(experimentTaskId, experimentDAO, RootConfig.createSameAsRetriever(), new EvaluatorFactory(URI_KB_CLASSIFIER), filterTask.keySet().iterator().next(),
                    new F1MeasureTestingObserver(this, experimentTaskId, experimentDAO, expectedResults),
                    holder, filterTask);
        }
    }

    @Test
    public void testIdentityFilter() throws Exception {
        int experimentTaskId = 1;
        SimpleLoggingResultStoringDAO4Debugging experimentDAO = new SimpleLoggingResultStoringDAO4Debugging();

        FilterWrapper identity = new IdentityWrapper();
        FilterHolder h = new FilterHolder(Collections.singletonList(identity));

        HashMap<ExperimentTaskConfiguration, Integer> filterTask = new HashMap<>(6);
        ExperimentTaskConfiguration configuration = new ExperimentTaskConfiguration(
                    new TestA2KBAnnotator(Arrays.asList(annotatorResults)), dataset, ExperimentType.A2KB, matching, identity.getConfig());
        filterTask.put(configuration, 1);

        runTest(experimentTaskId, experimentDAO, RootConfig.createSameAsRetriever(), new EvaluatorFactory(URI_KB_CLASSIFIER), filterTask.keySet().iterator().next(),
                new F1MeasureTestingObserver(this, experimentTaskId, experimentDAO, expectedResults),
                h, filterTask);
    }

    @Test
    public void testPopFilter() throws Exception {
        int experimentTaskId = 1;
        SimpleLoggingResultStoringDAO4Debugging experimentDAO = new SimpleLoggingResultStoringDAO4Debugging();

        FilterFactory factory = new FilterFactory(false);
        factory.registerFilter(PopularityFilter.class, factory.getPopularityFilterResolver());
        FilterHolder h = factory.getFilters();

        HashMap<ExperimentTaskConfiguration, Integer> filterTask = new HashMap<>(6);
        int count = 1;
        for (FilterWrapper w : h.getFilterList()) {
            ExperimentTaskConfiguration configuration = new ExperimentTaskConfiguration(
                    new TestA2KBAnnotator(Arrays.asList(annotatorResults)), dataset, ExperimentType.A2KB, matching, w.getConfig());
            filterTask.put(configuration, count);
            count++;
        }

        runTest(experimentTaskId, experimentDAO, RootConfig.createSameAsRetriever(), new EvaluatorFactory(URI_KB_CLASSIFIER), filterTask.keySet().iterator().next(),
                new F1MeasureTestingObserver(this, experimentTaskId, experimentDAO, expectedResults),
                h, filterTask);
    }

    @Test
    public void testMetadataCreation() {
        AdapterList<DatasetConfiguration> configurations = new AdapterList<>(Collections.singletonList(dataset));
        FilterFactory factory = new FilterFactory(false);
        factory.registerFilter(SparqlFilter.class, factory.getBasicFilterResolver());
        factory.registerFilter(PopularityFilter.class, factory.getPopularityFilterResolver());

        MetadataUtils utils = new MetadataUtils(configurations, factory.getFilters());
        assertEquals(14, utils.getAmountOfEntities());

        HashMap<String, Integer> expected = new HashMap<>(13);
        expected.put("Filter Persons", 9);
        expected.put("Filter Organizations", 1);
        expected.put("Filter Pagerank 10%-55%", 8);
        expected.put("Filter Hitsscore 55%-100%", 2);
        expected.put("nofilter", 14);
        expected.put("Filter Hitsscore 10%", 7);
        expected.put("Filter Places", 1);
        expected.put("Filter Pagerank 55%-100%", 3);
        expected.put("Filter Pagerank 10%", 3);
        expected.put("Filter Hitsscore 10%-55%", 5);
        assertTrue(expected.equals(utils.getAmountOfEntitiesPerFilter()));
    }
}
