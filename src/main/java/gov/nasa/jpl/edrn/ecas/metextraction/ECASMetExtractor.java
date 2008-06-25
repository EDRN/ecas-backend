//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.ecas.demo.extraction;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.MetExtractor;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.metadata.exceptions.MetExtractionException;
import gov.nasa.jpl.oodt.cas.metadata.util.XMLUtils;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here.
 * </p>
 * 
 */
public class ECASMetExtractor implements MetExtractor, MetFields {

	/* optional specified config file path */
	private String configFilePath = null;

	/**
	 * 
	 */
	public ECASMetExtractor() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nasa.jpl.oodt.cas.metadata.MetExtractor#extractMetadata(java.io.File)
	 */
	public Metadata extractMetadata(File f) throws MetExtractionException {
		return extractMetadata(f, this.configFilePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nasa.jpl.oodt.cas.metadata.MetExtractor#extractMetadata(java.lang.String)
	 */
	public Metadata extractMetadata(String filePath)
			throws MetExtractionException {
		return extractMetadata(new File(filePath));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nasa.jpl.oodt.cas.metadata.MetExtractor#extractMetadata(java.net.URL)
	 */
	public Metadata extractMetadata(URL fileUrl) throws MetExtractionException {
		try {
			return extractMetadata(new File(new URI(fileUrl.toExternalForm())));
		} catch (Exception e) {
			throw new MetExtractionException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nasa.jpl.oodt.cas.metadata.MetExtractor#setConfigFile(java.io.File)
	 */
	public void setConfigFile(File configFile) {
		setConfigFile(configFile.getAbsolutePath());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nasa.jpl.oodt.cas.metadata.MetExtractor#setConfigFile(java.lang.String)
	 */
	public void setConfigFile(String configFilePath) {
		this.configFilePath = configFilePath;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nasa.jpl.oodt.cas.metadata.MetExtractor#extractMetadata(java.io.File,
	 *      java.io.File)
	 */
	public Metadata extractMetadata(File f, File configFile)
			throws MetExtractionException {
		return extractMetadata(f, configFile.getAbsolutePath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nasa.jpl.oodt.cas.metadata.MetExtractor#extractMetadata(java.io.File,
	 *      java.lang.String)
	 */
	public Metadata extractMetadata(File f, String configFilePath)
			throws MetExtractionException {
		// check to see if config file path exists, else throw exception
		if (configFilePath == null) {
			throw new MetExtractionException(
					"No config file path specified for met extraction!");
		}

		Metadata defaultMetadata = null;
		try {
			defaultMetadata = new Metadata(new FileInputStream(new File(
					configFilePath)));
		} catch (Exception e) {
			throw new MetExtractionException(e);
		}

		// need to set FILENAME, FILE_LOCATION
		defaultMetadata.replaceMetadata(FILENAME, f.getName());
		defaultMetadata.replaceMetadata(FILE_LOCATION, f.getParentFile()
				.getAbsolutePath());

		return defaultMetadata;
	}

	public static void main(String[] args) throws Exception {

		String usage = "ECASMetExtractor <file> <configFile>\n";
		String filePath = null, configFilePath = null;

		if (args.length != 2) {
			System.err.println(usage);
			System.exit(1);
		}

		filePath = args[0];
		configFilePath = args[1];

		ECASMetExtractor extractor = new ECASMetExtractor();
		Metadata metadata = extractor.extractMetadata(new File(filePath),
				new File(configFilePath));

		String outMetFilePath = filePath + ".met";
		XMLUtils.writeXmlFile(metadata.toXML(), outMetFilePath);
	}

}
