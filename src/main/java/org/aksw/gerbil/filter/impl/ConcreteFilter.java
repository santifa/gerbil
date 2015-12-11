package org.aksw.gerbil.filter.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.aksw.gerbil.filter.Filter;
import org.aksw.gerbil.filter.FilterDefinition;
import org.apache.commons.lang.StringUtils;

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
        String prefix = "PREFIX ";

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
            builder.append(query.get(i));

            for (int j = 0; j < entities.size(); j++) {
                String entity = entities.get(j);
                entity = StringUtils.replaceOnce(entity, "<", "");
                builder.append(" <").append(entity).append("> ");
            }
            builder.append(query.get(i+1));
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

    @Override
    public String toString() {
        return "ConcreteFilter{" +
                "def=" + def +
                ", prefixMap=" + Arrays.toString(prefixMap) +
                ", prefixes='" + prefixes + '\'' +
                '}';
    }
}
