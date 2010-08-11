//Copyright (c) 2010, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.ecas.tools;

import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.filemgr.metadata.CoreMetKeys;
import java.io.File;
import java.io.FileInputStream;

/**
 * 
 * Describe your class here.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class ExtractAndDownloadGLSUrls {

  public static void main(String[] args) throws Exception {
    // first param is abs dir path
    String glsMetDir = args[0];
    if (!glsMetDir.endsWith("/"))
      glsMetDir += "/";

    File[] glsMetFiles = new File(glsMetDir).listFiles();
    for (File glsMetFile : glsMetFiles) {
      Metadata met = new Metadata(new FileInputStream(glsMetFile));
      // compute target path
      String targetPath = glsMetDir + met.getMetadata("subjectId") + "/"
          + met.getMetadata(CoreMetKeys.FILENAME);
      String glsMetUrl = met.getMetadata("ProductLocation");
      System.out.println("Downloading [" + glsMetUrl + "] to file path: ["
          + targetPath + "]");

      // move met file
      System.out.println("Moving met file: ["
          + glsMetFile.getAbsolutePath()
          + "] to ["
          + new File(targetPath).getParent()
          + "/"
          + glsMetFile.getName().substring(0,
              glsMetFile.getName().lastIndexOf(".")) + "]");
    }
  }

}
