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
import java.util.Vector;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import net.morphbank.mbsvc3.fsuherb.MapFsuHerbSpreadsheetToXml;
import net.morphbank.mbsvc3.xml.ObjectFactory;
import net.morphbank.mbsvc3.xml.XmlBaseObject;
import net.morphbank.mbsvc3.xml.XmlId;
import net.morphbank.mbsvc3.xml.XmlUtils;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.AttachmentImageAttribute;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;

/**
 * @author timo
 *
 * @code_status Alpha
 *
 * Apr 7, 2010
 */
public class CollectionObjectFieldMapper
{
	private static final Logger  log = Logger.getLogger(CollectionObjectFieldMapper.class);	
	
	protected CollectionObject collectionObject;
	protected Integer collectionObjectId;
	protected DwcMapper dwcMapper;
	protected Vector<QName> morphBankDwcQNames;
	protected DarwinCoreSpecimen spec;
	protected RedactorIFace redactor = new NonRedactor();
	//protected RedactorIFace redactor = new RedactorAuburn();

	public static Connection connection; //for testing

	protected Connection getConnection()
	{
		if (connection ==  null)
		{
			return DBConnection.getInstance().createConnection();
		}
		else return connection;
	}
	
	/**
	 * @param collectionObjectId
	 * @throws Exception
	 */
	public CollectionObjectFieldMapper(Integer collectionObjectId, final Integer dwcMappingId) throws Exception
	{
		this.collectionObjectId = collectionObjectId;
		buildQNames();
		dwcMapper = getDwcMapper(dwcMappingId);
		spec = new DarwinCoreSpecimen(dwcMapper);
		spec.setCollectionObjectId(collectionObjectId);	
	}
	
	/**
	 * @param obj
	 * @throws Exception
	 */
	public CollectionObjectFieldMapper(final CollectionObject obj, final Integer dwcMappingId) throws Exception
	{
		this.collectionObject = obj;
		dwcMapper = getDwcMapper(dwcMappingId);
		spec = new DarwinCoreSpecimen(dwcMapper);
		spec.setCollectionObject(obj);
	}

	/**
	 * @param collectionObjectId
	 * @param dwcMapper
	 * @throws Exception
	 */
	public CollectionObjectFieldMapper(Integer collectionObjectId, final DwcMapper dwcMapper) throws Exception
	{
		this.collectionObjectId = collectionObjectId;
		buildQNames();
		this.dwcMapper = dwcMapper;
		spec = new DarwinCoreSpecimen(dwcMapper);
		spec.setCollectionObjectId(collectionObjectId);	
	}
	/**
	 * @param collectionObjectId
	 * @throws Exception
	 */
	public void setCollectionObjectId(Integer collectionObjectId) throws Exception
	{
		this.collectionObjectId = collectionObjectId;
		spec.setCollectionObjectId(collectionObjectId);
	}
	
	/**
	 * @param obj
	 * @throws Exception
	 */
	public void setCollectionObject(final CollectionObject obj) throws Exception
	{
		this.collectionObject = obj;
		spec.setCollectionObject(obj);
	}
	
	/**
	 * @return the DarwinCore appropriate mappings for the current context.
	 * 
	 * Possibly lots and lots of work to do here.
	 */
	protected DwcMapper getDwcMapper(final Integer mappingId)
	{
		if (mappingId != null)
		{
			return new DwcMapper(mappingId);
		}
		return new DwcMapper();
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
	
	public Object getSpecNumber(DarwinCoreSpecimen aSpec)
	{
		DarwinCoreSpecimen dws = aSpec == null ? spec : aSpec;
		if (dws.isMapped("CatalogNumberNumeric"))
		{
			return dws.get("CatalogNumberNumeric");
		}		
		if (dws.isMapped("CatalogNumberText"))
		{
			return dws.get("CatalogNumberText");
		}
		if (dws.isMapped("CatalogNumber"))
		{
			return dws.get("CatalogNumber");
		}
	
		return dws.getCollectionObjectId();
	}
	/**
	 * 
	 * @param spec
	 * @return unique id in the form InstitutionCode-CollectionCodeCatalogNumber
	 * 
	 * 
	 * If catalognumber is not available then CollectionObjectId is used instead.
	 * 
	 * NOTE: This is specifically designed to meet the ID requirements for
	 * the Troy database! 
	 */
	protected String getSpecId(DarwinCoreSpecimen spec, boolean isImage)
	{
		String result = "";
		if (!isImage)
		{
			if (spec.isMapped("InstitutionCode") && spec.isMapped("CollectionCode"))
			{
				result = spec.get("InstitutionCode") + "-" + spec.get("CollectionCode");
			}
			else 
			{
				result = AppContextMgr.getInstance().getClassObject(Institution.class).getCode()
			 		+ "-" 
					+ AppContextMgr.getInstance().getClassObject(Collection.class).getCode()
					+ "-";
			}
		}
		
		if (isImage)
		{
			result += "I:"; 		
		}
		
		return result + getSpecNumber(spec);
		
		/* for auburn 
		String result = "";
		if (!isImage)
		{
			if (spec.isMapped("InstitutionCode")) //&& spec.isMapped("CollectionCode"))
			{
				//result = spec.get("InstitutionCode") + "-" + spec.get("CollectionCode");
				//result = (String )spec.get("InstitutionCode") + "-" + getSpecNumber(spec);
				//Auburn...The BarCode has been stashed in the CollectionCode field in the cache!
				result = (String )spec.get("InstitutionCode") + ":" + BasicSQLUtils.querySingleObj(connection, "select CollectionCode from tdwgtermsa where tdwgtermsaid = "
								+ spec.getCollectionObjectId());
			}
			else 
			{
				result = AppContextMgr.getInstance().getClassObject(Institution.class).getCode()
			 		+ "-" + getSpecNumber(spec);// + "-" 
					//+ AppContextMgr.getInstance().getClassObject(Collection.class).getCode();
			}
		}
		
		if (isImage)
		{
			//XXX HACK! The BarCode has been stashed in the CollectionCode field in the cache!
			result = spec.get("InstitutionCode") + "-I:" + BasicSQLUtils.querySingleObj(connection, "select CollectionCode from tdwgtermsa where tdwgtermsaid = "
					+ spec.getCollectionObjectId());
			
		}
		return result; // + getSpecNumber(spec);
		-- end auburn */
		

	}
	
	
	/**
	 * @param mi
	 * @return concept name that works for net.morphbank.mbsvc3.xml.ObjectFactory
	 */
	protected String getMiName(MappingInfo mi)
	{
		if (mi.getName().equals("ContinentOcean"))
		{
			return "Continent";
		}
		if (mi.getName().equals("ScientificNameAuthor"))
		{
			return "AuthorYearOfScientificName";
		}
		return mi.getName();
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
			Class<?> dataType = mi.getDataType();
			if (dataType == null)
			{
				log.warn("CollectionObjectMapper:setDwcSpecimenFields: skipping " + mi.getName() + ": unrecognized data type.");
				continue;
			}
			Object val = spec.get(mi.getName());
			try
			{
				if (redactor.isRedacted(spec, mi))
				{
					//just skip. don't even add a tag for the concept.
					System.out.println(getSpecId(spec, false));
					continue; 
				}
				//System.out.println("setting " + mi.getName() + ": " + val + " (" + dataType.getSimpleName() + ")");
				String miName = getMiName(mi);
				if (miName.equalsIgnoreCase("CatalogNumberNumeric") || miName.equalsIgnoreCase("DecimalLatitude") || miName.equalsIgnoreCase("DecimalLongitude"))
				{
					dataType = Double.class;
					if (val != null)
					{
						val = Double.valueOf(val.toString());
					}
				} 
//				else if (miName.equals("CatalogNumberNumeric"))
//				{
//					dataType = Integer.class;
//					if (val != null)
//					{
//						val = Integer.valueOf(val.toString());
//					}
//				}
//				else if (val != null && Number.class.isAssignableFrom(dataType) && !dataType.equals(GregorianCalendar.class))
//				{
//					val = ((Number )val).doubleValue();
//				}
				Method m = factory.getMethod("create" + StringUtils.capitalize(miName), dataType);
				//System.out.println("invoking " + m.getName() + "(" + val + ")");
				try
				{
					xmlSpec.addDarwinTag((JAXBElement<?> )m.invoke(objFac, dataType.cast(val)));
				} catch (ClassCastException ex)
				{
					xmlSpec.addDarwinTag((JAXBElement<?> )m.invoke(objFac, dataType.cast(val == null ? "" : val.toString())));
				}
			} catch(NoSuchMethodException ex)
			{
				log.warn("CollectionObjectMapper:setDwcSpecimenFields: skipping " + mi.getName() + ": no create method in Object Factory");
				xmlSpec.addUserProperty(mi.getName(), val.toString());
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
			String sciNameAuthor = spec.isMapped("ScientificNameAuthor") ? (String )spec.get("ScientificNameAuthor") : null;
			if (sciNameAuthor == null)
			{
				 sciNameAuthor = spec.isMapped("AuthorYearOfScientificName") ? (String)spec.get("AuthorYearOfScientificName") : null;
			}
			if (StringUtils.isNotBlank(sciNameAuthor))
			{
				//xmlSpec.setDetermination(
				//		MapFsuHerbSpreadsheetToXml.getXmlExternalId(
				//				XmlTaxonNameUtilities.getTaxonSciNameAuthorExtId((String )spec.get("ScientificName"), 
				//						(String )spec.get("ScientificNameAuthor"))));				
				xmlSpec.setDetermination(
						MapFsuHerbSpreadsheetToXml.getXmlExternalId(XmlUtils.SCI_NAME_AUTHOR_PREFIX + spec.get("ScientificName") + "|" + sciNameAuthor));				
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
			String sql = "select at.AttachmentLocation, at.CopyrightHolder, at.CopyrightDate, at.MimeType, at.Credit, at.OrigFilename, " +
					"at.Title, atia.height, atia.width, atia.resolution, atia.magnification, atia.creativeCommons, coat.remarks, coat.ordinal "
				+ "from collectionobjectattachment coat inner join attachment at on at.AttachmentID = coat.AttachmentID left join "
				+ "attachmentimageattribute atia on atia.attachmentimageattributeid = at.attachmentimageattributeid where "
				+ "at.MimeType like 'image/%' and coat.CollectionObjectID = " + collectionObjectId;
			stmt = getConnection().createStatement();
			rs = stmt.executeQuery(sql);
			Vector<AttachmentRecord> result = new Vector<AttachmentRecord>();
			while (rs.next())
			{
				result.add(new AttachmentRecord(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), 
						rs.getString(5), rs.getString(6), rs.getString(7), rs.getInt(8), rs.getInt(9), 
						rs.getDouble(10), rs.getDouble(11), rs.getString(12), rs.getString(13), rs.getInt(14)));
			}
			return result;
		} finally
		{
			if (rs != null) rs.close();
			if (stmt != null) stmt.close();
		}
	}
	
	/**
	 * @param imageObj
	 * @return
	 */
	protected AttachmentRecord getImage(ObjectAttachmentIFace<?> imageObj)
	{
		Attachment at = imageObj.getAttachment();
//		return null;
		AttachmentImageAttribute atia = at.getAttachmentImageAttribute();
		return new AttachmentRecord(at.getAttachmentLocation(),
				at.getCopyrightHolder(),
				at.getCopyrightDate(),
				at.getMimeType(),
				at.getCredit(),
				at.getOrigFilename(),
				at.getTitle(),
				atia != null ? atia.getHeight() : null,
				atia != null ? atia.getWidth() : null,
				atia != null ? atia.getResolution() : null,
				atia != null ? atia.getMagnification() : null,
				atia != null ? atia.getCreativeCommons() : null,
				imageObj.getRemarks(),
				imageObj.getOrdinal());
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
	
	/**
	 * @return
	 * @throws Exception
	 */
	public Vector<XmlBaseObject> getXmlImages() throws Exception
	{
		Vector<XmlBaseObject> result = new Vector<XmlBaseObject>();
		Vector<AttachmentRecord> images = getImages();
		for (AttachmentRecord image : images)
		{
			XmlBaseObject xmlImage = new XmlBaseObject("Image");
			xmlImage.addDescription("From specimen " + getSpecNumber(spec));
			//xmlImage.addUserProperty("imageUrl", "http://www.specimenimaging.com/images/AUA/" + image.getOrigFileName());
			//xmlImage.setDateToPublish(new Date("2012/09/01"));
			setXmlImageFields(xmlImage, image);
			result.add(xmlImage);
		}
		return result;
	}
	
	/**
	 * @param image
	 * @return
	 */
	public XmlBaseObject getXmlImage(ObjectAttachmentIFace<?> image)
	{
		if (!(image.getObject() instanceof CollectionObject))
		{
			throw new NotImplementedException("CollectionObjectFieldMapper: attachment type not supported: " + image.getClass().getName());
		}
		AttachmentRecord imageRec = getImage(image);
		XmlBaseObject xmlImage = new XmlBaseObject("Image");
		xmlImage.addDescription("From specimen " + getSpecNumber(spec));
		setXmlImageFields(xmlImage, imageRec);
		return xmlImage;
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

	@SuppressWarnings("unused")
	private class AttachmentRecord
	{
		private String attachmentLocation;
		private String copyrightHolder;
		private String copyrightDate;
		private String mimeType;
		private String credit;
		private String origFileName;
		private String title;
		private Integer height;
		private Integer width;
		private Double resolution;
		private Double magnification;
		private String creativeCommons;
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
				String copyrightHolder, String copyrightDate, String mimeType, String credit, 
				String origFileName, String title, Integer height, Integer width, Double resolution,
				Double magnification, String creativeCommons,
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
			this.height = height;
			this.width = width;
			this.resolution = resolution;
			this.magnification = magnification;
			this.creativeCommons = creativeCommons;
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
		/**
		 * @return the height
		 */
		public Integer getHeight()
		{
			return height;
		}
		/**
		 * @param height the height to set
		 */
		public void setHeight(Integer height)
		{
			this.height = height;
		}
		/**
		 * @return the width
		 */
		public Integer getWidth()
		{
			return width;
		}
		/**
		 * @param width the width to set
		 */
		public void setWidth(Integer width)
		{
			this.width = width;
		}
		/**
		 * @return the resolution
		 */
		public Double getResolution()
		{
			return resolution;
		}
		/**
		 * @param resolution the resolution to set
		 */
		public void setResolution(Double resolution)
		{
			this.resolution = resolution;
		}
		/**
		 * @return the magnification
		 */
		public Double getMagnification()
		{
			return magnification;
		}
		/**
		 * @param magnification the magnification to set
		 */
		public void setMagnification(Double magnification)
		{
			this.magnification = magnification;
		}
		/**
		 * @return the creativeCommons
		 */
		public String getCreativeCommons()
		{
			return creativeCommons;
		}
		/**
		 * @param creativeCommons the creativeCommons to set
		 */
		public void setCreativeCommons(String creativeCommons)
		{
			this.creativeCommons = creativeCommons;
		}
		
		
	}
}
