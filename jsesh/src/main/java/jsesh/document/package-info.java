/**
 * The document itself: a hieroglyphic text, its editing history and its
 * preferences.
 *
 * <p>
 * This package sits below both {@code jsesh.io} and {@code jsesh.editor}, and
 * is shared by them. {@link jsesh.document.MDCDocument} is the aggregate a user
 * works on — a text, plus the file, encoding, dialect and
 * {@link jsesh.document.DocumentPreferences} that go with it.
 * {@link jsesh.document.HieroglyphicTextModel} holds the text proper, along
 * with its undo history and change notifications.
 *
 * <p>
 * Nothing here knows about rendering, Swing, or file formats: reading and
 * writing documents belongs to {@code jsesh.io.document}, and the conversion
 * between {@link jsesh.document.DocumentPreferences} and a rendering style
 * belongs to {@code jsesh.render.style}.
 */
package jsesh.document;
