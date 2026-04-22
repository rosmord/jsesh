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
package jsesh.jhotdraw.documentPreferences.ui;

import java.awt.Component;
import java.util.Arrays;

import javax.swing.JFormattedTextField;

import jsesh.drawingspecifications.GeometrySpecification;
import jsesh.drawingspecifications.JSeshStyle;
import jsesh.drawingspecifications.PaintingSpecifications;
import jsesh.drawingspecifications.ShadingMode;
import jsesh.jhotdraw.utils.JSimpleDialog;
import jsesh.resources.JSeshMessages;
import jsesh.swing.units.LengthUnit;
import jsesh.swing.units.UnitMediator;

/**
 * Presenter for drawing preferences.
 * 
 * We stick with a modal dialog.
 *
 * @author rosmord
 *
 */
public class DrawingSpecificationsPresenter {

        JDrawingSpecificationEditor form;
        UnitMediator unitMediator = new UnitMediator();

        public DrawingSpecificationsPresenter() {
                form = new JDrawingSpecificationEditor();
                unitMediator.attachToComboBox(form.getUnitField());
                for (JFormattedTextField field : Arrays.asList(
                                form.getCartoucheLineWidthField(),
                                form.getColumnSkipField(),
                                form.getLineSkipField(),
                                form.getInterQuadratSkipField(),
                                form.getMaxCadratHeightField(),
                                form.getMaxCadratWidthField(),
                                form.getNormalSignHeightField(),
                                form.getSmallFontBodyLimitField())) {
                        unitMediator.managedTextField(field);
                }
        }

        private float getLength(JFormattedTextField field) {
                return (float) unitMediator.getManagedFieldInPoints(field);
        }

        /**
         * Copy the preference drawing implementations in the dialog.
         *
         * @param jseshStyle
         */
        public void loadPreferences(JSeshStyle jseshStyle) {
                GeometrySpecification geometry = jseshStyle.geometry();
                unitMediator.setCurrentUnit(LengthUnit.POINT);
                form.getCartoucheLineWidthField().setValue(
                                geometry.cartoucheLineWidth());
                form.getColumnSkipField().setValue(
                                geometry.columnSkip());
                form.getLineSkipField().setValue(
                                geometry.lineSkip());
                form.getInterQuadratSkipField().setValue(
                                geometry.smallSkip());
                form.getMaxCadratHeightField().setValue(
                                geometry.maxCadratHeight());
                form.getMaxCadratWidthField().setValue(
                                geometry.maxCadratWidth());
                form.getNormalSignHeightField().setValue(
                                geometry.standardSignHeight());
                form.getSmallFontBodyLimitField().setValue(
                                geometry.smallBodyScaleLimit());
                form.getUseLinesForShadingCheckBox().setSelected(
                                jseshStyle.painting().shadingStyle().equals(
                                                ShadingMode.LINE_HATCHING));
        }

        /**
         * Apply the current preferences to JSeshStyle and return the modified style.
         *
         * @param original the original style
         * @return a modified style, with the new preferences.
         */
        public JSeshStyle updatePreferences(JSeshStyle original) {
                unitMediator.setCurrentUnit(LengthUnit.POINT);
                double limit = ((Double) form.getSmallFontBodyLimitField().getValue());
                ShadingMode shadingMode = form.getUseLinesForShadingCheckBox().isSelected() ? ShadingMode.LINE_HATCHING
                                : ShadingMode.GRAY_SHADING;

                return original.copy()
                                .geometry(g -> g
                                                .cartoucheLineWidth(getLength(form
                                                                .getCartoucheLineWidthField()))
                                                .columnSkip(getLength(form.getColumnSkipField()))
                                                .lineSkip(getLength(form.getLineSkipField()))
                                                .maxCadratHeight(getLength(form
                                                                .getMaxCadratHeightField()))
                                                .maxCadratWidth(getLength(form
                                                                .getMaxCadratWidthField()))
                                                .standardSignHeight(getLength(form
                                                                .getNormalSignHeightField()))
                                                .smallSkip(getLength(form
                                                                .getInterQuadratSkipField()))
                                                .smallBodyScaleLimit((float) limit))
                                .painting(p -> p.shadingStyle(shadingMode))
                                .build();
        }

        public int showDialog(Component parent) {
                JSimpleDialog dialog = new JSimpleDialog(parent, form.getPanel(),
                                JSeshMessages.getString("drawingPrefs.title"));
                int result = dialog.show();
                dialog.dispose();
                return result;
        }
}
