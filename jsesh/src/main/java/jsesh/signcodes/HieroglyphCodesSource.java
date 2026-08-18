/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 * 
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.signcodes;

import java.util.Set;

/**
 * A catalogue of hieroglyph codes.
 * 
 * @author rosmord
 */
public interface HieroglyphCodesSource {

	/**
	 * Does this source know about a certain code.
	 * 
	 * @param code the code to search
	 * @return if the code is known by the source.
	 */
	boolean hasCode(CanonicalCode code);
	

	/**
	 * Gets the set of the codes for the signs defined by this hieroglyph codes.
	 * @return a immutable set of codes.
	 */
	Set<String> getCodes();
}
