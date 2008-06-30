//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.ecas.metextraction;

//JDK imports
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import static gov.nasa.jpl.oodt.cas.filemgr.metadata.CoreMetKeys.*;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.metadata.exceptions.MetExtractionException;
import gov.nasa.jpl.oodt.cas.metadata.extractors.CmdLineMetExtractor;

//EDRN imports
import static gov.nasa.jpl.edrn.ecas.metadata.EDRNMetadata.*;
import static gov.nasa.jpl.edrn.ecas.metadata.SELDIMetadata.*;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * An extractor for the FHCRC delivered SELDI data products for e-CAS
 * </p>.
 */
public class SELDIMetExtractor extends CmdLineMetExtractor {

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(SELDIMetExtractor.class
            .getName());

    public SELDIMetExtractor() {
        super(new SELDIMetExtractorConfigReader());
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.metadata.AbstractMetExtractor#extrMetadata(java.io.File)
     */
    @Override
    protected Metadata extrMetadata(File file) throws MetExtractionException {
        if (this.config == null) {
            throw new MetExtractionException(
                    "Config for SELDI extractor cannot be null!");
        }

        // get the file's metadata
        Metadata fileMet = (Metadata) ((SELDIMetExtractorConfig) this.config)
                .getMetadataForFile(file.getName().toUpperCase());

        if (fileMet == null) {
            LOG.log(Level.WARNING, "Unable to find metadata for file: [" + file
                    + "] within provided csv config file: [" + this.config
                    + "]");
        } else {
            // set the dyn fields
            overrideMetadata(file, fileMet);
        }

        return fileMet;
    }

    public static void main(String[] args) throws Exception {
        processMain(args, new SELDIMetExtractor());
    }

    private void overrideMetadata(File file, Metadata met) {
        met.replaceMetadata(FILENAME, file.getName());
        met.replaceMetadata(FILE_LOCATION, file.getParentFile()
                .getAbsolutePath());
        if (met.getMetadata(PRODUCT_TYPE) == null) {
            met.replaceMetadata(PRODUCT_TYPE, unidentifiedFile);
        }

        if (met.getMetadata(PRODUCT_TYPE).startsWith("UAB")) {
            met.addMetadata(SITE_SHORT_NAME, UAB_SHORT_NAME);
        } else if (met.getMetadata(PRODUCT_TYPE).startsWith("EVMS")) {
            met.addMetadata(SITE_SHORT_NAME, EVMS_SHORT_NAME);
        }

        met.addMetadata(INSTRUMENT_ID, SELDI_II_INSTRUMENT_ID);
        met.addMetadata(ORGAN_NAME, ORGAN_NAME_PROSTATE);
    }
}
