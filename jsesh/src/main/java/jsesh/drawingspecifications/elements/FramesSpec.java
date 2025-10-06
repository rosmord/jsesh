package jsesh.drawingspecifications.elements;

import jsesh.drawingspecifications.elements.enclosureframe.CartoucheSpec;
import jsesh.drawingspecifications.elements.enclosureframe.EnclosureSpec;
import jsesh.drawingspecifications.elements.enclosureframe.HwtSignSpec;
import jsesh.drawingspecifications.elements.enclosureframe.SerekhSpec;

/**
 * Specifications for cartouches and such.
 */
public record FramesSpec(
		CartoucheSpec cartoucheSpecifications,
		EnclosureSpec enclosureSpecifications,
		HwtSignSpec hwtSignSpecifications,
		SerekhSpec serekhSpecifications) {
			
	public static final FramesSpec DEFAULT = new FramesSpec(
			CartoucheSpec.DEFAULT,
			EnclosureSpec.DEFAULT,
			HwtSignSpec.DEFAULT,
			SerekhSpec.DEFAULT);
	/**
	 * Create a copy.
	 */
	public Builder copy() {
		return new Builder(this);
	}

	/** copy builder class */
	public static class Builder {
		private CartoucheSpec cartoucheSpecifications;
		private EnclosureSpec enclosureSpecifications;
		private HwtSignSpec hwtSignSpecifications;
		private SerekhSpec serekhSpecifications;

		public Builder(FramesSpec specs) {
			this.cartoucheSpecifications = specs.cartoucheSpecifications;
			this.enclosureSpecifications = specs.enclosureSpecifications;
			this.hwtSignSpecifications = specs.hwtSignSpecifications;
			this.serekhSpecifications = specs.serekhSpecifications;
		}

		public Builder cartoucheSpecifications(CartoucheSpec cartoucheSpecifications) {
			this.cartoucheSpecifications = cartoucheSpecifications;
			return this;
		}

		public Builder enclosureSpecifications(EnclosureSpec enclosureSpecifications) {
			this.enclosureSpecifications = enclosureSpecifications;
			return this;
		}

		public Builder hwtSignSpecifications(HwtSignSpec hwtSignSpecifications) {
			this.hwtSignSpecifications = hwtSignSpecifications;
			return this;
		}

		public Builder serekhSpecifications(SerekhSpec serekhSpecifications) {
			this.serekhSpecifications = serekhSpecifications;
			return this;
		}

		public FramesSpec build() {
			return new FramesSpec(cartoucheSpecifications, enclosureSpecifications, hwtSignSpecifications,
					serekhSpecifications);
		}

	}
}
