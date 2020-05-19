/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.search;

import org.junit.Test;

import static org.junit.Assert.*;

public class WildCardQueryTest {

	@Test
	public void doSimpleSearch() {
	}

	@Test
	public void doWildCardSearch() {
		String searchString = "r-a-QUERYSKIP-m";
		String mdc = "i-w-r:a-C1-m-pt:p*t";

	}


	@Test
	public void doSetSearch() {

	}
}