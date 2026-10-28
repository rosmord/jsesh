/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import java.util.ArrayList;
import java.util.List;

/// The content of a [Zone]: a text of its own, laid out in lines or columns.
///
/// Like [HieroglyphicText], it accepts every [TopItem]: line breaks, tabs,
/// and other zones. A page break in a zone is laid out as a line break.
public class ZoneContent extends EmbeddedModelElement implements ItemSequence {

    private static final long serialVersionUID = 4120795367524061347L;

    public ZoneContent() {
    }

    public ZoneContent(List<? extends TopItem> items) {
        insertItemsAt(0, items);
    }

    @Override
    public TopItem getItemAt(int index) {
        return (TopItem) getChildAt(index);
    }

    /// A zone accepts all top items.
    @Override
    public boolean accepts(TopItem item) {
        return item != null;
    }

    @Override
    public void insertItemsAt(int index, List<? extends TopItem> items) {
        if (!acceptsAll(items)) {
            throw new IllegalArgumentException("Can't insert null items in a zone");
        }
        int i = index;
        for (TopItem item : items) {
            addChildAt(i++, item);
        }
    }

    @Override
    public List<TopItem> removeItems(int start, int end) {
        List<TopItem> result = new ArrayList<>();
        for (EmbeddedModelElement e : removeChildren(start, end)) {
            result.add((TopItem) e);
        }
        return result;
    }

    @Override
    public void accept(ModelElementVisitor v) {
        v.visitZoneContent(this);
    }

    @Override
    public String toString() {
        return getChildrenAsString();
    }

    @Override
    public int compareToAux(ModelElement e) {
        return compareContents(e);
    }

    @Override
    public ZoneContent deepCopy() {
        ZoneContent r = new ZoneContent();
        copyContentTo(r);
        return r;
    }

    /// A zone can't be part of a quadrat.
    @Override
    public HorizontalListElement buildHorizontalListElement() {
        return null;
    }

    @Override
    public TopItem buildTopItem() {
        return new Zone(ZoneParameters.DEFAULT, deepCopy());
    }

    @Override
    public boolean isNavigable() {
        return true;
    }

    @Override
    public boolean edgeGapsAreStops() {
        return true;
    }
}
