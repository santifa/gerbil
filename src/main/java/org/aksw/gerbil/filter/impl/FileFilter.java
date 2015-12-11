package org.aksw.gerbil.filter.impl;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.aksw.gerbil.filter.FilterDefinition;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A file filter is a concrete filter that asks a knowledge base on the
 * hard drive. But be carefully we load this model in memory.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class FileFilter extends ConcreteFilter {

    private final static Logger LOGGER = LogManager.getLogger(FileFilter.class);

    private Model knowledgeBase;

    public FileFilter(FilterDefinition def, String[] prefixes) {
        super(def, prefixes);
        this.knowledgeBase = getModel(def.getServiceLocation());
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName, String annotatorName) {
        try {
            return resolve(entities);
        } catch (IOException e) {
            LOGGER.error("Entities could not resolved, returning nothing. : " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName) {
        try {
            return resolve(entities);
        } catch (IOException e) {
            LOGGER.error("Entities could not resolved, returning nothing. : " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private List<String> resolve(List<String> entities)  throws IOException {
        List<String> result = new ArrayList<>(entities.size());
        String queryString = buildQuery(entities);
        Query query = QueryFactory.create(queryString);

        try (QueryExecution qexec = QueryExecutionFactory.create(query, knowledgeBase)) {
            ResultSet queryResult = qexec.execSelect();

            while (queryResult.hasNext()) {
                QuerySolution solution = queryResult.nextSolution();
                RDFNode node = solution.get("v");
                result.add(node.asResource().getURI());
            }
        } catch (Exception e) {
            throw new IOException("Could not retrieve answer from " + knowledgeBase + " ; Skipping... " + e.getMessage());
        }

        return result;
    }

    private Model getModel(String fileLocation) {
        ModelMaker maker = ModelFactory.createFileModelMaker(new File(fileLocation).getParentFile().getAbsolutePath());
        maker.createModel(new File(fileLocation).getName());
        return RDFDataMgr.loadModel(fileLocation, Lang.TURTLE);
    }

    @Override
    public String toString() {
        return "FileFilter{ "+ super.toString() + " }";
    }


    @Override
    Object cloneChild() {
        return new FileFilter(getConfiguration(), prefixMap);
    }
}
