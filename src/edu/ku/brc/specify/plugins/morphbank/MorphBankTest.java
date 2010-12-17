/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.util.Vector;

import net.morphbank.mbsvc3.xml.Credentials;
import net.morphbank.mbsvc3.xml.Insert;
import net.morphbank.mbsvc3.xml.Request;
import net.morphbank.mbsvc3.xml.XmlBaseObject;
import net.morphbank.mbsvc3.xml.XmlId;
import net.morphbank.mbsvc3.xml.XmlUtils;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.lang.NotImplementedException;

import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;

/**
 * @author timo
 *
 * @code_status Alpha
 *
 * Apr 7, 2010
 */
public class MorphBankTest 
{
	//public static String MORPHBANK_URL = "http://test.morphbank.net";
	
	//The following two fields are modified if preference settings for the urls are set in specify
	public static String MORPHBANK_URL = "http://www.morphbank.net";
	public static String MORPHBANK_IM_POST_URL = "http://itest.morphbank.net/Image/imageFileUpload.php";
	
	public static String MORPHBANK_IMAGE_Q = "?id=";
	
	/**
	 * @param mapper
	 * @return
	 * @throws Exception
	 */
	protected static XmlBaseObject createXmlSpecimen(CollectionObjectFieldMapper mapper) throws Exception
	{
		XmlBaseObject xmlSpecimen = new XmlBaseObject("Specimen");
		xmlSpecimen.addDescription("From specimen " + mapper.getCollectionObjectId());
		mapper.setXmlSpecimenFields(xmlSpecimen);
		//addLocalId(xmlSpecimen);
		return xmlSpecimen;
	}
		
	/**
	 * @param id
	 * @param submitter
	 * @param owner
	 * @param dwcMapper
	 * @return
	 * @throws Exception
	 */
	public static Request createRequestFromCollectionObjectId(Integer id,
			Credentials submitter, Credentials owner, DwcMapper dwcMapper) throws Exception
	{
		return createRequestFromCollectionObjectId(id, submitter, owner, new CollectionObjectFieldMapper(id, dwcMapper));
	}
	
	/**
	 * @param id
	 * @param submitter
	 * @param owner
	 * @param fieldMapper
	 * @return
	 * @throws Exception
	 */
	public static Request createRequestFromCollectionObjectId(Integer id,
			Credentials submitter, Credentials owner, CollectionObjectFieldMapper fieldMapper) throws Exception
	{
		Request request = new Request();
		request.setSubmitter(submitter);
		Insert insert = new Insert();
		insert.setContributor(owner);
		request.getInsert().add(insert);
		XmlBaseObject xmlSpecimen = createXmlSpecimen(fieldMapper);
		insert.getXmlObjectList().add(xmlSpecimen);
		Vector<XmlBaseObject> xmlImages = fieldMapper.getXmlImages();
		for (XmlBaseObject xmlImage : xmlImages)
		{
			xmlImage.getView().add(new XmlId(77407));			
			insert.getXmlObjectList().add(xmlImage);
		}
		return request;
	}
	
	
	/**
	 * @param obj
	 * @param submitter
	 * @param owner
	 * @param mappingId
	 * @return
	 * @throws Exception
	 */
	public static Request createRequestFromCollectionObject(final CollectionObject obj, 
			final Credentials submitter, final Credentials owner, 
			final Integer mappingId) throws Exception
	{
		CollectionObjectFieldMapper fieldMapper = new CollectionObjectFieldMapper(obj, mappingId);
		Request request = new Request();
		request.setSubmitter(submitter);
		Insert insert = new Insert();
		insert.setContributor(owner);
		request.getInsert().add(insert);
		XmlBaseObject xmlSpecimen = createXmlSpecimen(fieldMapper);
		insert.getXmlObjectList().add(xmlSpecimen);
		Vector<XmlBaseObject> xmlImages = fieldMapper.getXmlImages();
		for (XmlBaseObject xmlImage : xmlImages)
		{
			xmlImage.getView().add(new XmlId(77407));			
			insert.getXmlObjectList().add(xmlImage);
		}
		return request;
	}
	
	/**
	 * @param image
	 * @param submitter
	 * @param owner
	 * @return
	 * @throws Exception
	 */
	public static Request createRequestFromImage(final ObjectAttachmentIFace<?> image, 
			final Integer conceptMappingId, final Credentials submitter, final Credentials owner) throws Exception
	{
		if (!(image.getObject() instanceof CollectionObject))
		{
			throw new NotImplementedException("MorphBankTest attachment type not supported: " + image.getClass().getName());
		}
		CollectionObjectFieldMapper fieldMapper = new CollectionObjectFieldMapper((CollectionObject )image.getObject(), conceptMappingId);
		Request request = new Request();
		request.setSubmitter(submitter);
		Insert insert = new Insert();
		insert.setContributor(owner);
		request.getInsert().add(insert);
		XmlBaseObject xmlSpecimen = createXmlSpecimen(fieldMapper);
		insert.getXmlObjectList().add(xmlSpecimen);
		XmlBaseObject xmlImage = fieldMapper.getXmlImage(image);
		xmlImage.getView().add(new XmlId(1000349));			
		insert.getXmlObjectList().add(xmlImage);
		return request;
	}

	/**
	 * @param id
	 * @param originalFileName
	 * @param imageFileName
	 * @return
	 * @throws Exception
	 */
	public static PostMethod getImagePostRequest(String id, String imageFileName) throws Exception 
	{
		return getImagePostRequest(MORPHBANK_IM_POST_URL, id, "dummy", imageFileName);
	}
	
	/**
	 * @param strURL
	 * @param id
	 * @param originalFileName
	 * @param imageFileName
	 * @return
	 * @throws Exception
	 */
	public static PostMethod getImagePostRequest(String strURL, String id, String originalFileName, String imageFileName) throws Exception 
	{
		File input = new File(imageFileName);
		// Prepare HTTP post
		PostMethod post = new PostMethod(strURL);

		Part[] parts = {new StringPart("id",id), new StringPart("fileName",originalFileName),
				new FilePart("image", originalFileName, input)
		};
		RequestEntity entity = new MultipartRequestEntity(parts, post.getParams());
		post.setRequestEntity(entity);
		return post;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try
		{
			String connStr = "jdbc:mysql://localhost/lsusmollusca?characterEncoding=UTF-8&autoReconnect=true"; 
			DwcMapper.connection = DriverManager.getConnection(connStr, "Master", "Master");
			CollectionObjectFieldMapper.connection = DwcMapper.connection;
			
			FileWriter reportFile = new FileWriter("/home/timo/mbreport.xml");
			PrintWriter report = new PrintWriter(reportFile);

			Request request =  createRequestFromCollectionObjectId(1, new Credentials(), new Credentials(), new DwcMapper());

			// Request request = mapper.createRequestFromFile(INPUT_FILE,
			FileWriter outFile = new FileWriter("/home/timo/mb.xml");
			PrintWriter out = new PrintWriter(outFile);
			XmlUtils.printXml(out, request);
			report.close();
			reportFile.close();

			
			System.exit(0);
		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

}
