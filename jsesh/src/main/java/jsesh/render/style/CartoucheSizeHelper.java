package jsesh.render.style;

import jsesh.model.constants.CartouchePart;
import jsesh.model.constants.CartoucheType;

public class CartoucheSizeHelper {

	// Prevents instantiation of helper class.
	private CartoucheSizeHelper() {
	}

	/**
	 * Compute the size needed for cartouches starts and ends along the main
	 * axis. Use the basic sizes values declared in this class.
	 *
	 * @param jseshStyle the corresponding {@link JSeshStyle}
	 * @param type
	 *                   the type of cartouche
	 * @param element    the part of the cartouche, whose meaning depends on
	 *                   {@code type}.
	 * @return a size.
	 */
	public static float computeCartouchePartLength(JSeshStyle jseshStyle, CartoucheType type, CartouchePart element) {
		GeometrySpecification geometry = jseshStyle.geometry();
		float result = 0;
		switch (type) {
			case CARTOUCHE:
				switch (element) {
					case NONE:
						result = 0;
						break;
					case FIRST:
						result = geometry.cartoucheLoopLength();
						break;
					case SECOND:
						result = geometry.cartoucheLoopLength() + geometry.cartoucheLineWidth();
						break;
					default:
						throw new RuntimeException("bad value for element " + element);
				}
				break;
			case CIRCULAR_ENCLOSURE:
				switch (element) {
					case NONE:
						result = 0;
						break;
					case FIRST:
					case SECOND:
						result = geometry.cartoucheLoopLength();
						break;
					default:
						throw new RuntimeException("bad value for element " + element);
				}
				break;
			case SEREKH:
				switch (element) {
					case FIRST:
						result = geometry.hwtSmallMargin()
								+ geometry.cartoucheLineWidth();
						break;
					case SECOND:
						result = geometry.hwtSmallMargin()
								+ geometry.cartoucheLineWidth()
								+ geometry.serekhDoorSize();
						break;
					default:
						break;
				}
				break;
			case HWT:
				switch (element) {
					case NONE:
						break;
					case FIRST:
						result = geometry.hwtSmallMargin() + geometry.cartoucheLineWidth();
						break;
					case SECOND:
					case THIRD:
						result = geometry.hwtSmallMargin() + geometry.cartoucheLineWidth()
								+ geometry.hwtSquareSize();
						break;
				}
				break;
			case CASTLE:
				if (element != CartouchePart.NONE) {
					result = geometry.hwtSmallMargin()
							+ geometry.bastionDepth()
							+ geometry.cartoucheLineWidth();
				}
				break;
		}
		return result;
	}

	/**
	 * length along the secondary axis of a cartouche from the cartouche
	 * external side to the text inside.
	 *
	 * @param jseshStyle the corresponding drawing specifications.
	 * @param type
	 * @return the length along the secondary axis of a cartouche from the
	 *         cartouche external side to the text inside.
	 */
	public static float computeCartoucheSecondaryLength(JSeshStyle jseshStyle, CartoucheType type) {
		float result = 0;
		GeometrySpecification geometry = jseshStyle.geometry();
		switch (type) {
			case CARTOUCHE:
			case SEREKH:
			case HWT:
			case CIRCULAR_ENCLOSURE:
				result = geometry.cartoucheMargin() + geometry.cartoucheLineWidth();
				break;
			case CASTLE:
				result = geometry.cartoucheMargin() + geometry.cartoucheLineWidth()
						+ geometry.bastionDepth();
				break;
		}
		return result;
	}

}
