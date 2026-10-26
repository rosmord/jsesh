/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import jsesh.model.constants.KerningAxis;

/// A kerning operator, `@hkern` or `@vkern`: the groups before and after it are
/// moved closer, so that they are about one inter-sign space apart where their
/// signs are closest.
///
/// A kerning operator is a group of its own, without content. It is found:
///
/// - in a horizontal group: `p*@hkern*q`;
/// - alone in a line of a quadrat: `p:@vkern:q`;
/// - alone in a quadrat, between quadrats: `p-@hkern-q`.
///
/// It only acts in a context of its own axis (see [KerningAxis]); elsewhere,
/// it is ignored, and the text is drawn as if it was not there.
///
/// The options (`@hkern[...]`) are not used yet; they are kept as written, so
/// that they are saved back.
public class Kerning extends InnerGroup {

    private static final long serialVersionUID = -3021561987414372204L;

    private final KerningAxis axis;
    private final String options;

    /// Create a kerning operator.
    ///
    /// @param axis the kerning axis.
    /// @param options the options, as written between the brackets, without them (empty if none).
    public Kerning(KerningAxis axis, String options) {
        this.axis = axis;
        this.options = options == null ? "" : options;
    }

    /// Create a kerning operator without options.
    public Kerning(KerningAxis axis) {
        this(axis, "");
    }

    public KerningAxis getAxis() {
        return axis;
    }

    /// The options, as written between the brackets (empty if none).
    public String getOptions() {
        return options;
    }

    @Override
    public void accept(ModelElementVisitor v) {
        v.visitKerning(this);
    }

    @Override
    public String toString() {
        return "(kerning " + axis.getCode() + (options.isEmpty() ? "" : " [" + options + "]") + ")";
    }

    @Override
    public int compareToAux(ModelElement e) {
        Kerning other = (Kerning) e;
        int result = axis.compareTo(other.axis);
        if (result == 0) {
            result = options.compareTo(other.options);
        }
        return result;
    }

    @Override
    protected boolean equalsIgnoreIdAux(ModelElement other) {
        Kerning o = (Kerning) other;
        return axis == o.axis && options.equals(o.options);
    }

    @Override
    public Kerning deepCopy() {
        return new Kerning(axis, options);
    }
}
