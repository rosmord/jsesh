/*
 * This file is distributed under the LGPL
 * Author: Serge Rosmorduc
 */


package jsesh.ui.export.rtf;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import net.miginfocom.swing.MigLayout;

import jsesh.ui.widgets.units.LengthUnit;

/**
 * Graphical panel for choosing RTF export preferences.
 * @author rosmord
 */
public class JRTFFileExportPreferences extends javax.swing.JPanel {

    /** Creates new form JRTFExportPreferences */
    public JRTFFileExportPreferences() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        browseButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        exportModeCB = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        unitCB = new javax.swing.JComboBox<>();
        fileField = new javax.swing.JFormattedTextField();
        cadratHeightField = new javax.swing.JFormattedTextField();

        jLabel1.setText("File name");

        browseButton.setText("browse");

        jLabel2.setText("Export mode");


        jLabel3.setText("Cadrat Height");

        setLayout(new MigLayout("insets dialog, wrap 3", "[][grow][]"));
        add(jLabel1, "right");
        add(fileField, "growx");
        add(browseButton, "wrap");
        add(jLabel2, "right");
        add(exportModeCB, "growx, span 2, wrap");
        add(jLabel3, "right");
        add(cadratHeightField, "growx");
        add(unitCB);
    }


    
    private javax.swing.JButton browseButton;
    private javax.swing.JFormattedTextField cadratHeightField;
    private javax.swing.JComboBox<RTFExportGranularity> exportModeCB;
    private javax.swing.JFormattedTextField fileField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JComboBox<LengthUnit> unitCB;
    

    public JButton getBrowseButton() {
        return browseButton;
    }

    /**
     * 
     * @return
     */
    public JFormattedTextField getCadratHeightField() {
        return cadratHeightField;
    }

    public JComboBox<RTFExportGranularity> getExportModeCB() {
        return exportModeCB;
    }

    public JFormattedTextField getFileField() {
        return fileField;
    }
    
    public JComboBox<LengthUnit> getUnitCB() {
        return unitCB;
    }

    
}
