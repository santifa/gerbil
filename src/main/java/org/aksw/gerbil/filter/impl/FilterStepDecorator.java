package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.FilterStep;
import org.aksw.gerbil.filter.FilterDefinition;

/**
 * Abstract class for decoration of the entity resolution.
 *
 * Created by Henrik Jürges on 24.11.15.
 */
public class FilterStepDecorator implements FilterStep {

    private FilterStep decoratedService;

    public FilterStepDecorator(FilterStep service) {
        this.decoratedService = service;
    }

    @Override
    public String[] resolveEntities(String[] entities, FilterDefinition conf, String datasetName, String annotatorName) {
        return decoratedService.resolveEntities(entities, conf, datasetName, annotatorName);
    }

    @Override
    public String[] resolveEntities(String[] entities, FilterDefinition conf, String datasetName) {
        return decoratedService.resolveEntities(entities, conf, datasetName);
    }
}
