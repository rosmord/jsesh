/**
 * Dialog system for files, which uses the "best" (as decided by me) way to ask
 * for a file or a directory.
 *
 * <p>Uses either the Swing or the native AWT file dialogs, depending on the
 * operation and on the system. Favors functionality if possible, but always
 * takes a working solution.
 *
 * <p>To use this file dialog system, two classes are important:
 * {@link org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialogFactory},
 * which is to be used to create the dialog, and
 * {@link org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialog}
 * itself.
 */
package org.qenherkhopeshef.swingUtils.portableFileDialog;
