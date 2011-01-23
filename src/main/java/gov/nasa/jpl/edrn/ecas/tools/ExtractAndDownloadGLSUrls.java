//Copyright (c) 2010, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.ecas.tools;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.metadata.util.PathUtils;

//JDK imports
import java.io.File;
import java.io.FileInputStream;

//APACHE imports
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
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
    String glsMetDir = args[0];
    String glsUsername = args[1];
    String glsPassword = args[2];

    if (args.length != 3) {
      System.err.println(usage);
      System.exit(1);
    }

    if (!glsMetDir.endsWith("/"))
      glsMetDir += "/";

    File[] glsMetFiles = new File(glsMetDir).listFiles();
    for (File glsMetFile : glsMetFiles) {
      Metadata met = new SerializableMetadata(new FileInputStream(glsMetFile));
      // compute target path
      String targetPathSpec = "/[FileLocation]/[subjectId]/[Filename]";
      String targetPathMetSpec = "/[FileLocation]/[subjectId]/[Filename].met";
      String targetPath = PathUtils.replaceEnvVariables(targetPathSpec, met);
      String targetMetPath = PathUtils.replaceEnvVariables(targetPathMetSpec, met);
      String glsMetUrl = met.getMetadata("ProductLocation");
      System.out.println("Downloading [" + glsMetUrl + "] to file path: ["
          + targetPath + "]");
      new File(targetPath).getParentFile().mkdirs();

      HttpClient httpClient = new HttpClient();
      GetMethod get = new GetMethod(glsMetUrl);
      String response = null;
      Credentials defaultcreds = new UsernamePasswordCredentials(glsUsername,
          glsPassword);     
      httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, 443, AuthScope.ANY_REALM), defaultcreds);

      get.setDoAuthentication(true);
      boolean downloadSuccess = false;
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

      if (downloadSuccess) {
        // move met file
        System.out.println("Moving met file: [" + glsMetFile.getAbsolutePath()
            + "] to [" + targetMetPath + "]");
        FileUtils.copyFile(glsMetFile, new File(targetMetPath));
        glsMetFile.deleteOnExit();

      }
    }
  }

}
