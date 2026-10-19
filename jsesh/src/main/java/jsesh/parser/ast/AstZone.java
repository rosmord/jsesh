/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

import java.util.List;

/// A rectangular zone containing text.
/// 
/// It can be text in lines or in columns.
/// @param options the raw options given between braces, or their default values.
/// @param list the content of the zone, as a list of top items.
/// @author rosmord
/// 
public record AstZone(AstOptionList options, AstTopItemList list) implements AstTopItem {

    public AstZone(AstOptionList options, List<AstTopItem> list) {
        this(options, new AstTopItemList(list));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitZone(this);
    }
    
}
