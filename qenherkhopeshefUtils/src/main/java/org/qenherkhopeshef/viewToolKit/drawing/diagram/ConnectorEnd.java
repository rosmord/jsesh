package org.qenherkhopeshef.viewtoolkit1.drawing.diagram;

/// The decoration drawn at one end of a [ConnectorElement].
///
/// The names follow the usual (UML) meanings, but nothing here is specific to
/// UML.
public enum ConnectorEnd {
	/// Plain line end.
	NONE,
	/// Open arrow (two strokes), e.g. a navigable association or a dependency.
	OPEN_ARROW,
	/// Hollow triangle, e.g. generalization or realization.
	HOLLOW_TRIANGLE,
	/// Hollow diamond, e.g. aggregation.
	HOLLOW_DIAMOND,
	/// Filled diamond, e.g. composition.
	FILLED_DIAMOND
}
