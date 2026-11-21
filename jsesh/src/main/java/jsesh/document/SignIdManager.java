package jsesh.document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jsesh.model.Hieroglyph;
import jsesh.model.HieroglyphicText;
import jsesh.model.ModelElementDeepAdapter;
import jsesh.model.ModifiersList;
import jsesh.model.TopItem;
import jsesh.model.constants.SymbolCodes;

/// Gives and keeps the *sign ids* (`A1\sid2424`) of a text.
///
/// A sign id is a positive integer carried by a real hieroglyph (type
/// [SymbolCodes#MDCCODE]: not spaces, red points, small text or philology
/// marks) as the integer modifier [#SIGN_ID_MODIFIER]. Ids are meant for
/// quoting a part of a text from outside: a part of a text is then a set of
/// ids.
///
/// The manager works according to a [Policy]. It is called by
/// [HieroglyphicTextModel] *before* the commands are built (the commands keep
/// the live objects, so ids must not change when a command is done again).
///
/// - an **insertion** (paste, glossary, palette, typing) gives a fresh id to
///   every sign;
/// - a **replacement** (grouping, exploding, ligatures, modifying a sign,
///   editing a line as MdC...) lets a sign keep its id when this id was held
///   by one of the replaced items, and when no other new sign already took
///   it. Other signs get a fresh id.
///
/// The highest id ever given ([#getMaxId()]) never decreases, even after an
/// undo, so that an id is never used for two signs.
public final class SignIdManager {

	/// The name of the modifier holding the id.
	public static final String SIGN_ID_MODIFIER = "sid";

	/// What the manager does with the ids of the edited text.
	public enum Policy {
		/// The ids are left alone. The default for a bare
		/// [HieroglyphicTextModel], so that models used for small fields and by
		/// libraries are not affected.
		UNMANAGED,
		/// The text has no ids: the signs which are inserted lose theirs.
		STRIP,
		/// Every sign has a unique id, given and kept by the editor.
		NUMBERED
	}

	private Policy policy = Policy.UNMANAGED;
	private int maxId = 0;
	/// True if the header of the document being read says it is numbered.
	private boolean numberedInHeader = false;

	public Policy getPolicy() {
		return policy;
	}

	/// Sets the policy. Changing the policy doesn't change the text: see
	/// [HieroglyphicTextModel#numberSigns()] and
	/// [HieroglyphicTextModel#removeSignIds()].
	public void setPolicy(Policy policy) {
		this.policy = policy;
	}

	public boolean isNumbered() {
		return policy == Policy.NUMBERED;
	}

	/// The highest id ever given to a sign of this text.
	public int getMaxId() {
		return maxId;
	}

	/// Raises the maximum id (never lowers it).
	public void raiseMaxId(int id) {
		if (id > maxId) {
			maxId = id;
		}
	}

	/// Used by the document reader: the header of the file says the document is
	/// numbered, and holds the highest id given.
	///
	/// @param max the maximum id read in the header.
	public void setFromHeader(int max) {
		numberedInHeader = true;
		raiseMaxId(max);
	}

	/// The id of a sign.
	///
	/// @return the id, or 0 if the sign has none.
	public static int getId(Hieroglyph h) {
		return h.getModifiers().getIntegerWithDefault(SIGN_ID_MODIFIER, 0);
	}

	/// Does this item get a sign id?
	static boolean isNumberable(Hieroglyph h) {
		return h.getType() == SymbolCodes.MDCCODE;
	}

	private static void setId(Hieroglyph h, int id) {
		ModifiersList modifiers = h.getModifiers();
		if (id > 0) {
			modifiers.setInteger(SIGN_ID_MODIFIER, id);
		} else if (modifiers.hasInteger(SIGN_ID_MODIFIER)) {
			modifiers.unSetInteger(SIGN_ID_MODIFIER);
		}
	}

	// --- Called by the model before an edition -------------------------------

	/// Prepares items which are about to be *inserted* in the text.
	///
	/// @param inserted the new items. They are modified in place.
	public void prepareInsertion(List<TopItem> inserted) {
		switch (policy) {
			case NUMBERED:
				for (Hieroglyph h : signsOf(inserted)) {
					setId(h, ++maxId);
				}
				break;
			case STRIP:
				strip(inserted);
				break;
			default:
				break;
		}
	}

	/// Prepares items which are about to *replace* other ones.
	///
	/// @param removed the items which will disappear (a copy is fine).
	/// @param inserted the new items. They are modified in place.
	public void prepareReplacement(List<TopItem> removed, List<TopItem> inserted) {
		switch (policy) {
			case NUMBERED:
				Set<Integer> available = new HashSet<>();
				for (Hieroglyph h : signsOf(removed)) {
					int id = getId(h);
					if (id > 0) {
						available.add(id);
					}
				}
				for (Hieroglyph h : signsOf(inserted)) {
					int id = getId(h);
					if (id > 0 && available.remove(id)) {
						// Kept. It can't be kept twice, as it is no longer available.
						raiseMaxId(id);
					} else {
						setId(h, ++maxId);
					}
				}
				break;
			case STRIP:
				strip(inserted);
				break;
			default:
				break;
		}
	}

	/// Removes the ids of the signs of the items.
	public static void strip(List<? extends TopItem> items) {
		for (Hieroglyph h : signsOf(items)) {
			setId(h, 0);
		}
	}

	// --- Called when a whole text is loaded -----------------------------------

	/// Called when a text has just been read from a document. The policy
	/// becomes [Policy#NUMBERED] if the header said so or if the text contains
	/// ids, and [Policy#STRIP] otherwise. The maximum id is raised to the
	/// highest id of the text, and the signs which repeat the id of a previous
	/// sign get a new one. Does nothing for an [Policy#UNMANAGED] manager.
	///
	/// The text is modified without notification: call this before the text is
	/// shown.
	///
	/// @param text the text which has just been loaded.
	public void adopt(HieroglyphicText text) {
		if (policy == Policy.UNMANAGED) {
			return;
		}
		// Not text.asList(): it holds copies, and the live items are changed here.
		List<TopItem> items = new ArrayList<>();
		for (int i = 0; i < text.getNumberOfChildren(); i++) {
			items.add(text.getTopItemAt(i));
		}
		List<Hieroglyph> signs = signsOf(items);
		boolean hasIds = false;
		for (Hieroglyph h : signs) {
			if (getId(h) > 0) {
				hasIds = true;
				break;
			}
		}
		if (!hasIds && !numberedInHeader) {
			policy = Policy.STRIP;
			return;
		}
		policy = Policy.NUMBERED;
		numberedInHeader = false;
		for (Hieroglyph h : signs) {
			raiseMaxId(getId(h));
		}
		Set<Integer> seen = new HashSet<>();
		for (Hieroglyph h : signs) {
			int id = getId(h);
			if (id > 0 && !seen.add(id)) {
				// A duplicate: keep the first.
				setId(h, ++maxId);
			}
		}
	}

	/// All the numberable signs of some items, in text order, whatever their
	/// depth (ligatures, cartouches, zones...).
	static List<Hieroglyph> signsOf(List<? extends TopItem> items) {
		List<Hieroglyph> result = new ArrayList<>();
		ModelElementDeepAdapter collector = new ModelElementDeepAdapter() {
			@Override
			public void visitHieroglyph(Hieroglyph h) {
				if (isNumberable(h)) {
					result.add(h);
				}
			}
		};
		for (TopItem item : items) {
			item.accept(collector);
		}
		return result;
	}
}
