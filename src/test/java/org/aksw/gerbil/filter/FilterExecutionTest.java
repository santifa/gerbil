package org.aksw.gerbil.filter;

import org.aksw.gerbil.annotator.TestA2KBAnnotator;
import org.aksw.gerbil.database.SimpleLoggingResultStoringDAO4Debugging;
import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentTaskConfiguration;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.evaluate.EvaluatorFactory;
import org.aksw.gerbil.execute.AbstractExperimentTaskTest;
import org.aksw.gerbil.filter.cache.FilterCache;
import org.aksw.gerbil.filter.impl.NormalFilter;
import org.aksw.gerbil.matching.Matching;
import org.aksw.gerbil.semantic.kb.SimpleWhiteListBasedUriKBClassifier;
import org.aksw.gerbil.semantic.kb.UriKBClassifier;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.web.config.RootConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

/**
 * Created by ratzeputz on 22.11.15.
 */
@RunWith(Parameterized.class)
public class FilterExecutionTest extends AbstractExperimentTaskTest {

    private static final String CACHE_LOCATION = "/tmp/filter";

    private static final String SERVICE_URL = "http://dbpedia.org/sparql";

    private static final String TEXTS[] = new String[] {
            "Heidi and her husband Seal live in Vegas.",
            "Three of the greatest guitarists started their career in a single band : Clapton, Beck, and Page.",
            "Allen founded the EMP in Seattle, which featured exhibitions about Hendrix and Dylan, but also about various science fiction movies." };

    private static final DatasetConfiguration GOLD_STD = new NIFFileDatasetConfig("Kore50-reduced",
            "src/test/resources/filter_example_data/kore50-reduced-nif.ttl", false, ExperimentType.A2KB);
    private static final UriKBClassifier URI_KB_CLASSIFIER = new SimpleWhiteListBasedUriKBClassifier(
            "http://dbpedia.org/resource/");

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testConfigs = new ArrayList<Object[]>();
        //
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
                                        "http://dbpedia.org/resource/Bob_Dylan"))) },
                // found 1xnull but missed 1xDBpedia
                // (TP=1,FP=1,FN=1,P=0.5,R=0.5,F1=0.5)
                GOLD_STD, Matching.WEAK_ANNOTATION_MATCH,
                new double[] { 1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0, 0.25, 0.25, 0.25, 0 } });
        return testConfigs;
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

    @Test
    public void test() throws Exception {
        int experimentTaskId = 1;
        SimpleLoggingResultStoringDAO4Debugging experimentDAO = new SimpleLoggingResultStoringDAO4Debugging();

        EntityResolutionService service = new SparqlEntityResolution(SERVICE_URL);
        service.initCache(FilterCache.getInstance(CACHE_LOCATION));
        FilterFactory factory = new FilterFactory(service);
        factory.registerFilter(NormalFilter.class, FilterFactory.getBasicResolver());

        int counter = 1;
        HashMap<ExperimentTaskConfiguration, Integer> filterTask = new HashMap<>(6);
        for (EntityFilter f : factory.getFilters().getFilterList()) {
            ExperimentTaskConfiguration configuration = new ExperimentTaskConfiguration(
                    new TestA2KBAnnotator(Arrays.asList(annotatorResults)), dataset, ExperimentType.A2KB, matching, f.getConfig());
            filterTask.put(configuration, counter);
            counter++;
        }


        runTest(experimentTaskId, experimentDAO, RootConfig.createSameAsRetriever(), new EvaluatorFactory(URI_KB_CLASSIFIER), filterTask.keySet().iterator().next(),
                new F1MeasureTestingObserver(this, experimentTaskId, experimentDAO, expectedResults),
                factory.getFilters(), filterTask);
    }
}
