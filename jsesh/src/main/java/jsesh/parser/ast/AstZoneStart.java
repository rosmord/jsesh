/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;


/**
 * A zone marker, introducing a new drawing zone (position, dimensions,
 * text orientation and direction), given as options: {@code zone[...]}.
 * <p>{@link jsesh.mdcreader.AstModelBuilder} drops the zone marker from the
 * model entirely (it never fills in its options); here the raw option list
 * is kept as parsed.
 *
 * @param options the raw options given between braces, or {@code null} if
 * the zone marker had none (bare {@code zone}).
 * @author rosmord
 */
public record AstZoneStart(AstOptionList options) implements AstNode {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitZoneStart(this);
    }
}
