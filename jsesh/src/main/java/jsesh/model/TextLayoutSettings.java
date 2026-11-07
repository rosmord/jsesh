/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;

/// Values for some of the [LayoutSetting]s, shared by the document preferences
/// and by zones.
///
/// An unset value is inherited: from the enclosing zone, or from the document.
/// The document preferences set them all (see [#DOCUMENT_DEFAULTS]).
///
/// Immutable.
public final class TextLayoutSettings implements Serializable, Comparable<TextLayoutSettings> {

    @Serial
    private static final long serialVersionUID = 1L;

    /// Nothing set: everything is inherited.
    public static final TextLayoutSettings INHERIT_ALL = new TextLayoutSettings(new EnumMap<>(LayoutSetting.class));

    /// Every setting at its default value for a document.
    public static final TextLayoutSettings DOCUMENT_DEFAULTS = buildDocumentDefaults();

    private final EnumMap<LayoutSetting, Object> values;

    private TextLayoutSettings(EnumMap<LayoutSetting, Object> values) {
        this.values = values;
    }

    private static TextLayoutSettings buildDocumentDefaults() {
        EnumMap<LayoutSetting, Object> map = new EnumMap<>(LayoutSetting.class);
        for (LayoutSetting setting : LayoutSetting.values()) {
            map.put(setting, normalised(setting.documentDefault()));
        }
        return new TextLayoutSettings(map);
    }

    /// @param setting a setting.
    /// @return its value, or null if it is not set.
    public Object get(LayoutSetting setting) {
        return values.get(setting);
    }

    /// @param setting a setting.
    /// @return true if it has a value.
    public boolean isSet(LayoutSetting setting) {
        return values.containsKey(setting);
    }

    /// @return true if no setting has a value.
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /// @return true if every setting has a value.
    public boolean isComplete() {
        return values.size() == LayoutSetting.values().length;
    }

    /// Sets (or unsets) a value.
    ///
    /// @param setting the setting.
    /// @param value its new value, of the class of its kind (see
    ///        [LayoutSetting.Kind#valueClass()]); null to unset it. Lengths are
    ///        rounded to six decimals.
    /// @return a copy of these settings, with the new value.
    /// @throws IllegalArgumentException if the value has the wrong class.
    public TextLayoutSettings with(LayoutSetting setting, Object value) {
        EnumMap<LayoutSetting, Object> map = new EnumMap<>(values);
        if (value == null) {
            map.remove(setting);
        } else {
            if (!setting.kind().valueClass().isInstance(value)) {
                throw new IllegalArgumentException("Bad value " + value + " for " + setting.settingName());
            }
            map.put(setting, normalised(value));
        }
        return new TextLayoutSettings(map);
    }

    /// Lengths are kept with six decimals, as they are written: so, settings
    /// read back from a header are equal to the ones written.
    private static Object normalised(Object value) {
        if (value instanceof Double d) {
            return Math.round(d * 1e6) / 1e6;
        }
        return value;
    }

    /// These settings, with the values set in other ones.
    ///
    /// @param other the settings which take precedence.
    /// @return the combined settings.
    public TextLayoutSettings overriddenBy(TextLayoutSettings other) {
        EnumMap<LayoutSetting, Object> map = new EnumMap<>(values);
        map.putAll(other.values);
        return new TextLayoutSettings(map);
    }

    /// @return the values which are set.
    public Map<LayoutSetting, Object> asMap() {
        return java.util.Collections.unmodifiableMap(values);
    }

    /// @return the orientation, or null if not set.
    public TextOrientation orientation() {
        return (TextOrientation) values.get(LayoutSetting.LAYOUT);
    }

    /// @return the direction, or null if not set.
    public TextDirection direction() {
        return (TextDirection) values.get(LayoutSetting.DIRECTION);
    }

    /// @param setting a setting of kind [LayoutSetting.Kind#LENGTH].
    /// @return its value, in percents of the unit, or null if not set.
    public Double length(LayoutSetting setting) {
        return (Double) values.get(setting);
    }

    /// @param setting a setting of kind [LayoutSetting.Kind#FLAG].
    /// @return its value, or null if not set.
    public Boolean flag(LayoutSetting setting) {
        return (Boolean) values.get(setting);
    }

    /// @param orientation the new orientation (null for inherited).
    /// @return a copy with the new value.
    public TextLayoutSettings withOrientation(TextOrientation orientation) {
        return with(LayoutSetting.LAYOUT, orientation);
    }

    /// @param direction the new direction (null for inherited).
    /// @return a copy with the new value.
    public TextLayoutSettings withDirection(TextDirection direction) {
        return with(LayoutSetting.DIRECTION, direction);
    }

    /// @param setting a length setting.
    /// @param percent its new value, in percents of the unit (null for inherited).
    /// @return a copy with the new value.
    public TextLayoutSettings withLength(LayoutSetting setting, Double percent) {
        return with(setting, percent);
    }

    /// @param setting a flag setting.
    /// @param value its new value (null for inherited).
    /// @return a copy with the new value.
    public TextLayoutSettings withFlag(LayoutSetting setting, Boolean value) {
        return with(setting, value);
    }

    /// An arbitrary but consistent order: setting by setting, unset first.
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public int compareTo(TextLayoutSettings other) {
        for (LayoutSetting setting : LayoutSetting.values()) {
            Comparable a = (Comparable) values.get(setting);
            Comparable b = (Comparable) other.values.get(setting);
            int c;
            if (a == null) {
                c = b == null ? 0 : -1;
            } else if (b == null) {
                c = 1;
            } else {
                c = a.compareTo(b);
            }
            if (c != 0) {
                return c;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TextLayoutSettings other && values.equals(other.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    /// @return the settings as zone options, e.g. `linegap=50,fitsigns=no`.
    @Override
    public String toString() {
        return values.entrySet().stream()
                .map(e -> e.getKey().settingName() + "=" + e.getKey().format(e.getValue()))
                .collect(Collectors.joining(","));
    }
}
