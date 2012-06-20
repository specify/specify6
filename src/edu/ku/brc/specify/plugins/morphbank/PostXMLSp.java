/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import java.io.File;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;

import edu.ku.brc.af.prefs.AppPreferences;


/**
 * @author timo
 *
 *Modified version of class from morphbank - net.morphbank.webclient.PostXML.
 *
 *Adds return value for post.
 */
public class PostXMLSp
{
	// static String UPLOAD_FILE =
	// "C:/dev/morphbank/spiderfiles/spiders2sample.xml";
	// static String UPLOAD_FILE =
	// "C:/dev/morphbank/CToL/xmlfiles/ctol0007.xml";
	static String UPLOAD_FILE = "/home/timo/mb.xml";
	// static String UPLOAD_FILE =
	// "C:/dev/morphbank/spiderfiles/spiders0004.xml";
	// static String URL = "http://localhost:8080/mb/restful";
	//http://www.susemorph.nrm.se/Image/imageFileUpload.php
	static String morphBankServiceURLSuffix = "/mbd/restful";
	
	final String morphBankServiceURL;
	
	public PostXMLSp()
	{
		AppPreferences prefs = AppPreferences.getRemote();
		String baseURL = prefs.get("morphbank.baseurl", null);
		morphBankServiceURL = baseURL.replaceFirst("http://.*?\\.", "http://services.") + morphBankServiceURLSuffix;
	}
	
	public static void main(String[] args) throws Exception
	{
		PostXMLSp postXML = new PostXMLSp();
		String uploadFile = UPLOAD_FILE;
		if (args.length > 0)
			uploadFile = args[0];
		else
		{
			System.out.println("Usage: java net.morphbank.webclient.PostXML [filepath] ");
		}
		PostResponse pr = postXML.post(uploadFile);
		System.out.println("status: " + pr.getStatusCode());
		System.out.println("response:");
		System.out.println(pr.getBody());
	}

	public PostResponse post(String strXMLFilename)
			throws Exception
	{
		File input = new File(strXMLFilename);
		
		// Prepare HTTP post
		PostMethod post = new PostMethod(morphBankServiceURL);
		// Request content will be retrieved directly
		// from the input stream Part[] parts = {
		Part[] parts = { new FilePart("uploadFile", strXMLFilename, input) };
		RequestEntity entity = new MultipartRequestEntity(parts, post
				.getParams());
		// RequestEntity entity = new FileRequestEntity(input,
		// "text/xml;charset=utf-8");
		post.setRequestEntity(entity);
		// Get HTTP client
		HttpClient httpclient = new HttpClient();
		// Execute request
		try
		{
			//System.out.println("Trying post");
			int result = httpclient.executeMethod(post);
			return new PostResponse(result, post.getResponseBodyAsString());
		} finally
		{
			// Release current connection to the connection pool once you are
			// done
			post.releaseConnection();
		}
	}

	public class PostResponse
	{
		protected final int statusCode;
		protected final String body;

		/**
		 * @param statusCode
		 * @param body
		 */
		public PostResponse(int statusCode, String body)
		{
			super();
			this.statusCode = statusCode;
			this.body = body;
		}

		/**
		 * @return the statusCode
		 */
		public int getStatusCode()
		{
			return statusCode;
		}

		/**
		 * @return the body
		 */
		public String getBody()
		{
			return body;
		}
	}
}
