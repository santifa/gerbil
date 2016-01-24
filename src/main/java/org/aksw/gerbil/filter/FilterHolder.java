package org.aksw.gerbil.filter;

import org.aksw.gerbil.filter.wrapper.FilterWrapper;
import org.aksw.gerbil.filter.wrapper.IdentityWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * A FilterHolder contains the actual filters to apply and
 * whether the goldstandard has to be cached before or not.
 * <p/>
 * <p/>
 * Created by Henrik Jürges on 19.11.15.
 */
public final class FilterHolder {

    private final List<FilterWrapper> filterList;

    /**
     * Instantiates a new Filter holder.
     *
     * @param filterList the filter list
     */
    public  FilterHolder(List<FilterWrapper> filterList) {
        this.filterList = filterList;
    }

    /**
     * Gets filter list.
     *
     * @return the filter list
     */
    public List<FilterWrapper> getFilterList() {
        return filterList;
    }

    /**
     * Gets filter by a {@link FilterDefinition}. <br/>
     * Note: This method takes not care of other configuration classes.
     *
     * @param filterConfig the filter config
     * @return the filter by config
     */
    public FilterWrapper getFilterByConfig(FilterDefinition filterConfig) {
        for (FilterWrapper f : filterList) {
            if (f.getConfig().equals(filterConfig)) {
                return f;
            }
        }
        return new IdentityWrapper();
    }

    /**
     * Returns the names of every filter
     *
     * @return the names
     */
    public List<String> getFilterNames() {
        List<String> names = new ArrayList<>(filterList.size());
        for (int i = 0; i < filterList.size(); i++) {
            names.add(filterList.get(i).getConfig().getName());
        }
        return names;
    }

    @Override
    public String toString() {
        return "FilterHolder{" +
                "filterList=" + filterList +
                '}';
    }
}
