package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.EntityResolutionService;
import org.aksw.gerbil.filter.FilterConfiguration;

/**
 * Abstract class for decoration of the entity resolution.
 *
 * Created by Henrik Jürges on 24.11.15.
 */
public class DecoratorEntityResolution implements EntityResolutionService {

    private EntityResolutionService decoratedService;

    public DecoratorEntityResolution(EntityResolutionService service) {
        this.decoratedService = service;
    }

    @Override
    public void setPrefixSet(String[] prefixes) {
        decoratedService.setPrefixSet(prefixes);
    }

    @Override
    public String[] resolveEntities(String[] entities, FilterConfiguration conf, String datasetName, String annotatorName) {
        return decoratedService.resolveEntities(entities, conf, datasetName, annotatorName);
    }

    @Override
    public String[] resolveEntities(String[] entities, FilterConfiguration conf, String datasetName) {
        return decoratedService.resolveEntities(entities, conf, datasetName);
    }
}
