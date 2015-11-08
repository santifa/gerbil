package org.aksw.gerbil.filter;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DbpediaEntityResolution performs regular sparql queries and returns
 * the entity types preserving owl#Thing.
 *
 * Created by Henrik Jürges on 07.11.15.
 */
public class DbpediaEntityResolution implements EntityResolutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbpediaEntityResolution.class);

    private String serviceUrl;

    private final String prefixSet;

    private final String[] prefixes;

    private final static String ONTOLOGY_START = "http://www.w3.org/2002/07/owl#Thing";
    private final static String PREFIX = "PREFIX";
    private final static String TYPE_QUERY = "SELECT ?type WHERE { ?name a ?type . } LIMIT 2";

    public DbpediaEntityResolution(String serviceUrl, String[] prefixes) {
        this.serviceUrl = serviceUrl;
        this.prefixes = prefixes;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < prefixes.length; i++) {
            builder.append(PREFIX).append(" ").append(prefixes[i]);
        }
        this.prefixSet = builder.append(" ").toString();
    }


    @Override
    public String getType(String entityName) {
        // FIXME ugly workaround for replacing entities, querysolutionmap is not working
        Query query = QueryFactory.create(prefixSet + StringUtils.replace(TYPE_QUERY, "?name", "<" + entityName + ">"));

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(serviceUrl, query)) {
            ResultSet result = qexec.execSelect();

            // fetch first two types
            while (result.hasNext()) {
                QuerySolution sol = result.next();
                RDFNode type = sol.get("?type");

                // if we don't have the owl#Thing type, we have our first type to return
                if (!StringUtils.equals(ONTOLOGY_START, type.asResource().getURI())) {
                    return shortenUri(type.asResource().getNameSpace(), type.asResource().getLocalName());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Could not type for fetch entity " + entityName, e.getMessage(), e);
        }
        return "";
    }

    @Override
    public String[] getSameAsTypes(String enityName) {
        return new String[0];
    }

    // replacing namespaces with prefixes
    private String shortenUri(String namespace, String localName) {
        for (int i = 0; i < prefixes.length; i++) {
            if (StringUtils.contains(prefixes[i], namespace)) {
                return StringUtils.substringBefore(prefixes[i], ":") + ":" + localName;
            }
        }
        return namespace + localName;
    }
}
