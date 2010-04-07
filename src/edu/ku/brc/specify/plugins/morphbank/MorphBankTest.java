/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import java.io.PrintWriter;

import net.morphbank.mbsvc3.xml.Credentials;
import net.morphbank.mbsvc3.xml.Insert;
import net.morphbank.mbsvc3.xml.Request;
import net.morphbank.mbsvc3.xml.XmlBaseObject;

/**
 * @author timo
 *
 * @code_status Alpha
 *
 * Apr 7, 2010
 */
public class MorphBankTest 
{
	
	protected XmlBaseObject createXmlSpecimen(CollectionObjectFieldMapper mapper) throws Exception
	{
		XmlBaseObject xmlSpecimen = new XmlBaseObject("Specimen");
		xmlSpecimen.addDescription("From specimen " + mapper.getCollectionObjectId());
		mapper.setXmlSpecimenFields(xmlSpecimen);
		//addLocalId(xmlSpecimen);
		return xmlSpecimen;
	}
	
	protected XmlBaseObject createXmlImage(CollectionObjectFieldMapper mapper)
	{
		XmlBaseObject xmlImage = new XmlBaseObject("Image");
		xmlImage.addDescription("From specimen " + mapper.getCollectionObjectId());
		mapper.setXmlImageFields(xmlImage);
		//addLocalId(xmlSpecimen);
		return xmlImage;
	}
	
	public Request createRequestFromCollectionObjectId(Integer Id,
			Credentials submitter, Credentials owner, PrintWriter report) throws Exception
	{
		CollectionObjectFieldMapper fieldMapper = new CollectionObjectFieldMapper(Id);
		Request request = new Request();
		request.setSubmitter(submitter);
		Insert insert = new Insert();
		insert.setContributor(owner);
		request.getInsert().add(insert);
		XmlBaseObject xmlSpecimen = createXmlSpecimen(fieldMapper);
		insert.getXmlObjectList().add(xmlSpecimen);
		XmlBaseObject xmlImage = createXmlImage(fieldMapper);
		insert.getXmlObjectList().add(xmlImage);
		return request;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub

	}

}
