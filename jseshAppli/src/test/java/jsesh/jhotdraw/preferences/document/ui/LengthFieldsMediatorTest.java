/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.preferences.document.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jsesh.ui.widgets.units.LengthUnit;

class LengthFieldsMediatorTest {

	private LengthFieldsMediator mediator;
	private JFormattedTextField field;
	private JComboBox<DocumentLengthUnit> combo;

	@BeforeEach
	void setUp() {
		mediator = new LengthFieldsMediator();
		field = LengthFieldsMediator.createField();
		combo = new JComboBox<>();
		mediator.managedTextField(field);
		mediator.attachToComboBox(combo);
		mediator.reset(18);
		mediator.setPercent(field, 50);
	}

	@Test
	void percentsAreShownAsIs() {
		assertEquals(50.0, ((Number) field.getValue()).doubleValue(), 1e-9);
		assertEquals(50.0, mediator.getPercent(field), 1e-9);
	}

	@Test
	void switchingToPointsConvertsThroughTheDocumentUnit() {
		combo.setSelectedItem(DocumentLengthUnit.of(LengthUnit.POINT));
		// 50% of 18 points.
		assertEquals(9.0, ((Number) field.getValue()).doubleValue(), 1e-9);
		assertEquals(50.0, mediator.getPercent(field), 1e-9);
		combo.setSelectedItem(DocumentLengthUnit.PERCENT);
		assertEquals(50.0, ((Number) field.getValue()).doubleValue(), 1e-9);
	}

	@Test
	void changingTheDocumentUnitKeepsThePercents() {
		mediator.setCurrentUnit(DocumentLengthUnit.of(LengthUnit.MM));
		mediator.documentUnitChanged(36);
		// The length shown in mm doubles, the percent is the same.
		assertEquals(50.0, mediator.getPercent(field), 1e-9);
		assertEquals(LengthUnit.MM.fromPoints(18), ((Number) field.getValue()).doubleValue(), 1e-9);
	}

	@Test
	void changingTheDocumentUnitDoesNotTouchPercents() {
		mediator.documentUnitChanged(36);
		assertEquals(50.0, ((Number) field.getValue()).doubleValue(), 1e-9);
		assertEquals(50.0, mediator.getPercent(field), 1e-9);
	}

	@Test
	void showsTwoDecimalsAtMostAndOnlyIfNeeded() {
		mediator.setPercent(field, 6);
		assertEquals("6", field.getText());
		mediator.setPercent(field, 33.333333);
		assertEquals(java.text.NumberFormat.getNumberInstance().format(33.33), field.getText());
	}

	@Test
	void roundedTextDoesNotRoundTheValue() throws Exception {
		mediator.setPercent(field, 33.333333);
		// What happens when the field loses the focus.
		field.commitEdit();
		assertEquals(33.333333, mediator.getPercent(field), 1e-12);
	}

	@Test
	void editedTextIsUsed() throws Exception {
		mediator.setPercent(field, 33.333333);
		field.setText(java.text.NumberFormat.getNumberInstance().format(12.5));
		assertEquals(12.5, mediator.getPercent(field), 1e-12);
	}

	@Test
	void percentIsTheFirstChoice() {
		assertTrue(DocumentLengthUnit.values().get(0).isPercent());
		assertEquals(LengthUnit.values().length + 1, DocumentLengthUnit.values().size());
	}
}
