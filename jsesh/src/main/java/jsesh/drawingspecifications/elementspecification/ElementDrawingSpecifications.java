package jsesh.drawingspecifications.elementspecification;

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

	/**
		 * Create a copy.
		 */
		public Builder copy() {
			return new Builder(this);
		}

		/** copy builder class */
		public static class Builder {
			private CartoucheSpecifications cartoucheSpecifications;
			private EcdoticSpecifications ecdoticSpecifications;
			private EnclosureSpecifications enclosureSpecifications;
			private HwtSignSpecifications hwtSignSpecifications;
			private SerekhSpecifications serekhSpecifications;
			public Builder(ElementDrawingSpecifications specs) {
				this.cartoucheSpecifications = specs.cartoucheSpecifications;
				this.ecdoticSpecifications = specs.ecdoticSpecifications;
				this.enclosureSpecifications = specs.enclosureSpecifications;
				this.hwtSignSpecifications = specs.hwtSignSpecifications;
				this.serekhSpecifications = specs.serekhSpecifications;
			}
			public Builder cartoucheSpecifications(CartoucheSpecifications cartoucheSpecifications) {
				this.cartoucheSpecifications = cartoucheSpecifications;
				return this;
			}
			public Builder ecdoticSpecifications(EcdoticSpecifications ecdoticSpecifications) {
				this.ecdoticSpecifications = ecdoticSpecifications;
				return this;
			}
			public Builder enclosureSpecifications(EnclosureSpecifications enclosureSpecifications) {
				this.enclosureSpecifications = enclosureSpecifications;
				return this;
			}
			public Builder hwtSignSpecifications(HwtSignSpecifications hwtSignSpecifications) {
				this.hwtSignSpecifications = hwtSignSpecifications;
				return this;
			}
			public Builder serekhSpecifications(SerekhSpecifications serekhSpecifications) {
				this.serekhSpecifications = serekhSpecifications;
				return this;
			}
			public ElementDrawingSpecifications build() {
				return new ElementDrawingSpecifications(cartoucheSpecifications, ecdoticSpecifications, enclosureSpecifications, hwtSignSpecifications, serekhSpecifications);
			}
		}
}
