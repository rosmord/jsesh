/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.search;

/**
 * Precision with which signs are matched.
 * Other terms will probably be added.
 */
public enum SignMatchPrecision {
	/**
	 * A Gardiner code should exactly match the sign.
	 */
	EXACT,
	/**
	 * FULL Variants should be matched (e.g. w and W).
	 * This could be the default value.
	 */
	FULL_VARIANT,
	/**
	 * Any kind of variant will match.
	 */
	ANY_VARIANT

}
