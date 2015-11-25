package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.FilterStep;
import org.aksw.gerbil.filter.FilterDefinition;
import org.aksw.gerbil.filter.cache.CachedResult;
import org.aksw.gerbil.filter.cache.FilterCache;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ratzeputz on 24.11.15.
 */
public class CacheFilterStep extends FilterStepDecorator {

    private static final Logger LOGGER = LogManager.getLogger(CacheFilterStep.class);

    private FilterCache cache;

    public CacheFilterStep(FilterStep service, String cacheLocation) {
        super(service);

        try {
            this.cache = FilterCache.getInstance(cacheLocation);
        } catch (IOException e) {
            LOGGER.error("Cache instance could not created. " + e.getMessage(), e);
        }
    }

    @Override
    public String[] resolveEntities(String[] entities, FilterDefinition conf, String datasetName, String annotatorName) {
        String[] resolvedEntities = new String[0];

        try {
            String md5sum = CachedResult.generateMd5Checksum(entities);

            if (cache.isVersionCached(conf.getName(), datasetName, annotatorName, md5sum)) {
                resolvedEntities = cache.getCachedResults(conf.getName(), datasetName, annotatorName);
            } else {
                resolvedEntities = super.resolveEntities(entities, conf, datasetName, annotatorName);
                CachedResult result = new CachedResult(conf.getName(), datasetName, annotatorName, resolvedEntities);
                result.setChecksum(md5sum);
                cache.cache(result);
            }
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Could not calculate md5 checksum for caching. " + e.getMessage(), e);
        }

        return resolvedEntities;
    }

    @Override
    public String[] resolveEntities(String[] entities, FilterDefinition conf, String datasetName) {
        String[] resolvedEntities = new String[0];

        try {
            String md5sum = CachedResult.generateMd5Checksum(entities);

            if (cache.isVersionCached(conf.getName(), datasetName, md5sum)) {
                resolvedEntities = cache.getCachedResults(conf.getName(), datasetName);
            } else {
                resolvedEntities = super.resolveEntities(entities, conf, datasetName);
                CachedResult result = new CachedResult(conf.getName(), datasetName, resolvedEntities);
                result.setChecksum(md5sum);
                cache.cache(result);
            }
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Could not calculate md5 checksum for caching. " + e.getMessage(), e);
        }

        return resolvedEntities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheFilterStep that = (CacheFilterStep) o;

        return !(cache != null ? !cache.equals(that.cache) : that.cache != null);

    }

    @Override
    public int hashCode() {
        return cache != null ? cache.hashCode() : 0;
    }
}
