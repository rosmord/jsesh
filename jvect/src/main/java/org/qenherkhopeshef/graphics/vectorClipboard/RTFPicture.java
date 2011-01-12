/*
      jvect-clipboard a cut and paste vector library for java
    Copyright (C) 2008	Serge Rosmorduc

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 */
package org.qenherkhopeshef.graphics.vectorClipboard;

import java.awt.datatransfer.Transferable;
import java.io.IOException;

import org.qenherkhopeshef.graphics.rtfBasicWriter.SimpleRTFWriter;

/**
 * A picture which can be either used as a stand-alone picture or embedded in RTF.
 * By default it will be considered as an embedded picture when calling buildTransferable.
 * @author rosmord
 *
 */
public abstract class RTFPicture implements TransferablePicture {
	
	private boolean embeddedInRTF= true;
	
	public void setEmbeddedInRTF(boolean embeddedInRTF) {
		this.embeddedInRTF = embeddedInRTF;
	}
	
	public boolean isEmbeddedInRTF() {
		return embeddedInRTF;
	}
	
	abstract void write(SimpleRTFWriter writer) throws IOException;
	
	public final Transferable buildTransferable() {
		if (isEmbeddedInRTF()) {
			return new RTFTransferable(this);
		} else {
			return buildStandAloneTransferable(); 
		}
	}

	/**
	 * Used by children classes to define how they create a transferable when used outside of an RTF picture.
	 * @return
	 */
	protected abstract Transferable buildStandAloneTransferable();
}