package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.Filter;
import org.aksw.gerbil.filter.FilterDefinition;
import org.apache.commons.lang.StringUtils;

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

    /**
     * The filter definition
     */
    protected FilterDefinition def;

    protected final String[] prefixMap;

    private String prefixes;

    /**
     * Instantiates a new Concrete filter.
     *
     * @param def      the def
     * @param prefixes the prefixes
     */
    public ConcreteFilter(FilterDefinition def, String[] prefixes) {
        this.def = def;
        this.prefixMap = prefixes;
        this.prefixes = buildPrefixes(prefixes);
    }


    @Override
    public FilterDefinition getConfiguration() {
        return def;
    }

    private String buildPrefixes(String[] prefixes) {
        String prefix = "PREFIX ";

        // create sparql query prefix
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < prefixes.length; i++) {
            if (!StringUtils.isEmpty(prefixes[i])) {
                builder.append(prefix).append(prefixes[i]).append(" ");
            }
        }
        return builder.toString();
    }


    /**
     * Build the query string.
     *
     * @param entities the entities
     * @param filter   the filter
     * @return the query
     */
    protected String buildQuery(List<String> entities, String filter) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefixes);

        if (StringUtils.contains(filter, "##")) {
            String[] filterParts = StringUtils.split(filter, "##");

            // insert between every part all entities
            for (int i = 0; i < filterParts.length && i % 2 == 0; i =+ 2) {
                builder.append(filterParts[i]);

                for (int j = 0; j < entities.size(); j++) {
                    builder.append("<").append(entities.get(j)).append(">").append(" ");
                }
                builder.append(filterParts[i+1]);
            }
        }
        return builder.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return cloneChild();
    }

    /**
     * A child object provide a clone method through this.
     *
     * @return the child object
     */
    abstract Object cloneChild();
}
