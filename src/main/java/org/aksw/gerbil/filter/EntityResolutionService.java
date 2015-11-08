package org.aksw.gerbil.filter;

/**
 * An entity resolution service looks up the entity name or id and
 * returns the found type or an empty string. As well as searching
 * for same as types.
 * <p/>
 * Created by Henrik Jürges on 07.11.15.
 */
public interface EntityResolutionService {

    /**
     * Gets the first found type.
     *
     * @param entityName the entity name
     * @return the type or null
     */
    abstract String getType(String entityName);

    /**
     * Get all types of the entity as well as same as relations.
     *
     * @param enityName the enity name
     * @return the array of all types or null
     */
    abstract String[] getSameAsTypes(String enityName);
}
