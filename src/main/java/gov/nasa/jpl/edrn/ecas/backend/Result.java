// Copyright 2006 California Institute of Technology. ALL RIGHTS
// RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id$

package gov.nasa.jpl.edrn.ecas.backend;

import java.util.List;

/**
 * Result.
 *
 * @author Kelly
 */
public class Result {
	public Result(Class kind, Object value) throws Throwable {
		if (kind != null) {
			java.lang.reflect.Constructor ctor = kind.getConstructor(ARGS);
			this.value = ctor.newInstance(new Object[]{ value });
		} else
			this.value = value;
	}
	
	public Object getValue() {
		return value;
	}

	private Object value;
	
	private static Class[] ARGS = { String.class };
}
