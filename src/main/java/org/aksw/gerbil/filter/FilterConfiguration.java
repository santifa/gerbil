package org.aksw.gerbil.filter;

/**
 * This represents a simple filter configuration.
 * <p/>
 * Created by Henrik Jürges on 07.11.15.
 */
public class FilterConfiguration {

    private String name;

    private String filter;

    /**
     * Instantiates a new Filter configuration.
     *
     * @param name   the name
     * @param filter the filter
     */
    public FilterConfiguration(String name, String filter) {
        this.name = name;
        this.filter = filter;
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
     * Gets name of a filter
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", filter='" + filter + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterConfiguration that = (FilterConfiguration) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return !(filter != null ? !filter.equals(that.filter) : that.filter != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (filter != null ? filter.hashCode() : 0);
        return result;
    }
}
