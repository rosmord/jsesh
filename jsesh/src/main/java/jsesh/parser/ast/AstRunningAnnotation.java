/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

import jsesh.sharedvocabulary.RunningAnnotationKind;

/// A running annotation: `%haplography[size=200]` or `%lacuna[size=fill]`.
///
/// The old forms `^-..-..-^` and `?-..-..-?` give the same node, with a `size`
/// option computed from the number of dots.
///
/// @param kind haplography or lacuna.
/// @param options the raw options given between braces (possibly none).
/// @see jsesh.mdcreader.MDCParserModelGenerator
public record AstRunningAnnotation(RunningAnnotationKind kind, AstOptionList options) implements AstTopItem {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitRunningAnnotation(this);
    }
}
