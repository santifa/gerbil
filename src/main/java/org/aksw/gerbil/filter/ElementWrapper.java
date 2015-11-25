package org.aksw.gerbil.filter;

import org.aksw.gerbil.transfer.nif.Marking;

/**
 * Created by ratzeputz on 25.11.15.
 */
public final class ElementWrapper<T> implements Marking {

    private final T element;

    public ElementWrapper(T element) {
        this.element = element;
    }

    public T getElement() {
        return element;
    }

    @Override
    public String toString() {
        return "ElementWrapper{" +
                "element='" + element + '\'' +
                '}';
    }
}
