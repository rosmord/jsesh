package org.qenherkhopeshef.swingUtils.lists;

import java.util.Objects;

/**
 * An entry which contains basic text (as opposed to genuine content).
 */
public final class StringListItem<T> implements ListItem<T>{

    private final String value;

    /**
     * create a new StringListItem entry.
     */
    public StringListItem(String value) {
        Objects.requireNonNull(value);
        this.value = value;
    }


    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }


    @Override
    public int hashCode() {
       return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof StringListItem other) {
            return Objects.equals(value, other.value);
        } else {
            return false;
        }
    }
}
