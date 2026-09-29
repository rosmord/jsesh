/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

/**
 * A superscript text annotation.
 * <p>The text is kept exactly as the parser read it, including any "\"
 * protection characters in front of "\" and "-"; unescaping them, as
 * {@link jsesh.mdcreader.AstModelBuilder} does, is left to the consumer.
 *
 * @author rosmord
 */
public record AstSuperscript(String text) implements AstNode {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitSuperscript(this);
    }
}
