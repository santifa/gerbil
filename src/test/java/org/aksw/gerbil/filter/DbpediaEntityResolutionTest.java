package org.aksw.gerbil.filter;

import it.uniroma1.lcl.jlt.util.Arrays;
import org.junit.Test;

import java.util.NoSuchElementException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Test the {@link DbpediaEntityResolution}
 *
 * Created by Henrik Jürges on 08.11.15.
 */
public class DbpediaEntityResolutionTest {

    private final String service = "http://dbpedia.org/sparql";

    private final String prefix = "foaf:<http://xmlns.com/foaf/0.1/>";

    private final String entityName = "http://dbpedia.org/resource/Victoria_Beckham";

    @Test
    public void testGetTypeWithoutPrefixes() throws Exception {
        DbpediaEntityResolution provider = new DbpediaEntityResolution(service);
        provider.setPrefixSet(new String[0]);
        String expected = "http://xmlns.com/foaf/0.1/Person";
        assertEquals(expected, provider.getType(entityName));
    }

    @Test
    public void testGetType() throws Exception {
        DbpediaEntityResolution provider = new DbpediaEntityResolution(service);
        provider.setPrefixSet(new String[] {prefix});
        String expected = "foaf:Person";
        assertEquals(expected, provider.getType(entityName));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetTypGettingNothing() {
        DbpediaEntityResolution provider = new DbpediaEntityResolution(service);
        provider.setPrefixSet(new String[] {prefix});
        String expected = "";
        assertEquals(expected, provider.getType("http://dbpedia.org/resourc"));
    }

    @Test
    public void testGetAllTypes() throws Exception {
        DbpediaEntityResolution provider = new DbpediaEntityResolution(service);
        provider.setPrefixSet(new String[] {prefix, "dbpedia-owl:<http://dbpedia.org/ontology/Person>"});
        String[] types = provider.getAllTypes(entityName);
        assertTrue(Arrays.contains(types, "dbpedia-owl:Person") &&
            Arrays.contains(types, "foaf:Person"));
    }
}