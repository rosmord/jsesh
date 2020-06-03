/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
/**
 * Simple searches for sequences of signs.
 * <p> Contains low-level code, plus usable user interfaces.
 * Basically, the two classes to use are
 * <ul>
 *   <li> JWildcardPanel : UI for simple searches.</li>
 *   <li> JSearchFolderPanel : UI for searches in folders.
 * </ul>
 * 
 * To create those objects, use {@link jsesh.search.ui.SearchPanelFactory}
 * 
 * TODO: the search for variants should be improved. First, with data about the signs.
 * G43 and G7 are not currently registered as variants.
 * 
 * Some though about transitivity and reflexivity in those relationships would 
 * be although needed.
 */
package jsesh.search;
