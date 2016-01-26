package org.aksw.gerbil.filter;

import com.google.common.base.Splitter;
import com.google.common.collect.*;
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
 * - relative amount of entities per dataset and filter
 * - annotions per word
 * - ambiguity of entities and surface forms
 * - diversity of entities
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

    private static final String ALL_DATASETS = "All Datasets";

    // row: entity, column: dataset, cell: duplicate count
    private Table<String, String, HashSet<String>> entityDiversity;

    // row: surfaceform, column: dataset, cell: duplicate count
    private Table<String, String, HashSet<String>> surfaceDiversity;

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
        calculateDiversity(datasets);
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
        ambig.put("data", convertAmbiguityTable(ambiguityEntities, ENTITIES_AMBIGUITY, "entity"));
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

    /**
     * Gets ambiguity of surface as json.
     *
     * @return the ambiguity of surface as json
     */
    @SuppressWarnings("rawtypes")
    public JSONObject getAmbiguityOfSurfaceAsJson() {
        JSONObject ambig = new JSONObject();
        ambig.put("data", convertAmbiguityTable(ambiguitySurface, SURFACE_AMBIGUITY, "surface"));
        ambig.put("medium", calculateAmbiguityMedium(ambiguitySurface, SURFACE_AMBIGUITY));
        return ambig;
    }


    /**
     * Gets entity diversity table.
     *
     * @return the entity diversity
     */
    public Table<String, String, HashSet<String>> getEntityDiversity() {
        return entityDiversity;
    }

    /**
     * Gets entity diversity table as json.
     *
     * @return the entity diversity as json
     */
    @SuppressWarnings("rawtypes")
    public JSONObject getEntityDiversityAsJson() {
        JSONObject div = new JSONObject();
        JSONArray a = new JSONArray();
        for (String row : entityDiversity.rowKeySet()) {
            if (ambiguityEntities.contains(row, ENTITIES_AMBIGUITY)) {
                JSONObject o = new JSONObject();
                o.put("entity", row);

                for (String s : entityDiversity.row(row).keySet()) {
                    double diversity = (double)entityDiversity.get(row, s).size() / (double)ambiguityEntities.get(row, ENTITIES_AMBIGUITY);
                    o.put(s, diversity);
                }
                a.add(o);
            }
        }

        div.put("data", a);
        div.put("medium", calculateMediumDiversity(entityDiversity, ambiguityEntities.column(ENTITIES_AMBIGUITY)));
        return div;
    }

    public Table<String, String, HashSet<String>> getSurfaceDiversity() {
        return surfaceDiversity;
    }

    @SuppressWarnings("rawtypes")
    public JSONObject getSurfaceDiversityAsJson() {
        JSONObject div = new JSONObject();
        JSONArray a = new JSONArray();
        for (String row : surfaceDiversity.rowKeySet()) {
            if (ambiguitySurface.contains(row, SURFACE_AMBIGUITY)) {
                JSONObject o = new JSONObject();
                o.put("entity", row);

                for (String s : surfaceDiversity.row(row).keySet()) {
                    double diversity = (double)surfaceDiversity.get(row, s).size() / (double)ambiguitySurface.get(row, SURFACE_AMBIGUITY);
                    o.put(s, diversity);
                }
                a.add(o);
            }
        }

        div.put("data", a);
        div.put("medium", calculateMediumDiversity(surfaceDiversity, ambiguitySurface.column(SURFACE_AMBIGUITY)));
        return div;
    }

    // medium_diversity = (sum (#used_sf / #sf) / #rows)
    @SuppressWarnings("rawtypes")
    private JSONObject calculateMediumDiversity(Table<String, String, HashSet<String>> diversityTable,
                                                Map<String, Integer> ambiguity) {
        JSONObject o = new JSONObject();
        for (String column : diversityTable.columnKeySet()) {
            int counter = 0;
            double diversity = 0.0;

            for (String row : diversityTable.column(column).keySet()) {
                if (ambiguity.containsKey(row)) {
                    counter++;
                    diversity += (double)diversityTable.get(row, column).size() / (double)ambiguity.get(row);
                }
            }
            diversity = diversity / (double)counter;
            o.put(column, diversity);
        }
        return o;
    }

    @SuppressWarnings("rawtypes")
    private JSONArray convertAmbiguityTable(Table<String, String, Integer> table, String ambiguityColumn, String name) {
        JSONArray a = new JSONArray();
        for (String row : table.rowKeySet()) {
            if (table.contains(row, ambiguityColumn)) {
                JSONObject o = new JSONObject();
                o.put(name, row);
                o.putAll(table.row(row));
                a.add(o);
            }
        }
        return a;
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
            medium = medium / (double)counter;
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

    private void calculateDiversity(Multimap<String, Document> datasets) {
        LOGGER.info("Calculating diversity");
        entityDiversity = HashBasedTable.create();
        surfaceDiversity = HashBasedTable.create();

        for (String datasetName : datasets.keySet()) {
            for (Document d : datasets.get(datasetName)) {
                for (Marking m : d.getMarkings()) {

                    // get all entities and it's corresponding surface forms
                    if (m instanceof Meaning) {
                        Set<String> uris = ((Meaning) m).getUris();
                        if (m instanceof Span) {
                            String t = StringUtils.substring(d.getText(), ((Span) m).getStartPosition(),
                                    ((Span) m).getStartPosition() + ((Span) m).getLength());
                            t = StringUtils.replace(t, " ", "_").toLowerCase();

                            for (String uri : uris) {
                                if (!StringUtils.contains(uri, "IITB")) {
                                    String shortUri = getEntityName(uri);
                                    // handle empty cells
                                    if (entityDiversity.contains(shortUri, datasetName)) {
                                        entityDiversity.get(shortUri, datasetName).add(t);
                                    } else {
                                        entityDiversity.put(shortUri, datasetName, new HashSet<String>());
                                        entityDiversity.get(shortUri, datasetName).add(t);
                                    }
                                    break;
                                }
                            }

                            if (!surfaceDiversity.contains(t, datasetName)) {
                                surfaceDiversity.put(t, datasetName, new HashSet<String>());
                            }

                            // add all short uris and IITB
                            for (String uri : uris) {
                                if (!StringUtils.contains(uri, "IITB")) {
                                    surfaceDiversity.get(t, datasetName).add(getEntityName(uri));
                                }

                            }
                        }
                    }
                }
            }
        }
        addToAllDatasets(surfaceDiversity);
        addToAllDatasets(entityDiversity);
    }

    private void addToAllDatasets(Table<String, String, HashSet<String>> table) {
        for (String row : table.rowKeySet()) {
            HashSet<String> forms = new HashSet<>();
            for (String column : table.row(row).keySet()) {
                forms.addAll(table.get(row, column));
            }
            table.put(row, ALL_DATASETS, forms);
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
                t = StringUtils.replace(t, " ", "_").toLowerCase();
                ambiguitySurface.put(t, column, 1);
                ambiguitySurface.put(t, ALL_DATASETS, 1);
            }
            // ignore the rest of entities
        }
    }


    private void fillEntitiesAmbiguityTable(Multimap<String, Document> datasets,
                                            Table<String, String, Integer> ambiguityEntities) {
        // check for every entity if there is a match wihtin the ambiguity tables
        for (String datasetName : datasets.keySet()) {
            List<List<Marking>> goldStandard = getGoldStandard(datasets.get(datasetName));
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
            String shortUri = getEntityName(uri);
            table.put(shortUri, column, 1);
            table.put(shortUri, ALL_DATASETS, 1);
        }
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
            if (table.containsRow(entity) || table.containsRow(entity.toLowerCase())) {
                table.put(entity, columnName, ambiguity);
            }
        }
    }

    // treat entitiy names correctly
    private String getEntityName(String s) {
        if (StringUtils.contains(s, "sentence-")) {
            return StringUtils.substringAfterLast(s, "sentence-");
        } else {
            return StringUtils.substringAfterLast(s, "/");
        }
    }
}
