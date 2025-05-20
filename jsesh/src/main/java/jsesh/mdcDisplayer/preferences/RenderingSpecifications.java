package jsesh.mdcDisplayer.preferences;

import jsesh.mdcDisplayer.preferences.elementSpecifications.ElementDrawingSpecifications;

/**
 * Specifications for rendering a text.
 * New system for drawing specifications.
 * Will change name when the old one is removed !!
 */
public record RenderingSpecifications(
		boolean smallSignsCentered,		
		PageSpecifications pageSpecifications,
		GroupsLayoutSpecifications groupsLayoutSpecifications,
		ElementDrawingSpecifications elementDrawingSpecifications,
		StrokeSpecifications strokeSpecifications
		) {
	
}
