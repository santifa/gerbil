package org.aksw.gerbil.filter.impl;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.aksw.gerbil.filter.FilterStep;
import org.aksw.gerbil.filter.FilterConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A SparqlFilterStep is a concrete filter it filters the given entities by
 * asking a knowledge base like Dbpedia.
 * <p/>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class SparqlFilterStep implements FilterStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(SparqlFilterStep.class);

    private String serviceUrl;

    private String prefixSet;

    private final static String PREFIX = "PREFIX ";

    /**
     * Instantiates a new Dbpedia entity resolution.
     *
     * @param serviceUrl the service url or knowledge base
     * @param prefixes   the prefixes for all sparql questions
     */
    public SparqlFilterStep(String serviceUrl, String[] prefixes) {
        this.serviceUrl = serviceUrl;
        setPrefixSet(prefixes);
    }

    private void setPrefixSet(String[] prefixes) {
        // create sparql query prefix
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < prefixes.length; i++) {
            builder.append(PREFIX).append(prefixes[i]).append(" ");
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

    @Override
    public String toString() {
        return "SparqlFilterStep{" +
                "prefixSet='" + prefixSet + '\'' +
                ", serviceUrl='" + serviceUrl + '\'' +
                '}';
    }
}
