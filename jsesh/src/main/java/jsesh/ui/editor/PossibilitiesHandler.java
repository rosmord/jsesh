/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.ui.editor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsesh.glyphs.possibilities.PossibilitiesList;
import jsesh.glyphs.possibilities.Possibility;
import jsesh.glyphs.possibilities.PossibilityRepository;

/// Manages the choice of the text to insert at a given point in a given editor.
///
/// Keeps the possibility list for the code just typed and which of its
/// possibilities is selected (the lists themselves are immutable and may be
/// shared). Also remembers the separator, as it is only needed when a
/// possibility list is used.
///
/// The last choice made for each code is remembered, for this editor only:
/// typing `iw` again proposes first the sign chosen last time for `iw`. If
/// the list for the code has changed since (a new sign with this value, an
/// embedded sign...), the choice is forgotten and the first possibility is
/// proposed.
///
/// @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
class PossibilitiesHandler {

	/// The last choice for a code: the list it was made in, and its position.
	private record Choice(PossibilitiesList list, int position) {
	}

	private char separator = ' ';

	private PossibilitiesList possibilities = null;
	private int position = 0;
	private final PossibilityRepository possibilityRepository;
	private final Map<String, Choice> lastChoices = new HashMap<>();

	public PossibilitiesHandler(PossibilityRepository possibilityRepository) {
		this.possibilityRepository = possibilityRepository;
	}

	/// Forgets the current list (but not the last choices).
	public void clear() {
		possibilities = null;
		position = 0;
		separator = ' ';
	}

	/// Prepares the list for a given code and separator. The possibility
	/// chosen last time for this code is selected, if the list hasn't changed
	/// since; otherwise, the first one.
	public void init(String code, char separator) {
		this.possibilities = possibilityRepository.getPossibilityListFor(code);
		Choice last = lastChoices.get(code);
		this.position = last != null && last.list().equals(possibilities) ? last.position() : 0;
		this.separator = separator;
	}

	private void rememberChoice() {
		lastChoices.put(possibilities.code(), new Choice(possibilities, position));
	}

	public boolean hasPossibilities() {
		return possibilities != null && !possibilities.isEmpty();
	}

	/// The selected possibility.
	public Possibility getPossibility() {
		return possibilities.get(position);
	}

	/// Selects the next possibility (back to the first one after the last).
	public void next() {
		position = (position + 1) % possibilities.size();
		rememberChoice();
	}

	/// All the possibilities for the current code.
	///
	/// @return an immutable list (empty if there is no possibility list).
	public List<Possibility> getChoices() {
		return possibilities == null ? List.of() : possibilities.asList();
	}

	/// Index of the current possibility in [#getChoices()].
	public int getSelectedIndex() {
		return position;
	}

	/// Make the possibility at the given index the current one.
	///
	/// @throws IndexOutOfBoundsException if there is no such possibility.
	public void select(int index) {
		if (index < 0 || index >= getChoices().size()) {
			throw new IndexOutOfBoundsException("No possibility at index " + index);
		}
		position = index;
		rememberChoice();
	}

	public char getSeparator() {
		return separator;
	}
}
