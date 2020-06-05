/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.search.ui.specifications;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import jsesh.editor.JMDCField;

/**
 * List of fields in JSearchEmbeddableForm.
 *
 * @author rosmord
 */
public interface JMdCSearchEmbeddableFormFieldsIF {

    JButton getAddSetButton();

    JButton getAddSkipButton();

    JSpinner getMatchLengthSpinner();

    JMDCField getSearchField();

    JCheckBox getMatchLayoutCheckBox();

}
