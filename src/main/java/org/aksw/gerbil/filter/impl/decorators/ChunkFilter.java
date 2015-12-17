package org.aksw.gerbil.filter.impl.decorators;

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
            return workOffChunks(chunk(entities), datasetName, annotatorName);
        }
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName) {
        if (chunkSize >= entities.size()) {
            return super.resolveEntities(entities, datasetName);
        } else {
            return workOffChunks(chunk(entities), datasetName, "");
        }
    }

    // split into parts
    private List<List<String>> chunk(List<String> entities) {
        int parts = entities.size() / chunkSize;
        int lastPart = entities.size() % chunkSize;
        List<List<String>> chunks = new ArrayList<>(parts + 1);

        for (int i = 0; i < parts; i++) {
            chunks.add(entities.subList(i * chunkSize, i * chunkSize + (chunkSize - 1)));
        }

        if (lastPart != 0) {
            chunks.add(entities.subList(entities.size() - lastPart, entities.size()));
        }
        return chunks;
    }

    // ask the filter to resolve chunks
    private List<String> workOffChunks(List<List<String>> chunks, String datasetName, String annotatorName) {
        List<String> result = new ArrayList<>();
        for (List<String> chunk : chunks) {
            if (StringUtils.isEmpty(annotatorName)) {
                result.addAll(super.resolveEntities(chunk, datasetName));
            } else {
                result.addAll(super.resolveEntities(chunk, datasetName, annotatorName));
            }
        }
        return result;
    }
}
