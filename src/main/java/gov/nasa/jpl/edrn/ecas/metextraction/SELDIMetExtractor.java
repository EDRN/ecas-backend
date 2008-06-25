//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.ecas.seldi.met;

//JDK imports
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import jpl.eda.util.DateConvert;
import gov.nasa.jpl.oodt.cas.filemgr.metadata.CoreMetKeys;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.metadata.exceptions.MetExtractionException;
import gov.nasa.jpl.oodt.cas.metadata.extractors.CmdLineMetExtractor;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class SELDIMetExtractor extends CmdLineMetExtractor implements
    SELDIMetadata, CoreMetKeys {

  /* map of String fileName=>its Metadata */
  private static Map metDb = null;

  /* our log stream */
  private static final Logger LOG = Logger.getLogger(SELDIMetExtractor.class
      .getName());

  /* the date format of the input metadata */
  private static final String inputDataDateFormat = "MM/dd/yy";

  /* our date format parser */
  private static final SimpleDateFormat inputDataDateFormatter = new SimpleDateFormat(
      inputDataDateFormat);

  /* the delimeter for the input metadata */
  private static final String FIELD_DELIMETER = ",";

  private static final String unidentifiedFile = "eCASFile";

  public SELDIMetExtractor() {

  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.oodt.cas.metadata.AbstractMetExtractor#extractMetadata(java.net.URL)
   */
  public Metadata extractMetadata(URL url) throws MetExtractionException {
    try {
      return this.extractMetadata(new File(new URI(url.toString())));
    } catch (URISyntaxException e) {
      e.printStackTrace();
      throw new MetExtractionException(e.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.oodt.cas.metadata.AbstractMetExtractor#extractMetadata(java.io.File,
   *      java.io.File)
   */
  public Metadata extractMetadata(File file, File configFile)
      throws MetExtractionException {
    // first set the config file
    setConfigFile(configFile);

    // now load the SELDI db (if it doesn't exist)
    try {
      checkAndLoadDb();
    } catch (Exception e) {
      e.printStackTrace();
      throw new MetExtractionException(e.getMessage());
    }

    // get the file's metadata
    Metadata fileMet = (Metadata) this.metDb.get(file.getName().toUpperCase());

    if (fileMet == null) {
      LOG.log(Level.WARNING, "Unable to find metadata for file: [" + file
          + "] within provided csv config file: [" + configFile + "]");
    } else {
      // set the dyn fields
      overrideMetadata(file, fileMet);
    }

    return fileMet;
  }

  public static void main(String[] args) throws Exception {
    processMain(args, new SELDIMetExtractor());
  }

  private void checkAndLoadDb() throws Exception {
    if (this.metDb == null) {
      // need to load it then
      if (this.confFile == null
          || (this.confFile != null && !this.confFile.exists())) {
        LOG
            .log(Level.WARNING,
                "Unable to initialize SELDI met extraction db: file does not exist or is null");
        return;
      }

      this.metDb = new HashMap();

      // okay to load the db, just load the .csv file
      // for each line in the file, process it as the metadata for one file
      // split on "|" to separate out the fields
      // split on "," to separate out multiple values

      // build up a hashtable of file name => Metadata

      BufferedReader br = null;
      String line = null;

      try {
        br = new BufferedReader(new FileReader(this.confFile));

        while ((line = br.readLine()) != null) {
          String[] fileToks = line.split(FIELD_DELIMETER);
          Metadata fileMet = new Metadata();
          fileMet.addMetadata(PRODUCT_TYPE, fileToks[PRODUCT_TYPE_COL]);
          Date recvDate = inputDataDateFormatter
              .parse(fileToks[PRODUCT_RECV_DATE_COL]);
          String productRecvTime = DateConvert.isoFormat(recvDate);
          fileMet.addMetadata(PRODUCT_RECEVIED_TIME, productRecvTime);
          fileMet.addMetadata(PRODUCT_DESCRIPTION, fileToks[PRODUCT_DESC_COL]);
          fileMet
              .addMetadata(FILE_LOCATION, fileToks[PRODUCT_FILELOCATION_COL]);
          fileMet.addMetadata(FILENAME, fileToks[PRODUCT_FILENAME_COL]);
          fileMet.addMetadata(PRODUCT_ID, fileToks[PRODUCT_ID_COL]);
          fileMet.addMetadata(DATASET_ID, fileToks[PRODUCT_DATASET_ID_COL]);
          fileMet.addMetadata(PROTOCOL_ID, fileToks[PRODUCT_PROTOCOL_ID_COL]);

          int nextIdx = PRODUCT_SITE_ID_COL;
          if (fileToks[PRODUCT_SITE_ID_COL].startsWith("\"")) {
            // it's a multiple valued column
            // basically just want to keep reading values until
            // you encounter one that ends with "
            // grab the first one, then increment
            fileMet.addMetadata(SITE_ID, fileToks[PRODUCT_SITE_ID_COL]
                .substring(1));
            nextIdx++;
            while (!fileToks[nextIdx].endsWith("\"")) {
              fileMet.addMetadata(SITE_ID, fileToks[nextIdx]);
              nextIdx++;
            }

            // add the last one, minus the "
            fileMet.addMetadata(SITE_ID, fileToks[nextIdx].substring(0,
                fileToks[nextIdx].length() - 1));
            nextIdx++;

          } else {
            fileMet.addMetadata(SITE_ID, fileToks[PRODUCT_SITE_ID_COL]);
          }

          int offset = nextIdx - PRODUCT_SITE_ID_COL;
          fileMet
              .addMetadata(ORGAN_ID, fileToks[PRODUCT_ORGAN_ID_COL + offset]);
          fileMet.addMetadata(SPECIMEN_ID, fileToks[PRODUCT_SPECIMEN_ID_COL
              + offset]);

          // now add it to the hashmap
          this.metDb.put(fileMet.getMetadata(FILENAME).toUpperCase(), fileMet);

        }

      } catch (FileNotFoundException e) {
        e.printStackTrace();
        LOG.log(Level.WARNING, "Unable to find SELDI met extraction db file: ["
            + this.confFile + "]");
        throw e;
      } finally {
        if (br != null) {
          try {
            br.close();
          } catch (Exception ignore) {
          }

          br = null;
        }
      }

    }

  }

  private void overrideMetadata(File file, Metadata met) {
    met.replaceMetadata(FILENAME, file.getName());
    met.replaceMetadata(FILE_LOCATION, file.getParentFile().getAbsolutePath());
    if (met.getMetadata(PRODUCT_TYPE) == null) {
      met.replaceMetadata(PRODUCT_TYPE, unidentifiedFile);
    }
  }

}
