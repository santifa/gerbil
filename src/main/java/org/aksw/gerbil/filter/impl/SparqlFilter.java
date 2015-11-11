package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.EntityFilter;

import java.util.List;

/**
 * Created by ratzeputz on 08.11.15.
 */
public class SparqlFilter<FilterConfiguration> implements EntityFilter {

    private FilterConfiguration conf;

    public SparqlFilter(FilterConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public List<List> filterEntities(List goldStandard, List annotatorResult) {
        return null;
    }
}
