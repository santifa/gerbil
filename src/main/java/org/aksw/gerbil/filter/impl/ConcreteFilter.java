package org.aksw.gerbil.filter.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.aksw.gerbil.filter.Filter;
import org.aksw.gerbil.filter.FilterDefinition;
import org.aksw.gerbil.filter.impl.decorators.FilterDecorator;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * The concrete filter is used for all basic filter classes.
 * Extend this class to implement new filter classes.<br/>
 * All concrete filter are decorated using the {@link FilterDecorator}
 * by the filter factory.<br/>
 * For proper loading all subclasses has to have the same constructor as this class.
 * <p/>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public abstract class ConcreteFilter implements Filter, Cloneable {

    private final static Logger LOGGER = LogManager.getLogger(ConcreteFilter.class);

    private static IRIFactory iriFactory = IRIFactory.semanticWebImplementation();

    /**
     * The filter definition
     */
    protected FilterDefinition def;

    protected final String[] prefixMap;

    private String prefixes;

    private List<String> query;

    /**
     * Instantiates a new Concrete filter.
     *
     * @param def      the def
     * @param prefixes the prefixes
     */
    public ConcreteFilter(FilterDefinition def, String[] prefixes) {
        this.def = def;
        this.prefixMap = prefixes;
        prepareQuery();
    }


    @Override
    public FilterDefinition getConfiguration() {
        return def;
    }

    private void prepareQuery() {
        this.prefixes = buildPrefixes(prefixMap);
        this.query = Splitter.on("##").splitToList(def.getFilter());

    }

    private String buildPrefixes(String[] prefixes) {
        String prefix = " PREFIX ";

        // create sparql query prefix
        StringBuilder builder = new StringBuilder();
        builder.append(prefix);
        Joiner.on(prefix).skipNulls().appendTo(builder, prefixes);
        return builder.append(' ').toString();
    }


    /**
     * Build the query string.
     *
     * @param entities the entities
     * @return the query
     */
    protected String buildQuery(List<String> entities) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefixes);

        for (int i = 0; i < query.size() && i % 2 == 0; i =+ 2) {
            builder.append(query.get(i)).append(' ');

            for (int j = 0; j < entities.size(); j++) {
                String entity = entities.get(j);
                // only valid iris pass this test;
                // this is for keeping the mess of non valid iris away from the filters and backends
                //entity = StringUtils.replace(entity, " ", "");
                IRI iri = iriFactory.create(entity);
                if (iri.hasViolation(false)) {
                    LOGGER.error("IRI violates the w3c standard; ignoring " + iri.toDisplayString());
                } else if (iri.hasViolation(true)) {
                    LOGGER.warn("IRI is not valid; using despite of " + iri.toDisplayString());
                    builder.append("<").append(entity).append("> ");
                } else {
                    builder.append("<").append(entity).append("> ");
                }
            }
            builder.append(' ').append(query.get(i+1));
        }
        return builder.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            return cloneChild();
        } catch (InstantiationException e) {
            LOGGER.error("Could not clone filter. " + def);
        }
        return null;
    }

    /**
     * A child object provide a clone method through this.
     *
     * @return the child object
     */
    abstract Object cloneChild() throws InstantiationException;

    @Override
    public String toString() {
        return "ConcreteFilter{" +
                "def=" + def +
                ", prefixMap=" + Arrays.toString(prefixMap) +
                ", prefixes='" + prefixes + '\'' +
                '}';
    }
}
