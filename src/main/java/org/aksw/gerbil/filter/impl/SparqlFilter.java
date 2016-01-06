package org.aksw.gerbil.filter.impl;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.aksw.gerbil.filter.FilterDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A SparqlFilter is a concrete filter that filters the given entities by
 * asking a knowledge base endpoint like Dbpedia via sparql.
 * <p/>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class SparqlFilter extends ConcreteFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SparqlFilter.class);

    /**
     * Instantiates a new Dbpedia entity resolution.
     *
     * @param prefixes   the prefixes for all sparql questions
     */
    public SparqlFilter(FilterDefinition def, String[] prefixes) throws InstantiationException {
        super(def, prefixes);
        checkEndpoint();
    }

    // check if the endpoint is available
    private void checkEndpoint() throws InstantiationException {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(def.getServiceLocation()).openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            connection.disconnect();

            if (responseCode != 200) {
                throw new InstantiationException("Endpoint not reachable. " + def.getServiceLocation());
            }
        } catch (IOException e) {
            throw new InstantiationException("Endpoint not reachable. " +
                    def.getServiceLocation() + " " + e.getMessage());
        }
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName, String annotatorName) {
        try {
            return resolve(entities);
        } catch (IOException e) {
            LOGGER.error("Entities could not resolved, returning nothing. : " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName) {
        try {
            return resolve(entities);
        } catch (IOException e) {
            LOGGER.error("Entities could not resolved, returning nothing. : " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }


    private List<String> resolve(List<String> entities)  throws IOException {
        List<String> result = new ArrayList<>(entities.size());
        String queryString = buildQuery(entities);

        try {
            // prevent from exiting the whole task if we get malformed uris for querying
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(def.getServiceLocation(), query);
            qexec.setTimeout(50000, 100000); // set timeout until first answer to fifty seconds and overall timeout to one minute
            ResultSet queryResult = qexec.execSelect();

            while (queryResult.hasNext()) {
                QuerySolution solution = queryResult.nextSolution();
                RDFNode node = solution.get("v");
                result.add(node.asResource().getURI());
            }

            qexec.close();
        } catch (Exception e) {
            throw new IOException("Could not retrieve answer from " + def.getServiceLocation()
                    + " for query " + queryString + " ; Skipping... " + e.getMessage());
        }

        return result;
    }


    @Override
    public String toString() {
        return "SparqlFilter{ " + super.toString() + " }";
    }

    @Override
    Object cloneChild() throws InstantiationException {
        return new SparqlFilter(getConfiguration(), prefixMap);
    }
}
