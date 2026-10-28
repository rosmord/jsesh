/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

/// A zone, `%zone[options](content)`: an area of the text with its own
/// layout (lines or columns), direction and, optionally, size.
///
/// A zone is placed in the line (or column) of its parent like a large
/// quadrat. Zones may contain anything a text contains, including other
/// zones. Its only child is its [ZoneContent], which is an editing scope.
///
/// The whole text plays the role of the outermost zone; its settings are
/// the document preferences.
public class Zone extends TopItem {

    private static final long serialVersionUID = -2717062434583104163L;

    private ZoneParameters parameters;

    /// Creates an empty zone with default parameters.
    public Zone() {
        this(ZoneParameters.DEFAULT, new ZoneContent());
    }

    /// @param parameters the zone settings.
    /// @param content the content, used as it is.
    public Zone(ZoneParameters parameters, ZoneContent content) {
        this.parameters = parameters;
        addChildAt(0, content);
    }

    public ZoneContent getContent() {
        return (ZoneContent) getChildAt(0);
    }

    public ZoneParameters getParameters() {
        return parameters;
    }

    public void setParameters(ZoneParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public void accept(ModelElementVisitor v) {
        v.visitZone(this);
    }

    @Override
    public String toString() {
        return "(zone " + parameters + " " + getChildrenAsString() + ")";
    }

    @Override
    public int compareToAux(ModelElement e) {
        int result = parameters.compareTo(((Zone) e).parameters);
        if (result == 0) {
            result = compareContents(e);
        }
        return result;
    }

    @Override
    protected boolean equalsIgnoreIdAux(ModelElement other) {
        return parameters.equals(((Zone) other).parameters);
    }

    @Override
    public Zone deepCopy() {
        Zone result = new Zone(parameters, getContent().deepCopy());
        copyStateTo(result);
        return result;
    }

    /// A zone can't be part of a quadrat.
    @Override
    public HorizontalListElement buildHorizontalListElement() {
        return null;
    }

    @Override
    public boolean isNavigable() {
        return true;
    }
}
