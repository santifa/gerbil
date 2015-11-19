package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.EntityFilter;
import org.aksw.gerbil.filter.EntityResolutionService;
import org.aksw.gerbil.filter.FilterConfiguration;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.Meaning;
import org.aksw.gerbil.transfer.nif.Span;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ratzeputz on 08.11.15.
 */
public class SparqlFilter implements EntityFilter {

    private final static Logger LOGGER = LogManager.getLogger(SparqlFilter.class);

    private FilterConfiguration conf;

    private EntityResolutionService service;

    public SparqlFilter(FilterConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public FilterConfiguration getConfig() {
        return conf;
    }

    @Override
    public void setEntityResolution(EntityResolutionService service) {
        this.service = service;
    }

    @Override
    public <E extends Marking> List<List<E>> filterGoldstandard(List<List<E>> entities, String datasetName) {
        List<String> entityNames = collectEntityNames(entities);
        String[] resolvedEntities = service.resolveEntities(entityNames.toArray(new String[entityNames.size()]), this.conf, datasetName);
        return removeUnresolvedEntites(entities, resolvedEntities);
    }

    @Override
    public <E extends Marking> List<List<E>> filterAnnotatorResults(List<List<E>> entities, String datasetName, String annotatorName) {
        List<String> entityNames = collectEntityNames(entities);
        String[] resolvedEntities = service.resolveEntities(entityNames.toArray(new String[entityNames.size()]), this.conf, datasetName, annotatorName);
        return removeUnresolvedEntites(entities, resolvedEntities);
    }


    // convert documents into a plain entity list
    // TODO implement span and typedspan
    private <E extends Marking> List<String> collectEntityNames(List<List<E>> document) {
        List<String> result = new ArrayList<>();

        for (List<E> documentPart : document) {
            for (E entity : documentPart) {
                if (entity instanceof Meaning) {
                    result.addAll(((Meaning) entity).getUris());
                } else if (entity instanceof Span) { }
            }
        }
        return result;
    }

    // TODO implement span and typedspan
    private <E extends Marking> List<List<E>> removeUnresolvedEntites(List<List<E>> document, String[] resolvedEntites) {
        System.out.println(Arrays.asList(resolvedEntites));
        List<List<E>> result = new ArrayList<>();

        for (List<E> documentPart : document) {
            List<E> partialResult = new ArrayList<>();

            for (E entity : documentPart) {
                if (entity instanceof Meaning) {
                    boolean found = false;
                    for (int i = 0; i < resolvedEntites.length; i++) {
                        if (((Meaning) entity).containsUri(resolvedEntites[i])) {
                            found = true;
                        }
                    }

                    if (found) {
                        partialResult.add(entity);
                    }
                } else if (entity instanceof Span) {
                    //FIXME workaround for spans
                    partialResult.add(entity);
                }
            }

            result.add(partialResult);
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SparqlFilter that = (SparqlFilter) o;

        if (!conf.equals(that.conf)) return false;
        return service.equals(that.service);

    }

    @Override
    public int hashCode() {
        int result = conf.hashCode();
        result = 31 * result + service.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SparqlFilter{" +
                "conf=" + conf +
                ", service=" + service +
                '}';
    }
}
