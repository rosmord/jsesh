/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.preferences.document.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import jsesh.document.DocumentPreferences;
import jsesh.model.LayoutSetting;
import jsesh.render.style.DocumentPreferencesStyleConverter;
import jsesh.render.style.JSeshStyle;
import jsesh.ui.widgets.units.LengthUnit;

class DrawingSpecificationsPresenterTest {

	private static DocumentPreferences prefsOf(JSeshStyle style) {
		return DocumentPreferencesStyleConverter.toDocumentPreferences(style);
	}

	@Test
	void untouchedDialogKeepsEveryLength() {
		DrawingSpecificationsPresenter p = new DrawingSpecificationsPresenter();
		JSeshStyle style = DocumentPreferencesStyleConverter.applyDocumentPreferences(
				prefsOf(JSeshStyle.DEFAULT).with(LayoutSetting.LINE_GAP, 33.333333), JSeshStyle.DEFAULT);
		p.loadPreferences(style);
		DocumentPreferences result = prefsOf(p.updatePreferences(style));
		assertEquals(33.333333, result.percent(LayoutSetting.LINE_GAP), 1e-9);
	}

	@Test
	void lengthsEditedInMillimetresAreSavedInPercents() {
		DrawingSpecificationsPresenter p = new DrawingSpecificationsPresenter();
		p.loadPreferences(JSeshStyle.DEFAULT);
		double unit = prefsOf(JSeshStyle.DEFAULT).unit();
		p.form.getLengthsUnitField().setSelectedItem(DocumentLengthUnit.of(LengthUnit.MM));
		// A line gap of 1 quadrat, written in mm.
		p.form.getLengthFields().get(LayoutSetting.LINE_GAP).setValue(LengthUnit.MM.fromPoints(unit));
		DocumentPreferences result = prefsOf(p.updatePreferences(JSeshStyle.DEFAULT));
		assertEquals(100.0, result.percent(LayoutSetting.LINE_GAP), 1e-6);
		assertEquals(unit, result.unit(), 1e-9);
	}

	@Test
	void changingTheDocumentUnitKeepsPercentsWhateverTheDisplayUnit() {
		DrawingSpecificationsPresenter p = new DrawingSpecificationsPresenter();
		p.loadPreferences(JSeshStyle.DEFAULT);
		DocumentPreferences before = prefsOf(JSeshStyle.DEFAULT);
		p.form.getLengthsUnitField().setSelectedItem(DocumentLengthUnit.of(LengthUnit.POINT));
		p.form.getDocumentUnitField().setValue(before.unit() * 2);
		DocumentPreferences after = prefsOf(p.updatePreferences(JSeshStyle.DEFAULT));
		assertEquals(before.unit() * 2, after.unit(), 1e-9);
		for (LayoutSetting s : LayoutSetting.values()) {
			if (s.kind() == LayoutSetting.Kind.LENGTH) {
				assertEquals(before.percent(s), after.percent(s), 1e-6, s.name());
			}
		}
	}
}
