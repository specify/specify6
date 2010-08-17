/**
 * 
 */
package edu.ku.brc.specify.utilapps.morphbank;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import net.morphbank.mbsvc3.xml.Credentials;
import net.morphbank.mbsvc3.xml.Request;
import net.morphbank.mbsvc3.xml.XmlUtils;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttachment;
import edu.ku.brc.specify.plugins.morphbank.CollectionObjectFieldMapper;
import edu.ku.brc.specify.plugins.morphbank.DwcMapper;
import edu.ku.brc.specify.plugins.morphbank.MorphBankTest;

/**
 * @author timo
 *
 * Generates files containing xml submit requests for the Morphbank webservice.
 * The files can then be sent to Morphbank for batch processing 
 * 
 * A darwin core mapping must be defined in the database. THe darwin core mappings are obtained from the export cache for the mapping.
 * 
 * 
 */
public class BatchMorphbankSubmit
{
	protected final int requestsPerFile; //the number submit requests contained in each file
	protected final Class<?> tblClass;
	protected final Class<?> attachmentClass;
	protected final List<Integer> keysToSubmit;
	protected final String fileNameBase;
	protected final File destinationDir;
	protected final int dwcMapId;
	protected DwcMapper dwcMapper = null;
	
	/**
	 * @param requestsPerFile
	 * @param tblClass
	 * @param attachmentClass
	 * @param keysToSubmit
	 */
	public BatchMorphbankSubmit(int requestsPerFile, Class<?> tblClass,
			Class<?> attachmentClass, List<Integer> keysToSubmit, String fileNameBase,
			File destinationDir, int dwcMapId)
	{
		super();
		this.requestsPerFile = requestsPerFile;
		this.tblClass = tblClass;
		this.attachmentClass = attachmentClass;
		this.keysToSubmit = keysToSubmit;
		this.fileNameBase = fileNameBase;
		this.destinationDir = destinationDir;
		this.dwcMapId = dwcMapId;
		this.dwcMapper = new DwcMapper(dwcMapId);
	}


	public void generateSubmissions() throws Exception
	{
		if (!tblClass.equals(CollectionObject.class))
		{
			throw new Exception("Class not supported: " + tblClass.getName());
		}
		
		List<Request> submissions = new Vector<Request>();
		int counter = 0;
		int fileCount = 1;
		for (Integer key : keysToSubmit)
		{
			submissions.add(generateSubmission(key));
			if (++counter == requestsPerFile)
			{
				writeToFile(submissions, fileCount++);
				submissions.clear();
				counter = 0;
			}
		}
		if (counter > 0)
		{
			writeToFile(submissions, fileCount);
		}
	}
	
	/**
	 * @param key
	 * @return
	 */
	protected Request generateSubmission(Integer key) throws Exception
	{
		return MorphBankTest.createRequestFromCollectionObjectId(key, new Credentials(0, 0), 
				new Credentials(0, 0), dwcMapper);
	}
	
	/**
	 * @param submissions
	 */
	protected void writeToFile(List<Request> submissions, int fileNum) throws IOException
	{
		String num = String.valueOf(fileNum);
		while (num.length() < 4)
		{
			num = "0" + num;
		}
		String fileName = destinationDir + File.separator + fileNameBase + "_" + num + ".xml";
		FileWriter outFile = new FileWriter(fileName);
		PrintWriter out = new PrintWriter(outFile);
		for (Request request : submissions)
		{
			XmlUtils.printXml(out, request);
		}
		out.close();
		outFile.close();

	}
	
		
	/**
	 * @return the requestsPerFile
	 */
	public int getRequestsPerFile()
	{
		return requestsPerFile;
	}


	/**
	 * @return the tblClass
	 */
	public Class<?> getTblClass()
	{
		return tblClass;
	}


	/**
	 * @return the attachmentClass
	 */
	public Class<?> getAttachmentClass()
	{
		return attachmentClass;
	}


	/**
	 * @return the keysToSubmit
	 */
	public List<Integer> getKeysToSubmit()
	{
		return keysToSubmit;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			String connStr = "jdbc:mysql://localhost/troysp6?characterEncoding=UTF-8&autoReconnect=true"; 
			DwcMapper.connection = DriverManager.getConnection(connStr, "Master", "Master");
			CollectionObjectFieldMapper.connection = DwcMapper.connection;
			
			Statement stmt = DwcMapper.connection.createStatement();
			ResultSet rs = stmt.executeQuery("select collectionobjectid from collectionobjectattachment order by 1");
			List<Integer> keys = new Vector<Integer>();
			while (rs.next())
			{
				keys.add(rs.getInt(1));
			}
			rs.close();
			stmt.close();
			
			BatchMorphbankSubmit mbs = new BatchMorphbankSubmit(100, CollectionObject.class, 
				CollectionObjectAttachment.class, keys, "troymbsubmission", 
				new File("/media/Terror/ConversionsAndFixes/Troy/"), 1);
			mbs.generateSubmissions();
			System.out.println("done.");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		

	}

}
