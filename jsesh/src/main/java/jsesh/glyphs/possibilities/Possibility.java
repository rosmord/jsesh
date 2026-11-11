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
package jsesh.glyphs.possibilities;

import java.util.Objects;

/// A candidate in a [PossibilitiesList]: either a single sign ([Sign]), or a
/// Manuel de Codage text ([MdCText]), for instance a word from the glossary.
///
/// A sign and a text with the same characters are different possibilities.
///
/// @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
public sealed interface Possibility {

	/// The possibility as Manuel de Codage text: the sign code for a single
	/// sign (which is valid Manuel de Codage), the text itself otherwise.
	/// Fit for display, or for drawing the possibility.
	String asMdC();

	/// A single sign.
	///
	/// @param code the code of the sign.
	record Sign(String code) implements Possibility {
		public Sign {
			Objects.requireNonNull(code);
		}

		@Override
		public String asMdC() {
			return code;
		}

		@Override
		public String toString() {
			return code;
		}
	}

	/// A Manuel de Codage text, inserted as is (it may contain several signs).
	///
	/// @param mdc the text.
	record MdCText(String mdc) implements Possibility {
		public MdCText {
			Objects.requireNonNull(mdc);
		}

		@Override
		public String asMdC() {
			return mdc;
		}

		@Override
		public String toString() {
			return mdc;
		}
	}
}
