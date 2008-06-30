//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.ecas.metadata;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Met keys for all EDRN software
 * </p>.
 */
public interface EDRNMetadata {

    public static final String PRODUCT_RECEIVED_DATE = "ProductReceivedDate";

    public static final String PRODUCT_DESCRIPTION = "ProductDescription";

    public static final String DATASET_ID = "DatasetId";

    public static final String PROTOCOL_ID = "ProtocolId";

    public static final String SITE_ID = "SiteId";

    public static final String ORGAN_ID = "OrganId";
    
    public static final String ORGAN_NAME = "OrganName";

    public static final String SPECIMEN_ID = "SpecimenId";

    public static final String SITE_SHORT_NAME = "SiteShortName";

    public static final String INSTRUMENT_ID =  "InstrumentId";

    public static final String UNKNOWN = "unknown";
    
    /* short name keys */
    public static final String EVMS_SHORT_NAME = "evms";
    
    public static final String UAB_SHORT_NAME = "ualb";
    
    /* instrument id keys */
    
    public static final String SELDI_II_INSTRUMENT_ID = "seldi2";
    
    /* organ name keys */
    
    public static final String ORGAN_NAME_PROSTATE = "Prostate";

}
