package jsesh.swing.signPalette;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 * A full palette display, with glyph informations.
 * Should be integrated and created by PalettePresenter.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */

public class HieroglyphicPaletteDialog {
	private JDialog dialog;

	private PalettePresenter presenter;

	public HieroglyphicPaletteDialog(JFrame component) {
		dialog = new JDialog(component, "Hieroglyphic Palette");
		JTabbedPane tabbedPane = new JTabbedPane();
		presenter = new PalettePresenter();

		// This part should probably be handled by using another prepared
		// element.
		tabbedPane.addTab("Palette", presenter.getSimplePalette());

		JPanel infoPanel = new JPanel();

		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		JScrollPane sp1 = new JScrollPane(presenter.getGlyphDescriptionField());
		sp1.setBorder(BorderFactory.createTitledBorder("Glyph info"));

		JScrollPane sp2 = new JScrollPane(presenter.getSignDescriptionField());
		sp2.setBorder(BorderFactory.createTitledBorder("Sign Info"));
		infoPanel.add(sp2);
		infoPanel.add(sp1);
		
		tabbedPane.addTab("Sign Description", infoPanel);
		dialog.getContentPane().add(tabbedPane);
		dialog.pack();
	}

	public JDialog getDialog() {
		return dialog;
	}

	public void setHieroglyphPaletteListener(
			HieroglyphPaletteListener hieroglyphPaletteListener) {
		presenter.setHieroglyphPaletteListener(hieroglyphPaletteListener);
	}

}
