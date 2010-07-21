package org.qenherkhopeshef.guiFramework.demo.mdi;

import org.qenherkhopeshef.guiFramework.SimpleApplicationFramework;

public class SimpleTextEditorApplication {

	private TextEditorApplicationController textAppWorkflow;
	
	public SimpleTextEditorApplication() {
		textAppWorkflow= new TextEditorApplicationController();
		SimpleApplicationFramework simpleApplicationFramework= 
			new SimpleApplicationFramework("mdi8n", "menu.txt", textAppWorkflow);
	}

    
	
}
