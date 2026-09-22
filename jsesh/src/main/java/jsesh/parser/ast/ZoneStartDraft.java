package jsesh.parser.ast;

import jsesh.model.api.ZoneStartInterface;

/**
 * Mutable, transient stand-in for a {@link AstZoneStart}, used only inside
 * {@link AstBuilder}. See {@link HieroglyphDraft} for why this indirection
 * exists: the grammar builds a bare zone marker, then optionally attaches
 * its options, before it is consumed.
 *
 * @author rosmord
 */
final class ZoneStartDraft implements ZoneStartInterface {

    private AstOptionList options;

    void setOptions(AstOptionList options) {
        this.options = options;
    }

    AstZoneStart toAstZoneStart() {
        return new AstZoneStart(options);
    }
}
