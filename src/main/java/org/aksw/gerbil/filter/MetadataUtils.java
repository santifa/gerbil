package org.aksw.gerbil.filter;

import org.aksw.gerbil.dataset.Dataset;
import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.filter.wrapper.FilterWrapper;
import org.aksw.gerbil.filter.wrapper.IdentityWrapper;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.web.config.AdapterList;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Provide metadata for the filter subsystem.
 * The metrics are only calculated for included datasets.
 * Some metrics are: <br/>
 * - number of entities
 * - amount of entities per filter per dataset
 * <p/>
 * <br/>
 * This also can be used for precaching all dataset goldstandards.
 * <p/>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class MetadataUtils {

    private static final Logger LOGGER = LogManager.getLogger(MetadataUtils.class);

    private HashMap<String, Integer> amountOfEntitiesPerFilter;

    /**
     * Instantiates a new Metadata utils.
     * The metadata utils creates and holds different measurements.
     *
     * @param datasets the datasets
     * @param holder   the holder
     */
    public MetadataUtils(AdapterList<DatasetConfiguration> datasets, FilterHolder holder) {
        LOGGER.info("Creating metadata...");
        int size = (int)Math.round(holder.getFilterList().size() * 1.25); // hashmap grows when 0.75 percent filled
        amountOfEntitiesPerFilter = new HashMap<>(size);
        for (FilterWrapper wrapper : holder.getFilterList()) {
            amountOfEntitiesPerFilter.put(wrapper.getConfig().getName(), 0);
        }
        createMetadata(datasets, holder);
    }

    /**
     * Gets amount of entities from the identity or "nofilter" filter.
     *
     * @return the amount of entities
     */
    public int getAmountOfEntities() {
        return amountOfEntitiesPerFilter.get(IdentityWrapper.CONF.getName());
    }

    /**
     * Gets amount of entities per filter.
     *
     * @return the amount of entities per filter
     */
    public HashMap<String, Integer> getAmountOfEntitiesPerFilter() {
        return amountOfEntitiesPerFilter;
    }

    private void createMetadata(AdapterList<DatasetConfiguration> datasets, FilterHolder holder) {
        for (DatasetConfiguration conf : datasets.getConfigurations()) {
            // if dataset loading failes
            try {
                LOGGER.info("Processing " + conf.getName());
                Dataset dataset = conf.getDataset(conf.getExperimentType());
                List<List<Marking>> goldStandard = getGoldStandard(dataset);

                // run every filter and collect metadata
                // as well as precache every goldstandard for faster processing later
                for (FilterWrapper wrapper : holder.getFilterList()) {
                    List<List<Marking>> result = wrapper.filterGoldstandard(goldStandard, conf.getName());

                    int amount = getAmountOfEntities(result);
                    amountOfEntitiesPerFilter.put(wrapper.getConfig().getName(),
                            amountOfEntitiesPerFilter.get(wrapper.getConfig().getName()) + amount);
                }

            } catch (GerbilException e) {
                LOGGER.error("Failed to load dataset. " + e.getMessage(), e);
            }
        }
    }

    // collect goldstandard
    private List<List<Marking>> getGoldStandard(Dataset dataset) {
        List<List<Marking>> goldStandard = new ArrayList<>(dataset.getInstances().size());
        for (Document d : dataset.getInstances()) {
            goldStandard.add(d.getMarkings());
        }
        return goldStandard;
    }

    // count entities
    private int getAmountOfEntities(List<List<Marking>> result) {
        int amount = 0;
        for (List<Marking> entities : result) {
            amount += entities.size();
        }
        return amount;
    }
}
