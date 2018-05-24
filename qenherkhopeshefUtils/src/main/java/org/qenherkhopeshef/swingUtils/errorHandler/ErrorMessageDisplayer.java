package org.qenherkhopeshef.swingUtils.errorHandler;

import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialog;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialogFactory;

/**
 * Utility class for displaying low level error messages.
 *
 * @author rosmord
 */
public class ErrorMessageDisplayer {

    private Throwable message;

    private JPanel messagePanel = null;
    private JTextArea mesgArea;
    private JTextArea techArea;

    public ErrorMessageDisplayer(Throwable message) {
        super();
        this.message = message;
    }

    public void display() {
        if (messagePanel == null) {
            buildMessagePanel();
        }
        JOptionPane
                .showMessageDialog(
                        null,
                        messagePanel,
                        Messages.getString("ErrorMessageDisplayer.error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
    }

    private void buildMessagePanel() {
        messagePanel = new JPanel();
        messagePanel
                .setLayout(new BoxLayout(messagePanel, BoxLayout.PAGE_AXIS));
        String msg = message.getLocalizedMessage();
        if ("".equals(msg) || null == msg) //$NON-NLS-1$
        {
            msg = message.getMessage();
        }

        mesgArea = new JTextArea(msg);
        mesgArea.setEditable(false);
        StringWriter writer = new StringWriter();
        message.printStackTrace(new PrintWriter(writer));

        techArea = new JTextArea(writer.toString());
        techArea.setEditable(false);

        messagePanel.add(mesgArea);

        messagePanel.add(new JLabel(Messages
                .getString("ErrorMessageDisplayer.technical_info_title"))); //$NON-NLS-1$

        JScrollPane techDetailsScroll = new JScrollPane(techArea);
        techDetailsScroll.setPreferredSize(new Dimension(640, 480));
        messagePanel.add(techDetailsScroll);
        JButton saveButton = new JButton(Messages
                .getString("ErrorMessageDisplayer.save_info")); //$NON-NLS-1$
        saveButton.setToolTipText(Messages
                .getString("ErrorMessageDisplayer.save_info_tooltip")); //$NON-NLS-1$
        messagePanel.add(saveButton);
        saveButton.addActionListener(e -> save());
    }

    private void save() {
        PortableFileDialog fileChooser = PortableFileDialogFactory.createFileSaveDialog(mesgArea);
        if (fileChooser.show() == FileOperationResult.OK) {
            File saveFile = fileChooser.getSelectedFile();
            if (saveFile.exists()) {
                if (JOptionPane.showConfirmDialog(messagePanel,
                        "Overwrite file " + saveFile.getName() + " ?",
                        "Confirm sava", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            //Saving tokenInfoLabels
            try {
                try (PrintWriter writer = new PrintWriter(new FileWriter(saveFile))) {
                    message.printStackTrace(writer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
