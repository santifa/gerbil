package org.aksw.gerbil.filter.impl;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.aksw.gerbil.filter.FilterDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
    public SparqlFilter(FilterDefinition def, String[] prefixes) {
        super(def, prefixes);
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName, String annotatorName) {
        try {
            return resolve(entities, def.getFilter());
        } catch (IOException e) {
            LOGGER.error("Entities could not resolved, returning nothing. : " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName) {
        try {
            return resolve(entities, def.getFilter());
        } catch (IOException e) {
            LOGGER.error("Entities could not resolved, returning nothing. : " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }


    private List<String> resolve(List<String> entities, String filter)  throws IOException {
        List<String> result = new ArrayList<>(entities.size());
        String queryString = buildQuery(entities, filter);
        Query query = QueryFactory.create(queryString);

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(def.getServiceLocation(), query)) {
            qexec.setTimeout(1000, 5000); // set timeout until first answer to one second and overall timeout to 5 seconds
            ResultSet queryResult = qexec.execSelect();

            while (queryResult.hasNext()) {
                QuerySolution solution = queryResult.nextSolution();
                RDFNode node = solution.get("v");
                result.add(node.asResource().getURI());
            }
        } catch (Exception e) {
            throw new IOException("Could not retrieve answer from " + def.getServiceLocation() + " ; Skipping... " + e.getMessage());
        }

        return result;
    }


    @Override
    public String toString() {
        return "SparqlFilter{ " + super.toString() + " }";
    }

    @Override
    Object cloneChild() {
        return new SparqlFilter(getConfiguration(), prefixMap);
    }
}
