/*
 * @(#)Main.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */

package jsesh.jhotdraw;

import jsesh.hieroglyphs.CompositeHieroglyphsManager;
import jsesh.hieroglyphs.DefaultHieroglyphicFontManager;

import org.jhotdraw.app.*;

/**
 * Main class.
 * 
 * @author Serge Rosmorduc.
 * @version $Id: Main.java 604 2010-01-09 12:00:29Z rawcoder $
 */
public class Main {
	public final static String NAME = "JSesh";
	public final static String COPYRIGHT = "JSesh is LGPL Software writen by S. Rosmorduc";

	/**
	 * Launches the application.
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		DefaultHieroglyphicFontManager manager = DefaultHieroglyphicFontManager.getInstance();
		JSeshApplicationModel applicationModel = new JSeshApplicationModel();
		applicationModel.setCopyright(COPYRIGHT);
		applicationModel.setName(NAME);
		// We use setViewClass method instead of setViewClassName
		// Because we will probably move the code...

		applicationModel.setViewClass(JSeshView.class);
		applicationModel.setVersion(Main.class.getPackage()
				.getImplementationVersion());

		Application app;
		if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
			app = new MyOSXApplication();
			// app = new SDIApplication();
		} else if (System.getProperty("os.name").toLowerCase()
				.startsWith("win")) {
			// app = new DefaultMDIApplication();
			// app = new SDIApplication();
			app = new MyOSXApplication();

		} else {
			// app = new SDIApplication();
			app = new MyOSXApplication();
		}
		app.setModel(applicationModel);
		app.launch(args);
	}
}
