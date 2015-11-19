package org.aksw.gerbil.filter;

import com.google.common.collect.Lists;
import org.aksw.gerbil.filter.impl.NullFilter;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;

import java.util.List;

/**
 * A FilterHolder contains the actual filters to apply and
 * whether the goldstandard has to be cached before or not.
 *
 *
 * Created by Henrik Jürges on 19.11.15.
 */
public final class FilterHolder {

    private final List<EntityFilter> filterList;

    public final boolean isCacheGolstandard;

    public  FilterHolder(List<EntityFilter> filterList, boolean isCacheGoldstandard) {
        this.filterList = filterList;
        this.isCacheGolstandard = isCacheGoldstandard;
    }

    public List<EntityFilter> getFilterList() {
        return filterList;
    }

    public void cacheGoldstandard(List<Document> datasets, String datasetName) {
        List<List<Marking>> goldstandard = Lists.newArrayList();
        for (Document doc : datasets) {
            goldstandard.add(doc.getMarkings());
        }

        for (EntityFilter f : filterList) {
            if (!f.getConfig().equals(NullFilter.CONF)) {
                f.cache(goldstandard, datasetName);
            }
        }
    }

    @Override
    public String toString() {
        return "FilterHolder{" +
                "filterList=" + filterList +
                '}';
    }
}
