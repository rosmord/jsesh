/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.preferences.document.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jsesh.resources.JSeshMessages;
import jsesh.ui.widgets.units.LengthUnit;

/// A unit in which the lengths of a document (spacing, sizes of quadrats...)
/// are shown in the preferences dialog.
///
/// Documents store these lengths in percents of the unit of the document
/// (the height of a quadrat, see [jsesh.model.LayoutSetting]), so [#PERCENT] is
/// the natural choice. The other units are the absolute units of [LengthUnit]
/// (points, inches, mm), for users who prefer to think in those: the dialog
/// converts them through the unit of the document, and the document always
/// stores percents.
public final class DocumentLengthUnit {

	/// Percents of the unit of the document (the height of a quadrat).
	public static final DocumentLengthUnit PERCENT = new DocumentLengthUnit(null);

	private static final List<DocumentLengthUnit> VALUES;

	static {
		List<DocumentLengthUnit> values = new ArrayList<>();
		values.add(PERCENT);
		for (LengthUnit unit : LengthUnit.values()) {
			values.add(new DocumentLengthUnit(unit));
		}
		VALUES = Collections.unmodifiableList(values);
	}

	/// The absolute unit, or null for percents.
	private final LengthUnit absolute;

	private DocumentLengthUnit(LengthUnit absolute) {
		this.absolute = absolute;
	}

	/// @return all the units, [#PERCENT] first.
	public static List<DocumentLengthUnit> values() {
		return VALUES;
	}

	/// @param unit an absolute unit.
	/// @return the corresponding document length unit.
	public static DocumentLengthUnit of(LengthUnit unit) {
		for (DocumentLengthUnit value : VALUES) {
			if (value.absolute == unit) {
				return value;
			}
		}
		throw new IllegalArgumentException("Unknown unit " + unit);
	}

	/// @return true for percents of the unit of the document.
	public boolean isPercent() {
		return absolute == null;
	}

	/// Converts a length shown in this unit.
	///
	/// @param value the length, in this unit.
	/// @param documentUnit the unit of the document, in points.
	/// @return the length, in percents of the unit of the document.
	public double toPercent(double value, double documentUnit) {
		if (absolute == null) {
			return value;
		}
		return absolute.toPoints(value) / documentUnit * 100;
	}

	/// Converts a length to this unit.
	///
	/// @param percent the length, in percents of the unit of the document.
	/// @param documentUnit the unit of the document, in points.
	/// @return the length, in this unit.
	public double fromPercent(double percent, double documentUnit) {
		if (absolute == null) {
			return percent;
		}
		return absolute.fromPoints(percent / 100 * documentUnit);
	}

	/// @return the name of the unit, for the combo box.
	@Override
	public String toString() {
		return absolute == null ? JSeshMessages.getString("drawingPrefs.lengthUnit.percent") : absolute.toString();
	}
}
