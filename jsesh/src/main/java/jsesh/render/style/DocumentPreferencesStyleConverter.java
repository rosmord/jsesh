package jsesh.render.style;

import jsesh.document.DocumentPreferences;

/**
 * Converts between {@link DocumentPreferences} and {@link JSeshStyle}.
 *
 * <p>
 * The two describe the same layout settings from different angles:
 * {@link DocumentPreferences} is what gets written in a document header, while
 * {@link JSeshStyle} is what the renderer actually uses. Keeping the mapping in
 * one place avoids the two copies drifting apart.
 *
 * <p>
 * The conversion is deliberately placed here, in {@code jsesh.render}, rather
 * than in {@code jsesh.document}: a document should not need to know about
 * rendering styles.
 *
 * @author rosmord
 */
public class DocumentPreferencesStyleConverter {

	private DocumentPreferencesStyleConverter() {
	}

	/**
	 * Creates document preferences reflecting a rendering style.
	 *
	 * @param jseshStyle the style to convert.
	 * @return the matching document preferences.
	 */
	public static DocumentPreferences toDocumentPreferences(
			JSeshStyle jseshStyle) {
		return new DocumentPreferences()
				.withTextDirection(jseshStyle.options().textDirection())
				.withTextOrientation(jseshStyle.options().textOrientation())
				.withSmallSignCentered(jseshStyle.options().smallSignCentered())
				.withLimitQuadratWidthInLines(
						jseshStyle.options().limitQuadratWidthInLines())
				.withCartoucheLineWidth(
						jseshStyle.geometry().cartoucheLineWidth())
				.withColumnSkip(jseshStyle.geometry().columnSkip())
				.withLineSkip(jseshStyle.geometry().lineSkip())
				.withMaxQuadratHeight(jseshStyle.geometry().maxCadratHeight())
				.withMaxQuadratWidth(jseshStyle.geometry().maxCadratWidth())
				.withSmallBodyScaleLimit(
						jseshStyle.geometry().smallBodyScaleLimit())
				.withStandardSignHeight(
						jseshStyle.geometry().standardSignHeight())
				.withSmallSkip(jseshStyle.geometry().smallSkip())
				.withAlphabeticTextBaseline(
						jseshStyle.geometry().alphabeticTextBaseline())
				.withUseLinesForShading(jseshStyle.painting().shadingStyle()
						.equals(ShadingMode.LINE_HATCHING))
				.withPage(
						jseshStyle.geometry().page().pageWidth(),
						jseshStyle.geometry().page().pageHeight(),
						jseshStyle.geometry().page().marginLeft(),
						jseshStyle.geometry().page().marginRight(),
						jseshStyle.geometry().page().marginTop(),
						jseshStyle.geometry().page().marginBottom());
	}

	private static PageSpecification pageOf(DocumentPreferences preferences) {
		if (!preferences.hasPage()) {
			return PageSpecification.NONE;
		}
		return new PageSpecification(
				(float) preferences.getPageWidth(),
				(float) preferences.getPageHeight(),
				(float) preferences.getMarginLeft(),
				(float) preferences.getMarginRight(),
				(float) preferences.getMarginTop(),
				(float) preferences.getMarginBottom());
	}

	/**
	 * Applies document preferences on top of an existing style.
	 *
	 * @param preferences the document preferences to apply.
	 * @param original    the style used as a basis for the new one.
	 * @return a new style, with the document preferences applied.
	 */
	public static JSeshStyle applyDocumentPreferences(
			DocumentPreferences preferences, JSeshStyle original) {
		return original.copy()
				.geometry(g -> g
						.cartoucheLineWidth(
								(float) preferences.getCartoucheLineWidth())
						.maxCadratWidth(
								(float) preferences.getMaxQuadratWidth())
						.maxCadratHeight(
								(float) preferences.getMaxQuadratHeight())
						.lineSkip((float) preferences.getLineSkip())
						.columnSkip((float) preferences.getColumnSkip())
						.standardSignHeight(
								(float) preferences.getStandardSignHeight())
						.smallBodyScaleLimit(
								(float) preferences.getSmallBodyScaleLimit())
						.smallSkip((float) preferences.getSmallSkip())
						.alphabeticTextBaseline(
								(float) preferences.getAlphabeticTextBaseline())
						.page(pageOf(preferences)))
				.geometry(g -> {
					// With a page, the margins around the text are the page's.
					if (preferences.hasPage()) {
						g.leftMargin((float) preferences.getMarginLeft())
								.topMargin((float) preferences.getMarginTop());
					}
					return g;
				})
				.options(o -> o
						.smallSignCentered(preferences.isSmallSignCentered())
						.limitQuadratWidthInLines(
								preferences.isLimitQuadratWidthInLines())
						.textOrientation(preferences.getTextOrientation())
						.textDirection(preferences.getTextDirection()))
				.painting(c -> c.shadingStyle(
						preferences.isUseLinesForShading()
								? ShadingMode.LINE_HATCHING
								: ShadingMode.GRAY_SHADING))
				.build();
	}
}
