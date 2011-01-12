package org.qenherkhopeshef.graphics.eps;

import java.io.IOException;

/**
 * Possible error when generating EPS.
 * Mostly I/O Errors, in fact.
 * @author rosmord
 *
 */

public class EPSOutException extends RuntimeException {

	public EPSOutException(IOException e) {
		super(e);
	}

}
