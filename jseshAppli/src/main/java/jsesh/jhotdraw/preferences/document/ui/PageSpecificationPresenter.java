package jsesh.jhotdraw.preferences.document.ui;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;

import jsesh.jhotdraw.preferences.document.ui.JPageSpecificationEditor.SizeChoice;
import jsesh.jhotdraw.utils.JSimpleDialog;
import jsesh.render.style.GeometrySpecification;
import jsesh.render.style.JSeshStyle;
import jsesh.render.style.PageDecoration;
import jsesh.render.style.PageSpecification;
import jsesh.render.style.PageTextPosition;
import jsesh.render.style.StandardPageSize;
import jsesh.resources.JSeshMessages;
import jsesh.ui.widgets.units.LengthUnit;
import jsesh.ui.widgets.units.UnitMediator;

/// Presenter for the page size and margins of a document.
///
/// The values are edited in a [LengthUnit] chosen by the user (millimetres
/// when the dialog opens), and stored in layout units (points).
public class PageSpecificationPresenter {

	/// The page proposed when the document has none: A4, 20 mm margins.
	private static final PageSpecification DEFAULT_PAGE = new PageSpecification(
			StandardPageSize.A4.width(), StandardPageSize.A4.height(),
			56.69f, 56.69f, 56.69f, 56.69f);

	private final JPageSpecificationEditor form = new JPageSpecificationEditor();
	private final UnitMediator unitMediator = new UnitMediator();
	private final List<JFormattedTextField> fields;

	/// True while the presenter itself changes the form, so that its listeners
	/// don't react to the changes.
	private boolean adjusting = false;

	public PageSpecificationPresenter() {
		unitMediator.attachToComboBox(form.getUnitField());
		fields = Arrays.asList(form.getWidthField(), form.getHeightField(),
				form.getMarginLeftField(), form.getMarginRightField(),
				form.getMarginTopField(), form.getMarginBottomField());
		fields.forEach(unitMediator::managedTextField);

		form.getEnableBox().addActionListener(
				e -> form.setPageControlsEnabled(form.getEnableBox().isSelected()));
		form.getSizeField().addActionListener(e -> sizeChosen());
		form.getPortraitButton().addActionListener(e -> orientationChosen());
		form.getLandscapeButton().addActionListener(e -> orientationChosen());
		form.getWidthField().addPropertyChangeListener("value", e -> dimensionsChanged());
		form.getHeightField().addPropertyChangeListener("value", e -> dimensionsChanged());
	}

	private double points(JFormattedTextField field) {
		return unitMediator.getManagedFieldInPoints(field);
	}

	private void setPoints(JFormattedTextField field, double points) {
		field.setValue(unitMediator.getCurrentUnit().fromPoints(points));
	}

	/// The user picked a size in the combo box.
	private void sizeChosen() {
		if (adjusting) {
			return;
		}
		SizeChoice choice = (SizeChoice) form.getSizeField().getSelectedItem();
		if (choice == null || choice.size() == null) {
			return;
		}
		double w = choice.size().width();
		double h = choice.size().height();
		if (form.getLandscapeButton().isSelected()) {
			double tmp = w;
			w = h;
			h = tmp;
		}
		adjusting = true;
		try {
			setPoints(form.getWidthField(), w);
			setPoints(form.getHeightField(), h);
		} finally {
			adjusting = false;
		}
	}

	/// The user clicked on portrait or landscape: width and height are swapped
	/// if needed.
	private void orientationChosen() {
		double w = points(form.getWidthField());
		double h = points(form.getHeightField());
		if (w != h && (w > h) != form.getLandscapeButton().isSelected()) {
			adjusting = true;
			try {
				setPoints(form.getWidthField(), h);
				setPoints(form.getHeightField(), w);
			} finally {
				adjusting = false;
			}
		}
	}

	/// The width or the height changed: the size in the combo box and the
	/// orientation follow.
	private void dimensionsChanged() {
		if (adjusting || form.getWidthField().getValue() == null
				|| form.getHeightField().getValue() == null) {
			return;
		}
		double w = points(form.getWidthField());
		double h = points(form.getHeightField());
		showShape(w, h);
	}

	/// Shows the standard size and the orientation matching given dimensions.
	private void showShape(double w, double h) {
		Optional<StandardPageSize> standard = StandardPageSize.find(w, h);
		adjusting = true;
		try {
			form.getSizeField().setSelectedItem(standard.map(SizeChoice::new).orElse(JPageSpecificationEditor.CUSTOM));
			if (w != h) {
				form.getLandscapeButton().setSelected(w > h);
				form.getPortraitButton().setSelected(w < h);
			}
		} finally {
			adjusting = false;
		}
	}

	/// Copy the page of a style into the dialog.
	///
	/// @param jseshStyle the style to read.
	public void loadPreferences(JSeshStyle jseshStyle) {
		PageSpecification page = jseshStyle.geometry().page();
		boolean hasPage = page.isDefined();
		PageSpecification shown = hasPage ? page : DEFAULT_PAGE;
		adjusting = true;
		try {
			unitMediator.setCurrentUnit(LengthUnit.POINT);
			form.getWidthField().setValue((double) shown.pageWidth());
			form.getHeightField().setValue((double) shown.pageHeight());
			form.getMarginLeftField().setValue((double) shown.marginLeft());
			form.getMarginRightField().setValue((double) shown.marginRight());
			form.getMarginTopField().setValue((double) shown.marginTop());
			form.getMarginBottomField().setValue((double) shown.marginBottom());
			PageDecoration decoration = jseshStyle.geometry().pageDecoration();
			form.getNumberPositionField().setSelectedItem(decoration.numberPosition());
			form.getTitlePositionField().setSelectedItem(decoration.titlePosition());
			form.getTitleField().setText(decoration.title());
			form.getAlternateBox().setSelected(decoration.alternate());
			form.getEnableBox().setSelected(hasPage);
			form.setPageControlsEnabled(hasPage);
		} finally {
			adjusting = false;
		}
		showShape(shown.pageWidth(), shown.pageHeight());
		unitMediator.setCurrentUnit(LengthUnit.MM);
	}

	private PageSpecification currentPage() {
		return new PageSpecification(
				round(points(form.getWidthField())),
				round(points(form.getHeightField())),
				round(points(form.getMarginLeftField())),
				round(points(form.getMarginRightField())),
				round(points(form.getMarginTopField())),
				round(points(form.getMarginBottomField())));
	}

	// Two decimals are enough, and keep the saved values readable.
	private static float round(double points) {
		return Math.round(points * 100) / 100f;
	}

	/// Checks the values of the dialog.
	///
	/// @return null if the values can be used (or if there is no page), else
	/// the code of the message to show to the user.
	public String validationError() {
		if (!form.getEnableBox().isSelected()) {
			return null;
		}
		PageSpecification page = currentPage();
		if (page.pageWidth() <= 0 || page.pageHeight() < 0
				|| page.marginLeft() < 0 || page.marginRight() < 0
				|| page.marginTop() < 0 || page.marginBottom() < 0) {
			return "pagePrefs.error.negative";
		}
		if (page.usableWidth() <= 0 || (page.pageHeight() > 0 && page.usableHeight() <= 0)) {
			return "pagePrefs.error.margins";
		}
		return null;
	}

	/// Apply the content of the dialog to a style.
	///
	/// When the check box is not selected, the style gets no page.
	///
	/// @param original the original style.
	/// @return a modified style.
	public JSeshStyle updatePreferences(JSeshStyle original) {
		PageSpecification page = form.getEnableBox().isSelected()
				? currentPage()
				: PageSpecification.NONE;
		boolean hadPage = original.geometry().page().isDefined();
		GeometrySpecification defaults = GeometrySpecification.DEFAULT;
		PageDecoration decoration = new PageDecoration(
				(PageTextPosition) form.getNumberPositionField().getSelectedItem(),
				(PageTextPosition) form.getTitlePositionField().getSelectedItem(),
				form.getTitleField().getText().strip(),
				form.getAlternateBox().isSelected());
		return original.copy().geometry(g -> {
			g.page(page).pageDecoration(decoration);
			// With a page, the margins around the text are the page's.
			if (page.isDefined()) {
				g.leftMargin(page.marginLeft()).topMargin(page.marginTop());
			} else if (hadPage) {
				g.leftMargin(defaults.leftMargin()).topMargin(defaults.topMargin());
			}
			return g;
		}).build();
	}

	/// Shows the dialog, until the values are valid or the user cancels.
	///
	/// @param parent the parent component.
	/// @return [JOptionPane#OK_OPTION] or [JOptionPane#CANCEL_OPTION].
	public int showDialog(Component parent) {
		while (true) {
			JSimpleDialog dialog = new JSimpleDialog(parent, form.getPanel(),
					JSeshMessages.getString("pagePrefs.title"));
			int result = dialog.show();
			dialog.dispose();
			String error = result == JOptionPane.OK_OPTION ? validationError() : null;
			if (error == null) {
				return result;
			}
			JOptionPane.showMessageDialog(parent, JSeshMessages.getString(error),
					JSeshMessages.getString("pagePrefs.title"), JOptionPane.ERROR_MESSAGE);
		}
	}
}
