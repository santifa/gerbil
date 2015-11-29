package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.Filter;
import org.aksw.gerbil.filter.FilterDefinition;

import java.util.List;

/**
 * Abstract class for decorating the filter objects.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class FilterDecorator implements Filter {

    private Filter decoratedService;

    public FilterDecorator(Filter service) {
        this.decoratedService = service;
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName, String annotatorName) {
        return decoratedService.resolveEntities(entities, datasetName, annotatorName);
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName) {
        return decoratedService.resolveEntities(entities, datasetName);
    }

    @Override
    public FilterDefinition getConfiguration() {
        return decoratedService.getConfiguration();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterDecorator that = (FilterDecorator) o;

        return !(decoratedService != null ? !decoratedService.equals(that.decoratedService) : that.decoratedService != null);

    }

    @Override
    public int hashCode() {
        return decoratedService != null ? decoratedService.hashCode() : 0;
    }
}
