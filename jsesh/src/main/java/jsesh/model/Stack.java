/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

/// Two groups drawn one over the other, as in `%stack(a:b,c)`.
///
/// This is the group version of [Overwrite] (`a##b`), which only handles two
/// hieroglyphs. The two children are [SubCadrat]s, which may hold any group of
/// signs (including other stacks). The first one is drawn first; the second one is
/// drawn over it. Both are centered in the resulting box.
public class Stack extends InnerGroup {

    private static final long serialVersionUID = -2975431862054418734L;

    /// Create a stack.
    ///
    /// @param first the group drawn first.
    /// @param second the group drawn over the first one.
    public Stack(SubCadrat first, SubCadrat second) {
        addChild(first);
        addChild(second);
    }

    public SubCadrat getFirst() {
        return (SubCadrat) getChildAt(0);
    }

    public SubCadrat getSecond() {
        return (SubCadrat) getChildAt(1);
    }

    @Override
    public void accept(ModelElementVisitor v) {
        v.visitStack(this);
    }

    @Override
    public String toString() {
        return "(stack " + getChildrenAsString() + ")";
    }

    @Override
    public int compareToAux(ModelElement e) {
        return compareContents(e);
    }

    @Override
    public Stack deepCopy() {
        return new Stack(getFirst().deepCopy(), getSecond().deepCopy());
    }
}
