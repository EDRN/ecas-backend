//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.ecas.metextraction;

//JDK imports
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

//OODT imports
import org.apache.oodt.cas.metadata.MetExtractorConfig;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Configuration for the {@link SELDIMetExtractor}
 * </p>.
 */
public class SELDIMetExtractorConfig implements MetExtractorConfig,
        Serializable {

    private static final long serialVersionUID = 6701238214989972174L;

    private Map<String, Metadata> metDb;

    public SELDIMetExtractorConfig() {
        this.metDb = new HashMap<String, Metadata>();
    }

    public void addMetadataForFile(String file, Metadata met) {
        this.metDb.put(file, met);
    }

    public Metadata getMetadataForFile(String filename) {
        return this.metDb.get(filename);
    }

}
