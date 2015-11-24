package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.EntityResolutionService;
import org.aksw.gerbil.filter.FilterConfiguration;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Splits the list of entities into chunks and collect the
 * partial results.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class ChunkEntityResolution extends DecoratorEntityResolution {

    private int chunkSize;

    public ChunkEntityResolution(EntityResolutionService service, int chunkSize) {
        super(service);
        this.chunkSize = chunkSize;
    }

    @Override
    public String[] resolveEntities(String[] entities, FilterConfiguration conf, String datasetName, String annotatorName) {
        if (chunkSize >= entities.length) {
            return super.resolveEntities(entities, conf, datasetName, annotatorName);
        } else {
            return chunk(entities, conf, datasetName, annotatorName);
        }
    }

    @Override
    public String[] resolveEntities(String[] entities, FilterConfiguration conf, String datasetName) {
        if (chunkSize >= entities.length) {
            return super.resolveEntities(entities, conf, datasetName);
        } else {
            return chunk(entities, conf, datasetName, "");
        }
    }

    private String[] chunk(String[] entities, FilterConfiguration conf, String datasetName, String annotatorName) {
        int parts = entities.length / chunkSize;
        int lastPart = entities.length % chunkSize;
        List<String> result = new ArrayList<>(entities.length);

        // chunk the main part of the list
        for (int i = 0; i < parts; i++) {
            String[] subArray = subArray(entities, i * chunkSize, i * chunkSize + (chunkSize - 1));

            if (StringUtils.isEmpty(annotatorName)) {
                subArray = super.resolveEntities(subArray, conf, datasetName);
            } else {
                subArray = super.resolveEntities(subArray, conf, datasetName, annotatorName);
            }
            Collections.addAll(result, subArray);
        }

        // collect the last chunk
        if (lastPart != 0) {
            String[] lastArray;
            if (StringUtils.isEmpty(annotatorName)) {
                lastArray = super.resolveEntities(
                        subArray(entities, entities.length - lastPart, entities.length), conf, datasetName);
            } else {
                lastArray = super.resolveEntities(
                        subArray(entities, entities.length - lastPart, entities.length), conf, datasetName, annotatorName);
            }
            Collections.addAll(result, lastArray);
        }

        return result.toArray(new String[result.size()]);
    }

    public static String[] subArray(String[] a, int startIdx, int endIdx) {
        ArrayList<String> b = new ArrayList<>();

        for(int k = startIdx; k < endIdx; ++k) {
            b.add(a[k]);
        }

        return b.toArray(new String[b.size()]);
    }
}
