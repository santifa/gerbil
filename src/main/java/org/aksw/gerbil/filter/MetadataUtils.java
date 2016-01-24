package org.aksw.gerbil.filter;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.filter.wrapper.FilterWrapper;
import org.aksw.gerbil.transfer.nif.*;
import org.aksw.gerbil.web.config.AdapterList;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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

    private Table<String, String, Integer> entitiesPerFilterAndDataset;

    private HashMap<String, Double> annotationsPerWords;

    /* The table is defined as follows
     *
     *         | Entity Ambiguity or Surface Ambiguity | All Datasets | Dataset 1 | ...
     * Entity1 | 2                or 0                 | 1            | 1
     * Entity2 | 0                or 5                 | 1            | 0
     * Entity3 | 21               or 12                | 0            | 0
     * ...     | sf for an entity or entities for sf   | true or false for an entity
     *
     * for the sake of performance and memory we collect the data only for existing datasets
     * and not all entities
     */
    private Table<String, String, Integer> ambiguitySurface;

    private static final String SURFACE_AMBIGUITY = "Surface Form Ambiguity";

    private Table<String, String, Integer> ambiguityEntities;

    private static final String ENTITIES_AMBIGUITY = "Entity Ambiguity";

    private int amountOfEntities = 0;

    private int amountOfWords = 0;

    /**
     * Instantiates a new Metadata utils.
     * The metadata utils creates and holds different measurements.
     * The metadata is created at creation time.
     *
     * @param configurations the datasets configurations
     * @param holder         the holder
     */
    public MetadataUtils(AdapterList<DatasetConfiguration> configurations, FilterHolder holder) {
        LOGGER.info("Creating metadata...");
        Multimap<String, Document> datasets = preloadDatasets(configurations);

        // Row: Filter, Column: Dataset
        entitiesPerFilterAndDataset = HashBasedTable.create(holder.getFilterList().size(),
                datasets.size());
        int size = (int)Math.round(datasets.size() * 1.3);
        annotationsPerWords = new HashMap<>(size);

        createFilterMetadata(datasets, holder);
        calculateDensity(datasets);
        calculateAmbiguity(datasets);
    }

    /**
     * Gets amount of entities from the identity or "nofilter" filter.
     * This represents the amount of all entities found within the datasets.
     * This also contains duplicate entities.
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
    public Map<String, Map<String, Integer>> getAmountOfEntitiesPerFilter() {
        return entitiesPerFilterAndDataset.rowMap();
    }

    /**
     * Gets amount of entities per filter as json.
     *
     * @return the amount of entities per filter as json
     */
    @SuppressWarnings("rawtypes")
    public JSONArray getAmountOfEntitiesPerFilterAsJson() {
        JSONArray a = new JSONArray();
        for (String filterName : entitiesPerFilterAndDataset.rowKeySet()) {
            JSONObject o = new JSONObject();
            int amount = 0;
            for (String datasetName : entitiesPerFilterAndDataset.row(filterName).keySet()) {
                amount += entitiesPerFilterAndDataset.row(filterName).get(datasetName);
            }

            o.put("amount", amount);
            o.putAll(entitiesPerFilterAndDataset.row(filterName));

            o.put("filter", filterName);
            a.add(o);
        }
        return a;
    }

    /**
     * Gets annotations per word.
     *
     * @return the annotations per word
     */
    public Map<String, Double> getAnnotationsPerWord() {
        return annotationsPerWords;
    }

    /**
     * Gets annotations per word as json.
     *
     * @return the annotations per word as json
     */
    @SuppressWarnings("rawtypes")
    public JSONObject getAnnotationsPerWordAsJson() {
        JSONObject o = new JSONObject();
        o.putAll(annotationsPerWords);
        return o;
    }

    /**
     * Gets ambiguity of entities all entities.
     * Entities with no ambiguity (an empty ambiguity column) exists
     *
     * @return the ambiguity of entities
     */
    public Table<String, String, Integer> getAmbiguityOfEntities() {
        return ambiguityEntities;
    }

    /**
     * Gets ambiguity of entities as json.
     * Entities without ambiguity are not included
     *
     * @return the ambiguity of entities as json
     */
    @SuppressWarnings("rawtypes")
    public JSONObject getAmbiguityOfEntitiesAsJson() {
        JSONObject ambig = new JSONObject();
        JSONArray a = new JSONArray();
        for (String row : ambiguityEntities.rowKeySet()) {
            if (ambiguityEntities.contains(row, ENTITIES_AMBIGUITY)) {
                JSONObject o = new JSONObject();
                o.put("entity", row);
                o.putAll(ambiguityEntities.row(row));
                a.add(o);
            }
        }

        ambig.put("data", a);
        ambig.put("medium", calculateAmbiguityMedium(ambiguityEntities, ENTITIES_AMBIGUITY));
        return ambig;
    }

    /**
     * Gets ambiguity of surface.
     *
     * @return the ambiguity of surface
     */
    public Table<String, String, Integer> getAmbiguityOfSurface() {
        return ambiguitySurface;
    }

    @SuppressWarnings("rawtypes")
    public JSONObject getAmbiguityOfSurfaceAsJson() {
        JSONObject ambig = new JSONObject();
        JSONArray a = new JSONArray();
        for (String row : ambiguitySurface.rowKeySet()) {
            if (ambiguitySurface.contains(row, SURFACE_AMBIGUITY)) {
                JSONObject o = new JSONObject();
                o.put("entity", row);
                o.putAll(ambiguitySurface.row(row));
                a.add(o);
            }
        }

        ambig.put("data", a);
        ambig.put("medium", calculateAmbiguityMedium(ambiguitySurface, SURFACE_AMBIGUITY));
        return ambig;
    }

    // calculate medium ambiguity for every column
    @SuppressWarnings("rawtypes")
    private JSONObject calculateAmbiguityMedium(Table<String, String, Integer> table, String ambiguityColumn) {
        JSONObject o = new JSONObject();
        for (String column : table.columnKeySet()) {
            if (ambiguityColumn.equalsIgnoreCase(column)) {
                continue; // skip the amiguity column
            }

            double medium = 0.0;
            int counter = 0;
            Map<String, Integer> columnEntries = table.column(column);

            for (String row : columnEntries.keySet()) {
                if (table.contains(row, ambiguityColumn)) {
                    counter++;
                    medium += table.get(row, ambiguityColumn);
                }
            }
            medium = medium / counter;
            o.put(column, medium);
        }
        return o;
    }

    // load and hold datasets for different inspections
    private Multimap<String, Document> preloadDatasets(AdapterList<DatasetConfiguration> configurations) {
        Multimap<String, Document> goldStandards = ArrayListMultimap.create();

        for (DatasetConfiguration conf : configurations.getConfigurations()) {
            // ignore OKE Task 2
            if (StringUtils.contains(conf.getName(), "OKE 2015 Task 2")) {
                continue;
            }

            try {
                LOGGER.info("Loading " + conf.getName());
                goldStandards.putAll(conf.getName(), conf.getDataset(conf.getExperimentType()).getInstances());
            } catch (GerbilException e) {
                LOGGER.error("Failed to load dataset. " + e.getMessage());
            }
        }
        return goldStandards;
    }

    private void createFilterMetadata(Multimap<String, Document> datasets, FilterHolder holder) {
        // run every filter and collect metadata
        // as well as precache every goldstandard for faster processing later
        LOGGER.info("Creating filter metadata");
        for (String datasetName : datasets.keySet()) {
            List<List<Marking>> goldStandard = getGoldStandard(datasets.get(datasetName));

            for (FilterWrapper wrapper : holder.getFilterList()) {
                List<List<Marking>> result = wrapper.filterGoldstandard(goldStandard, datasetName);
                entitiesPerFilterAndDataset.put(wrapper.getConfig().getName(),
                        datasetName, getAmountOfEntities(result));
            }
        }
    }

    private void calculateDensity(Multimap<String, Document> datasets) {
        LOGGER.info("Calculating density distribution");
        for (String datasetName : datasets.keySet()) {
            Collection<Document> documents = datasets.get(datasetName);
            List<List<Marking>> goldStandard = getGoldStandard(documents);

            int entities = getAmountOfEntities(goldStandard);
            amountOfEntities += entities;
            annotationsPerWords.put(datasetName,
                    calculateAnnotationsPerWord(documents, entities));

        }
        double overallQuotient = (double) amountOfEntities / (double) amountOfWords;
        annotationsPerWords.put("All Datasets", overallQuotient);
    }

    private void calculateAmbiguity(Multimap<String, Document> datasets) {
        LOGGER.info("Calculating ambiguity");
        // start with amount of all entities and 3 columns more see -> ambiguityData definition
        int rowSize = (int)Math.round(amountOfEntities * 1.3);
        int columnsSize = (int)Math.round((datasets.keySet().size() + 2) * 1.3);
        ambiguityEntities = HashBasedTable.create(rowSize, columnsSize);
        ambiguitySurface = HashBasedTable.create(rowSize, columnsSize);


        File ambiguityEntitiesFile = new File("gerbil_data/resources/filter/ambiguity_e");
        File ambiguitySurfaceFile = new File("gerbil_data/resources/filter/ambiguity_sf");
        fillEntitiesAmbiguityTable(datasets, ambiguityEntities);
        fillSurfaceAmbiguityTalbe(datasets, ambiguitySurface);

        try {
            readAmbiguityFile(ambiguityEntitiesFile, ENTITIES_AMBIGUITY, ambiguityEntities);
            readAmbiguityFile(ambiguitySurfaceFile, SURFACE_AMBIGUITY, ambiguitySurface);
        } catch (IOException e) {
            LOGGER.error("Couldn't fetch ambiguity data. Please check if " + ambiguityEntities
                    + " and " + ambiguitySurface + " exists. If not create them. " + e.getMessage(), e);
            LOGGER.error("Stopping ambiguity calculation. ");
        }
    }

    private void fillSurfaceAmbiguityTalbe(Multimap<String, Document> datasets,
                                           Table<String, String, Integer> ambiguitySurface) {
        for (String datasetName : datasets.keySet()) {
            for (Document d : datasets.get(datasetName)) {
                getSurfaceForms(d.getText(), d.getMarkings(), datasetName, ambiguitySurface);
            }
        }
    }

    private void getSurfaceForms(String text, List<Marking> markings, String column,
                                 Table<String, String, Integer> ambiguitySurface) {
        for (Marking m : markings) {
            if (m instanceof Span) {
                String t = StringUtils.substring(text, ((Span) m).getStartPosition(),
                        ((Span) m).getStartPosition() + ((Span) m).getLength());
                t = StringUtils.replace(t, " ", "_");
                ambiguitySurface.put(t, column, 1);
                ambiguitySurface.put(t, "All Datasets", 1);
            }
            // ignore the rest of entities
        }
    }


    private void fillEntitiesAmbiguityTable(Multimap<String, Document> datasets,
                                            Table<String, String, Integer> ambiguityEntities) {
        // check for every entity if there is a match wihtin the ambiguity tables
        for (String datasetName : datasets.keySet()) {
            List<List<Marking>> goldStandard = getGoldStandard(datasets.get(datasetName));
            //System.out.println("adding column " + datasetName);
            for (List<Marking> entities : goldStandard) {
                checkEntitiesAmbiguity(entities, datasetName, ambiguityEntities);
            }
        }
    }

    private void checkEntitiesAmbiguity(List<Marking> entities, String column, Table<String, String, Integer> table) {
        for (Marking entity : entities) {
            if (entity instanceof Meaning) {
                Set<String> uris = ((Meaning) entity).getUris();
                addEntitiesToTable(column, table, uris);

            } else if (entity instanceof TypedSpan) {
                Set<String> types = ((TypedSpan) entity).getTypes();
                addEntitiesToTable(column, table, types);
            }
            // ignore the rest of entities
        }
    }

    // add found entities to the table if there is a match
    private void addEntitiesToTable(String column, Table<String, String, Integer> table, Set<String> uris) {
        for (String uri : uris) {
            String shortUri = shortenUri(uri);
            table.put(shortUri, column, 1);
            table.put(shortUri, "All Datasets", 1);
        }
    }

    // returns the part after the last slash
    private String shortenUri(String uri) {
        return StringUtils.substringAfterLast(uri, "/");
    }


    // calculates the quotient of annotations or entities per dataset based on the amount of words in this dataset.
    private double calculateAnnotationsPerWord(Collection<Document> documents, int amountOfAnnotations) {
        int words = 0;
        for (Document d : documents) {
            String text = d.getText();
            words += Splitter.on(' ').omitEmptyStrings().trimResults().splitToList(text).size();
        }
        amountOfWords += words;
        return (double)amountOfAnnotations / (double)words;
    }

    // collect goldstandard from documents
    private List<List<Marking>> getGoldStandard(Collection<Document> dataset) {
        List<List<Marking>> goldStandard = new ArrayList<>(dataset.size());
        for (Document d : dataset) {
            goldStandard.add(d.getMarkings());
        }
        return goldStandard;
    }

    // count entities in goldstandard
    private int getAmountOfEntities(List<List<Marking>> result) {
        int amount = 0;
        for (List<Marking> entities : result) {
            amount += entities.size();
        }
        return amount;
    }

    private void readAmbiguityFile(File file, String columnName, Table<String, String, Integer> table) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            // a line has as first part the ambiguity counter and as second the entity name with < >
            List<String> parts = Splitter.on(' ').omitEmptyStrings().splitToList(line);
            Integer ambiguity = Integer.decode(parts.get(0));
            String entity = parts.get(1).replace("<", "").replace(">", "");
            // store only entities we found in our datasets
            if (table.containsRow(entity)) {
                table.put(entity, columnName, ambiguity);
            }
        }
    }
}
