package jsesh.tmp.simpleDemo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import jsesh.mdc.MDCParserFacade;
import jsesh.mdc.utils.MDCSyntaxError;

/**
 * DemoMDCDisplayer.java
 * 
 * A basic demo test for MDC displaying facilities.
 * 
 * Created: Mon Jun 10 13:06:34 2002
 * 
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC
 *         </a>
 * 
 */

public class DemoMDCDisplayerAppli extends JFrame {
	HieroglyphicTextBuilder builder;

	MDCParserFacade parser;

	DemoMDCDisplayer display;

	JFileChooser fc;

	public DemoMDCDisplayerAppli() {
		super("Demo Manuel de codage Displayer");
		builder = new HieroglyphicTextBuilder();
		parser = new MDCParserFacade(builder);
		display = new DemoMDCDisplayer();
		// parser.setDebug(true);
		fc = new JFileChooser();
		setSize(300, 300);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}

			public void windowOpened(WindowEvent e) {
			}
		});
		setJMenuBar(createMenu());
		getContentPane().add(new JScrollPane(display));
	}

	private void loadText() {
		fc.setDialogTitle("choose a manuel de codage text");
		int result = fc.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try {
				parser.parse(new FileReader(file));
				display.setText(builder.getText());
			} catch (MDCSyntaxError e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {

		try {
			//UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.setLookAndFeel(UIManager
					.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		DemoMDCDisplayerAppli f = new DemoMDCDisplayerAppli();
		f.setVisible(true);
	}

	protected JMenuBar createMenu() {
		JMenuBar mb = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.add(new AbstractAction("Open") {
			public void actionPerformed(ActionEvent e) {
				loadText();
			}
		});

		menu.add(new AbstractAction("Exit") {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mb.add(menu);
		return mb;
	}
} // DemoMDCDisplayerAppli
