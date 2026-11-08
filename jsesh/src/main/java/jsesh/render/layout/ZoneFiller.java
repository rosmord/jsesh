/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.layout;

import jsesh.render.view.MDCView;

/// Lays out again a zone which fills its line (`%zone[fill=yes]`) at the
/// length its parent gives it (see [FillLayout]). Implemented by [Layout],
/// which rebuilds the content of the zone as if the zone had that length as
/// `width` (or `height`).
@FunctionalInterface
public interface ZoneFiller {

    /// @param zoneView the view of the zone, already placed in its line; its
    ///        position is kept.
    /// @param length its new length along the lines of its parent (its width
    ///        if the parent is in lines, its height if it is in columns), frame
    ///        included. A length which is not larger than its natural length
    ///        gives it back its natural size.
    void fill(MDCView zoneView, double length);
}
