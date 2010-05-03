/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

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
	
	protected static XmlBaseObject createXmlSpecimen(CollectionObjectFieldMapper mapper) throws Exception
	{
		XmlBaseObject xmlSpecimen = new XmlBaseObject("Specimen");
		xmlSpecimen.addDescription("From specimen " + mapper.getCollectionObjectId());
		mapper.setXmlSpecimenFields(xmlSpecimen);
		//addLocalId(xmlSpecimen);
		return xmlSpecimen;
	}
		
	public static Request createRequestFromCollectionObjectId(Integer Id,
			Credentials submitter, Credentials owner) throws Exception
	{
		CollectionObjectFieldMapper fieldMapper = new CollectionObjectFieldMapper(Id);
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
	
	public static Request createRequestFromCollectionObject(final CollectionObject obj, 
			Credentials submitter, Credentials owner) throws Exception
	{
		CollectionObjectFieldMapper fieldMapper = new CollectionObjectFieldMapper(obj);
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
			Credentials submitter, Credentials owner) throws Exception
	{
		if (!(image.getObject() instanceof CollectionObject))
		{
			throw new NotImplementedException("MorphBankTest attachment type not supported: " + image.getClass().getName());
		}
		CollectionObjectFieldMapper fieldMapper = new CollectionObjectFieldMapper((CollectionObject )image.getObject());
		Request request = new Request();
		request.setSubmitter(submitter);
		Insert insert = new Insert();
		insert.setContributor(owner);
		request.getInsert().add(insert);
		XmlBaseObject xmlSpecimen = createXmlSpecimen(fieldMapper);
		insert.getXmlObjectList().add(xmlSpecimen);
		XmlBaseObject xmlImage = fieldMapper.getXmlImage(image);
		xmlImage.getView().add(new XmlId(77407));			
		insert.getXmlObjectList().add(xmlImage);
		return request;
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

			Request request =  createRequestFromCollectionObjectId(1, new Credentials(), new Credentials());

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
