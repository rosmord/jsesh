/*
 * Created on 4 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.graphics.export.generic;

import jsesh.editor.caret.MDCCaret;

/**
 * Interface for classes that can provide a caret.
 * @author S. Rosmorduc
 */
public interface CaretBroker {
	MDCCaret getCaret();
}
