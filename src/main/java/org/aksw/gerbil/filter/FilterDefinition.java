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

    /**
     * Instantiates a new Filter definition.
     *
     * @param name         the name
     * @param filter       the filter
     * @param whiteList    the white list
     * @param serviceLocation provide one either a url for a sparql endpoint or a file location
     */
    public FilterDefinition(String name, String filter, List<String> whiteList, String serviceLocation) {
        this.name = name;
        this.filter = filter;
        this.entityUriWhitelist = whiteList;
        this.serviceLocation = serviceLocation;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterDefinition that = (FilterDefinition) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (filter != null ? !filter.equals(that.filter) : that.filter != null) return false;
        if (entityUriWhitelist != null ? !entityUriWhitelist.equals(that.entityUriWhitelist) : that.entityUriWhitelist != null)
            return false;
        return !(serviceLocation != null ? !serviceLocation.equals(that.serviceLocation) : that.serviceLocation != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (filter != null ? filter.hashCode() : 0);
        result = 31 * result + (entityUriWhitelist != null ? entityUriWhitelist.hashCode() : 0);
        result = 31 * result + (serviceLocation != null ? serviceLocation.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FilterDefinition{" +
                "name='" + name + '\'' +
                ", filter='" + filter + '\'' +
                ", entityUriWhitelist=" + entityUriWhitelist +
                ", serviceLocation='" + serviceLocation + '\'' +
                '}';
    }
}
