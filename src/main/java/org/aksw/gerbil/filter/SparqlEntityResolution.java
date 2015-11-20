package org.aksw.gerbil.filter;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.aksw.gerbil.filter.cache.CachedResult;
import org.aksw.gerbil.filter.cache.FilterCache;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * A SparqlEntityResolution performs regular sparql queries and returns
 * the entity types preserving owl#Thing.
 * <p/>
 * Created by Henrik Jürges on 07.11.15.
 */
public class SparqlEntityResolution implements EntityResolutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SparqlEntityResolution.class);

    private String serviceUrl;

    private String prefixSet;

    private FilterCache cache;

    private final static String PREFIX = "PREFIX";

    /**
     * Instantiates a new Dbpedia entity resolution.
     *
     * @param serviceUrl the service url
     */
    public SparqlEntityResolution(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }


    @Override
    public void setPrefixSet(String[] prefixes) {
        // create sparql query prefix
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
    public void precache(String[] entitites, FilterConfiguration conf, String datasetName) {
        if (cache != null && !isCached(entitites, conf.getName(), datasetName, "")) {
            try {
                String[] result = resolve(entitites, conf.getFilter());
                cacheResults(result, conf.getName(), datasetName, "");
            } catch (IOException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }

    }

    @Override
    public String[] resolveEntities(String[] entities, FilterConfiguration conf, String datasetName,String annotatorName) {
        String[] result;

        if (cache != null && isCached(entities, conf.getName(), datasetName, annotatorName)) {
            result = getFromCache(conf.getName(), datasetName, annotatorName);
        } else {
            try {
                result = resolve(entities, conf.getFilter());
                cacheResults(result, conf.getName(), datasetName, annotatorName);
            } catch (IOException e) {
                LOGGER.error("Catched: " + e.getMessage(), e);
                result = entities;
            }
        }
        return result;
    }

    @Override
    public String[] resolveEntities(String[] entities, FilterConfiguration conf, String datasetName) {
        String[] result;

        if (cache != null && isCached(entities, conf.getName(), datasetName, "")) {
            result = getFromCache(conf.getName(), datasetName, "");
        } else {
            try {
                result = resolve(entities, conf.getFilter());
                cacheResults(result, conf.getName(), datasetName, "");
            } catch (IOException e) {
                LOGGER.error("Catched: " + e.getMessage(), e);
                result = entities;
            }

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

    private String[] resolve(String[] entities, String filter)  throws IOException {
        List<String> result = new ArrayList<>(entities.length);
        String queryString = buildQuery(entities, filter);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Filter query is: " + queryString);
        }
        Query query = QueryFactory.create(queryString);

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(serviceUrl, query)) {
            ResultSet queryResult = qexec.execSelect();

            while (queryResult.hasNext()) {
                QuerySolution solution = queryResult.nextSolution();
                RDFNode node = solution.get("v");
                result.add(node.asResource().getURI());
            }
        } catch (Exception e) {
            throw new IOException("Could not retrieve answer from " + serviceUrl + " ; Skipping... " + e.getMessage());
        }

        return result.toArray(new String[result.size()]);
    }

    private String buildQuery(String[] entities, String filter) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefixSet);

        if (StringUtils.contains(filter, "##")) {
            String[] filterParts = StringUtils.split(filter, "##");

            // insert between every part all entities
            for (int i = 0; i < filterParts.length && i % 2 == 0; i =+ 2) {
                builder.append(filterParts[i]);

                for (int j = 0; j < entities.length; j++) {
                    builder.append("<").append(entities[j]).append(">").append(" ");
                }
                builder.append(filterParts[i+1]);
            }
        }
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
        return "SparqlEntityResolution{" +
                "prefixSet='" + prefixSet + '\'' +
                ", serviceUrl='" + serviceUrl + '\'' +
                '}';
    }
}