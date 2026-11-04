package jsesh.render.style;

/// The texts written in the margins of each page: the page number, and a title.
///
/// They are only written when the document is cut into pages (see
/// [PageSpecification#isPaginated()]).
///
/// @param numberPosition where the page number is written.
/// @param titlePosition where the title is written.
/// @param title the title, which is the same on all pages. May be empty.
/// @param alternate if true, the texts are on the positions given for the odd pages
/// (1, 3...), and on the opposite side (left and right exchanged) for the even
/// pages, as in a book.
public record PageDecoration(
		PageTextPosition numberPosition,
		PageTextPosition titlePosition,
		String title,
		boolean alternate) {

	/// What is put between the title and the page number when they are at the same
	/// place.
	private static final String SEPARATOR = " — ";

	/// Nothing is written in the margins.
	public static final PageDecoration NONE = new PageDecoration(PageTextPosition.NONE, PageTextPosition.NONE, "");

	/// Texts which are at the same place on all pages.
	public PageDecoration(PageTextPosition numberPosition, PageTextPosition titlePosition, String title) {
		this(numberPosition, titlePosition, title, false);
	}

	public PageDecoration {
		numberPosition = numberPosition == null ? PageTextPosition.NONE : numberPosition;
		titlePosition = titlePosition == null ? PageTextPosition.NONE : titlePosition;
		title = title == null ? "" : title;
	}

	/// @return true if something is written in the margins.
	public boolean isDefined() {
		return numberPosition != PageTextPosition.NONE
				|| (titlePosition != PageTextPosition.NONE && !title.isEmpty());
	}

	/// Where a text is on a given page.
	///
	/// @param position the position given for the odd pages.
	/// @param pageNumber the number of the page, from 1.
	/// @return the position, mirrored on even pages if the texts alternate.
	private PageTextPosition positionOn(PageTextPosition position, int pageNumber) {
		return alternate && pageNumber % 2 == 0 ? position.mirrored() : position;
	}

	/// The text to write at a given place of a page.
	///
	/// When the title and the number are at the same place, the title comes first.
	///
	/// @param position the place (not [PageTextPosition#NONE]).
	/// @param pageNumber the number of the page, from 1.
	/// @return the text, or null if nothing is written there.
	public String textAt(PageTextPosition position, int pageNumber) {
		if (position == PageTextPosition.NONE) {
			return null;
		}
		String result = null;
		if (positionOn(titlePosition, pageNumber) == position && !title.isEmpty()) {
			result = title;
		}
		if (positionOn(numberPosition, pageNumber) == position) {
			String number = Integer.toString(pageNumber);
			result = result == null ? number : result + SEPARATOR + number;
		}
		return result;
	}
}
