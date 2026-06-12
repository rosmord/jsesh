package org.qenherkhopeshef.swingUtils.lists;

import java.util.Objects;

/**
 * An entry which contains a mere textual label, not a real entry.
 * 
 * <p> Note that if the combobox displays a list of Strings, this allows you
 * to make a clear distinction between pseudo-entries (LabelListItem) and actual entries
 * (DataListItem).
 */
public final class LabelListItem<T> implements ListItem<T>{

    private final String value;

    /**
     * create a new StringListItem entry.
     */
    public LabelListItem(String value) {
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
        if (obj instanceof LabelListItem other) {
            return Objects.equals(value, other.value);
        } else {
            return false;
        }
    }
}
