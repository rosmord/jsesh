package jsesh.mdcDisplayer.preferences.elementSpecifications;

/**
 * Specifications for particular elements, mainly cartouches and such.
 */
public record ElementDrawingSpecifications(
			CartoucheSpecifications cartoucheSpecifications,
			EcdoticSpecifications ecdoticSpecifications,
			EnclosureSpecifications enclosureSpecifications,
			HwtSignSpecifications hwtSignSpecifications,
			SerekhSpecifications serekhSpecifications
		) {

}
