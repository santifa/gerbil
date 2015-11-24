package org.aksw.gerbil.filter;

/**
 * An entity resolution service looks up a number of entities
 * for a filter definition.
 * <p/>
 * Created by Henrik Jürges on 07.11.15.
 */
public interface EntityResolutionService {

    /**
     * Sets a prefix set, mostly a combination of a short form
     * and an uri as long form. E.g. foaf:&lt http://xmlns.com/foaf/0.1/&gt
     *
     * @param prefixes the prefixes
     */
    void setPrefixSet(String[] prefixes);

    /**
     * Resolve entities from an annotator result.
     *
     * @param entities      the entities
     * @param conf          the conf
     * @param datasetName   the dataset name
     * @param annotatorName the annotator name
     * @return the string [ ]
     */
    String[] resolveEntities(String[] entities, FilterConfiguration conf, String datasetName, String annotatorName);

    /**
     * Resolve entities from a gold standard.
     *
     * @param entities    the entities
     * @param conf        the conf
     * @param datasetName the dataset name
     * @return the string [ ]
     */
    String[] resolveEntities(String[] entities, FilterConfiguration conf, String datasetName);
}
