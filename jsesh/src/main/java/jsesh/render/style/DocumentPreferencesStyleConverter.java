package jsesh.render.style;

import jsesh.document.DocumentPreferences;

/// Converts between [DocumentPreferences] and [JSeshStyle].
///
/// The two describe the same layout settings from different angles:
/// [DocumentPreferences] is what gets written in a document header (lengths in
/// percents of the unit of the document), while [JSeshStyle] is what the
/// renderer actually uses (lengths in points). The correspondence of the
/// layout settings themselves is in [LayoutSettingsApplier], shared with
/// zones.
///
/// The conversion is deliberately placed here, in `jsesh.render`, rather
/// than in `jsesh.document`: a document should not need to know about
/// rendering styles.
///
/// @author rosmord
public class DocumentPreferencesStyleConverter {

	private DocumentPreferencesStyleConverter() {
	}

	/// Creates document preferences reflecting a rendering style.
	///
	/// The unit of the document is the [GeometrySpecification#unit()] of the
	/// style.
	///
	/// @param jseshStyle the style to convert.
	/// @return the matching document preferences.
	public static DocumentPreferences toDocumentPreferences(JSeshStyle jseshStyle) {
		GeometrySpecification g = jseshStyle.geometry();
		double unit = g.unit();
		return new DocumentPreferences()
				.withUnit(unit)
				.withSettings(LayoutSettingsApplier.settingsOf(jseshStyle, unit))
				.withSmallBodyScaleLimit(g.smallBodyScaleLimit())
				.withUseLinesForShading(jseshStyle.painting().shadingStyle()
						.equals(ShadingMode.LINE_HATCHING))
				.withUseUnderlineForRed(jseshStyle.painting().underlineRed())
				.withPage(
						g.page().pageWidth(),
						g.page().pageHeight(),
						g.page().marginLeft(),
						g.page().marginRight(),
						g.page().marginTop(),
						g.page().marginBottom())
				.withPageTexts(
						g.pageDecoration().numberPosition(),
						g.pageDecoration().titlePosition(),
						g.pageDecoration().title(),
						g.pageDecoration().alternate());
	}

	private static PageSpecification pageOf(DocumentPreferences preferences) {
		if (!preferences.hasPage()) {
			return PageSpecification.NONE;
		}
		return new PageSpecification(
				preferences.pageWidth(),
				preferences.pageHeight(),
				preferences.marginLeft(),
				preferences.marginRight(),
				preferences.marginTop(),
				preferences.marginBottom());
	}

	/// Applies document preferences on top of an existing style.
	///
	/// @param preferences the document preferences to apply.
	/// @param original    the style used as a basis for the new one.
	/// @return a new style, with the document preferences applied.
	public static JSeshStyle applyDocumentPreferences(
			DocumentPreferences preferences, JSeshStyle original) {
		JSeshStyle.Builder builder = original.copy()
				.geometry(g -> g
						.unit(preferences.unit())
						.smallBodyScaleLimit(preferences.smallBodyScaleLimit())
						.page(pageOf(preferences))
						.pageDecoration(new PageDecoration(
								preferences.pageNumberPosition(),
								preferences.pageTitlePosition(),
								preferences.pageTitle(),
								preferences.pageTextsAlternate())))
				.geometry(g -> {
					// With a page, the margins around the text are the page's.
					if (preferences.hasPage()) {
						g.leftMargin(preferences.marginLeft())
								.topMargin(preferences.marginTop());
					}
					return g;
				})
				.painting(c -> c.shadingStyle(
						preferences.useLinesForShading()
								? ShadingMode.LINE_HATCHING
								: ShadingMode.GRAY_SHADING)
						.underlineRed(preferences.useUnderlineForRed()));
		return LayoutSettingsApplier.apply(preferences.settings(), preferences.unit(), builder).build();
	}
}
