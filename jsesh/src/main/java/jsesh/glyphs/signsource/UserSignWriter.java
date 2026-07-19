package jsesh.glyphs.signsource;

import jsesh.glyphs.shape.ShapeChar;

/**
 * Write side for newly imported signs: somewhere to put a sign the user has
 * just picked out of an external font or drawing file.
 *
 * <p>
 * The sign importer only ever needs to hand over a code and a shape, so this
 * is all it asks for. Keeping the contract here means {@code jsesh.glyphs}
 * stays free of the application's preference tree: the implementation JSesh
 * itself uses, {@code jsesh.defaults.UserFontDirectoryManager}, is app-scoped
 * and lives above this package.
 *
 * <p>
 * An embedder with its own idea of where user signs belong can implement this
 * instead, and pass it to {@link ExternalSignImporterModel}.
 */
public interface UserSignWriter {

    /**
     * Stores a new user sign.
     *
     * @param code  the Manuel de Codage code for the new sign.
     * @param shape the shape to store.
     */
    void insertNewSign(String code, ShapeChar shape);
}
