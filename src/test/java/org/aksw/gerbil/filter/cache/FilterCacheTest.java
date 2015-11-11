package org.aksw.gerbil.filter.cache;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * Some simple tests for the cache mechanism.
 *
 * Created by Henrik Jürges on 09.11.15.
 */
public class FilterCacheTest {

    private final File cacheLocation = new File("gerbil_data/cache/filter");

    @Test
    public void testCacheGoldStandard() throws Exception {
        FilterCache cache = FilterCache.getInstance();
        cache.cache(new String[] {"http://dbpedia.org/resource/Victoria_Beckham"}, "a filter", "gold1");
        assertTrue(new File(cacheLocation, "a_filter_gt_gold1").exists());
    }

    @Test
    public void testCacheAnnotatorResults() throws Exception {
        FilterCache cache = FilterCache.getInstance();
        cache.cache(new String[] {"http://dbpedia.org/resource/Victoria_Beckman"}, "a filter", "gold1", "anno");
        assertTrue(new File(cacheLocation, "a_filter_anno_gold1").exists());

        // recache if object is newer
        cache.cache(new String[] {"http://dbpedia.org/resource/Victoria_Beck"}, "a filter", "gold1", "anno");
        CachedResult expected = new CachedResult("a filter", "gold2", "anno", new String[] {"http://dbpedia.org/resource/Victoria_Beck"});
        assertTrue(cache.isCached("a filter", "gold1", "anno"));
        assertArrayEquals(expected.getEntities(), cache.getCachedResults("a filter", "gold1", "anno"));
    }

    @Test
    public void testComplexCacheOperations() throws Exception {
        FilterCache cache = FilterCache.getInstance();

        // store some results
        cache.cache(new String[] {"http://dbpedia.org/resource/Victoria_Beckham"}, "a filter", "gold1");
        cache.cache(new String[] {"http://dbpedia.org/resource/Victoria_Beckman"}, "a filter", "gold2");
        cache.cache(new String[] {"http://dbpedia.org/resource/Victoria_Beckham"}, "a filter", "gold1", "anno1");
        cache.cache(new String[] {"http://dbpedia.org/resource/Victoria_Beckman"}, "a filter", "gold2", "anno1");
        cache.cache(new String[] {"http://dbpedia.org/resource/Victoria_Beckfrau"}, "a filter", "gold1", "anno2");
        cache.cache(new String[] {"http://dbpedia.org/resource/Victoria_Beckmen"}, "a filter", "gold2", "anno2");

        // some results are cached
        assertTrue(cache.isCached("a filter", "gold1"));
        assertTrue(cache.isCached("a filter", "gold1", "anno1"));

        // check if results are cached properly
        CachedResult expected = new CachedResult("a filter", "gold2", new String[] {"http://dbpedia.org/resource/Victoria_Beckman"});
        CachedResult expected2 = new CachedResult("a filter", "gold2", "anno1", new String[] {"http://dbpedia.org/resource/Victoria_Beckman"});
        assertArrayEquals(expected.getEntities(), cache.getCachedResults("a filter", "gold2"));
        assertArrayEquals(expected2.getEntities(), cache.getCachedResults("a filter", "gold2", "anno1"));
    }

}