package org.aksw.gerbil.utils.filter;

import org.aksw.gerbil.transfer.nif.Marking;

import java.util.List;

/**
 * An entity filter describes a mechanism to select
 * special entities out of all found.
 * <p/>
 * Created by Henrik Jürges on 07.11.15.
 *
 * @param <T> the type parameter
 */
public interface EntityFilter<T extends Marking> {

    /**
     * Filter a list of lists
     *
     * @param elements the elements lists
     * @return the list
     */
    List<List<T>> filterEntities(List<List<T>> elements);

}
