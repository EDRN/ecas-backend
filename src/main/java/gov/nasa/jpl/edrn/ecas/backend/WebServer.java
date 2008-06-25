// Copyright 2006 California Institute of Technology. ALL RIGHTS
// RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id$

package gov.nasa.jpl.edrn.ecas.backend;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Vector;
import java.io.IOException;
import org.apache.xmlrpc.AuthenticatedXmlRpcHandler;

/**
 * XML-RPC Web Server.
 *
 * @author Kelly
 */
public final class WebServer extends org.apache.xmlrpc.WebServer implements AuthenticatedXmlRpcHandler {
	public WebServer(int port) throws IOException {
		super(port);
		addHandler("$default", this);
	}
	
	public Object execute(String methodSpecifier, Vector params, String user, String password) throws Exception {
		for (Iterator i = dispatchers.iterator(); i.hasNext();) {
			Result rc = ((Dispatcher) i.next()).handleRequest(methodSpecifier, params, user, password);
			if (rc != null)
				return rc.getValue();
		}
		throw new IllegalStateException("No request dispatcher was able to return a non-null value to the XML-RPC caller");
	}

	public void addDispatcher(Dispatcher dispatcher) {
		if (dispatcher == null)
			throw new IllegalArgumentException("Non-null dispatchers are illegal");
		dispatchers.add(dispatcher);
	}
		
	private List dispatchers = new ArrayList();
}