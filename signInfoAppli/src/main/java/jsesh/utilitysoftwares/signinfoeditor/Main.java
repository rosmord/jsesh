/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.utilitysoftwares.signinfoeditor;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jsesh.JSeshUserSignLibraryConfiguration;
import jsesh.swing.signPalette.PalettePresenter;
import jsesh.utilitysoftwares.signinfoeditor.model.SignInfoModel;
import jsesh.utilitysoftwares.signinfoeditor.ui.SignInfoPresenter;
import jsesh.utilitysoftwares.signinfoeditor.ui.TagEditorPresenter;

import org.qenherkhopeshef.guiFramework.PropertyHolder;
import org.qenherkhopeshef.guiFramework.SimpleApplicationFactory;

/**
 * 
 * @author rosmord
 */
public class Main implements PropertyHolder {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new Main());
	}

	public JFrame mainFrame;

	public SignInfoModel signInfoModel;

	public TagEditorPresenter tagEditorPresenter;

	public SignInfoPresenter signInfoPresenter;

	public JDialog paletteDialog;

	public JDialog tagEditorDialog;

	private JSeshUserSignLibraryConfiguration jseshConfig;

	public Main() {
		try {
			jseshConfig = new JSeshUserSignLibraryConfiguration();
			signInfoModel = new SignInfoModel();

			mainFrame = new JFrame("Sign info Editor");
			signInfoPresenter = new SignInfoPresenter(signInfoModel);

			// Build the framework.
			SimpleApplicationFactory framework = new SimpleApplicationFactory(
					"definitionsI8n", "menu.txt", this);
			framework.addActionList("action_list.txt");
			// Prepare the main frame
			mainFrame.setJMenuBar(framework.getJMenuBar());
			mainFrame.getContentPane().setLayout(new BorderLayout());
			mainFrame.getContentPane().add(signInfoPresenter.getPanel(),
					BorderLayout.CENTER);
			mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			mainFrame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					quit();
				}
			});

			buildPalette();
			buildTagEditor();

			// Navigation Binding
			signInfoPresenter.getPanel().getNextButton().setAction(
					framework.getAction("nextSign"));
			signInfoPresenter.getPanel().getPreviousButton().setAction(
					framework.getAction("previousSign"));
			mainFrame.pack();
			mainFrame.setVisible(true);

			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					openDefault();
				}

			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public SignInfoPresenter getSignInfoPresenter() {
		return signInfoPresenter;
	}

	public void buildPalette() {
		PalettePresenter simplePalettePresenter = new PalettePresenter(jseshConfig.hieroglyphShapeRepository(),
				jseshConfig.hieroglyphDatabase());
		paletteDialog = new JDialog(mainFrame);
		paletteDialog.getContentPane().add(
				simplePalettePresenter.getSimplePalette());
		simplePalettePresenter.setDragEnabled(true);
		paletteDialog.pack();
	}

	public void buildTagEditor() {
		tagEditorPresenter = new TagEditorPresenter(signInfoModel);
		tagEditorDialog = new JDialog(mainFrame);
		tagEditorDialog.getContentPane()
				.add(tagEditorPresenter.getJTagEditor());
		tagEditorDialog.pack();
	}

	public void displayTagEditor() {
		tagEditorDialog.setVisible(!tagEditorDialog.isVisible());
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		return signInfoPresenter.getPropertyChangeSupport();
	}

	public void displayPalette() {
		this.paletteDialog.setVisible(!paletteDialog.isVisible());

	}

	/**
	 * Ask for a XML definition file and open it. (not used any more, maybe in
	 * the future...)
	 * 
	 *
	 */
	@Deprecated
	public void open() {
		FileDialog fileDialog = new FileDialog(mainFrame, "Open");
		if (signInfoPresenter.getCurrentFile() != null) {
			String parent = signInfoPresenter.getCurrentFile().getParent();
			if (parent == null)
				parent = "";
			fileDialog.setDirectory(parent);
			fileDialog.setFile(signInfoPresenter.getCurrentFile().getName());
		}
		fileDialog.setVisible(true);
		if (fileDialog.getFile() != null) {
			File dir = new File(fileDialog.getDirectory());
			File f = new File(dir, fileDialog.getFile());
			signInfoPresenter.openFile(f);
			tagEditorPresenter.setSignInfoModel(signInfoPresenter
					.getSignInfoModel());
		}
	}

	public void openDefault() {
		// The open method should probably be located at this level, not in the
		// presenter...
		// OR we should wrap the SignInfoModel in a proxy class, so that it can
		// be changed.

		if (signInfoPresenter.isExpertMode()) {
			signInfoPresenter.openExpertFile();
		} else {
			signInfoPresenter.openFile(HieroglyphDatabaseRepository
					.getUserSignDefinitionFile());
		}

		tagEditorPresenter.setSignInfoModel(signInfoPresenter
				.getSignInfoModel());
	}

	public void selectExpertFile() {
		FileDialog fileDialog = new FileDialog(mainFrame, "Select Expert File", FileDialog.SAVE);
		if (signInfoPresenter.getExpertFile() != null) {
			String parent = signInfoPresenter.getExpertFile().getParent();
			if (parent == null)
				parent = "";
			fileDialog.setDirectory(parent);
			fileDialog.setFile(signInfoPresenter.getExpertFile().getName());
		}
		fileDialog.setVisible(true);
		if (fileDialog.getFile() != null) {
			File dir = new File(fileDialog.getDirectory());
			File f = new File(dir, fileDialog.getFile());
			signInfoPresenter.setExpertFile(f);
			if (signInfoPresenter.isExpertMode())
				openDefault();
		}
	}

	public void newFile() {
		int answer = JOptionPane
				.showConfirmDialog(
						mainFrame,
						"You will create an empty description file, \nand possibly lose your previous editing.\n Is that what you want?",
						"Confirm clear", JOptionPane.YES_NO_OPTION);
		if ((answer == JOptionPane.YES_OPTION)) {
			signInfoPresenter.openSystemFile();
			tagEditorPresenter.setSignInfoModel(signInfoPresenter
					.getSignInfoModel());
		}
	}

	public void saveAs() {
		FileDialog fileDialog = new FileDialog(mainFrame, "Save as");
		if (signInfoPresenter.getCurrentFile() != null) {
			String parent = signInfoPresenter.getCurrentFile().getParent();
			if (parent == null)
				parent = "";
			fileDialog.setDirectory(parent);
			fileDialog.setFile(signInfoPresenter.getCurrentFile().getName());
		}
		fileDialog.setMode(FileDialog.SAVE);
		fileDialog.setVisible(true);
		if (fileDialog.getFile() != null) {
			File dir = new File(fileDialog.getDirectory());
			File f = new File(dir, fileDialog.getFile());
			signInfoPresenter.saveFile(f);
		}
	}

	public void save() {
		signInfoPresenter.save();
	}

	public void resetDefault() {
		newFile();
	}

	public boolean isExpertMode() {
		return signInfoPresenter.isExpertMode();
	}

	public void setExpertMode(boolean expert) {
		if (expert && signInfoPresenter.getExpertFile() == null)
			selectExpertFile();
		signInfoPresenter.setExpertMode(expert);
		openDefault();
	}

	public void quit() {
		boolean reallyQuit = true;
		if (signInfoPresenter.getSignInfoModel().isDirty()) {
			int answer = JOptionPane.showConfirmDialog(mainFrame,
					"Data was not saved. Really quit ?", "Confirm exit",
					JOptionPane.YES_NO_OPTION);
			reallyQuit = (answer == JOptionPane.YES_OPTION);
		}
		if (reallyQuit)
			System.exit(0);
	}

}
