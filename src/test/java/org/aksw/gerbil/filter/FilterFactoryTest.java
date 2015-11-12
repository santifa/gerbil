package org.aksw.gerbil.filter;

import org.aksw.gerbil.filter.cache.FilterCache;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by ratzeputz on 12.11.15.
 */
public class FilterFactoryTest {

    private static EntityResolutionService service;

    private static final String serviceUrl = "http://dbpedia.org/sparql";

    private static final String cacheLocation = "gerbil_data/cache/filter";

    @BeforeClass
    public static void setUp() throws IOException {
        service = new DbpediaEntityResolution(serviceUrl);
        service.initCache(FilterCache.getInstance());
    }

    @Test
    public void testRegisterFilter() throws Exception {
        FilterFactory factory = new FilterFactory(service);

    }
}