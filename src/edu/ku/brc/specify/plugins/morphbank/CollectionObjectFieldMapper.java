/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

import javax.xml.namespace.QName;

import net.morphbank.mbsvc3.fsuherb.MapFsuHerbSpreadsheetToXml;
import net.morphbank.mbsvc3.xml.ObjectFactory;
import net.morphbank.mbsvc3.xml.XmlBaseObject;
import net.morphbank.mbsvc3.xml.XmlTaxonNameUtilities;
import net.morphbank.mbsvc3.xml.XmlUtils;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Institution;

/**
 * @author timo
 *
 * @code_status Alpha
 *
 * Apr 7, 2010
 */
public class CollectionObjectFieldMapper
{
	//TO DO This class should work with hibernate objects as well as record ids (as for DarwinCoreSpecimen)
	protected CollectionObject collectionObject;
	protected Integer collectionObjectId;
	protected DwcMapper dwcMapper;
	protected Vector<QName> morphBankDwcQNames;
	protected DarwinCoreSpecimen spec;

	public static Connection connection; //for testing

	/**
	 * @param collectionObjectId
	 * @throws Exception
	 */
	public CollectionObjectFieldMapper(Integer collectionObjectId) throws Exception
	{
		this.collectionObjectId = collectionObjectId;
		buildQNames();
		dwcMapper = getDwcMapper();
		spec = new DarwinCoreSpecimen(dwcMapper);
		spec.setCollectionObjectId(collectionObjectId);	
	}
	
	public void setCollectionObjectId(Integer collectionObjectId) throws Exception
	{
		this.collectionObjectId = collectionObjectId;
		spec.setCollectionObjectId(collectionObjectId);
	}
	/**
	 * @return the DarwinCore appropriate mappings for the current context.
	 * 
	 * Possibly lots and lots of work to do here.
	 */
	protected DwcMapper getDwcMapper()
	{
		return new DwcMapper(1);
	}
	
	
	/**
	 * @throws Exception
	 * 
	 * Builds a list of QNames declared in MorphBank's ObjectFactory.
	 * 
	 * Probably will eventually not be necessary.
	 */
	protected void buildQNames() throws Exception
	{
		Class<?> ojClass = ObjectFactory.class;
		Field[] flds = ojClass.getFields();
		morphBankDwcQNames = new Vector<QName>();
		for (Field fld : flds)
		{
			int mod = fld.getModifiers();
			if (fld.getType().equals(javax.xml.namespace.QName.class)
					&& Modifier.isFinal(mod) 
					&& Modifier.isStatic(mod)
					&& Modifier.isPublic(mod))
			{
				morphBankDwcQNames.add((QName )fld.get(null));
			}
		}
	}
	
	
	/**
	 * @param spec
	 * @return unique id in the form InstitutionCode-CollectionCodeCatalogNumber
	 * 
	 * If catalognumber is not available then CollectionObjectId is used instead.
	 */
	protected String getSpecId(DarwinCoreSpecimen spec)
	{
		String result = "";
		if (spec.isMapped("InstitutionCode") && spec.isMapped("CollectionCode"))
		{
			result = spec.get("InstitutionCode") + "-" + spec.get("CollectionCode");
		}
		else 
		{
			result = AppContextMgr.getInstance().getClassObject(Institution.class).getCode() + "-" 
				+ AppContextMgr.getInstance().getClassObject(Collection.class).getCode();
		}
		
		if (spec.isMapped("CatalogNumberNumeric"))
		{
			return result + spec.get("CatalogNumberNumeric");
		}
		
		if (spec.isMapped("CatalogNumberText"))
		{
			return result + spec.get("CatalogNumberText");
		}
		
		return result + spec.getCollectionObjectId();
	}
	
	
	public void setXmlSpecimenFields(XmlBaseObject xmlSpec) throws Exception
	{
		xmlSpec.setId(MapFsuHerbSpreadsheetToXml.getXmlExternalId(getSpecId(spec)));
		
		if (spec.isMapped("ScientificName"))
		{
			if (spec.isMapped("ScientificNameAuthor"))
			{
				xmlSpec.setDetermination(
						MapFsuHerbSpreadsheetToXml.getXmlExternalId(
								XmlTaxonNameUtilities.getTaxonSciNameAuthorExtId((String )spec.get("ScientificName"), 
										(String )spec.get("ScientificNameAuthor"))));				
			}
			else
			{
				xmlSpec.setDetermination(
						MapFsuHerbSpreadsheetToXml.getXmlExternalId(XmlUtils.SCI_NAME_PREFIX + spec.get("ScientificName")));				
			}
		}
		
		
//		spec.setCollectionObjectId(collectionObjectId);
//		for (Pair<String, Object> fld : spec.getFieldValues())
//		{
//			return new JAXBElement<String>(_InstitutionCode_QNAME, String.class,
//					null, value);
//		}
	}

	/**
	 * @param stmt
	 * @return
	 * @throws Exception
	 * 
	 * Gets the images associated with the current specimen.
	 * This implementation does not use the hibernate object.
	 */
	protected ResultSet getImages(Statement stmt) throws Exception
	{
		String sql = "select at.AttachmentLocation, at.CopyrightHolder, at.OrigFilename, at.Title, coat.remarks, coat.ordinal "
			+ "from collectionobjectattachment coat inner join attachment at on at.AttachmentID = coat.AttachmentID where "
			+ "coat.CollectionObjectID = " + collectionObjectId;
		return stmt.executeQuery(sql);
	}
	
	protected void setXmlImageField(XmlBaseObject image)
	{
		
	}
	
	public Vector<XmlBaseObject> getXmlImages() throws Exception
	{
			Statement stmt = null;
			ResultSet images = null;
			Vector<XmlBaseObject> result = new Vector<XmlBaseObject>();
			try
			{
				stmt = connection.createStatement();
				images = getImages(stmt);
				while (images.next())
				{
					XmlBaseObject xmlImage = new XmlBaseObject("Image");
					xmlImage.addDescription("From specimen " + getCollectionObjectId());
					setXmlImageField(xmlImage);
					result.add(xmlImage);
				}
		/*xmlImage.setId(MapFsuHerbSpreadsheetToXml.getImageId(image));
		xmlImage.setSpecimen(MapFsuHerbSpreadsheetToXml.getSpecimenId(image));
		xmlImage.getView().add(MapFsuHerbSpreadsheetToXml.getViewId(image));

		String originalFileName = image.getValue("OriginalFileName");
		xmlImage.setOriginalFileName(originalFileName);
		xmlImage.addUserProperty("imageUrl", image.getValue("ImageURL"));
		xmlImage.setImageType(getImageType(originalFileName));
		xmlImage.setPhotographer(image.getValue("Photographer"));
		xmlImage.setCopyrightText(image.getValue("Copyright"));
		*/
		//TODO add user properties and ext links as necessary
			} finally
			{
				if (images != null) images.close();
				if (stmt != null) stmt.close();
			}
			return result;
	}
	
	/**
	 * @return the collectionObjectId
	 */
	public Integer getCollectionObjectId() 
	{
		return collectionObjectId;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		

	}

}
