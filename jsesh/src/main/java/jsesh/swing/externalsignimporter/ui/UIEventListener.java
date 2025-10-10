package jsesh.graphics.glyphs.ui;

import java.awt.event.ActionListener;
import java.awt.event.FocusListener;

public interface UIEventListener extends ActionListener, FocusListener {
	void resizeVerticallyTo(double y); 
}
