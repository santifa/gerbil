package org.aksw.gerbil.filter;

import org.aksw.gerbil.config.GerbilConfiguration;
import org.aksw.gerbil.filter.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * The FilterFactory creates a {@link FilterHolder} with all registered Filters.
 * Use this Object for the filter process.
 * <br/>
 * For the creation of the filter instances use the {@code registerFilter} method
 * and provide a {@link ConfigResolver} which refers to the filter.properties.
 * <p/>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class FilterFactory {

    private final static Logger LOGGER = LoggerFactory.getLogger(FilterFactory.class);

    // refers to filter.properties
    private static final String CHUNK = "org.aksw.gerbil.util.filter.chunk";
    private static final String CACHE = "org.aksw.gerbil.util.filter.cache";
    private static final String CACHE_LOCATION = "org.aksw.gerbil.util.filter.cachelocation";
    private static final String PRECACHE = "org.aksw.gerbil.util.filter.precache";
    private static final String WHITELIST = "org.aksw.gerbil.util.filter.whitelist";

    private static final String FILTER_PREFIX = "org.aksw.gerbil.util.filter.prefix.";
    private static final String FILTER_BASIC = "org.aksw.gerbil.util.filter.";
    private static final String FILTER_FILE = "org.aksw.gerbil.util.filter.file.";

    private List<FilterWrapper> filters = new ArrayList<>(42);

    private final List<String> whiteList = new ArrayList<>();

    private boolean isDummy = false;

    private String[] prefixes;
    /**
     * Instantiates a new Filter factory.
     *
     * @param serviceUrl the service url
     */
    public FilterFactory(String serviceUrl) {
        List<String> p = getPrefixSet();
        this.prefixes = p.toArray(new String[p.size()]);
        whiteList.addAll(GerbilConfiguration.getInstance().getList(WHITELIST));

        // initialize null object
        addNullFilter();
    }

    /**
     * Instantiates an empty Filter factory which creates only dummy objects.
     */
    public FilterFactory() {
        addNullFilter();
        this.isDummy = true;
    }

    // creates the dummy filter
    private void addNullFilter() {
        filters.add(new NullFilterWrapper());
    }

    /**
     * Register new filter objects via reflections.
     * The created filter will stored inside a {@link NormalFilterWrapper}
     *
     * @param <E>                the type of the filter
     * @param filter             the filter class extending {@link ConcreteFilter}, a filter class must have a constructor which takes
     *                           the configuration class.
     * @param resolver           the concrete configuration resolver
     */
    public <E extends ConcreteFilter> void registerFilter(Class<E> filter, ConfigResolver<FilterDefinition> resolver) {
        List<FilterDefinition> configurations = resolver.resolve();
        for (FilterDefinition c : configurations) {

            try {
                for (Constructor<?> co : filter.getConstructors()) {
                    if (co.getParameterTypes().length == 2 && c.getClass().isAssignableFrom(co.getParameterTypes()[0])) {
                        Filter filterInstance = (Filter) co.newInstance(c, prefixes);
                        filterInstance = decorateFilter(filterInstance);
                        NormalFilterWrapper wrapper = new NormalFilterWrapper(filterInstance);
                        filters.add(wrapper);

                        LOGGER.info("Filter " + filter + " with " + c + " loaded.");
                    }
                }
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                LOGGER.error("Filter configuration " + c + " for " + filter.getClass() + " could not be loaded", e.getMessage(), e);
            }
        }
    }

    private Filter decorateFilter(Filter service) {

        if (GerbilConfiguration.getInstance().containsKey(CHUNK)) {
            service = new ChunkFilter(service, GerbilConfiguration.getInstance().getInt(CHUNK));
        }

        if (GerbilConfiguration.getInstance().getBoolean(CACHE)) {
            service = new CacheFilter(service,
                    GerbilConfiguration.getInstance().getString(CACHE_LOCATION));
        }

        service = new UriCleaner(service);
        return service;
    }

    /**
     * Returns a {@link FilterHolder} containing all registered filter.
     *
     * @return the filter holder
     */
    public FilterHolder getFilters() {
        if (isDummy) {
            return new FilterHolder(new ArrayList<>(filters), false);
        } else {
            return new FilterHolder(new ArrayList<>(filters),
                    GerbilConfiguration.getInstance().getBoolean(PRECACHE));
        }
    }

    // returns a set of prefixes defined in the filter.properties
    private static final List<String> getPrefixSet() {
        return new ConfigResolver<String>() {

            @Override
            int resolve(int counter, List<String> result) {
                if (GerbilConfiguration.getInstance().containsKey(FILTER_PREFIX + counter)) {
                    result.add(GerbilConfiguration.getInstance().getString(FILTER_PREFIX + counter));
                    return ++counter;
                }
                return -1;
            }
        }.resolve();
    }

    /**
     * Gets the basic configuration resolver.
     *
     * @return the basic resolver
     */
    public final ConfigResolver<FilterDefinition> getBasicFilterResolver() {
        return new ConfigResolver<FilterDefinition>() {

            @Override
            int resolve(int counter, List<FilterDefinition> result) {
                if (GerbilConfiguration.getInstance().containsKey(FILTER_BASIC + counter + ".name") &&
                        GerbilConfiguration.getInstance().containsKey(FILTER_BASIC + counter + ".filter") &&
                        GerbilConfiguration.getInstance().containsKey(FILTER_BASIC + counter + ".service")) {

                    result.add(new FilterDefinition(GerbilConfiguration.getInstance().getString(FILTER_BASIC + counter + ".name"),
                            GerbilConfiguration.getInstance().getString(FILTER_BASIC + counter + ".filter"), whiteList,
                            GerbilConfiguration.getInstance().getString(FILTER_BASIC + counter + ".service")));
                    return ++counter;
                }
                return -1;
            }
        };
    }

    public final ConfigResolver<FilterDefinition> getFileFilterResolver() {
        return new ConfigResolver<FilterDefinition>() {
            @Override
            int resolve(int counter, List<FilterDefinition> result) {
                if (GerbilConfiguration.getInstance().containsKey(FILTER_FILE + counter + ".name") &&
                        GerbilConfiguration.getInstance().containsKey(FILTER_FILE + counter + ".filter") &&
                        GerbilConfiguration.getInstance().containsKey(FILTER_FILE + counter + ".file")) {

                    result.add(new FilterDefinition(
                            GerbilConfiguration.getInstance().getString(FILTER_FILE + counter + ".name"),
                            GerbilConfiguration.getInstance().getString(FILTER_FILE + counter + ".filter"),
                            whiteList,
                            GerbilConfiguration.getInstance().getString(FILTER_FILE + counter + ".file")));
                    return ++counter;
                }
                return -1;
            }
        };
    }

    /**
     * A Config resolver.
     *
     * @param <T> configuration class
     */
    public abstract static class ConfigResolver<T> {

        /**
         * Resolves the configuration objects.
         *
         * @return the list of configuration objects
         */
        public List<T> resolve() {
            List<T> result = new ArrayList<>();
            int counter = 1;
            // search endless for the next prefix definition
            while (counter != -1) {
                counter = resolve(counter, result);
            }
            return result;
        }

        /**
         * Override this method to provide the configuration objects.
         * We provide a arbitrary counter for going through the filter.properties.
         * Add your objects to the result list.
         *
         * @param counter the step counter
         * @param result  the result collection
         * @return the next step or -1
         */
        abstract int resolve(int counter, List<T> result);
    }
}
