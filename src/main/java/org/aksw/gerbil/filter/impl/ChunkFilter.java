package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.Filter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits the list of entities into chunks and collect the
 * partial results.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class ChunkFilter extends FilterDecorator {

    private int chunkSize;

    public ChunkFilter(Filter service, int chunkSize) {
        super(service);
        this.chunkSize = chunkSize;
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName, String annotatorName) {
        if (chunkSize >= entities.size()) {
            return super.resolveEntities(entities, datasetName, annotatorName);
        } else {
            return chunk(entities, datasetName, annotatorName);
        }
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName) {
        if (chunkSize >= entities.size()) {
            return super.resolveEntities(entities, datasetName);
        } else {
            return chunk(entities, datasetName, "");
        }
    }

    private List<String> chunk(List<String> entities, String datasetName, String annotatorName) {
        int parts = entities.size() / chunkSize;
        int lastPart = entities.size() % chunkSize;
        List<String> result = new ArrayList<>(entities.size());

        // chunk the main part of the list
        for (int i = 0; i < parts; i++) {
            List<String> subArray;

            if (StringUtils.isEmpty(annotatorName)) {
                subArray = super.resolveEntities(
                        entities.subList(i * chunkSize, i * chunkSize + (chunkSize - 1)), datasetName);
            } else {
                subArray = super.resolveEntities(
                        entities.subList(i * chunkSize, i * chunkSize + (chunkSize - 1)), datasetName, annotatorName);
            }
            result.addAll(subArray);
        }

        // collect the last chunk
        if (lastPart != 0) {
            List<String> lastArray;
            if (StringUtils.isEmpty(annotatorName)) {
                lastArray = super.resolveEntities(
                        entities.subList(entities.size() - lastPart, entities.size()), datasetName);
            } else {
                lastArray = super.resolveEntities(
                        entities.subList(entities.size() - lastPart, entities.size()), datasetName, annotatorName);
            }
            result.addAll(lastArray);
        }

        return result;
    }
}
