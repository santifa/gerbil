package org.aksw.gerbil.filter.cache;

import org.junit.Test;

import java.io.File;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * Some simple tests for the cache mechanism.
 *
 * Created by Henrik Jürges on 09.11.15.
 */
public class FilterCacheTest {

    private final File cacheLocation = new File("gerbil_data/cache/filter");

    private final String[] entities1 = new String[] {"http://dbpedia.org/resource/Victoria_Beckham",
            "http://dbpedia.org/resource/Victoria", "http://dbpedia.org/resource/Victoria_Beck"};

    private final String[] entities2 = new String[] {"http://dbpedia.org/resource/Victoria_Beckham",
            "http://dbpedia.org/resource/Victoria"};

    @Test
    public void testCacheGoldStandard() throws Exception {
        FilterCache cache = FilterCache.getInstance();
        CachedResult res = new CachedResult("a filter", "gold1", new String[] {"http://dbpedia.org/resource/Victoria_Beckham"});
        cache.cache(res);
        assertTrue(res.getCacheFile(cacheLocation).exists());
    }

    @Test
    public void testCacheAnnotatorResults() throws Exception {
        FilterCache cache = FilterCache.getInstance();
        CachedResult res = new CachedResult("a filter", "gold1", "anno", new String[] {"http://dbpedia.org/resource/Victoria_Beckman"});
        res.setChecksum(CachedResult.generateMd5Checksum(entities1));
        cache.cache(res);
        assertTrue(res.getCacheFile(cacheLocation).exists());

        // recache if object is newer
        CachedResult expected = new CachedResult("a filter", "gold1", "anno", new String[] {"http://dbpedia.org/resource/Victoria_Beck"});
        expected.setChecksum(CachedResult.generateMd5Checksum(entities2));
        cache.cache(expected);
        assertTrue(cache.isVersionCached("a filter", "gold1", "anno", CachedResult.generateMd5Checksum(entities2)));
        assertArrayEquals(expected.getEntities(), cache.getCachedResults("a filter", "gold1", "anno"));
    }

    @Test
    public void testComplexCacheOperations() throws Exception {
        FilterCache cache = FilterCache.getInstance();

        CachedResult res1 = new CachedResult("a filter", "gold1", entities1);
        res1.setChecksum(CachedResult.generateMd5Checksum(entities1));
        CachedResult res2 = new CachedResult("a filter", "gold2", entities2);
        res2.setChecksum(CachedResult.generateMd5Checksum(entities2));
        CachedResult res3 = new CachedResult("a filter", "gold2", "anno1", entities2);
        res3.setChecksum(CachedResult.generateMd5Checksum(entities2));

        cache.cache(res1);
        cache.cache(res2);
        cache.cache(res3);

        System.out.println(CachedResult.generateMd5Checksum(entities2));
        System.out.println(CachedResult.generateMd5Checksum(entities1));
        System.out.println(CachedResult.generateMd5Checksum(entities1));
        // some results are cached
        assertTrue(cache.isVersionCached("a filter", "gold1", CachedResult.generateMd5Checksum(entities1)));
        assertFalse(cache.isVersionCached("a filter", "gold1", CachedResult.generateMd5Checksum(entities2)));
        assertTrue(cache.isCached("a filter", "gold2", "anno1"));


        // check if results are cached properly
        assertArrayEquals(res1.getEntities(), cache.getCachedResults("a filter", "gold1"));
        assertArrayEquals(res3.getEntities(), cache.getCachedResults("a filter", "gold2", "anno1"));
    }

}