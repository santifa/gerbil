package org.aksw.gerbil.filter.cache;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Some simple tests for the cache mechanism.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class FilterCacheTest {

    private final File cacheLocation = new File("gerbil_data/cache/filter");

    private final List<String> entities1 = Arrays.asList("http://dbpedia.org/resource/Victoria_Beckham",
            "http://dbpedia.org/resource/Victoria", "http://dbpedia.org/resource/Victoria_Beck");

    private final List<String> entities2 = Arrays.asList("http://dbpedia.org/resource/Victoria_Beckham",
            "http://dbpedia.org/resource/Victoria");

    @Test
    public void testCacheGoldStandard() throws Exception {
        FilterCache cache = FilterCache.getInstance(cacheLocation.getAbsolutePath());
        CachedResult res = new CachedResult("a filter", "gold1", new String[] {"http://dbpedia.org/resource/Victoria_Beckham"});
        cache.cache(res);
        assertTrue(res.getCacheFile(cacheLocation).exists());
    }

    @Test
    public void testCacheAnnotatorResults() throws Exception {
        FilterCache cache = FilterCache.getInstance(cacheLocation.getAbsolutePath());
        CachedResult res = new CachedResult("a filter", "gold1", "anno", new String[] {"http://dbpedia.org/resource/Victoria_Beckman"});
        res.setChecksum(CachedResult.generateMd5Checksum(entities1));
        cache.cache(res);
        assertTrue(res.getCacheFile(cacheLocation).exists());

        // recache if object is newer
        CachedResult expected = new CachedResult("a filter", "gold1", "anno", new String[] {"http://dbpedia.org/resource/Victoria_Beck"});
        expected.setChecksum(CachedResult.generateMd5Checksum(entities2));
        cache.cache(expected);
        assertTrue(cache.isVersionCached("a filter", "gold1", "anno", CachedResult.generateMd5Checksum(entities2)));
       List<String> expectedList = new ArrayList<>();
        Collections.addAll(expectedList, expected.getEntities());
        assertEquals(expectedList, cache.getCachedResults("a filter", "gold1", "anno"));
    }

    @Test
    public void testComplexCacheOperations() throws Exception {
        FilterCache cache = FilterCache.getInstance("/tmp/filter");

        CachedResult res1 = new CachedResult("a filter", "gold1", entities1.toArray(new String[entities1.size()]));
        res1.setChecksum(CachedResult.generateMd5Checksum(entities1));
        CachedResult res2 = new CachedResult("a filter", "gold2", entities2.toArray(new String[entities2.size()]));
        res2.setChecksum(CachedResult.generateMd5Checksum(entities2));
        CachedResult res3 = new CachedResult("a filter", "gold2", "anno1", entities2.toArray(new String[entities2.size()]));
        res3.setChecksum(CachedResult.generateMd5Checksum(entities2));

        cache.cache(res1);
        cache.cache(res2);
        cache.cache(res3);

        // some results are cached
        assertTrue(cache.isVersionCached("a filter", "gold1", CachedResult.generateMd5Checksum(entities1)));
        assertFalse(cache.isVersionCached("a filter", "gold1", CachedResult.generateMd5Checksum(entities2)));
        assertTrue(cache.isCached("a filter", "gold2", "anno1"));


        // check if results are cached properly
        List<String> res1List = new ArrayList<>();
        Collections.addAll(res1List, res1.getEntities());
        List<String> res3List = new ArrayList<>();
        Collections.addAll(res3List, res3.getEntities());
        assertEquals(res1List, cache.getCachedResults("a filter", "gold1"));
        assertEquals(res3List, cache.getCachedResults("a filter", "gold2", "anno1"));
    }

}