package org.qenherkhopeshef.guiFramework.demo.mdi;

import java.awt.Dimension;
import java.beans.PropertyChangeSupport;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;

import org.qenherkhopeshef.guiFramework.PropertyHolder;
import org.qenherkhopeshef.swingUtils.PortableFileDialog;

/**
 * An abstraction which represents an editing session for one document.
 * @author rosmord
 */
public class DocumentEditorSessionController implements PropertyHolder, MDIFrameController {

    private JFrame jFrame;
    private JTextArea jTextArea;
    private UndoManager undoManager = new UndoManager();
    private TextEditorApplicationController applicationDelegate;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public DocumentEditorSessionController(TextEditorApplicationController applicationDelegate) {
        this.applicationDelegate = applicationDelegate;
        jFrame = new JFrame("New document");
        jTextArea = new JTextArea();
        JScrollPane jScrollPane = new JScrollPane(jTextArea);
        jScrollPane.setPreferredSize(new Dimension(800, 600));
        jFrame.getContentPane().add(jScrollPane);
        jTextArea.getDocument().addDocumentListener(new MyDocumentListener());
        jTextArea.getDocument().addUndoableEditListener(new MyUndoManager());
        jFrame.pack();
    }

    public JFrame getFrame() {
        return jFrame;
    }

   

   
    public Object getApplicationDelegate() {
        return applicationDelegate;
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    public boolean isDocumentEditor() {
        return true;
    }

    public void closeDocument() {
        int result;
        // Close the current document editor if possible ?
        if (isDirty() || 1 == 1) {
            result= JOptionPane.showConfirmDialog(getFrame(),"MESSAGE", "title", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            switch (result) {
                case JOptionPane.YES_OPTION:
                    break;
                case JOptionPane.NO_OPTION:
                    break;
                case JOptionPane.CANCEL_OPTION:
                    break;
            }
        }
    }

    public void saveDocument() {
    }

    public void saveDocumentAs() {
        PortableFileDialog dialog= PortableFileDialog.createFileSaveDialog();
        
    }

    public void undo() {
        boolean oldRedo = isRedoPossible();
        boolean oldUndo = isUndoPossible();
        undoManager.undo();
        getPropertyChangeSupport().firePropertyChange("redoPossible", oldRedo, isRedoPossible());
        getPropertyChangeSupport().firePropertyChange("undoPossible", oldUndo, isUndoPossible());

    }

    public void redo() {
        boolean oldRedo = isRedoPossible();
        boolean oldUndo = isUndoPossible();
        undoManager.redo();
        getPropertyChangeSupport().firePropertyChange("redoPossible", oldRedo, isRedoPossible());
        getPropertyChangeSupport().firePropertyChange("undoPossible", oldUndo, isUndoPossible());

    }

    public void cut() {
    }

    public void copy() {
    }

    public void paste() {
    }

    private class MyDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent arg0) {
            setDirty(true);
        }

        public void removeUpdate(DocumentEvent arg0) {
            setDirty(true);
        }

        public void changedUpdate(DocumentEvent arg0) {
            setDirty(true);
        }
    }

    private class MyUndoManager implements UndoableEditListener {

        public void undoableEditHappened(UndoableEditEvent e) {
            boolean oldRedo = isRedoPossible();
            boolean oldUndo = isUndoPossible();
            undoManager.addEdit(e.getEdit());
            getPropertyChangeSupport().firePropertyChange("redoPossible", oldRedo, isRedoPossible());
            getPropertyChangeSupport().firePropertyChange("undoPossible", oldUndo, isUndoPossible());
        }
    }

    public boolean isRedoPossible() {
        return undoManager.canRedo();
    }

    public boolean isUndoPossible() {
        return undoManager.canUndo();
    }

    
    private boolean dirty = false;
    public static final String PROP_DIRTY = "dirty";

    /**
     * Get the value of dirty
     *
     * @return the value of dirty
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Set the value of dirty
     *
     * @param dirty new value of dirty
     */
    public void setDirty(boolean dirty) {
        boolean oldDirty = this.dirty;
        this.dirty = dirty;
        getPropertyChangeSupport().firePropertyChange(PROP_DIRTY, oldDirty, dirty);
    }

    

}
