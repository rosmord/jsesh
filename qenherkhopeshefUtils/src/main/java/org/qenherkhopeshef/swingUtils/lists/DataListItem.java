package org.qenherkhopeshef.swingUtils.lists;

import java.util.Objects;

public final class DataListItem<T> implements ListItem<T> {

    private final T value;

    /**
     * create a new DataListItem entry.
     */
    public DataListItem(T value) {
        Objects.requireNonNull(value);
        this.value = value;
    }


    /**
     * @return the value
     */
    public T getValue() {
        return value;
    }


    @Override
    public int hashCode() {
       return value.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof DataListItem other) {
            return Objects.equals(value, other.value);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
