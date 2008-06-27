//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.ecas.metextraction;

import java.text.SimpleDateFormat;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public interface SELDIMetadata {

  public static final int PRODUCT_TYPE_COL = 0;
  
  public static final int PRODUCT_RECV_DATE_COL = 1;

  public static final int PRODUCT_DESC_COL = 2;
  
  public static final int PRODUCT_DATASET_NAME_COl = 3;

  public static final int PRODUCT_FILELOCATION_COL = 4;

  public static final int PRODUCT_FILENAME_COL = 5;

  public static final int PRODUCT_ID_COL = 6;

  public static final int PRODUCT_DATASET_ID_COL = 7;

  public static final int PRODUCT_PROTOCOL_ID_COL = 8;

  public static final int PRODUCT_SITE_ID_COL = 9;

  public static final int PRODUCT_ORGAN_ID_COL = 10;

  public static final int PRODUCT_SPECIMEN_ID_COL = 11;

  /* column names */

  public static final String PRODUCT_DESCRIPTION = "ProductDescription";

  public static final String DATASET_ID = "DatasetId";

  public static final String PROTOCOL_ID = "ProtocolId";

  public static final String SITE_ID = "SiteId";

  public static final String ORGAN_ID = "OrganId";

  public static final String SPECIMEN_ID = "SpecimenId";
  

  /* the date format of the input metadata */
  public static final String inputDataDateFormat = "MM/dd/yy";

  /* our date format parser */
  public static final SimpleDateFormat inputDataDateFormatter = new SimpleDateFormat(
          inputDataDateFormat);

  /* the delimeter for the input metadata */
  public static final String FIELD_DELIMETER = ",";

}
