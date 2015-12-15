package org.aksw.gerbil.filter;

import java.util.List;

/**
 * A filter is applied on either the annotator result or the dataset gold standard.
 * The filter chain works as follows. The entities are unwrapped and only all the uris are collected as so called
 * "entity names". The uris are then tested if there are a valid iri (using jena) and "whitelisted"
 * in the filter.properties discarding everything not passing the test.
 * Then depending on the configuration there are split into parts and cached.
 * At least a concrete filter is applied and the filter result is transformed back into entities valid for evaluation.
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
