package org.aksw.gerbil.filter;

import com.google.common.base.Joiner;
import org.aksw.gerbil.config.GerbilConfiguration;
import org.aksw.gerbil.filter.impl.*;
import org.aksw.gerbil.filter.impl.decorators.CacheFilter;
import org.aksw.gerbil.filter.impl.decorators.ChunkFilter;
import org.aksw.gerbil.filter.impl.decorators.UriCleaner;
import org.aksw.gerbil.filter.wrapper.FilterWrapper;
import org.aksw.gerbil.filter.wrapper.FilterWrapperImpl;
import org.aksw.gerbil.filter.wrapper.IdentityWrapper;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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
    private static final boolean CACHE = GerbilConfiguration.getInstance().getBoolean("org.aksw.gerbil.util.filter.cache");
    private static final String CACHE_LOCATION = "org.aksw.gerbil.util.filter.cachelocation";

    private List<ConcreteFilter> filters = new ArrayList<>(42);

    private List<Filter> filterCombinator = new ArrayList<>(10);

    private final List<String> whiteList = new ArrayList<>();

    private final String[] prefixes;

    private final boolean isDummy;

    /**
     * Instantiates a new Filter factory.
     *
     * @param isDummy the is dummy
     */
    public FilterFactory(boolean isDummy) {
        this.isDummy = isDummy;
        List<String> p = getPrefixSet();
        this.prefixes = p.toArray(new String[p.size()]);
        CollectionUtils.addAll(whiteList,
                GerbilConfiguration.getInstance().getStringArray("org.aksw.gerbil.util.filter.whitelist"));
    }

    /**
     * Register new filter objects via reflections.
     * The created filter will stored inside a {@link FilterWrapperImpl}
     *
     * @param <E>      the type of the filter
     * @param filter   the filter class extending {@link ConcreteFilter}, a filter class must have a constructor which takes
     *                      the configuration class.
     * @param resolver the concrete configuration resolver
     */
    public <E extends ConcreteFilter> void registerFilter(Class<E> filter, ConfigResolver<FilterDefinition> resolver) {
        List<FilterDefinition> configurations = resolver.resolve();
        for (FilterDefinition c : configurations) {

            try {
                for (Constructor<?> co : filter.getConstructors()) {
                    if (co.getParameterTypes().length == 2 && c.getClass().isAssignableFrom(co.getParameterTypes()[0])) {
                        filters.add((ConcreteFilter) co.newInstance(c, prefixes));
                        LOGGER.info("Loaded Filter " + filter + " with " + c + " loaded.");
                    }
                }
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                LOGGER.error("Filter configuration " + c + " for " + filter.getClass() + " could not be loaded", e.getMessage(), e);
            }
        }
    }

    /**
     * Register new filter combinators.
     * The filter for combining has to be registered in advance.
     *
     * @param resolver the resolver
     */
    public void registerFilterCombinator(ConfigResolver<FilterDefinition> resolver) {
        List<FilterDefinition> configurations = resolver.resolve();
        for (FilterDefinition conf : configurations) {

            List<Filter> combinedFilter = new ArrayList<>();
            for (String filterName : conf.getFilter().split(",")) {
                for (ConcreteFilter filter : filters) {
                    if (filterName.equalsIgnoreCase(filter.getConfiguration().getName())) {
                        combinedFilter.add(filter);
                    }
                }
            }
            filterCombinator.add(new FilterCombinator(combinedFilter, conf));
        }
    }

    private Filter decorateFilter(Filter service) {
        // chunk filter requests
        if (service.getConfiguration().getChunksize() > 0) {
            service = new ChunkFilter(service, service.getConfiguration().getChunksize());
        }
        // cache filter requests
        if (CACHE) {
            service = new CacheFilter(service,
                    GerbilConfiguration.getInstance().getString(CACHE_LOCATION));
        }
        // clean filter requests from unknown links
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
            // create new dummy objects
            return new FilterHolder(Collections.singletonList((FilterWrapper) new IdentityWrapper()));
        } else {
            List<FilterWrapper> clonedFilters = new ArrayList<>(filters.size());
            clonedFilters.add(new IdentityWrapper());

            // clone every filter to be concurrent
            try {
                for (ConcreteFilter f : filters) {
                    Filter clone = (Filter) f.clone();
                    clone = decorateFilter(clone);
                    clonedFilters.add(new FilterWrapperImpl(clone));
                }

                for (Filter f : filterCombinator) {
                    clonedFilters.add(new FilterWrapperImpl(f)); //FIXME not really cloned
                }
            } catch (CloneNotSupportedException e) {
                LOGGER.error("Filter could not be cloned. " + e.getMessage(), e);
            }

            return new FilterHolder(clonedFilters);
        }
    }

    // returns a set of prefixes defined in the filter.properties
    private static List<String> getPrefixSet() {
        return new ConfigResolver<String>() {
            private static final String FILTER_PREFIX = "org.aksw.gerbil.util.filter.prefix.";

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
            private static final String FILTER_BASIC = "org.aksw.gerbil.util.filter.";

            @Override
            int resolve(int counter, List<FilterDefinition> result) {

                if (GerbilConfiguration.getInstance().containsKey(FILTER_BASIC + counter + ".name") &&
                        GerbilConfiguration.getInstance().containsKey(FILTER_BASIC + counter + ".filter") &&
                        GerbilConfiguration.getInstance().containsKey(FILTER_BASIC + counter + ".service")) {

                    if (GerbilConfiguration.getInstance().containsKey(FILTER_BASIC + counter + ".chunk")) {
                        result.add(new FilterDefinition(GerbilConfiguration.getInstance().getString(FILTER_BASIC + counter + ".name"),
                                GerbilConfiguration.getInstance().getString(FILTER_BASIC + counter + ".filter"), whiteList,
                                GerbilConfiguration.getInstance().getString(FILTER_BASIC + counter + ".service"),
                                GerbilConfiguration.getInstance().getInt(FILTER_BASIC + counter + ".chunk")));
                    } else {
                        result.add(new FilterDefinition(GerbilConfiguration.getInstance().getString(FILTER_BASIC + counter + ".name"),
                                GerbilConfiguration.getInstance().getString(FILTER_BASIC + counter + ".filter"), whiteList,
                                GerbilConfiguration.getInstance().getString(FILTER_BASIC + counter + ".service")));
                    }

                    return ++counter;
                }
                return -1;
            }
        };
    }

    /**
     * Gets file filter resolver.
     *
     * @return the file filter resolver
     */
    public final ConfigResolver<FilterDefinition> getFileFilterResolver() {
        return new ConfigResolver<FilterDefinition>() {
            private static final String FILTER_FILE = "org.aksw.gerbil.util.filter.file.";

            @Override
            int resolve(int counter, List<FilterDefinition> result) {
                if (GerbilConfiguration.getInstance().containsKey(FILTER_FILE + counter + ".name") &&
                        GerbilConfiguration.getInstance().containsKey(FILTER_FILE + counter + ".filter") &&
                        GerbilConfiguration.getInstance().containsKey(FILTER_FILE + counter + ".service")) {

                    if (GerbilConfiguration.getInstance().containsKey(FILTER_FILE + counter + ".chunk")) {
                        result.add(new FilterDefinition(
                                GerbilConfiguration.getInstance().getString(FILTER_FILE + counter + ".name"),
                                GerbilConfiguration.getInstance().getString(FILTER_FILE + counter + ".filter"),
                                whiteList,
                                GerbilConfiguration.getInstance().getString(FILTER_FILE + counter + ".service"),
                                GerbilConfiguration.getInstance().getInt(FILTER_FILE + counter + ".chunk")));
                    } else {
                        result.add(new FilterDefinition(
                                GerbilConfiguration.getInstance().getString(FILTER_FILE + counter + ".name"),
                                GerbilConfiguration.getInstance().getString(FILTER_FILE + counter + ".filter"),
                                whiteList,
                                GerbilConfiguration.getInstance().getString(FILTER_FILE + counter + ".service")));
                    }
                    return ++counter;
                }
                return -1;
            }
        };
    }

    /**
     * Gets popularity filter resolver.
     *
     * @return the popularity filter resolver
     */
    public final ConfigResolver<FilterDefinition> getPopularityFilterResolver() {
        return new ConfigResolver<FilterDefinition>() {
            private static final String FILTER_POP = "org.aksw.gerbil.util.filter.pop.";

            @Override
            int resolve(int counter, List<FilterDefinition> result) {
                if (GerbilConfiguration.getInstance().containsKey(FILTER_POP + counter + ".name") &&
                        GerbilConfiguration.getInstance().containsKey(FILTER_POP + counter + ".filter") &&
                        GerbilConfiguration.getInstance().containsKey(FILTER_POP + counter + ".service")) {

                    String filter = Joiner.on(",")
                            .join(GerbilConfiguration.getInstance().getStringArray(FILTER_POP + counter + ".filter"));

                    if (GerbilConfiguration.getInstance().containsKey(FILTER_POP + counter + ".chunk")) {
                        result.add(new FilterDefinition(
                                GerbilConfiguration.getInstance().getString(FILTER_POP + counter + ".name"),
                                filter, whiteList,
                                GerbilConfiguration.getInstance().getString(FILTER_POP + counter + ".service"),
                                GerbilConfiguration.getInstance().getInt(FILTER_POP + counter + ".chunk")));
                    } else {
                        result.add(new FilterDefinition(
                                GerbilConfiguration.getInstance().getString(FILTER_POP + counter + ".name"),
                                filter, whiteList,
                                GerbilConfiguration.getInstance().getString(FILTER_POP + counter + ".service")));
                    }

                    return ++counter;
                }
                return -1;
            }
        };
    }

    /**
     * Gets filter combinator resolver.
     *
     * @return the filter combinator resolver
     */
    public final ConfigResolver<FilterDefinition> getFilterCombinatorResolver() {
        return new ConfigResolver<FilterDefinition>() {
            private static final String FILTER_COMB = "org.aksw.gerbil.util.filter.combinator.";

            @Override
            int resolve(int counter, List<FilterDefinition> result) {
                if (GerbilConfiguration.getInstance().containsKey(FILTER_COMB + counter + ".name") &&
                        GerbilConfiguration.getInstance().containsKey(FILTER_COMB + counter + ".filter") &&
                        GerbilConfiguration.getInstance().containsKey(FILTER_COMB + counter + ".service")) {

                    String filter = Joiner.on(",")
                            .join(GerbilConfiguration.getInstance().getStringArray(FILTER_COMB + counter + ".filter"));
                    result.add(new FilterDefinition(GerbilConfiguration.getInstance().getString(FILTER_COMB + counter + ".name"),
                            filter, whiteList,
                            GerbilConfiguration.getInstance().getString(FILTER_COMB + counter + ".service")));
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
