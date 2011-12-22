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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

//APACHE imports
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CircularRedirectException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

/**
 * 
 * Nitfy tool to help out with staging and ingesting GLS data from Canary.
 * 
 * @author mattmann, rverma
 * @version $Revision$
 * 
 */
public class ExtractAndDownloadGLSUrls {

	private static final Logger LOG = Logger
	.getLogger(ExtractAndDownloadGLSUrls.class.getName());

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

		if (glsMetFiles.length == 0) {
			LOG.warning("No met files found in [" + glsMetDir + "]. Stopping.");
			System.exit(1);
		}

		for (File glsMetFile : glsMetFiles) {
			Metadata met = new SerializableMetadata(new FileInputStream(
					glsMetFile));
			// compute target path
			String targetPathSpec = "/[FileLocation]/[Filename]";
			String targetPathMetSpec = "/[FileLocation]/[Filename].met";
			String targetPath = PathUtils.replaceEnvVariables(targetPathSpec, met);
			String targetMetPath = PathUtils.replaceEnvVariables(targetPathMetSpec, met);
			String glsMetUrl = met.getMetadata("ProductLocation");
			glsMetUrl = URIUtil.encodePath(glsMetUrl, "ISO-8859-1"); // necessary to correct for bad URIs
			LOG.info("Downloading [" + glsMetUrl + "] to file path: ["
					+ targetPath + "]");
			new File(targetPath).getParentFile().mkdirs();

			// Use custom redirect handler to correct redirected bad URIs
			DefaultHttpClient httpClient = new DefaultHttpClient();
			RedirectHandler redirectHandler = new CanaryRedirectHandler();
			httpClient.setRedirectHandler(redirectHandler);

			boolean downloadSuccess = false;
			try {
				httpClient.getCredentialsProvider().setCredentials(
						new AuthScope(AuthScope.ANY_HOST, 443),
						new UsernamePasswordCredentials(glsUsername,
								glsPassword));

				HttpGet get = new HttpGet(glsMetUrl);
				ResponseHandler<String> responseHandler = new BasicResponseHandler();

				LOG.info("Executing HTTPS request: [" + get.getRequestLine()
						+ "]");
				HttpResponse responsee = httpClient.execute(get);
				LOG.info("Response status: ["
						+ responsee.getStatusLine().getStatusCode() + "]");
				if (responsee.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					throw new HttpException(responsee.getStatusLine()
							.toString());
				}

				InputStreamReader isr = new InputStreamReader(responsee
						.getEntity().getContent(), "UTF-8");
				Writer writer = new StringWriter();

				char[] buff = new char[1024];
				try {
					Reader buffReader = new BufferedReader(isr);
					int i;
					while ((i = buffReader.read(buff)) != -1) {
						writer.write(buff, 0, i);
					}
				} finally {
					isr.close();
				}

				FileUtils.writeStringToFile(new File(targetPath),
						writer.toString(), "UTF-8");
				downloadSuccess = true;

			} catch (Exception e) {
				LOG.severe(e.getMessage());
				e.printStackTrace();
			} finally {
				httpClient.getConnectionManager().shutdown();
			}

			if (downloadSuccess) {
				// move met file
				LOG.info("Moving met file: [" + glsMetFile.getAbsolutePath()
						+ "] to [" + targetMetPath + "]");
				FileUtils.copyFile(glsMetFile, new File(targetMetPath));
				glsMetFile.deleteOnExit();

			}

		}
	}

}

/**
 * Custom HTTP redirect handler for intercepting HTTP responses and generating a new
 * URI with special characters, like spaces, removed. 
 * 
 * This class is necessary to handle Canary Amazon AWS URIs, which contain many special
 * characters Java's HttpClient library has a tough time dealing with. Without redirect 
 * interception, secondary redirects fail to resolve as usable URIs for download
 * 
 * Special thanks to:
 *    Aymon Fournier: http://stackoverflow.com/a/3427169
 * 
 * @author rverma
 *
 */
class CanaryRedirectHandler extends DefaultRedirectHandler {

	private static final String REDIRECT_LOCATIONS = "http.protocol.redirect-locations";

	public CanaryRedirectHandler() {
		super();
	}

	public boolean isRedirectRequested(final HttpResponse response,
			final HttpContext context) {
		if (response == null) {
			throw new IllegalArgumentException("HTTP was null");
		}

		int statusCode = response.getStatusLine().getStatusCode();
		switch (statusCode) {
		case HttpStatus.SC_MOVED_TEMPORARILY:
		case HttpStatus.SC_MOVED_PERMANENTLY:
		case HttpStatus.SC_SEE_OTHER:
		case HttpStatus.SC_TEMPORARY_REDIRECT: return true;
		default: return false;
		}
	}

	public URI getLocationURI(final HttpResponse response,
			final HttpContext context) throws ProtocolException {
		if (response == null) {
			throw new IllegalArgumentException("HTTP was null");
		}

		Header locationHeader = response.getFirstHeader("location");
		if (locationHeader == null) {
			throw new ProtocolException("Received redirect response "
					+ response.getStatusLine() + " but no location header");
		}

		// Replace all spaces with %20!
		String location = locationHeader.getValue().replaceAll(" ", "%20");

		URI uri;
		try {
			uri = new URI(location);
		} catch (URISyntaxException ex) {
			throw new ProtocolException("Invalid redirect URI: " + location, ex);
		}

		HttpParams params = response.getParams();
		// rfc2616 demands the location value be a complete URI
		// Location = "Location" ":" absoluteURI
		if (!uri.isAbsolute()) {
			if (params.isParameterTrue(ClientPNames.REJECT_RELATIVE_REDIRECT)) {
				throw new ProtocolException("Relative redirect location '"
						+ uri + "' not allowed");
			}
			// Adjust location URI
			HttpHost target = (HttpHost) context
			.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
			if (target == null) {
				throw new IllegalStateException("Target host not available "
						+ "in the HTTP context");
			}

			HttpRequest request = (HttpRequest) context
			.getAttribute(ExecutionContext.HTTP_REQUEST);

			try {
				URI requestURI = new URI(request.getRequestLine().getUri());
				URI absoluteRequestURI = URIUtils.rewriteURI(requestURI,
						target, true);
				uri = URIUtils.resolve(absoluteRequestURI, uri);
			} catch (URISyntaxException ex) {
				throw new ProtocolException(ex.getMessage(), ex);
			}
		}

		if (params.isParameterFalse(ClientPNames.ALLOW_CIRCULAR_REDIRECTS)) {

			RedirectLocations redirectLocations = (RedirectLocations) context
			.getAttribute(REDIRECT_LOCATIONS);

			if (redirectLocations == null) {
				redirectLocations = new RedirectLocations();
				context.setAttribute(REDIRECT_LOCATIONS, redirectLocations);
			}

			URI redirectURI;
			if (uri.getFragment() != null) {
				try {
					HttpHost target = new HttpHost(uri.getHost(),
							uri.getPort(), uri.getScheme());
					redirectURI = URIUtils.rewriteURI(uri, target, true);
				} catch (URISyntaxException ex) {
					throw new ProtocolException(ex.getMessage(), ex);
				}
			} else {
				redirectURI = uri;
			}

			if (redirectLocations.contains(redirectURI)) {
				throw new CircularRedirectException("Circular redirect to '"
						+ redirectURI + "'");
			} else {
				redirectLocations.add(redirectURI);
			}
		}

		return uri;
	}
}
