// Copyright 2006 California Institute of Technology. ALL RIGHTS
// RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id$

package gov.nasa.jpl.edrn.ecas.backend;

import java.util.List;

/**
 * Dispatcher.
 *
 * @author Kelly
 */
public interface Dispatcher {
	Result handleRequest(String methodSpecifier, List params, String user, String password) throws Exception;
}
