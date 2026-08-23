/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 * 
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.actions.file;

import java.net.URI;
import java.net.URISyntaxException;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.actions.generic.AbstractOpenDocumentAction;

import org.jhotdraw_7_6.app.Application;

/**
 * Import a PDF pasted on the clipboard.
 * @author rosmord
 *
 */
@SuppressWarnings("serial")
public class ImportRTFAction extends AbstractOpenDocumentAction {
	public static final String ID= "file.import.rtf";

	public ImportRTFAction(Application app) {
		super(app);
		BundleHelper.getInstance().configureActionWithID(this, ID);
	}

	
	@Override
	protected URI getDocumentURI() {
		try {
			// Return pseudo-uri for rtf on clipboard.
			return new URI("clipboard:rtf");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}


}
