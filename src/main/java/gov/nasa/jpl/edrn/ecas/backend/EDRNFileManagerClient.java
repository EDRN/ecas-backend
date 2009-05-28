// Copyright 2006 California Institute of Technology. ALL RIGHTS
// RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id$

package gov.nasa.jpl.edrn.ecas.backend;

//APACHE imports
import gov.nasa.jpl.oodt.cas.filemgr.datatransfer.DataTransfer;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import gov.nasa.jpl.oodt.cas.filemgr.system.XmlRpcFileManagerClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xmlrpc.CommonsXmlRpcTransportFactory;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

/**
 * Dispatcher.
 * 
 * @author John J. Tran
 */

public class EDRNFileManagerClient extends XmlRpcFileManagerClient {

	/* our xml rpc client */
    private XmlRpcClient client = null;

    /* our log stream */
    private static Logger LOG = Logger.getLogger(XmlRpcFileManagerClient.class
            .getName());

    /* file manager url */
    private URL fileManagerUrl = null;

    /* data transferer needed if client is request to move files itself */
    // private DataTransfer dataTransfer = null;

    /**
     * <p>
     * Constructs a new XmlRpcFileManagerClient with the given <code>url</code>.
     * </p>
     * 
     * @param url
     *            The url pointer to the xml rpc file manager service.
     */
    public EDRNFileManagerClient(URL url) throws ConnectionException {
    	super(url);
        // set up the configuration, if there is any
        if (System.getProperty("gov.nasa.jpl.oodt.cas.filemgr.properties") != null) {
            String configFile = System
                    .getProperty("gov.nasa.jpl.oodt.cas.filemgr.properties");
            LOG.log(Level.INFO,
                    "Loading File Manager Configuration Properties from: ["
                            + configFile + "]");
            try {
                System.getProperties().load(
                        new FileInputStream(new File(configFile)));
            } catch (Exception e) {
                LOG.log(Level.INFO,
                        "Error loading configuration properties from: ["
                                + configFile + "]");
            }

        }

        CommonsXmlRpcTransportFactory transportFactory = new CommonsXmlRpcTransportFactory(
                url);
        int connectionTimeoutMins = Integer
                .getInteger(
                        "gov.nasa.jpl.oodt.cas.filemgr.system.xmlrpc.connectionTimeout.minutes",
                        20).intValue();
        int connectionTimeout = connectionTimeoutMins * 60 * 1000;
        int requestTimeoutMins = Integer
                .getInteger(
                        "gov.nasa.jpl.oodt.cas.filemgr.system.xmlrpc.requestTimeout.minutes",
                        60).intValue();
        int requestTimeout = requestTimeoutMins * 60 * 1000;
        transportFactory.setConnectionTimeout(connectionTimeout);
        transportFactory.setTimeout(requestTimeout);
        client = new XmlRpcClient(url, transportFactory);
        fileManagerUrl = url;

        if (!isAlive()) {
            throw new ConnectionException("Exception connecting to filemgr: ["
                    + this.fileManagerUrl + "]");
        }

    }

	// bounce the server
	public void bounce() throws RepositoryManagerException {
		Vector<Object> argList = new Vector<Object>();

		System.out.println("[debug]: EDRNFileManagerClient -- start bounce()");
		
		try {
			client.execute("filemgr.bounce", argList);
		} catch (XmlRpcException e) {
			throw new RepositoryManagerException(e.getMessage());
		} catch (IOException e) {
			throw new RepositoryManagerException(e.getMessage());
		}
		
		System.out.println("[debug]: EDRNFileManagerClient -- finished bounce()");

	}
	
}