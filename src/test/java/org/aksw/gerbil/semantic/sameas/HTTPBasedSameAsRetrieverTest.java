/**
 * This file is part of General Entity Annotator Benchmark.
 *
 * General Entity Annotator Benchmark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * General Entity Annotator Benchmark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with General Entity Annotator Benchmark.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.aksw.gerbil.semantic.sameas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.gerbil.semantic.sameas.HTTPBasedSameAsRetriever;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class HTTPBasedSameAsRetrieverTest {

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testConfigs = new ArrayList<Object[]>();
        testConfigs.add(new Object[] { null, null });
        testConfigs.add(new Object[] { "http://aksw.org/notInWiki/Peter_Pan", null });
        testConfigs.add(new Object[] { "http://dbpedia.org/resource/Kaufland",
                Arrays.asList("http://fr.dbpedia.org/resource/Kaufland", "http://de.dbpedia.org/resource/Kaufland",
                        "http://wikidata.dbpedia.org/resource/Q685967", "http://cs.dbpedia.org/resource/Kaufland",
                        "http://nl.dbpedia.org/resource/Kaufland", "http://pl.dbpedia.org/resource/Kaufland",
                        "http://wikidata.org/entity/Q685967", "http://rdf.freebase.com/ns/m.0dwt4w",
                        "http://yago-knowledge.org/resource/Kaufland") });
        testConfigs.add(new Object[] { "http://dbpedia.org/resource/Malaysia",
                Arrays.asList("http://de.dbpedia.org/resource/Malaysia") });
        return testConfigs;
    }

    private String uri;
    private Set<String> expectedURIs;

    public HTTPBasedSameAsRetrieverTest(String uri, Collection<String> expectedURIs) {
        this.uri = uri;
        if (expectedURIs != null) {
            this.expectedURIs = new HashSet<String>();
            this.expectedURIs.addAll(expectedURIs);
        }
    }

    @Test
    public void test() {
        HTTPBasedSameAsRetriever retriever = new HTTPBasedSameAsRetriever();
        Set<String> uris = retriever.retrieveSameURIs(uri);
        if (expectedURIs == null) {
            Assert.assertNull(uris);
        } else {
            Assert.assertNotNull(uris);
            Assert.assertTrue(uris.containsAll(expectedURIs));
        }
        IOUtils.closeQuietly(retriever);
    }
}
