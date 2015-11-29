package org.aksw.gerbil.filter;

import java.util.List;

/**
 * An entity resolution service looks up a number of entities
 * for a filter definition.
 * <p/>
 * Created by Henrik Jürges on 07.11.15.
 */
public interface Filter {

    /**
     * Returns the filter configuration.
     *
     * @return the configuration
     */
    FilterDefinition getConfiguration();

    /**
     * Resolve entities from an annotator result.
     *
     * @param entities      the entities
     * @param datasetName   the dataset name
     * @param annotatorName the annotator name
     * @return the string [ ]
     */
    List<String> resolveEntities(List<String> entities, String datasetName, String annotatorName);

    /**
     * Resolve entities from a gold standard.
     *
     * @param entities    the entities
     * @param datasetName the dataset name
     * @return the string [ ]
     */
    List<String> resolveEntities(List<String> entities, String datasetName);
}
