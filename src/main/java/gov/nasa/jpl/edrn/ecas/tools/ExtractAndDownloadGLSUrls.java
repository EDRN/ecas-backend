//Copyright (c) 2010, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.ecas.tools;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;

//APACHE imports
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.Header;

import org.apache.commons.io.FileUtils;

/**
 * 
 * Nitfy tool to help out with staging and ingesting GLS data
 * from Canary.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */

public class ExtractAndDownloadGLSUrls {

  public static void main(String[] args) throws Exception {
    // first param is abs dir path
    String usage = "java " + ExtractAndDownloadGLSUrls.class.getName()
        + " <met dir> <user> <pass>\n";

    if (args.length != 3) {
      System.err.println(usage);
      System.exit(1);
    }

    String glsMetDir = args[0];
    String glsUsername = args[1];
    String glsPassword = args[2];

    if (!glsMetDir.endsWith("/"))
      glsMetDir += "/";

    File[] glsMetFiles = new File(glsMetDir).listFiles();
    for (File glsMetFile : glsMetFiles) {
      Metadata met = new Metadata(new FileInputStream(glsMetFile));
      // compute target path
      String targetPathSpec = "/[FileLocation]/[subjectId]/[Filename]";
      String targetPathMetSpec = "/[FileLocation]/[subjectId]/[Filename].met";

      if (false) {
	  targetPathSpec = "/home/jtran/data-stage/[subjectId]/[Filename]";
	  targetPathMetSpec = "/home/jtran/data-stage/[subjectId]/[Filename].met";
      }

      String targetPath = PathUtils.replaceEnvVariables(targetPathSpec, met);
      String targetMetPath = PathUtils.replaceEnvVariables(targetPathMetSpec, met);
      String glsMetUrl = met.getMetadata("ProductLocation");

      // make sure that " " is not allowed in URL
      glsMetUrl = glsMetUrl.replaceAll(" ", "%20");

      System.out.println("Downloading [" + glsMetUrl + "] to file path: [" + targetPath + "]");
      new File(targetPath).getParentFile().mkdirs();

      HttpClient httpClient = new HttpClient();

      boolean downloadSuccess = false;
      String redirectLocation = "";

      {
	  GetMethod get = new GetMethod(glsMetUrl);
	  String response = null;
	  Credentials defaultcreds = 
             new UsernamePasswordCredentials(glsUsername, glsPassword);     
	  httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, 443, AuthScope.ANY_REALM), defaultcreds);
	  
	  get.setDoAuthentication(true);
	  
	  try {
	      httpClient.executeMethod(get);
	      if (get.getStatusCode() == 302 ) {
		  Header locationHeader = get.getResponseHeader("location");
		  if (locationHeader != null) {
		      redirectLocation = locationHeader.getValue();
		  } else {
		      throw new HttpException("ERROR: Unknown URL redirection address");
		  }
	      }
	      else if (get.getStatusCode() != HttpStatus.SC_OK) {
		  // throw new HttpException(get.getStatusLine().toString());
	      }
	      response = get.getResponseBodyAsString().trim();
	      FileUtils.writeStringToFile(new File(targetPath), response, "UTF-8");
	      downloadSuccess = true;
	  } catch (Exception e) {
	      e.printStackTrace();
	  } finally {
	      get.releaseConnection();
	  }
      }

      // in case the file has been redirected try again
      if (! redirectLocation.equals("")) {
	  // redirectLocation = redirectLocation.replace(' ', '+');	
	  GetMethod get = new GetMethod(redirectLocation.replaceAll(" ", "%20"));
	  String response = null;

	  System.out.println("New location: [" + redirectLocation + "]");
	  Credentials defaultcreds = 
             new UsernamePasswordCredentials(glsUsername, glsPassword);     
	  httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, 443, AuthScope.ANY_REALM), defaultcreds);
	  
	  get.setDoAuthentication(true);
	  
	  try {
	      httpClient.executeMethod(get);
	      if (get.getStatusCode() != HttpStatus.SC_OK) {
		  throw new HttpException(get.getStatusLine().toString());
	      }
	      response = get.getResponseBodyAsString().trim();
	      FileUtils.writeStringToFile(new File(targetPath), response, "UTF-8");
	      downloadSuccess = true;
	  } catch (Exception e) {
	      e.printStackTrace();
	  } finally {
	      get.releaseConnection();
	  }
      }

      if (downloadSuccess) {
        System.out.println("*** DOWNLOAD: SUCCESS ***");
        // Copy met file
        System.out.println("Copying met file: [" + glsMetFile.getAbsolutePath()
            + "] to [" + targetMetPath + "]");
        FileUtils.copyFile(glsMetFile, new File(targetMetPath));
        glsMetFile.deleteOnExit();

      } else {
        System.out.println("*** DOWNLOAD: FAILED ***");
      }
    }
  }

}
