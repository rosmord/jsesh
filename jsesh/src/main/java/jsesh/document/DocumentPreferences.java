package jsesh.document;

import jsesh.model.LayoutSetting;
import jsesh.model.TextLayoutSettings;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.render.style.PageTextPosition;

/// The layout preferences of a document, as saved in its header.
///
/// The layout of the text is described by:
///
/// - the **unit** of the document, in points: the height of a quadrat. It is
///   the only absolute length of the text. Changing it scales the whole
///   document;
/// - the [TextLayoutSettings], the same settings as those of zones
///   (`linegap`, `quadratwidth`, `fitsigns`...), with the lengths in percents
///   of the unit. Here, they are all set.
///
/// Some preferences are not proportional to the text, and are in points: the
/// page (a physical size), and the sign height under which the small body font
/// is used (a legibility limit).
///
/// The header is read and written by `jsesh.io.document.DocumentHeaderCodec`,
/// which also reads the headers of older versions.
///
/// @param unit the unit of the document (the height of a quadrat), in points.
/// @param settings the layout settings; unset ones get their default value.
/// @param smallBodyScaleLimit the height of A1, in points, under which signs
///        are drawn with the small body font.
/// @param useLinesForShading are shadings drawn with hatching lines (rather
///        than in gray)?
/// @param useUnderlineForRed are the red quadrats underlined (and drawn in
///        black), as in monochrome typography, rather than drawn in red?
/// @param pageWidth total width of the page; 0 if the document has no page.
/// @param pageHeight total height of the page; 0 if not specified.
/// @param marginLeft left margin of the page.
/// @param marginRight right margin of the page.
/// @param marginTop top margin of the page.
/// @param marginBottom bottom margin of the page.
/// @param pageNumberPosition where the page number is written.
/// @param pageTitlePosition where the title is written.
/// @param pageTextsAlternate are left and right exchanged on even pages?
/// @param pageTitle the title written on each page (never null, maybe empty).
public record DocumentPreferences(
		double unit,
		TextLayoutSettings settings,
		double smallBodyScaleLimit,
		boolean useLinesForShading,
		boolean useUnderlineForRed,
		double pageWidth,
		double pageHeight,
		double marginLeft,
		double marginRight,
		double marginTop,
		double marginBottom,
		PageTextPosition pageNumberPosition,
		PageTextPosition pageTitlePosition,
		boolean pageTextsAlternate,
		String pageTitle) {

	/// The default small body scale limit, in points.
	public static final double DEFAULT_SMALL_BODY_SCALE_LIMIT = 12;

	/// The preferences of a document whose header sets nothing.
	public static final DocumentPreferences DEFAULT = new DocumentPreferences(
			LayoutSetting.DEFAULT_UNIT, TextLayoutSettings.DOCUMENT_DEFAULTS,
			DEFAULT_SMALL_BODY_SCALE_LIMIT, false, false,
			0, 0, 0, 0, 0, 0,
			PageTextPosition.NONE, PageTextPosition.NONE, false, "");

	/// Completes the settings with their default values, and replaces null
	/// page texts by "nothing".
	public DocumentPreferences {
		if (!(unit > 0)) {
			throw new IllegalArgumentException("The unit must be positive: " + unit);
		}
		settings = TextLayoutSettings.DOCUMENT_DEFAULTS.overriddenBy(
				settings == null ? TextLayoutSettings.INHERIT_ALL : settings);
		pageNumberPosition = pageNumberPosition == null ? PageTextPosition.NONE : pageNumberPosition;
		pageTitlePosition = pageTitlePosition == null ? PageTextPosition.NONE : pageTitlePosition;
		pageTitle = pageTitle == null ? "" : pageTitle;
	}

	/// The default preferences.
	public DocumentPreferences() {
		this(DEFAULT.unit, DEFAULT.settings, DEFAULT.smallBodyScaleLimit, DEFAULT.useLinesForShading, DEFAULT.useUnderlineForRed,
				0, 0, 0, 0, 0, 0, PageTextPosition.NONE, PageTextPosition.NONE, false, "");
	}

	// Reading.

	/// @return the orientation of the text.
	public TextOrientation textOrientation() {
		return settings.orientation();
	}

	/// @return the direction of the text.
	public TextDirection textDirection() {
		return settings.direction();
	}

	/// @param setting a length setting.
	/// @return its value, in percents of the unit.
	public double percent(LayoutSetting setting) {
		return settings.length(setting);
	}

	/// @param setting a length setting.
	/// @return its value, in points.
	public double points(LayoutSetting setting) {
		return unit * settings.length(setting) / 100;
	}

	/// @param setting a flag setting.
	/// @return its value.
	public boolean flag(LayoutSetting setting) {
		return settings.flag(setting);
	}

	/// Does the document have a page (and so, a fixed width for its lines)?
	///
	/// @return true if the page width is positive.
	public boolean hasPage() {
		return pageWidth > 0;
	}

	/// @return true if a page number or a title is written in the margins.
	public boolean hasPageTexts() {
		return pageNumberPosition != PageTextPosition.NONE
				|| (pageTitlePosition != PageTextPosition.NONE && !pageTitle.isEmpty());
	}

	// Pseudo-setters.

	/// @param unit the new unit, in points. The other lengths of the text,
	///        being relative to it, follow: this scales the whole text.
	/// @return a copy of these preferences, with the new unit.
	public DocumentPreferences withUnit(double unit) {
		return new DocumentPreferences(unit, settings, smallBodyScaleLimit, useLinesForShading, useUnderlineForRed,
				pageWidth, pageHeight, marginLeft, marginRight, marginTop, marginBottom,
				pageNumberPosition, pageTitlePosition, pageTextsAlternate, pageTitle);
	}

	/// @param settings new settings (the unset ones get their default value).
	/// @return a copy of these preferences, with the new settings.
	public DocumentPreferences withSettings(TextLayoutSettings settings) {
		return new DocumentPreferences(unit, settings, smallBodyScaleLimit, useLinesForShading, useUnderlineForRed,
				pageWidth, pageHeight, marginLeft, marginRight, marginTop, marginBottom,
				pageNumberPosition, pageTitlePosition, pageTextsAlternate, pageTitle);
	}

	/// @param setting a setting.
	/// @param value its new value (null for its default value).
	/// @return a copy of these preferences, with the new value.
	public DocumentPreferences with(LayoutSetting setting, Object value) {
		return withSettings(settings.with(setting, value));
	}

	/// @param setting a length setting.
	/// @param points its new value, in points (converted to percents of the unit).
	/// @return a copy of these preferences, with the new value.
	public DocumentPreferences withPoints(LayoutSetting setting, double points) {
		return with(setting, points / unit * 100);
	}

	/// @param textOrientation the orientation of the text.
	/// @return a copy of these preferences, with the new value.
	public DocumentPreferences withTextOrientation(TextOrientation textOrientation) {
		if (textOrientation == null)
			throw new NullPointerException("TextOrientation should not be null");
		return with(LayoutSetting.LAYOUT, textOrientation);
	}

	/// @param textDirection the direction of the text.
	/// @return a copy of these preferences, with the new value.
	public DocumentPreferences withTextDirection(TextDirection textDirection) {
		if (textDirection == null)
			throw new NullPointerException("TextDirection should not be null");
		return with(LayoutSetting.DIRECTION, textDirection);
	}

	/// @param smallBodyScaleLimit the height of A1, in points, under which the
	///        small body font is used.
	/// @return a copy of these preferences, with the new value.
	public DocumentPreferences withSmallBodyScaleLimit(double smallBodyScaleLimit) {
		return new DocumentPreferences(unit, settings, smallBodyScaleLimit, useLinesForShading, useUnderlineForRed,
				pageWidth, pageHeight, marginLeft, marginRight, marginTop, marginBottom,
				pageNumberPosition, pageTitlePosition, pageTextsAlternate, pageTitle);
	}

	/// @param useLinesForShading are shadings drawn with lines?
	/// @return a copy of these preferences, with the new value.
	public DocumentPreferences withUseLinesForShading(boolean useLinesForShading) {
		return new DocumentPreferences(unit, settings, smallBodyScaleLimit, useLinesForShading, useUnderlineForRed,
				pageWidth, pageHeight, marginLeft, marginRight, marginTop, marginBottom,
				pageNumberPosition, pageTitlePosition, pageTextsAlternate, pageTitle);
	}

	/// @param useUnderlineForRed are red quadrats underlined, rather than drawn in red?
	/// @return a copy of these preferences, with the new value.
	public DocumentPreferences withUseUnderlineForRed(boolean useUnderlineForRed) {
		return new DocumentPreferences(unit, settings, smallBodyScaleLimit, useLinesForShading, useUnderlineForRed,
				pageWidth, pageHeight, marginLeft, marginRight, marginTop, marginBottom,
				pageNumberPosition, pageTitlePosition, pageTextsAlternate, pageTitle);
	}

	/// Sets the page of the document.
	///
	/// @param pageWidth total width of the page; 0 for no page.
	/// @param pageHeight total height of the page; 0 if not specified.
	/// @param marginLeft left margin.
	/// @param marginRight right margin.
	/// @param marginTop top margin.
	/// @param marginBottom bottom margin.
	/// @return a copy of these preferences, with the new page.
	public DocumentPreferences withPage(double pageWidth, double pageHeight,
			double marginLeft, double marginRight, double marginTop,
			double marginBottom) {
		return new DocumentPreferences(unit, settings, smallBodyScaleLimit, useLinesForShading, useUnderlineForRed,
				pageWidth, pageHeight, marginLeft, marginRight, marginTop, marginBottom,
				pageNumberPosition, pageTitlePosition, pageTextsAlternate, pageTitle);
	}

	/// Sets the texts written in the margins of each page.
	///
	/// @param numberPosition where the page number is written.
	/// @param titlePosition where the title is written.
	/// @param title the title (a single line).
	/// @return a copy of these preferences, with the new texts.
	public DocumentPreferences withPageTexts(PageTextPosition numberPosition,
			PageTextPosition titlePosition, String title) {
		return withPageTexts(numberPosition, titlePosition, title, false);
	}

	/// Sets the texts written in the margins of each page.
	///
	/// @param numberPosition where the page number is written (on odd pages if the texts alternate).
	/// @param titlePosition where the title is written (on odd pages if the texts alternate).
	/// @param title the title (a single line).
	/// @param alternate true if left and right are exchanged on even pages.
	/// @return a copy of these preferences, with the new texts.
	public DocumentPreferences withPageTexts(PageTextPosition numberPosition,
			PageTextPosition titlePosition, String title, boolean alternate) {
		return new DocumentPreferences(unit, settings, smallBodyScaleLimit, useLinesForShading, useUnderlineForRed,
				pageWidth, pageHeight, marginLeft, marginRight, marginTop, marginBottom,
				numberPosition, titlePosition, alternate, title);
	}
}
