package org.aksw.gerbil.filter.impl;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.aksw.gerbil.filter.EntityResolutionService;
import org.aksw.gerbil.filter.FilterConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    private final static String PREFIX = "PREFIX";

    /**
     * Instantiates a new Dbpedia entity resolution.
     *
     * @param serviceUrl the service url
     */
    public SparqlEntityResolution(String serviceUrl, String[] prefixes) {
        this.serviceUrl = serviceUrl;
        setPrefixSet(prefixes);
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
    public String[] resolveEntities(String[] entities, FilterConfiguration conf, String datasetName, String annotatorName) {
        try {
            return resolve(entities, conf.getFilter());
        } catch (IOException e) {
            LOGGER.error("Entities could not resolved, returning nothing. : " + e.getMessage(), e);
            return new String[0];
        }
    }

    @Override
    public String[] resolveEntities(String[] entities, FilterConfiguration conf, String datasetName) {
        try {
            return resolve(entities, conf.getFilter());
        } catch (IOException e) {
            LOGGER.error("Entities could not resolved, returning nothing. : " + e.getMessage(), e);
            return new String[0];
        }
    }


    private String[] resolve(String[] entities, String filter)  throws IOException {
        List<String> result = new ArrayList<>(entities.length);
        String queryString = buildQuery(entities, filter);
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

  /*  // checks whether the resolution is cached
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
    }*/

    @Override
    public String toString() {
        return "SparqlEntityResolution{" +
                "prefixSet='" + prefixSet + '\'' +
                ", serviceUrl='" + serviceUrl + '\'' +
                '}';
    }
}
