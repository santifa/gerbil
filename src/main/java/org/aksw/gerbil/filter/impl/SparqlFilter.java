package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.EntityFilter;
import org.aksw.gerbil.filter.FilterConfiguration;
import org.aksw.gerbil.transfer.nif.Marking;

import java.util.List;

/**
 * Created by ratzeputz on 08.11.15.
 */
public class SparqlFilter implements EntityFilter {

    private FilterConfiguration conf;

    public SparqlFilter(FilterConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public FilterConfiguration getConfig() {
        return conf;
    }

    @Override
    public <E extends Marking> List<E> filterGoldstandard(List<E> entities, String datasetName) {
        return null;
    }

    @Override
    public <E extends Marking> List<E> filterAnnotatorResults(List<E> entities, String datasetName, String annotatorName) {
        return null;
    }
}
