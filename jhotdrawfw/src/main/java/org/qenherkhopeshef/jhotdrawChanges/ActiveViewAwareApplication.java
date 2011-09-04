package org.qenherkhopeshef.jhotdrawChanges;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;

public interface ActiveViewAwareApplication extends Application {
	void setActiveView(View view);
}
