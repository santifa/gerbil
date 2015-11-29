package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.FilterWrapper;
import org.aksw.gerbil.filter.Filter;
import org.aksw.gerbil.filter.FilterDefinition;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.Meaning;
import org.aksw.gerbil.transfer.nif.TypedSpan;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * A "normal" filter denotes an unboxing or wrapping between the
 * filter steps entities and the entities in the outside world.
 * Or more easier it knows how to get the URI strings for all the other filter steps.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class NormalFilterWrapper implements FilterWrapper {

    private final static Logger LOGGER = LogManager.getLogger(NormalFilterWrapper.class);

    private Filter service;

    public NormalFilterWrapper(Filter service) {
        this.service = service;
    }

    @Override
    public FilterDefinition getConfig() {
        return service.getConfiguration();
    }

    @Override
    public <E extends Marking> List<List<E>> filterGoldstandard(List<List<E>> entities, String datasetName) {
        List<String> entityNames = collectEntityNames(entities);
        List<String> resolvedEntities = service.resolveEntities(entityNames, datasetName);
        return removeUnresolvedEntites(entities, resolvedEntities);
    }

    @Override
    public <E extends Marking> List<List<E>> filterAnnotatorResults(List<List<E>> entities, String datasetName, String annotatorName) {
        List<String> entityNames = collectEntityNames(entities);
        List<String> resolvedEntities = service.resolveEntities(entityNames, datasetName, annotatorName);
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

    private <E extends Marking> List<List<E>> removeUnresolvedEntites(List<List<E>> document, List<String> resolvedEntites) {
        List<List<E>> result = new ArrayList<>();

        for (List<E> documentPart : document) {
            List<E> partialResult = new ArrayList<>();

            for (E entity : documentPart) {
                if (entity instanceof Meaning) {
                    boolean found = false;
                    for (int i = 0; i < resolvedEntites.size(); i++) {
                        if (((Meaning) entity).containsUri(resolvedEntites.get(i))) {
                            found = true;
                        }
                    }

                    if (found) {
                        partialResult.add(entity);
                    }
                } else if (entity instanceof TypedSpan) {
                    boolean found = false;
                    for (int i = 0; i < resolvedEntites.size(); i++) {
                        if (((TypedSpan) entity).getTypes().contains(resolvedEntites.get(i))) {
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

        NormalFilterWrapper that = (NormalFilterWrapper) o;

        return !(service != null ? !service.equals(that.service) : that.service != null);

    }

    @Override
    public int hashCode() {
        return service != null ? service.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "NormalFilterWrapper{" +
                ", service=" + service +
                '}';
    }
}
