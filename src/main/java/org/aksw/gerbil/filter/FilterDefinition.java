package org.aksw.gerbil.filter;

import java.util.List;

/**
 * This represents a simple filter configuration.
 * <p/>
 * Created by Henrik Jürges on 07.11.15.
 */
public class FilterDefinition {

    private String name;

    private String filter;

    private List<String> entityUriWhitelist;

    private String serviceLocation = "";

    private int chunksize = 0;

    /**
     * Instantiates a new Filter definition.
     *
     * @param name         the name
     * @param filter       the filter
     * @param whiteList    the white list
     * @param serviceLocation provide one either a url for a sparql endpoint or a file location
     */
    public FilterDefinition(String name, String filter, List<String> whiteList,
                            String serviceLocation) {
        this.name = name;
        this.filter = filter;
        this.entityUriWhitelist = whiteList;
        this.serviceLocation = serviceLocation;
    }

    /**
     * Instantiates a new Filter definition.
     *
     * @param name         the name
     * @param filter       the filter
     * @param whiteList    the white list
     * @param serviceLocation provide one either a url for a sparql endpoint or a file location
     */
    public FilterDefinition(String name, String filter, List<String> whiteList,
                            String serviceLocation, int chunksize) {
        this.name = name;
        this.filter = filter;
        this.entityUriWhitelist = whiteList;
        this.serviceLocation = serviceLocation;
        this.chunksize = chunksize;
    }

    /**
     * Gets name of a filter
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets string defined as .filter property
     *
     * @return the filter
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Get entity uri whitelist.
     *
     * @return the whitelist
     */
    public List<String> getEntityUriWhitelist() {
        return entityUriWhitelist;
    }

    /**
     * Gets file location.
     *
     * @return the file location
     */
    public String getServiceLocation() {
        return serviceLocation;
    }


    /**
     * Gets chunksize.
     *
     * @return the chunksize
     */
    public int getChunksize() {
        return chunksize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterDefinition that = (FilterDefinition) o;

        if (chunksize != that.chunksize) return false;
        if (!name.equals(that.name)) return false;
        if (!filter.equals(that.filter)) return false;
        if (!entityUriWhitelist.equals(that.entityUriWhitelist)) return false;
        return serviceLocation.equals(that.serviceLocation);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + filter.hashCode();
        result = 31 * result + entityUriWhitelist.hashCode();
        result = 31 * result + serviceLocation.hashCode();
        result = 31 * result + chunksize;
        return result;
    }

    @Override
    public String toString() {
        return "FilterDefinition{" +
                "name='" + name + '\'' +
                ", filter='" + filter + '\'' +
                ", entityUriWhitelist=" + entityUriWhitelist +
                ", serviceLocation='" + serviceLocation + '\'' +
                ", chunksize=" + chunksize +
                '}';
    }


}
