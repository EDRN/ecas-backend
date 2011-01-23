//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.ecas.metextraction;

//JDK imports
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.commons.util.DateConvert;
import static org.apache.oodt.cas.filemgr.metadata.CoreMetKeys.*;
import static gov.nasa.jpl.edrn.ecas.metadata.SELDIMetadata.*;
import org.apache.oodt.cas.metadata.MetExtractorConfig;
import org.apache.oodt.cas.metadata.MetExtractorConfigReader;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractorConfigReaderException;

// EDRN imports
import static gov.nasa.jpl.edrn.ecas.metadata.EDRNMetadata.*;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class SELDIMetExtractorConfigReader implements MetExtractorConfigReader {

    /* our log stream */
    private static final Logger LOG = Logger
            .getLogger(SELDIMetExtractorConfigReader.class.getName());

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.metadata.MetExtractorConfigReader#parseConfigFile(java.io.File)
     */
    public MetExtractorConfig parseConfigFile(File c)
            throws MetExtractorConfigReaderException {

        SELDIMetExtractorConfig config = new SELDIMetExtractorConfig();

        // okay to load the db, just load the .csv file
        // for each line in the file, process it as the metadata for one
        // file
        // split on "|" to separate out the fields
        // split on "," to separate out multiple values

        // build up a hashtable of file name => Metadata

        BufferedReader br = null;
        String line = null;

        try {
            br = new BufferedReader(new FileReader(c));

            while ((line = br.readLine()) != null) {
                String[] fileToks = line.split(FIELD_DELIMETER);
                Metadata fileMet = new Metadata();
                fileMet.addMetadata(PRODUCT_TYPE, fileToks[PRODUCT_TYPE_COL]);
                Date recvDate = inputDataDateFormatter
                        .parse(fileToks[PRODUCT_RECV_DATE_COL]);
                String productRecvTime = DateConvert.isoFormat(recvDate);
                fileMet.addMetadata(PRODUCT_RECEVIED_TIME, productRecvTime);
                fileMet.addMetadata(PRODUCT_DESCRIPTION,
                        fileToks[PRODUCT_DESC_COL]);
                fileMet.addMetadata(FILE_LOCATION,
                        fileToks[PRODUCT_FILELOCATION_COL]);
                fileMet.addMetadata(FILENAME, fileToks[PRODUCT_FILENAME_COL]);
                fileMet.addMetadata(PRODUCT_ID, fileToks[PRODUCT_ID_COL]);
                fileMet.addMetadata(DATASET_ID,
                        fileToks[PRODUCT_DATASET_ID_COL]);
                fileMet.addMetadata(PROTOCOL_ID,
                        fileToks[PRODUCT_PROTOCOL_ID_COL]);

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
                fileMet.addMetadata(ORGAN_ID, fileToks[PRODUCT_ORGAN_ID_COL
                        + offset]);
                fileMet.addMetadata(SPECIMEN_ID,
                        fileToks[PRODUCT_SPECIMEN_ID_COL + offset]);

                // now add it to the hashmap
                config.addMetadataForFile(fileMet.getMetadata(FILENAME)
                        .toUpperCase(), fileMet);

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Unable to find SELDI met extraction db file: [" + c + "]");
            throw new MetExtractorConfigReaderException(
                    "Unable to find SELDI met extraction db file: [" + c + "]");
        } catch (ParseException e) {
            e.printStackTrace();
            throw new MetExtractorConfigReaderException(
                    "Error parsing ISO date!");
        } catch (IOException e) {
            e.printStackTrace();
            throw new MetExtractorConfigReaderException(
                    "Error parsing .csv file line!");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception ignore) {
                }

                br = null;
            }
        }
        
        
        return config;

    }

}
