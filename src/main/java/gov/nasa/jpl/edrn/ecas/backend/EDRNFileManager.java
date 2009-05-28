//Copyright (c) 2005, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.ecas.backend;

//APACHE imports
import gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog;
import gov.nasa.jpl.oodt.cas.filemgr.datatransfer.DataTransfer;
import gov.nasa.jpl.oodt.cas.filemgr.datatransfer.TransferStatusTracker;
import gov.nasa.jpl.oodt.cas.filemgr.metadata.extractors.FilemgrMetExtractor;
import gov.nasa.jpl.oodt.cas.filemgr.repository.RepositoryManager;
import gov.nasa.jpl.oodt.cas.filemgr.structs.Element;
import gov.nasa.jpl.oodt.cas.filemgr.structs.ExtractorSpec;
import gov.nasa.jpl.oodt.cas.filemgr.structs.FileTransferStatus;
import gov.nasa.jpl.oodt.cas.filemgr.structs.Product;
import gov.nasa.jpl.oodt.cas.filemgr.structs.ProductPage;
import gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType;
import gov.nasa.jpl.oodt.cas.filemgr.structs.Query;
import gov.nasa.jpl.oodt.cas.filemgr.structs.Reference;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.CatalogException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.VersioningException;
import gov.nasa.jpl.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import gov.nasa.jpl.oodt.cas.filemgr.util.XmlRpcStructFactory;
import gov.nasa.jpl.oodt.cas.filemgr.versioning.Versioner;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.metadata.exceptions.MetExtractionException;
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xmlrpc.WebServer;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * An XML RPC-based File manager.
 * </p>
 * 
 */
public class EDRNFileManager {

    /* the port to run the XML RPC web server on, default is 1999 */
    private int webServerPort = 1999;

    /* our Catalog */
    private Catalog catalog = null;

    /* our RepositoryManager */
    private RepositoryManager repositoryManager = null;

    /* our DataTransfer */
    private DataTransfer dataTransfer = null;

    /* our log stream */
    private Logger LOG = Logger.getLogger(EDRNFileManager.class.getName());

    /* our xml rpc web server */
    private WebServer webServer = null;

    /* our data transfer status tracker */
    private TransferStatusTracker transferStatusTracker = null;

    /**
     * <p>
     * Creates a new XmlRpcFileManager with the given metadata store factory,
     * and the given data store factory, on the given port.
     * </p>
     * 
     * @param port
     *            The web server port to run the XML Rpc server on, defaults to
     *            1999.
     */
    public EDRNFileManager(int port) throws Exception {
    	
        // set up the configuration, if there is any
        if (System.getProperty("gov.nasa.jpl.oodt.cas.filemgr.properties") != null) {
            String configFile = System
                    .getProperty("gov.nasa.jpl.oodt.cas.filemgr.properties");
            LOG.log(Level.INFO,
                    "Loading File Manager Configuration Properties from: ["
                            + configFile + "]");
            System.getProperties().load(
                    new FileInputStream(new File(configFile))); 
        }

        this.webServerPort = port;
        
        // start up the web server
        webServer = new WebServer(webServerPort);
        webServer.addHandler("filemgr", this);
        webServer.start();
  
        // make sure that all configurations are read in correctly
        this.loadConfig();
        
        LOG.log(Level.INFO, "File Manager started by "
                + System.getProperty("user.name", "unknown"));

    }
     
    private boolean loadConfig() throws FileNotFoundException, IOException, URISyntaxException {
    	return this.bounce();
    }
    
    private String[] getFilesInDirectory(String directory) throws URISyntaxException {
    	return getFilesInDirectory(new URI(directory));
    }
    
	private String[] getFilesInDirectory(URI directory) {
		File dir = new File(directory);

		String[] children = dir.list();
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.startsWith(".");
			}
		};
		children = dir.list(filter);

		return children;
	}
    
    public static void main(String[] args) throws Exception {
		int portNum = -1;
		String usage = "FileManager --portNum <port number for xml rpc service>\n";

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--portNum")) {
				portNum = Integer.parseInt(args[++i]);
			}
		}

		if (portNum == -1) {
			System.err.println(usage);
			System.exit(1);
		}

		EDRNFileManager manager = new EDRNFileManager(portNum);

		for (;;)
			try {
				Thread.currentThread().join();
			} catch (InterruptedException ignore) {
				// ignoramus maximus (for now)
			}
	}
	
    public synchronized boolean bounce() throws FileNotFoundException, IOException, URISyntaxException {
        
    	String metaFactory = null, dataFactory = null, transferFactory = null;

        // update the validation properties
        String curationRepository = System.getProperty("gov.nasa.jpl.oodt.cas.filemgr.curationRepoPath.baseDir");
        
        /*
        String repositoryMgrDirsStr = System.getProperty("gov.nasa.jpl.oodt.cas.filemgr.repositorymgr.dirs");
        String validationDirsStr = System.getProperty("gov.nasa.jpl.oodt.cas.filemgr.validation.dirs");
        */
        
        String repositoryMgrDirsStr = "";
        String validationDirsStr = "";
        
        String curationRepositoryPath = PathUtils.replaceEnvVariables(curationRepository);
        String curationPolicies[] = getFilesInDirectory(curationRepositoryPath);
        
        if (curationPolicies != null) {
        	
			validationDirsStr += curationRepository + "/" + curationPolicies[0];
			repositoryMgrDirsStr += curationRepository + "/" + curationPolicies[0];
        	
			for (int i = 1; i < curationPolicies.length; i++) {
				String policy = curationPolicies[i];
				validationDirsStr += "," + curationRepository + "/" + policy;
				repositoryMgrDirsStr += "," + curationRepository + "/" + policy;
			}

			System.setProperty("gov.nasa.jpl.oodt.cas.filemgr.validation.dirs",
					validationDirsStr);
			System.setProperty(
					"gov.nasa.jpl.oodt.cas.filemgr.repositorymgr.dirs",
					repositoryMgrDirsStr);
		}
        
        System.out.println("[debug] curationRepositoryPath: " + curationRepositoryPath);
        System.out.println("[debug] validationDir: " + validationDirsStr);
        System.out.println("[debug] repositoryMgrDirsStr: " + repositoryMgrDirsStr);
        
        metaFactory = System
                .getProperty("filemgr.catalog.factory",
                        "gov.nasa.jpl.oodt.cas.filemgr.catalog.DataSourceCatalogFactory");
        dataFactory = System
                .getProperty("filemgr.repository.factory",
                        "gov.nasa.jpl.oodt.cas.filemgr.repository.DataSourceRepositoryManagerFactory");
        transferFactory = System
                .getProperty("filemgr.datatransfer.factory",
                        "gov.nasa.jpl.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory");

        catalog = GenericFileManagerObjectFactory
                .getCatalogServiceFromFactory(metaFactory);
        repositoryManager = GenericFileManagerObjectFactory
                .getRepositoryManagerServiceFromFactory(dataFactory);
        dataTransfer = GenericFileManagerObjectFactory
                .getDataTransferServiceFromFactory(transferFactory);

        transferStatusTracker = new TransferStatusTracker(catalog);   
        
        // got to start the server before setting up the transfer client since
        // it checks for a live server
       
        dataTransfer.setFileManagerUrl(new URL("http://localhost:" + webServerPort));

        System.out.println("[debug] EDRNFileManager.bounce() completes...");
        
        return true;
    }

    public boolean isAlive() {
        return true;
    }

    public boolean transferringProduct(Hashtable productHash) {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        transferStatusTracker.transferringProduct(p);
        return true;
    }

    public Hashtable getCurrentFileTransfer() {
        FileTransferStatus status = transferStatusTracker
                .getCurrentFileTransfer();
        if (status == null) {
            return new Hashtable();
        } else
            return XmlRpcStructFactory.getXmlRpcFileTransferStatus(status);
    }

    public Vector getCurrentFileTransfers() {
        List currentTransfers = transferStatusTracker.getCurrentFileTransfers();

        if (currentTransfers != null && currentTransfers.size() > 0) {
            return XmlRpcStructFactory
                    .getXmlRpcFileTransferStatuses(currentTransfers);
        } else
            return new Vector();
    }

    public double getProductPctTransferred(Hashtable productHash) {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        double pct = transferStatusTracker.getPctTransferred(product);
        return pct;
    }

    public double getRefPctTransferred(Hashtable refHash) {
        Reference reference = XmlRpcStructFactory
                .getReferenceFromXmlRpc(refHash);
        double pct = 0.0;

        try {
            pct = transferStatusTracker.getPctTransferred(reference);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception getting transfer percentage for ref: ["
                            + reference.getOrigReference() + "]: Message: "
                            + e.getMessage());
        }
        return pct;
    }

    public boolean removeProductTransferStatus(Hashtable productHash) {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        transferStatusTracker.removeProductTransferStatus(product);
        return true;
    }

    public boolean isTransferComplete(Hashtable productHash) {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return transferStatusTracker.isTransferComplete(product);
    }

    public Hashtable pagedQuery(Hashtable queryHash, Hashtable productTypeHash,
            int pageNum) throws CatalogException {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        Query query = XmlRpcStructFactory.getQueryFromXmlRpc(queryHash);

        ProductPage prodPage = null;

        try {
            prodPage = catalog.pagedQuery(query, type, pageNum);

            if (prodPage == null) {
                prodPage = ProductPage.blankPage();
            } else {
                // it is possible here that the underlying catalog did not
                // set the ProductType
                // to obey the contract of the File Manager, we need to make
                // sure its set here
                setProductType(prodPage.getPageProducts());
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Catalog exception performing paged query for product type: ["
                            + type.getProductTypeId() + "] query: [" + query
                            + "]: Message: " + e.getMessage());
            throw new CatalogException(e.getMessage());
        }

        return XmlRpcStructFactory.getXmlRpcProductPage(prodPage);
    }

    public Hashtable getFirstPage(Hashtable productTypeHash) {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        ProductPage page = catalog.getFirstPage(type);
        try {
            setProductType(page.getPageProducts());
        } catch (Exception e) {
            LOG.log(Level.WARNING,
                    "Unable to set product types for product page list: ["
                            + page + "]");
        }
        return XmlRpcStructFactory.getXmlRpcProductPage(page);
    }

    public Hashtable getLastPage(Hashtable productTypeHash) {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        ProductPage page = catalog.getLastProductPage(type);
        try {
            setProductType(page.getPageProducts());
        } catch (Exception e) {
            LOG.log(Level.WARNING,
                    "Unable to set product types for product page list: ["
                            + page + "]");
        }
        return XmlRpcStructFactory.getXmlRpcProductPage(page);
    }

    public Hashtable getNextPage(Hashtable productTypeHash,
            Hashtable currentPageHash) {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        ProductPage currPage = XmlRpcStructFactory
                .getProductPageFromXmlRpc(currentPageHash);
        ProductPage page = catalog.getNextPage(type, currPage);
        try {
            setProductType(page.getPageProducts());
        } catch (Exception e) {
            LOG.log(Level.WARNING,
                    "Unable to set product types for product page list: ["
                            + page + "]");
        }
        return XmlRpcStructFactory.getXmlRpcProductPage(page);
    }

    public Hashtable getPrevPage(Hashtable productTypeHash,
            Hashtable currentPageHash) {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        ProductPage currPage = XmlRpcStructFactory
                .getProductPageFromXmlRpc(currentPageHash);
        ProductPage page = catalog.getPrevPage(type, currPage);
        try {
            setProductType(page.getPageProducts());
        } catch (Exception e) {
            LOG.log(Level.WARNING,
                    "Unable to set product types for product page list: ["
                            + page + "]");
        }
        return XmlRpcStructFactory.getXmlRpcProductPage(page);
    }

    public String addProductType(Hashtable productTypeHash)
            throws RepositoryManagerException {
        ProductType productType = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        repositoryManager.addProductType(productType);
        return productType.getProductTypeId();

    }

    public synchronized boolean setProductTransferStatus(Hashtable productHash)
            throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        catalog.setProductTransferStatus(product);
        return true;
    }

    public int getNumProducts(Hashtable productTypeHash)
            throws CatalogException {
        int numProducts = -1;

        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);

        try {
            numProducts = catalog.getNumProducts(type);
        } catch (CatalogException e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception when getting num products: Message: "
                            + e.getMessage());
            throw new CatalogException(e.getMessage());
        }

        return numProducts;
    }

    public Vector getTopNProducts(int n) throws CatalogException {
        List topNProducts = null;

        try {
            topNProducts = catalog.getTopNProducts(n);
            return XmlRpcStructFactory.getXmlRpcProductList(topNProducts);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception when getting topN products: Message: "
                            + e.getMessage());
            throw new CatalogException(e.getMessage());
        }
    }

    public Vector getTopNProducts(int n, Hashtable productTypeHash)
            throws CatalogException {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        List topNProducts = null;

        try {
            topNProducts = catalog.getTopNProducts(n, type);
            return XmlRpcStructFactory.getXmlRpcProductList(topNProducts);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception when getting topN products by product type: ["
                            + type.getProductTypeId() + "]: Message: "
                            + e.getMessage());
            throw new CatalogException(e.getMessage());
        }

    }

    public boolean hasProduct(String productName) throws CatalogException {
        Product p = catalog.getProductByName(productName);
        return p != null
                && p.getTransferStatus().equals(Product.STATUS_RECEIVED);
    }

    public Hashtable getMetadata(Hashtable productHash) throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);

        try {
            Metadata m = catalog.getMetadata(product);
            return m.getHashtable();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Exception obtaining metadata from catalog for product: ["
                            + product.getProductName() + "]: Message: "
                            + e.getMessage());
            throw new CatalogException(e.getMessage());
        }
    }

    public Hashtable getReducedMetadata(Hashtable productHash, Vector elements)
            throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);

        try {
            Metadata m = catalog.getReducedMetadata(product, elements);
            return m.getHashtable();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Exception obtaining metadata from catalog for product: ["
                            + product.getProductName() + "]: Message: "
                            + e.getMessage());
            throw new CatalogException(e.getMessage());
        }
    }

    public Vector getProductTypes() throws RepositoryManagerException {
        List productTypeList = null;

        try {
            productTypeList = repositoryManager.getProductTypes();
            return XmlRpcStructFactory
                    .getXmlRpcProductTypeList(productTypeList);
        } catch (RepositoryManagerException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Unable to obtain product types from repository manager: Message: "
                            + e.getMessage());
            throw new RepositoryManagerException(e.getMessage());
        }
    }

    public Vector getProductReferences(Hashtable productHash)
            throws CatalogException {
        List referenceList = null;
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);

        try {
            referenceList = catalog.getProductReferences(product);
            return XmlRpcStructFactory.getXmlRpcReferences(referenceList);
        } catch (CatalogException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Unable to obtain references for product: ["
                    + product.getProductName() + "]: Message: "
                    + e.getMessage());
            throw new CatalogException(e.getMessage());
        }

    }

    public Hashtable getProductById(String productId) throws CatalogException {
        Product product = null;

        try {
            product = catalog.getProductById(productId);
            // it is possible here that the underlying catalog did not
            // set the ProductType
            // to obey the contract of the File Manager, we need to make
            // sure its set here
            product.setProductType(this.repositoryManager
                    .getProductTypeById(product.getProductType()
                            .getProductTypeId()));
            return XmlRpcStructFactory.getXmlRpcProduct(product);
        } catch (CatalogException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Unable to obtain product by id: ["
                    + productId + "]: Message: " + e.getMessage());
            throw new CatalogException(e.getMessage());
        } catch (RepositoryManagerException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Unable to obtain product type by id: ["
                    + product.getProductType().getProductTypeId()
                    + "]: Message: " + e.getMessage());
            throw new CatalogException(e.getMessage());
        }

    }

    public Hashtable getProductByName(String productName)
            throws CatalogException {
        Product product = null;

        try {
            product = catalog.getProductByName(productName);
            // it is possible here that the underlying catalog did not
            // set the ProductType
            // to obey the contract of the File Manager, we need to make
            // sure its set here
            product.setProductType(this.repositoryManager
                    .getProductTypeById(product.getProductType()
                            .getProductTypeId()));
            return XmlRpcStructFactory.getXmlRpcProduct(product);
        } catch (CatalogException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Unable to obtain product by name: ["
                    + productName + "]: Message: " + e.getMessage());
            throw new CatalogException(e.getMessage());
        } catch (RepositoryManagerException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Unable to obtain product type by id: ["
                    + product.getProductType().getProductTypeId()
                    + "]: Message: " + e.getMessage());
            throw new CatalogException(e.getMessage());
        }
    }

    public Vector getProductsByProductType(Hashtable productTypeHash)
            throws CatalogException {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        List productList = null;

        try {
            productList = catalog.getProductsByProductType(type);
            return XmlRpcStructFactory.getXmlRpcProductList(productList);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Exception obtaining products by product type for type: ["
                            + type.getName() + "]: Message: " + e.getMessage());
            throw new CatalogException(e.getMessage());
        }
    }

    public Vector getElementsByProductType(Hashtable productTypeHash)
            throws ValidationLayerException {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        List elementList = null;

        try {
            elementList = catalog.getValidationLayer().getElements(type);
            return XmlRpcStructFactory.getXmlRpcElementList(elementList);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Exception obtaining elements for product type: ["
                            + type.getName() + "]: Message: " + e.getMessage());
            throw new ValidationLayerException(e.getMessage());
        }

    }

    public Hashtable getElementById(String elementId)
            throws ValidationLayerException {
        Element element = null;

        try {
            element = catalog.getValidationLayer().getElementById(elementId);
            return XmlRpcStructFactory.getXmlRpcElement(element);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "exception retrieving element by id: ["
                    + elementId + "]: Message: " + e.getMessage());
            throw new ValidationLayerException(e.getMessage());
        }
    }

    public Hashtable getElementByName(String elementName)
            throws ValidationLayerException {
        Element element = null;

        try {
            element = catalog.getValidationLayer()
                    .getElementByName(elementName);
            return XmlRpcStructFactory.getXmlRpcElement(element);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "exception retrieving element by name: ["
                    + elementName + "]: Message: " + e.getMessage());
            throw new ValidationLayerException(e.getMessage());
        }
    }

    public Vector query(Hashtable queryHash, Hashtable productTypeHash)
            throws CatalogException {
        Query query = XmlRpcStructFactory.getQueryFromXmlRpc(queryHash);
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        List productIdList = null;
        List productList = null;

        try {
            productIdList = catalog.query(query, type);

            if (productIdList != null && productIdList.size() > 0) {
                productList = new Vector(productIdList.size());
                for (Iterator i = productIdList.iterator(); i.hasNext();) {
                    String productId = (String) i.next();
                    Product product = catalog.getProductById(productId);
                    // it is possible here that the underlying catalog did not
                    // set the ProductType
                    // to obey the contract of the File Manager, we need to make
                    // sure its set here
                    product.setProductType(this.repositoryManager
                            .getProductTypeById(product.getProductType()
                                    .getProductTypeId()));
                    productList.add(product);
                }
                return XmlRpcStructFactory.getXmlRpcProductList(productList);
            } else {
                return new Vector(); // null values not supported by XML-RPC
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Exception performing query against catalog for product type: ["
                            + type.getName() + "] Message: " + e.getMessage());
            throw new CatalogException(e.getMessage());
        }

    }

    public Hashtable getProductTypeByName(String productTypeName)
            throws RepositoryManagerException {
        ProductType type = repositoryManager
                .getProductTypeByName(productTypeName);
        return XmlRpcStructFactory.getXmlRpcProductType(type);
    }

    public Hashtable getProductTypeById(String productTypeId)
            throws RepositoryManagerException {
        ProductType type = null;

        try {
            type = repositoryManager.getProductTypeById(productTypeId);
            return XmlRpcStructFactory.getXmlRpcProductType(type);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Exception obtaining product type by id for product type: ["
                            + productTypeId + "]: Message: " + e.getMessage());
            throw new RepositoryManagerException(e.getMessage());
        }
    }

    public synchronized String catalogProduct(Hashtable productHash)
            throws CatalogException {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return catalogProduct(p);
    }

    public synchronized boolean addMetadata(Hashtable productHash,
            Hashtable metadata) throws CatalogException {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        Metadata m = new Metadata();
        m.addMetadata(metadata);
        return addMetadata(p, m);
    }

    public synchronized boolean addProductReferences(Hashtable productHash)
            throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return addProductReferences(product);
    }

    public String ingestProduct(Hashtable productHash, Hashtable metadata,
            boolean clientTransfer) throws VersioningException,
            RepositoryManagerException, DataTransferException, CatalogException {

        // first, create the product
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        p.setTransferStatus(Product.STATUS_TRANSFER);
        catalogProduct(p);

        // now add the metadata
        Metadata m = new Metadata();
        m.addMetadata(metadata);
        addMetadata(p, m);

        if (!clientTransfer) {
            LOG.log(Level.FINEST,
                    "File Manager: ingest: no client transfer enabled, "
                            + "server transfering product: ["
                            + p.getProductName() + "]");
            // version the product
            Versioner versioner = null;
            try {
                versioner = GenericFileManagerObjectFactory
                        .getVersionerFromClassName(p.getProductType()
                                .getVersioner());
                versioner.createDataStoreReferences(p, m);
            } catch (Exception e) {
                LOG.log(Level.SEVERE,
                        "ingestProduct: VersioningException when versioning Product: "
                                + p.getProductName() + " with Versioner "
                                + p.getProductType().getVersioner()
                                + ": Message: " + e.getMessage());
                throw new VersioningException(e);
            }

            // add the newly versioned references to the data store
            addProductReferences(p);

            // now transfer the product
            try {
                dataTransfer.transferProduct(p);
                // now update the product's transfer status in the data store
                p.setTransferStatus(Product.STATUS_RECEIVED);

                try {
                    catalog.setProductTransferStatus(p);
                } catch (CatalogException e) {
                    LOG
                            .log(
                                    Level.SEVERE,
                                    "ingestProduct: CatalogException "
                                            + "when updating product transfer status for Product: "
                                            + p.getProductName() + " Message: "
                                            + e.getMessage());
                    throw e;
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE,
                        "ingestProduct: DataTransferException when transfering Product: "
                                + p.getProductName() + ": Message: "
                                + e.getMessage());
                throw new DataTransferException(e);
            }
        }
        // that's it!
        return p.getProductId();
    }

    public boolean transferFile(String filePath, byte[] fileData, int offset,
            int numBytes) {
        File outFile = new File(filePath);
        boolean success = true;

        FileOutputStream fOut = null;

        if (outFile.exists()) {
            try {
                fOut = new FileOutputStream(outFile, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                LOG.log(Level.SEVERE,
                        "FileNotFoundException when trying to use RandomAccess file on "
                                + filePath + ": Message: " + e.getMessage());
                success = false;
            }
        } else {
            // create the output directory
            String outFileDirPath = outFile.getAbsolutePath().substring(0,
                    outFile.getAbsolutePath().lastIndexOf("/"));
            LOG.log(Level.INFO, "Outfile directory: " + outFileDirPath);
            File outFileDir = new File(outFileDirPath);
            outFileDir.mkdirs();

            try {
                fOut = new FileOutputStream(outFile, false);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                LOG.log(Level.SEVERE,
                        "FileNotFoundException when trying to use RandomAccess file on "
                                + filePath + ": Message: " + e.getMessage());
                success = false;
            }
        }

        if (success) {
            try {
                fOut.write(fileData, (int) offset, (int) numBytes);
            } catch (IOException e) {
                e.printStackTrace();
                LOG.log(Level.SEVERE, "IOException when trying to write file "
                        + filePath + ": Message: " + e.getMessage());
                success = false;
            } finally {
                if (fOut != null) {
                    try {
                        fOut.close();
                    } catch (Exception ignore) {
                    }

                    fOut = null;
                }
            }
        }

        outFile = null;
        return success;
    }

    public boolean moveProduct(Hashtable productHash, String newPath)
            throws DataTransferException {

        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);

        // first thing we care about is if the product is flat or heirarchical
        if (p.getProductStructure().equals(Product.STRUCTURE_FLAT)) {
            // we just need to get its first reference
            if (p.getProductReferences() == null
                    || (p.getProductReferences() != null && p
                            .getProductReferences().size() != 1)) {
                throw new DataTransferException(
                        "Flat products must have a single reference: cannot move");
            }

            // okay, it's fine to move it
            // first, we need to update the data store ref
            Reference r = (Reference) p.getProductReferences().get(0);
            if (r.getDataStoreReference().equals(
                    new File(newPath).toURI().toString())) {
                throw new DataTransferException("cannot move product: ["
                        + p.getProductName() + "] to same location: ["
                        + r.getDataStoreReference() + "]");
            }

            // create a copy of the current data store path: we'll need it to
            // do the data transfer
            Reference copyRef = new Reference(r);

            // update the copyRef to have the data store ref as the orig ref
            // the the newLoc as the new ref
            copyRef.setOrigReference(r.getDataStoreReference());
            copyRef.setDataStoreReference(new File(newPath).toURI().toString());

            p.getProductReferences().clear();
            p.getProductReferences().add(copyRef);

            // now transfer it
            try {
                this.dataTransfer.transferProduct(p);
            } catch (IOException e) {
                throw new DataTransferException(e.getMessage());
            }

            // now delete the original copy
            try {
                if (!new File(new URI(copyRef.getOrigReference())).delete()) {
                    LOG.log(Level.WARNING, "Deletion of original file: ["
                            + r.getDataStoreReference()
                            + "] on product move returned false");
                }
            } catch (URISyntaxException e) {
                throw new DataTransferException(
                        "URI Syntax exception trying to remove original product ref: Message: "
                                + e.getMessage());
            }

            // now save the updated reference
            try {
                this.catalog.modifyProduct(p);
                return true;
            } catch (CatalogException e) {
                throw new DataTransferException(e.getMessage());
            }
        } else
            throw new UnsupportedOperationException(
                    "Moving of heirarhical products not supported yet");
    }

    public boolean removeFile(String filePath) {
        return new File(filePath).delete();
    }

    public boolean modifyProduct(Hashtable productHash) throws CatalogException {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);

        try {
            catalog.modifyProduct(p);
        } catch (CatalogException e) {
            LOG.log(Level.WARNING, "Exception modifying product: ["
                    + p.getProductId() + "]: Message: " + e.getMessage(), e);
            throw e;
        }

        return true;
    }

    public boolean removeProduct(Hashtable productHash) throws CatalogException {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);

        try {
            catalog.removeProduct(p);
        } catch (CatalogException e) {
            LOG.log(Level.WARNING, "Exception modifying product: ["
                    + p.getProductId() + "]: Message: " + e.getMessage(), e);
            throw e;
        }

        return true;
    }


    public boolean shutdown() {
        if (this.webServer != null) {
            this.webServer.shutdown();
            this.webServer = null;
            return true;
        } else
            return false;
    }

    private synchronized String catalogProduct(Product p)
            throws CatalogException {
        try {
            catalog.addProduct(p);
        } catch (CatalogException e) {
            LOG.log(Level.SEVERE,
                    "ingestProduct: CatalogException when adding Product: "
                            + p.getProductName() + " to Catalog: Message: "
                            + e.getMessage());
            throw e;
        }

        return p.getProductId();
    }

    private synchronized boolean addMetadata(Product p, Metadata m)
            throws CatalogException {

        // first do server side metadata extraction
        Metadata metadata = null;

        try {
            metadata = runExtractors(p, m);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            catalog.addMetadata(metadata, p);
        } catch (CatalogException e) {
            LOG.log(Level.SEVERE,
                    "ingestProduct: CatalogException when adding metadata "
                            + metadata + " for product: " + p.getProductName()
                            + ": Message: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "ingestProduct: General Exception when adding metadata "
                            + metadata + " for product: " + p.getProductName()
                            + ": Message: " + e.getMessage());
            throw new CatalogException(e.getMessage());
        }

        return true;
    }

    private Metadata runExtractors(Product product, Metadata metadata) {
        // make sure that the product type definition is present
        try {
            product.setProductType(repositoryManager.getProductTypeById(product
                    .getProductType().getProductTypeId()));
        } catch (RepositoryManagerException e) {
            e.printStackTrace();
            return null;
        }

        Metadata met = new Metadata();
        met.addMetadata(metadata.getHashtable());

        if (product.getProductType().getExtractors() != null
                && product.getProductType().getExtractors().size() > 0) {
            for (Iterator i = product.getProductType().getExtractors()
                    .iterator(); i.hasNext();) {
                ExtractorSpec spec = (ExtractorSpec) i.next();
                FilemgrMetExtractor extractor = GenericFileManagerObjectFactory
                        .getExtractorFromClassName(spec.getClassName());
                extractor.configure(spec.getConfiguration());
                LOG.log(Level.INFO, "Running Met Extractor: ["
                        + extractor.getClass().getName()
                        + "] for product type: ["
                        + product.getProductType().getName() + "]");
                try {
                    met = extractor.extractMetadata(product, met);
                } catch (MetExtractionException e) {
                    e.printStackTrace();
                    LOG.log(Level.WARNING,
                            "Exception extractor metadata from product: ["
                                    + product.getProductName()
                                    + "]: using extractor: ["
                                    + extractor.getClass().getName()
                                    + "]: Message: " + e.getMessage());
                }
            }
        }

        return met;
    }

    private synchronized boolean addProductReferences(Product product)
            throws CatalogException {
        catalog.addProductReferences(product);
        return true;
    }

    private void setProductType(List products) throws Exception {
        if (products != null && products.size() > 0) {
            for (Iterator i = products.iterator(); i.hasNext();) {
                Product p = (Product) i.next();
                try {
                    p.setProductType(repositoryManager.getProductTypeById(p
                            .getProductType().getProductTypeId()));
                } catch (RepositoryManagerException e) {
                    throw new Exception(e.getMessage());
                }
            }
        }
    }

}

