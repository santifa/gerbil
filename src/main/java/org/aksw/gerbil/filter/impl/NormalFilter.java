package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.EntityFilter;
import org.aksw.gerbil.filter.FilterDefinition;
import org.aksw.gerbil.filter.FilterStep;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.Meaning;
import org.aksw.gerbil.transfer.nif.TypedSpan;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * A "normal" filter denotes in near future an unboxing or wrapping between the
 * filter steps and the
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class NormalFilter implements EntityFilter {

    private final static Logger LOGGER = LogManager.getLogger(NormalFilter.class);

    private FilterDefinition conf;

    private FilterStep service;

    public NormalFilter(FilterDefinition conf) {
        this.conf = conf;
    }

    @Override
    public FilterDefinition getConfig() {
        return conf;
    }

    @Override
    public void setEntityResolution(FilterStep service) {
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
    private <E extends Marking> List<String> collectEntityNames(List<List<E>> document) {
        List<String> result = new ArrayList<>();

        for (List<E> documentPart : document) {
            for (E entity : documentPart) {
                if (entity instanceof Meaning) {
                    result.addAll(((Meaning) entity).getUris());


                } else if (entity instanceof TypedSpan) {
                    result.addAll(((TypedSpan) entity).getTypes());
                } else {
                    LOGGER.error("Unexpected Type. Can't apply filter, ignoring.");
                }
            }
        }
        return result;
    }

    private <E extends Marking> List<List<E>> removeUnresolvedEntites(List<List<E>> document, String[] resolvedEntites) {
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
                } else if (entity instanceof TypedSpan) {
                    boolean found = false;
                    for (int i = 0; i < resolvedEntites.length; i++) {
                        if (((TypedSpan) entity).getTypes().contains(resolvedEntites[i])) {
                            found = true;
                        }
                    }

                    if (found) {
                        partialResult.add(entity);
                    }
                } else {
                    LOGGER.error("Unexpected Type. Can't apply filter, returning original results.");
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

        NormalFilter that = (NormalFilter) o;

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
        return "NormalFilter{" +
                "conf=" + conf +
                ", service=" + service +
                '}';
    }
}
