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

import org.apache.commons.io.FileUtils;

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
 * A darwin core mapping must be defined in the database. THe darwin core values are obtained from the export cache for the mapping.
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
	
	protected Integer mbUserID = null;
	protected Integer mbGroupID = null;
	
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
		//for Auburn
		mbUserID = 691554; 
		mbGroupID = 692592;
		
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
			try 
			{
				submissions.add(generateSubmission(key));
				if (++counter == requestsPerFile)
				{
					writeToFile(submissions, fileCount++);
					submissions.clear();
					counter = 0;
				}
			} catch (DwcMapper.MissingRecordException mrex)
			{
				System.out.println(mrex.getMessage());
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
		return MorphBankTest.createRequestFromCollectionObjectId(key, new Credentials(691554, 692592), 
				new Credentials(mbUserID, mbGroupID), dwcMapper);
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
		
		//Now strip out redundant headers and stuff
		File f = new File(fileName);
		List<?> objLines = FileUtils.readLines(f);
		List<String> lines = new Vector<String>(objLines.size());
		//copying to workaround generics issues
		for (Object obj : objLines)
		{
			lines.add((String)obj);
		}
		String header = (String )lines.get(0);
		String requestHeaderStart = "<mb:request xsi:";
		String requestFooter = "</mb:request>";
		String insertHeader = "<insert>";
		String insertFooter = "</insert>";
		//String footer = (String )lines.get(lines.size()-1);
		int numLines = lines.size();
		for (int l = numLines - 1; l > 0; l--)
		{
			String line = (String )lines.get(l);
//			if (l == 0) 
//			{
//				lines.set(0, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
//				continue;
//			}
			if (header.equals(line))
			{
				lines.remove(l);
			} else if (line.trim().startsWith(requestHeaderStart)) 
			{
				if (l > 2)
				{
					//remove the submitter id
					lines.remove(l+4);
					lines.remove(l+3);
					lines.remove(l+2);
					lines.remove(l+1);
					lines.remove(l);
				} else
				{
					//lines.set(l, "<ns2:request xsi:schemaLocation=\"http://www.morphbank.net/mbsvc3/ http://www.morphbank.net/schema/mbsvc3.xsd\">");
					lines.set(l, "<ns2:request xsi:schemaLocation=\"http://www.morphbank.net/mbsvc3/ http://www.morphbank.net/schema/mbsvc3.xsd\" "
						+ "xmlns:dwc=\"http://rs.tdwg.org/dwc/dwcore/\" "
						+ "xmlns:ns2=\"http://www.morphbank.net/mbsvc3/\" "
						+ "xmlns:dwcg=\"http://rs.tdwg.org/dwc/geospatial/\" "
						+ "xmlns:dwce=\"http://rs.tdwg.org/dwc/dwelement\" "
						+ "xmlns:dwcc=\"http://rs.tdwg.org/dwc/curatorial/\" "
						+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
				}
			} else if (requestFooter.equals(line.trim()))
			{
				if (l == numLines - 1)
				{
					lines.set(l, "</ns2:request>");
				} else
				{
					lines.remove(l);
				}
			} else if (insertFooter.equals(line.trim()))
			{
				if (l != numLines - 2)
				{
					lines.remove(l);
				}
			} else if (insertHeader.equals(line.trim()))
			{
				if (l > 7)
				{
					//remove the submitter id
					lines.remove(l+4);
					lines.remove(l+3);
					lines.remove(l+2);
					lines.remove(l+1);
					lines.remove(l);
				} 
			} else if (line.contains("CatalogNumberNumeric"))
			{
				lines.set(l, line.replace(".0</", "</"));
			}
		}
		//FileUtils.writeLines(f, lines);
		FileUtils.writeLines(f, "ISO-8859-1", lines);
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
			//String connStr = "jdbc:mysql://localhost/troy?characterEncoding=UTF-8&autoReconnect=true"; 
			String connStr = "jdbc:mysql://localhost/auburn?characterEncoding=UTF-8&autoReconnect=true"; 
			
			DwcMapper.connection = DriverManager.getConnection(connStr, "Master", "Master");
			CollectionObjectFieldMapper.connection = DwcMapper.connection;
			
			Statement stmt = DwcMapper.connection.createStatement();
			//filter out records with messy taxon info or barcode or other known problems...
//			String sql = "select coa.collectionobjectid from collectionobjectattachment coa inner join "
//				+ "collectionobject co on co.collectionobjectid = coa.collectionobjectid inner join "
//				+ "determination d on d.collectionobjectid = co.collectionobjectid inner join "
//				+ "taxon t on t.taxonid = d.taxonid or t.taxonid = d.preferredtaxonid "
//				+ "where t.fullname not like '%unplaced%' "
//				//+ " and co.AltCatalogNumber not in (select AltCatalogNumber from DupBarCodes) "
//				+ "order by 1";
			
			ResultSet rs = stmt.executeQuery("select collectionobjectid from collectionobjectattachment order by 1");
			//ResultSet rs = stmt.executeQuery(sql);
			List<Integer> keys = new Vector<Integer>();
			while (rs.next())
			{
				keys.add(rs.getInt(1));
			}
			rs.close();
			stmt.close();
			
//			BatchMorphbankSubmit mbs = new BatchMorphbankSubmit(100, CollectionObject.class, 
//				CollectionObjectAttachment.class, keys, "troymbsubmission", 
//				new File("/media/Terror/ConversionsAndFixes/Troy/BSII"), 1);

			BatchMorphbankSubmit mbs = new BatchMorphbankSubmit(100, CollectionObject.class, 
					CollectionObjectAttachment.class, keys, "auburn", 
					new File("/media/Terror/ConversionsAndFixes/auburn"), 2 /*the GoodA mapping */);

			mbs.generateSubmissions();
			System.out.println("done.");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		

	}

}
