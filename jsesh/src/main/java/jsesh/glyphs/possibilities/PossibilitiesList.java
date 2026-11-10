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
  Created on 14 nov. 2004
 */
package jsesh.glyphs.possibilities;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/// The candidates proposed for a code typed by the user (a transliteration,
/// a Gardiner code...), the preferred one first.
///
/// Immutable, without duplicates. Which candidate is currently selected is
/// not part of the list: it belongs to the editor which uses it.
///
/// @author S. Rosmorduc
public final class PossibilitiesList {
	private final String code;
	private final List<Possibility> possibilities;

	private PossibilitiesList(String code, List<Possibility> possibilities) {
		this.code = Objects.requireNonNull(code);
		this.possibilities = List.copyOf(possibilities);
	}

	/// An empty list for a code.
	public static PossibilitiesList empty(String code) {
		return new PossibilitiesList(code, List.of());
	}

	/// A list of single signs, in the given order (duplicates are dropped).
	///
	/// @param code      the code the list was built for.
	/// @param signCodes the codes of the signs.
	public static PossibilitiesList ofSigns(String code, List<String> signCodes) {
		Builder builder = builder(code);
		signCodes.forEach(builder::addSign);
		return builder.build();
	}

	/// A builder for a list.
	///
	/// @param code the code the list is built for.
	public static Builder builder(String code) {
		return new Builder(code);
	}

	/// The code this list was built for.
	public String code() {
		return code;
	}

	/// The possibilities, as an immutable list.
	public List<Possibility> asList() {
		return possibilities;
	}

	public boolean isEmpty() {
		return possibilities.isEmpty();
	}

	public int size() {
		return possibilities.size();
	}

	public Possibility get(int index) {
		return possibilities.get(index);
	}

	/// A new list with the possibilities of this list, then those of
	/// `other` which are not already in it. Keeps the code of this list.
	public PossibilitiesList combine(PossibilitiesList other) {
		if (other.isEmpty()) {
			return this;
		}
		Builder builder = builder(code);
		possibilities.forEach(builder::add);
		other.possibilities.forEach(builder::add);
		return builder.build();
	}

	/// Two lists are equal if they have the same code and the same possibilities, in the same order.
	@Override
	public boolean equals(Object obj) {
		return obj instanceof PossibilitiesList other
				&& code.equals(other.code)
				&& possibilities.equals(other.possibilities);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, possibilities);
	}

	@Override
	public String toString() {
		return code + " -> " + possibilities;
	}

	/// Builds a [PossibilitiesList]. A possibility already added is ignored.
	public static final class Builder {
		private final String code;
		private final LinkedHashSet<Possibility> possibilities = new LinkedHashSet<>();

		private Builder(String code) {
			this.code = Objects.requireNonNull(code);
		}

		public Builder add(Possibility possibility) {
			possibilities.add(Objects.requireNonNull(possibility));
			return this;
		}

		public Builder addAll(PossibilitiesList list) {
			list.possibilities.forEach(this::add);
			return this;
		}

		/// Adds a single sign.
		public Builder addSign(String signCode) {
			return add(Possibility.createSignPossibility(signCode));
		}

		/// Adds a Manuel de Codage text.
		public Builder addMdCText(String mdcText) {
			return add(Possibility.createMdCPossibility(mdcText));
		}

		public PossibilitiesList build() {
			return new PossibilitiesList(code, new ArrayList<>(possibilities));
		}
	}
}
