package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.FilterDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class PopularityFilter extends ConcreteFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PopularityFilter.class);

    /**
     * Instantiates a new Concrete filter.
     *
     * @param def      the def
     * @param prefixes the prefixes
     */
    public PopularityFilter(FilterDefinition def, String[] prefixes) {
        super(def, prefixes);
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName, String annotatorName) {
        System.out.println("limit einsetzen.");
        return Collections.emptyList();
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName) {
        System.out.println("limit einsetzen.");
        return Collections.emptyList();
    }
}
