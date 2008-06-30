//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.ecas.versioning;

//JDK imports
import java.util.Date;

//OODT imports
import gov.nasa.jpl.oodt.cas.filemgr.structs.Product;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.VersioningException;
import gov.nasa.jpl.oodt.cas.filemgr.versioning.MetadataBasedFileVersioner;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

//EDRN imports
import static gov.nasa.jpl.edrn.ecas.metadata.EDRNMetadata.*;
import gov.nasa.jpl.edrn.ecas.util.EDRNDateUtils;
import gov.nasa.jpl.edrn.ecas.util.InvalidDateException;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class EDRNProductVersioner extends MetadataBasedFileVersioner {

    private String filePathSpec = "/[SiteShortName]/[ProductReceivedDate]/[OrganName]/[InstrumentId]/[Filename]";

    public EDRNProductVersioner() {
        setFlatProducts(true);
        setFilePathSpec(filePathSpec);
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.versioning.MetadataBasedFileVersioner#createDataStoreReferences(gov.nasa.jpl.oodt.cas.filemgr.structs.Product,
     *      gov.nasa.jpl.oodt.cas.metadata.Metadata)
     */
    @Override
    public void createDataStoreReferences(Product product, Metadata metadata)
            throws VersioningException {
        // compute ProductReceivedDate
        String prodReceivedDate = null;
        try {
            prodReceivedDate = EDRNDateUtils
                    .getProdDateFromProductReceivedTimeString(EDRNDateUtils
                            .getIsoDate(new Date()));
            metadata.addMetadata(PRODUCT_RECEIVED_DATE, prodReceivedDate);
        } catch (InvalidDateException e) {
            throw new VersioningException(
                    "Invalid production date time!: Message: " + e.getMessage());
        }

        if (!metadata.containsKey(ORGAN_NAME)) {
            metadata.addMetadata(ORGAN_NAME, UNKNOWN);
        }

        super.createDataStoreReferences(product, metadata);

    }

}
