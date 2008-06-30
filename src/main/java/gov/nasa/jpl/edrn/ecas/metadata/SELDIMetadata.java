//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.ecas.metadata;

//JDK imports
import java.text.SimpleDateFormat;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Metadata while reading the SELDI xls file dontated
 * by FHCRC.
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
  

  /* the date format of the input metadata */
  public static final String inputDataDateFormat = "MM/dd/yy";

  /* our date format parser */
  public static final SimpleDateFormat inputDataDateFormatter = new SimpleDateFormat(
          inputDataDateFormat);

  /* the delimeter for the input metadata */
  public static final String FIELD_DELIMETER = ",";
  
  public static final String unidentifiedFile = "eCASFile";

}
