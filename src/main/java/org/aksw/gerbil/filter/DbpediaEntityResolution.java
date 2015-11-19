package org.aksw.gerbil.filter;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.aksw.gerbil.filter.cache.CachedResult;
import org.aksw.gerbil.filter.cache.FilterCache;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * A DbpediaEntityResolution performs regular sparql queries and returns
 * the entity types preserving owl#Thing.
 * <p/>
 * Created by Henrik Jürges on 07.11.15.
 */
public class DbpediaEntityResolution implements EntityResolutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbpediaEntityResolution.class);

    private String serviceUrl;

    private String prefixSet;

    private String[] prefixes;

    private FilterCache cache;

    private final static String PREFIX = "PREFIX";

    private final static String HEAD = "SELECT DISTINCT ?v WHERE { values ?v { ";

    /**
     * Instantiates a new Dbpedia entity resolution.
     *
     * @param serviceUrl the service url
     */
    public DbpediaEntityResolution(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }


    @Override
    public void setPrefixSet(String[] prefixes) {
        this.prefixes = prefixes;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < prefixes.length; i++) {
            builder.append(PREFIX).append(" ").append(prefixes[i]).append(" ");
        }
        this.prefixSet = builder.toString();
    }

    @Override
    public void initCache(FilterCache cache) {
        this.cache = cache;
    }

    @Override
    public void precache() {

    }

    @Override
    public String[] resolveEntities(String[] entities, FilterConfiguration conf, String datasetName,String annotatorName) {
        String[] result;

        if (cache != null && isCached(entities, conf.getName(), datasetName, annotatorName)) {
            result = getFromCache(conf.getName(), datasetName, annotatorName);
        } else {
            result = resolve(entities, conf.getFilter());
            cacheResults(entities, conf.getName(), datasetName, annotatorName);
        }
        return result;
    }

    @Override
    public String[] resolveEntities(String[] entities, FilterConfiguration conf, String datasetName) {
        String[] result;

        if (cache != null && isCached(entities, conf.getName(), datasetName, "")) {
            result = getFromCache(conf.getName(), datasetName, "");
        } else {
            result = resolve(entities, conf.getFilter());
            cacheResults(result, conf.getName(), datasetName, "");
        }
        return result;
    }

    private void cacheResults(String[] entities, String filterName, String datasetName, String annotatorName) {
        try {
            String md5sum = CachedResult.generateMd5Checksum(entities);

            if (StringUtils.isEmpty(annotatorName)) {
                CachedResult result = new CachedResult(filterName, datasetName, entities);
                result.setChecksum(md5sum);
                cache.cache(result);
            } else {
                CachedResult result = new CachedResult(filterName, datasetName, annotatorName, entities);
                result.setChecksum(md5sum);
                cache.cache(result);
            }
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Caching results failed. Moving on. " + e.getMessage(), e);
        }
    }

    private String[] resolve(String[] entities, String filter) {
        List<String> result = new ArrayList<>(entities.length);
        String queryString = buildQuery(entities, filter);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Filter query is: " + queryString);
        }
        LOGGER.error("Filter query is: " + queryString);
        Query query = QueryFactory.create(queryString);

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(serviceUrl, query)) {
            ResultSet queryResult = qexec.execSelect();

            while (queryResult.hasNext()) {
                QuerySolution solution = queryResult.nextSolution();
                RDFNode node = solution.get("v");
                result.add(node.asResource().getURI());
            }
        }

        return result.toArray(new String[result.size()]);
    }

    private String buildQuery(String[] entities, String filter) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefixSet).append(HEAD);
        for (int i = 0; i < entities.length; i++) {
            builder.append("<").append(entities[i]).append(">").append(" ");
        }
        builder.append("} ").append(filter);
        return builder.toString();
    }

    // checks whether the resolution is cached
    private boolean isCached(String[] entities, String filterName, String datasetName, String annotatorName) {
        boolean result = false;
        try {
            String md5sum = CachedResult.generateMd5Checksum(entities);
            if (StringUtils.isEmpty(annotatorName)) {
                result = cache.isVersionCached(filterName, datasetName, md5sum);
            } else {
                result = cache.isVersionCached(filterName, datasetName, annotatorName, md5sum);
            }
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("MD5 checksum algorithm not found. Could not fetch if result is cached or not." +
                    "Assuming not. " + e.getMessage(), e);
        }
        return result;
    }

    // retrieves entites from cache
    private String[] getFromCache(String filterName, String datasetName, String annotatorName) {
        if (StringUtils.isEmpty(annotatorName)) {
            return cache.getCachedResults(filterName, datasetName);
        } else {
            return cache.getCachedResults(filterName, datasetName, annotatorName);
        }
    }

    @Override
    public String toString() {
        return "DbpediaEntityResolution{" +
                "prefixSet='" + prefixSet + '\'' +
                ", serviceUrl='" + serviceUrl + '\'' +
                '}';
    }
}
