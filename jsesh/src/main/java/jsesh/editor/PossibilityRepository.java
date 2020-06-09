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
package jsesh.editor;

import java.util.EventObject;
import java.util.HashMap;
import java.util.List;

import jsesh.glossary.GlossaryEntry;
import jsesh.glossary.GlossaryEntryAdded;
import jsesh.glossary.GlossaryEntryRemoved;
import jsesh.glossary.GlossaryManager;
import jsesh.glossary.JSeshGlossary;
import jsesh.hieroglyphs.data.HieroglyphDatabaseRepository;
import jsesh.hieroglyphs.data.GardinerCode;
import jsesh.hieroglyphs.data.PossibilitiesList;
import jsesh.hieroglyphs.data.SignDescriptionConstants;

/**
 * Shared repository to manage access to possibility lists. Also, maintains
 * possibility lists coherence when the glossary is changed.
 * 
 * @author rosmord
 */
public class PossibilityRepository {

	private static PossibilityRepository instance= new PossibilityRepository();
	
	private HashMap<String, PossibilitiesList> map = new HashMap<String, PossibilitiesList>();

	private PossibilityRepository() {
		getGlossary().addEventLink(GlossaryEntryAdded.class, this, "update");
		getGlossary().addEventLink(GlossaryEntryRemoved.class, this, "update");
	}

	public PossibilitiesList getPossibilityListFor(String code) {
		if (map.containsKey(code)) {
			return map.get(code);
		} else {
			// compute the actual list, combining the signs values and the
			// glossary.
			List<GlossaryEntry> fromGlossary = getGlossary().get(code);
			// See if we have a Gardiner code or a translitteration.
			PossibilitiesList possibilities;

			if (GardinerCode.isCorrectGardinerCodeIgnoreCase(code.toString().toUpperCase())) {
				possibilities = HieroglyphDatabaseRepository.getHieroglyphDatabase()
						.getSuitableSignsForCode(code);
			} else {
				possibilities = HieroglyphDatabaseRepository.getHieroglyphDatabase()
						.getPossibilityFor(code,
								SignDescriptionConstants.KEYBOARD);
			}

			if (possibilities == null) {
				possibilities = new PossibilitiesList(code);
			}

			PossibilitiesList result = new PossibilitiesList(possibilities);

			if (result.isEmpty() && fromGlossary.isEmpty()) {
				possibilities.addSign(code);
			}

			for (GlossaryEntry e : fromGlossary) {
				result.addMdCText(e.getMdc());
			}
			map.put(code, result);
			return result;
		}
	}

	public void update(EventObject ev) {
		String code = null;
		// We should have a common parent class.
		if (ev instanceof GlossaryEntryAdded) {
			code = ((GlossaryEntryAdded) ev).getEntry().getKey();
		} else if (ev instanceof GlossaryEntryRemoved) {
			code = ((GlossaryEntryRemoved) ev).getEntry().getKey();
		} else {
			throw new RuntimeException("unexpected event " + ev);
		}
		map.remove(code);
	}

	private final JSeshGlossary getGlossary() {
		return GlossaryManager.getInstance().getGlossary();
	}

	public static PossibilityRepository getInstance() {
		return instance;
	}
}
