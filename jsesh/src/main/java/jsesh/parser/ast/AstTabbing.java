/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

/**
 * A complex tabulation: {@code %[...]}, with options such as {@code id},
 * {@code orientation} and {@code justification}.
 * <p>Unlike {@link jsesh.mdcreader.AstModelBuilder}, which interprets those
 * options immediately (defaulting missing ones), this keeps the raw option
 * list as parsed; interpreting it is left to the consumer.
 *
 * @param options the raw options given between braces.
 * @author rosmord
 */
public record AstTabbing(AstOptionList options) implements AstNode {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitTabbing(this);
    }
}
