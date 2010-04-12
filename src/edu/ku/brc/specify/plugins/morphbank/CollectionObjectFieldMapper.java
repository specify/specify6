/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.Vector;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import net.morphbank.mbsvc3.fsuherb.MapFsuHerbSpreadsheetToXml;
import net.morphbank.mbsvc3.xml.ObjectFactory;
import net.morphbank.mbsvc3.xml.XmlBaseObject;
import net.morphbank.mbsvc3.xml.XmlId;
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
	 * 
	 * @param spec
	 * @return unique id in the form InstitutionCode-CollectionCodeCatalogNumber
	 * 
	 * If catalognumber is not available then CollectionObjectId is used instead.
	 */
	protected String getSpecId(DarwinCoreSpecimen spec, boolean isImage)
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
		
		if (isImage)
		{
			result += "-I";
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
	
	/**
	 * @param dwcType
	 * @return
	 */
	protected Class<?> getClassForDwcType(MappingInfo mi)
	{
		String dwcType = mi.getDataType();
		
		if (dwcType == null)
		{
			//Some concepts don't have type is spexportschemaitem - possibly an import problem or a problem with our .xsd files??
			String name = mi.getName();
			if (name.startsWith("DecimalLatitude") || name.equals("DecimalLongitude"))
			{
				return Double.class;
			}
			if (name.endsWith("Collected") || name.endsWith("Identified"))
			{
				return Integer.class;
			}
			
			return String.class;
		}
		
		if (dwcType.equals("xsd:string"))
		{
			return String.class;
		}
		if (dwcType.equals("xsd:dateTime"))
		{
			return Date.class;
		}
		if (dwcType.equals("xsd:decimal"))
		{
			return Double.class;
		}
		if (dwcType.equals("xsd:nonNegativeInteger"))
		{
			return Integer.class;
		}
		if (dwcType.equals("xsd:gYear"))
		{
			return Integer.class;
		}
		return null;
	}
	
	/**
	 * @param xmlSpec
	 */
	protected void setDwcSpecimenFields(XmlBaseObject xmlSpec) throws Exception
	{
		Class<?> factory = ObjectFactory.class;
		ObjectFactory objFac = new ObjectFactory();
		for (int c = 0; c < dwcMapper.getConceptCount(); c++)
		{
			MappingInfo mi = dwcMapper.getConcept(c);
			Class<?> dataType = getClassForDwcType(mi);
			if (dataType == null)
			{
				System.out.println("CollectionObjectMapper:setDwcSpecimenFields: skipping " + mi.getName() + ": unrecognized data type.");
				continue;
			}
			try
			{
				if (mi.getName().equals("CatalogNumberNumeric") && dataType.equals(Integer.class))
				{
					dataType = Double.class;
				}
				Method m = factory.getMethod("create" + mi.getName(), dataType);
				System.out.println("invoking " + m.getName() + "(" + spec.get(mi.getName()) + ")");
				xmlSpec.addDarwinTag((JAXBElement<?> )m.invoke(objFac, dataType.cast(spec.get(mi.getName()))));
			} catch(NoSuchMethodException ex)
			{
				System.out.println("CollectionObjectMapper:setDwcSpecimenFields: skipping " + mi.getName() + ": no create method in Object Factory");
				continue;
			}
		}
	}
	
	/**
	 * @param xmlSpec
	 * @throws Exception
	 */
	public void setXmlSpecimenFields(XmlBaseObject xmlSpec) throws Exception
	{
		xmlSpec.setId(MapFsuHerbSpreadsheetToXml.getXmlExternalId(getSpecId(spec, false)));
		
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
		
		setDwcSpecimenFields(xmlSpec);
		
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
	protected Vector<AttachmentRecord> getImages() throws Exception
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			String sql = "select at.AttachmentLocation, at.CopyrightHolder, at.CopyrightDate, at.MimeType, at.Credit, at.OrigFilename, at.Title, coat.remarks, coat.ordinal "
				+ "from collectionobjectattachment coat inner join attachment at on at.AttachmentID = coat.AttachmentID where "
				+ "at.MimeType like 'image/%' and coat.CollectionObjectID = " + collectionObjectId;
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			Vector<AttachmentRecord> result = new Vector<AttachmentRecord>();
			while (rs.next())
			{
				result.add(new AttachmentRecord(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), 
						rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getInt(9)));
			}
			return result;
		} finally
		{
			if (rs != null) rs.close();
			if (stmt != null) stmt.close();
		}
	}
	
	/**
	 * @param imageAttachment
	 * @return
	 */
	protected XmlId getImageId(AttachmentRecord imageAttachment) {
		String idStr = getSpecId(spec, true) + "." + imageAttachment.getOrdinal();
		if (idStr == null || idStr.length() == 0) return null;
		return MapFsuHerbSpreadsheetToXml.getXmlExternalId(idStr);
	}

	/**
	 * @param mimeType
	 * @return
	 */
	protected String getImageType(String mimeType)
	{
		if (mimeType == null)
		{
			return null;
		}
		
		return mimeType.replace("image/", "");
	}
	
	/**
	 * @param image
	 * @param imageAttachment
	 */
	protected void setXmlImageFields(XmlBaseObject image, AttachmentRecord imageAttachment)
	{
		image.setId(getImageId(imageAttachment));
		
		image.setSpecimen(MapFsuHerbSpreadsheetToXml.getXmlExternalId(getSpecId(spec, false)));
		//??? image.getView().add(MapFsuHerbSpreadsheetToXml.getViewId(image));
		image.setOriginalFileName(imageAttachment.getOrigFileName());
		
		String copyrightText = imageAttachment.getCopyrightHolder();
		if (imageAttachment.getCopyrightDate() != null)
		{
			if (copyrightText != null)
			{
				copyrightText += " " + imageAttachment.getCopyrightDate();
			} else 
			{
				copyrightText = imageAttachment.getCopyrightDate();
			}
		}
		image.setCopyrightText(copyrightText);
		
		image.setImageType(getImageType(imageAttachment.getMimeType()));
		
		image.setPhotographer(imageAttachment.getCredit());
		
		//TODO add user properties and ext links as necessary
		
		
		/*xmlImage.getView().add(MapFsuHerbSpreadsheetToXml.getViewId(image));
		String originalFileName = image.getValue("OriginalFileName");
		xmlImage.setOriginalFileName(originalFileName);
		xmlImage.addUserProperty("imageUrl", image.getValue("ImageURL"));
		xmlImage.setImageType(getImageType(originalFileName));
		xmlImage.setPhotographer(image.getValue("Photographer"));
		xmlImage.setCopyrightText(image.getValue("Copyright"));
		*/
	}
	
	public Vector<XmlBaseObject> getXmlImages() throws Exception
	{
		Vector<XmlBaseObject> result = new Vector<XmlBaseObject>();
		Vector<AttachmentRecord> images = getImages();
		for (AttachmentRecord image : images)
		{
			XmlBaseObject xmlImage = new XmlBaseObject("Image");
			xmlImage.addDescription("From specimen " + getCollectionObjectId());
			setXmlImageFields(xmlImage, image);
			result.add(xmlImage);
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

	private class AttachmentRecord
	{
		private String attachmentLocation;
		private String copyrightHolder;
		private String copyrightDate;
		private String mimeType;
		private String credit;
		private String origFileName;
		private String title;
		private String remarks;
		private Integer ordinal;
		/**
		 * @param attachmentLocation
		 * @param copyrightHolder
		 * @param origFileName
		 * @param title
		 * @param remarks
		 * @param ordinal
		 */
		public AttachmentRecord(String attachmentLocation,
				String copyrightHolder, String copyrightDate, String mimeType, String credit, String origFileName, String title,
				String remarks, int ordinal)
		{
			super();
			this.attachmentLocation = attachmentLocation;
			this.copyrightHolder = copyrightHolder;
			this.copyrightDate = copyrightDate;
			this.mimeType = mimeType;
			this.credit = credit;
			this.origFileName = origFileName;
			this.title = title;
			this.remarks = remarks;
			this.ordinal = ordinal;
		}
		/**
		 * @return the attachmentLocation
		 */
		public String getAttachmentLocation()
		{
			return attachmentLocation;
		}
		/**
		 * @param attachmentLocation the attachmentLocation to set
		 */
		public void setAttachmentLocation(String attachmentLocation)
		{
			this.attachmentLocation = attachmentLocation;
		}
		/**
		 * @return the copyrightHolder
		 */
		public String getCopyrightHolder()
		{
			return copyrightHolder;
		}
		/**
		 * @param copyrightHolder the copyrightHolder to set
		 */
		public void setCopyrightHolder(String copyrightHolder)
		{
			this.copyrightHolder = copyrightHolder;
		}
		/**
		 * @return the origFileName
		 */
		public String getOrigFileName()
		{
			return origFileName;
		}
		/**
		 * @param origFileName the origFileName to set
		 */
		public void setOrigFileName(String origFileName)
		{
			this.origFileName = origFileName;
		}
		/**
		 * @return the title
		 */
		public String getTitle()
		{
			return title;
		}
		/**
		 * @param title the title to set
		 */
		public void setTitle(String title)
		{
			this.title = title;
		}
		/**
		 * @return the remarks
		 */
		public String getRemarks()
		{
			return remarks;
		}
		/**
		 * @param remarks the remarks to set
		 */
		public void setRemarks(String remarks)
		{
			this.remarks = remarks;
		}
		/**
		 * @return the ordinal
		 */
		public Integer getOrdinal()
		{
			return ordinal;
		}
		/**
		 * @param ordinal the ordinal to set
		 */
		public void setOrdinal(Integer ordinal)
		{
			this.ordinal = ordinal;
		}
		/**
		 * @return the copyrightDate
		 */
		public String getCopyrightDate()
		{
			return copyrightDate;
		}
		/**
		 * @param copyrightDate the copyrightDate to set
		 */
		public void setCopyrightDate(String copyrightDate)
		{
			this.copyrightDate = copyrightDate;
		}
		/**
		 * @return the mimeType
		 */
		public String getMimeType()
		{
			return mimeType;
		}
		/**
		 * @param mimeType the mimeType to set
		 */
		public void setMimeType(String mimeType)
		{
			this.mimeType = mimeType;
		}
		/**
		 * @return the credit
		 */
		public String getCredit()
		{
			return credit;
		}
		/**
		 * @param credit the credit to set
		 */
		public void setCredit(String credit)
		{
			this.credit = credit;
		}
		
		
	}
}
