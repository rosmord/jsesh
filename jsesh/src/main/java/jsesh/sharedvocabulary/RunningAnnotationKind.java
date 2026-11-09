/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.sharedvocabulary;

/// The two sorts of *running annotation*: a line drawn along the text to mark
/// a stretch of it (see `00_Documents/documentation/technical_glossary.md`).
///
/// They are written `%haplography[size=...]` and `%lacuna[size=...]`, and read
/// from the old forms `^-..-..-^` and `?-..-..-?`.
public enum RunningAnnotationKind {
    /// A running variant (haplography): a solid line with an arrow at each end.
    HAPLOGRAPHY("haplography"),
    /// A running lacuna: a dashed line, without arrows.
    LACUNA("lacuna");

    private final String mdcName;

    RunningAnnotationKind(String mdcName) {
        this.mdcName = mdcName;
    }

    /// @return the name of the construct in MdC, without the leading `%`.
    public String mdcName() {
        return mdcName;
    }
}
