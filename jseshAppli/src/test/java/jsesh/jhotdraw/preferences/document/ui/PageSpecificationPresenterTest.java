package jsesh.jhotdraw.preferences.document.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jsesh.jhotdraw.preferences.document.ui.JPageSpecificationEditor.SizeChoice;
import jsesh.render.style.GeometrySpecification;
import jsesh.render.style.JSeshStyle;
import jsesh.render.style.PageSpecification;
import jsesh.render.style.StandardPageSize;

class PageSpecificationPresenterTest {

	private static JSeshStyle withPage(PageSpecification page) {
		return JSeshStyle.DEFAULT.copy().geometry(g -> g.page(page)).build();
	}

	/// The form is exercised through the presenter's own listeners.
	private static final class Access extends PageSpecificationPresenter {
		JPageSpecificationEditor form() {
			try {
				var f = PageSpecificationPresenter.class.getDeclaredField("form");
				f.setAccessible(true);
				return (JPageSpecificationEditor) f.get(this);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Test
	void noPageGivesUnchecked() {
		Access p = new Access();
		p.loadPreferences(JSeshStyle.DEFAULT);
		assertFalse(p.form().getEnableBox().isSelected());
		assertFalse(p.form().getWidthField().isEnabled());
		assertFalse(p.updatePreferences(JSeshStyle.DEFAULT).geometry().hasPage());
	}

	@Test
	void checkingTheBoxGivesAnA4Page() {
		Access p = new Access();
		p.loadPreferences(JSeshStyle.DEFAULT);
		p.form().getEnableBox().doClick();
		assertTrue(p.form().getWidthField().isEnabled());
		assertNull(p.validationError());
		PageSpecification page = p.updatePreferences(JSeshStyle.DEFAULT).geometry().page();
		assertEquals(StandardPageSize.A4.width(), page.pageWidth(), 0.01);
		assertEquals(StandardPageSize.A4.height(), page.pageHeight(), 0.01);
		assertEquals(56.69f, page.marginLeft(), 0.01);
	}

	@Test
	void existingPageIsRoundTripped() {
		PageSpecification page = new PageSpecification(595.28f, 841.89f, 57, 40, 50, 60);
		Access p = new Access();
		p.loadPreferences(withPage(page));
		assertTrue(p.form().getEnableBox().isSelected());
		assertEquals(SizeChoice.class, p.form().getSizeField().getSelectedItem().getClass());
		assertEquals(StandardPageSize.A4, ((SizeChoice) p.form().getSizeField().getSelectedItem()).size());
		assertTrue(p.form().getPortraitButton().isSelected());
		JSeshStyle result = p.updatePreferences(withPage(page));
		assertEquals(page, result.geometry().page());
		assertEquals(57, result.geometry().leftMargin(), 0.001);
		assertEquals(50, result.geometry().topMargin(), 0.001);
	}

	@Test
	void unCheckingRemovesThePageAndRestoresMargins() {
		PageSpecification page = new PageSpecification(595.28f, 841.89f, 57, 57, 57, 57);
		JSeshStyle original = withPage(page).copy().geometry(g -> g.leftMargin(57).topMargin(57)).build();
		Access p = new Access();
		p.loadPreferences(original);
		p.form().getEnableBox().doClick();
		JSeshStyle result = p.updatePreferences(original);
		assertFalse(result.geometry().hasPage());
		assertEquals(GeometrySpecification.DEFAULT.leftMargin(), result.geometry().leftMargin(), 0.001);
		assertEquals(GeometrySpecification.DEFAULT.topMargin(), result.geometry().topMargin(), 0.001);
	}

	@Test
	void choosingLandscapeSwapsDimensions() {
		Access p = new Access();
		p.loadPreferences(withPage(new PageSpecification(595.28f, 841.89f, 57, 57, 57, 57)));
		p.form().getLandscapeButton().doClick();
		PageSpecification page = p.updatePreferences(JSeshStyle.DEFAULT).geometry().page();
		assertEquals(841.89f, page.pageWidth(), 0.01);
		assertEquals(595.28f, page.pageHeight(), 0.01);
		assertEquals(StandardPageSize.A4, ((SizeChoice) p.form().getSizeField().getSelectedItem()).size());
	}

	@Test
	void choosingAStandardSizeSetsTheDimensions() {
		Access p = new Access();
		p.loadPreferences(withPage(new PageSpecification(300, 400, 20, 20, 20, 20)));
		assertEquals(JPageSpecificationEditor.CUSTOM, p.form().getSizeField().getSelectedItem());
		p.form().getSizeField().setSelectedItem(new SizeChoice(StandardPageSize.LETTER));
		PageSpecification page = p.updatePreferences(JSeshStyle.DEFAULT).geometry().page();
		assertEquals(StandardPageSize.LETTER.width(), page.pageWidth(), 0.01);
		assertEquals(StandardPageSize.LETTER.height(), page.pageHeight(), 0.01);
	}

	@Test
	void typingDimensionsSelectsTheMatchingStandardSize() {
		Access p = new Access();
		p.loadPreferences(withPage(new PageSpecification(300, 400, 20, 20, 20, 20)));
		// The dialog opens in millimetres.
		p.form().getWidthField().setValue(297.0);
		p.form().getHeightField().setValue(210.0);
		SizeChoice choice = (SizeChoice) p.form().getSizeField().getSelectedItem();
		assertNotNull(choice.size());
		assertEquals(StandardPageSize.A4, choice.size());
		assertTrue(p.form().getLandscapeButton().isSelected());
	}

	@Test
	void marginsLargerThanThePageAreRejected() {
		Access p = new Access();
		p.loadPreferences(withPage(new PageSpecification(300, 400, 20, 20, 20, 20)));
		// The dialog opens in millimetres: 100 mm on each side is more than 300 pt.
		p.form().getMarginLeftField().setValue(100.0);
		p.form().getMarginRightField().setValue(100.0);
		assertEquals("pagePrefs.error.margins", p.validationError());
	}

	@Test
	void validationIsSkippedWithoutPage() {
		Access p = new Access();
		p.loadPreferences(JSeshStyle.DEFAULT);
		assertNull(p.validationError());
	}
}
