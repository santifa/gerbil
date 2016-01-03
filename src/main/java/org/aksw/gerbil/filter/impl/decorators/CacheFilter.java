package org.aksw.gerbil.filter.impl.decorators;

import org.aksw.gerbil.filter.Filter;
import org.aksw.gerbil.filter.cache.CachedResult;
import org.aksw.gerbil.filter.cache.FilterCache;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * A cache filter decorates a filter and provides access to the caching instance.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class CacheFilter extends FilterDecorator {

    private static final Logger LOGGER = LogManager.getLogger(CacheFilter.class);

    private FilterCache cache;

    public CacheFilter(Filter service, String cacheLocation) {
        super(service);

        try {
            this.cache = FilterCache.getInstance(cacheLocation);
        } catch (IOException e) {
            LOGGER.error("Cache instance could not created. " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName, String annotatorName) {
        List<String> resolvedEntities;

        String md5sum = CachedResult.generateMd5Checksum(entities);
        if (cache.isVersionCached(getConfiguration().getName(), datasetName, annotatorName, md5sum)) {
            resolvedEntities = cache.getCachedResults(getConfiguration().getName(), datasetName, annotatorName);
        } else {
            resolvedEntities = super.resolveEntities(entities, datasetName, annotatorName);
            cleanResult(resolvedEntities);
            CachedResult result = new CachedResult(getConfiguration().getName(), datasetName, annotatorName,
                     resolvedEntities.toArray(new String[resolvedEntities.size()]));
            result.setChecksum(md5sum);
            cache.cache(result);
        }
        return resolvedEntities;
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName) {
        List<String> resolvedEntities;

        String md5sum = CachedResult.generateMd5Checksum(entities);
        if (cache.isVersionCached(getConfiguration().getName(), datasetName, md5sum)) {
            resolvedEntities = cache.getCachedResults(getConfiguration().getName(), datasetName);
        } else {
            resolvedEntities = super.resolveEntities(entities, datasetName);
            //cleanResult(resolvedEntities);
            System.out.println(resolvedEntities);

            CachedResult result = new CachedResult(getConfiguration().getName(), datasetName,
                    resolvedEntities.toArray(new String[resolvedEntities.size()]));
            result.setChecksum(md5sum);
            cache.cache(result);
        }
        return resolvedEntities;
    }

    // clean empty IRIs
    private void cleanResult(List<String> result) {
        Iterator<String> itr = result.iterator();
        while (itr.hasNext()) {
            if (StringUtils.isEmpty(itr.next())) {
                itr.remove();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheFilter that = (CacheFilter) o;

        return !(cache != null ? !cache.equals(that.cache) : that.cache != null);

    }

    @Override
    public int hashCode() {
        return cache != null ? cache.hashCode() : 0;
    }
}
