/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.search.ui.specifications;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import jsesh.editor.JMDCField;
import jsesh.editor.MdCSearchQuery;

/**
 * List of fields in JSearchEmbeddableForm.
 * @author rosmord
 */
 public interface JSearchEmbeddableFormFieldsIF {

        JButton getAddSetButton();

        JButton getAddSkipButton();

        JSpinner getMatchLengthSpinner();

        JMDCField getSearchField();

        JCheckBox getMatchLayoutCheckBox();

    }