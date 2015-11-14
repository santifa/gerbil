package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.EntityFilter;
import org.aksw.gerbil.filter.FilterConfiguration;
import org.aksw.gerbil.transfer.nif.Marking;

import java.util.List;

/**
 * A basic id filter, it returns it's input and is used for an unfiltered task result.
 *
 * Created by Henrik Jürges on 13.11.15.
 */
public class NullFilter implements EntityFilter {

    private final static FilterConfiguration CONF = new FilterConfiguration("nofilter", "");


    @Override
    public FilterConfiguration getConfig() {
        return CONF;
    }

    @Override
    public <E extends Marking> List<E> filterGoldstandard(List<E> entities, String datasetName) {
        return entities;
    }

    @Override
    public <E extends Marking> List<E> filterAnnotatorResults(List<E> entities, String datasetName, String annotatorName) {
        return entities;
    }
}
