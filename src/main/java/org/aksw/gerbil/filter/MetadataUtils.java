package org.aksw.gerbil.filter;

import org.aksw.gerbil.dataset.Dataset;
import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.filter.wrapper.FilterWrapper;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.web.config.AdapterList;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

    private HashMap<String, HashMap<String, Integer>> entitiesPerFilterAndDataset;

    private int amountOfEntities = 0;

    /**
     * Instantiates a new Metadata utils.
     * The metadata utils creates and holds different measurements.
     *
     * @param datasets the datasets
     * @param holder   the holder
     */
    public MetadataUtils(AdapterList<DatasetConfiguration> datasets, FilterHolder holder) {
        LOGGER.info("Creating metadata...");
        int size = (int)Math.round(holder.getFilterList().size() * 1.3); // hashmap grows when 0.75 percent filled
        entitiesPerFilterAndDataset = new HashMap<>(size);

        for (FilterWrapper wrapper : holder.getFilterList()) {
            entitiesPerFilterAndDataset.put(wrapper.getConfig().getName(),
                    new HashMap<String, Integer>());
        }
        createMetadataForFilters(datasets, holder);
    }

    /**
     * Gets amount of entities from the identity or "nofilter" filter.
     *
     * @return the amount of entities
     */
    public int getAmountOfEntities() {
        return amountOfEntities;
    }

    /**
     * Gets amount of entities per filter.
     *
     * @return the amount of entities per filter
     */
    public HashMap<String, HashMap<String, Integer>> getAmountOfEntitiesPerFilter() {
        return entitiesPerFilterAndDataset;
    }

    private void createMetadataForFilters(AdapterList<DatasetConfiguration> datasets, FilterHolder holder) {
        for (DatasetConfiguration conf : datasets.getConfigurations()) {
            // ignore OKE Task 2
            if (StringUtils.contains(conf.getName(), "OKE 2015 Task 2")) {
                continue;
            }

            // if dataset loading failes
            try {
                LOGGER.info("Processing " + conf.getName());
                Dataset dataset = conf.getDataset(conf.getExperimentType());
                List<List<Marking>> goldStandard = getGoldStandard(dataset);
                amountOfEntities += getAmountOfEntities(goldStandard);

                // run every filter and collect metadata
                // as well as precache every goldstandard for faster processing later
                for (FilterWrapper wrapper : holder.getFilterList()) {
                    List<List<Marking>> result = wrapper.filterGoldstandard(goldStandard, conf.getName());
                    entitiesPerFilterAndDataset.get(wrapper.getConfig().getName())
                        .put(dataset.getName(), getAmountOfEntities(result));
                }
            } catch (GerbilException e) {
                LOGGER.error("Failed to load dataset. " + e.getMessage());
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

    // count sub lists
    private int getAmountOfEntities(List<List<Marking>> result) {
        int amount = 0;
        for (List<Marking> entities : result) {
            amount += entities.size();
        }
        return amount;
    }

    // convert amount of entities per filter per dataset to a json object
    @SuppressWarnings("rawtypes")
    public String entityMetadataToJson() {
        JSONObject base = new JSONObject();
        base.put("overallAmount", amountOfEntities);

        JSONArray a = new JSONArray();
        for (String name : entitiesPerFilterAndDataset.keySet()) {
            JSONObject o = new JSONObject();
            mapToJson(o, entitiesPerFilterAndDataset.get(name));
            o.put("filter", name);
            a.add(o);
        }
        base.put("filters", a);
        return base.toJSONString();
    }

    @SuppressWarnings("rawtypes")
    private void mapToJson(JSONObject o, HashMap<String, Integer> datasets) {
        int amount = 0;
        for (String dataset : datasets.keySet()) {
            amount += datasets.get(dataset);
        }
        o.put("amount", amount);
        o.putAll(datasets);
    }
}
