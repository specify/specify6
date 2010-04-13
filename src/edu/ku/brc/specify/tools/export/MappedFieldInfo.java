/**
 * 
 */
package edu.ku.brc.specify.tools.export;

import org.dom4j.Element;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.helpers.XMLHelper;

/**
 * @author timo
 * 
 * @code_status Alpha
 *
 *Information about the specify field side of a [Concept (dwc or other) -> specify field] mapping
 */
public class MappedFieldInfo 
{
	protected final String stringId; //gives unique 'path' from root object to the field
	protected final String fieldName; 
	protected final String tableIds; //list of tableids in the path from root to the field
	protected final boolean isRel; //basically equivalent to is aggregated
	protected final boolean isActive; //should the mapping be automatically applied
	
	/**
	 * @param stringId
	 * @param fieldName
	 * @param tableIds
	 * @param isRel
	 * @param isActive
	 */
	public MappedFieldInfo(String stringId, String fieldName, String tableIds, boolean isRel, boolean isActive) 
	{
		super();
		this.stringId = stringId;
		this.fieldName = fieldName;
		this.tableIds = tableIds;
		this.isRel = isRel;
		this.isActive = isActive;
	}

	/**
	 * @param stringId
	 * @param fieldName
	 * @param tableIds
	 * @param isRel
	 */
	public MappedFieldInfo(String stringId, String fieldName, String tableIds, boolean isRel) 
	{
		this(stringId, fieldName, tableIds, isRel, true);
	}

	/**
	 * @param def
	 */
	public MappedFieldInfo(Element def)
	{
		this(XMLHelper.getAttr(def, "specify_field", null), 
				XMLHelper.getAttr(def, "fieldname", null),
				XMLHelper.getAttr(def, "table_path", null),
				XMLHelper.getAttr(def, "is_relationship", false),
				XMLHelper.getAttr(def, "active", true));
	}
	
	/**
	 * @return the stringId
	 */
	public String getStringId() 
	{
		return stringId;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() 
	{
		return fieldName;
	}

	/**
	 * @return the tableIds
	 */
	public String getTableIds() 
	{
		return tableIds;
	}
	
	/**
	 * @return isRel
	 */
	public boolean isRel()
	{
		return isRel;
	}

	/**
	 * @return isActive
	 */
	public boolean isActive()
	{
		return isActive;
	}

	/**
	 * @return xml def (partial)
	 */
	public String toXML()
	{
		return "specify_field=\"" + stringId + "\" fieldname=\"" + fieldName + "\" table_path=\""
			+ tableIds + "\" is_relationship=\"" + (isRel ? "true" : "false") + "\" active=\""
			+ (isActive ? "true" : "false") + "\""; 
	}

	/**
	 * @return java type of the mapped field
	 */
	public Class<?> getDataType()
	{
		if (isRel)
		{
			return String.class;
		}
		
		Integer tblId = Integer.valueOf(tableIds.substring(tableIds.lastIndexOf(",")+1));
		DBTableInfo tbl = DBTableIdMgr.getInstance().getInfoById(tblId);
		DBFieldInfo fld = tbl.getFieldByName(fieldName);
		return fld.getDataClass();
	}
	
	/**
	 * @return root table id for mapping
	 */
	public int getContextTableId()
	{
		return Integer.parseInt(tableIds.substring(tableIds.indexOf(",")));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "(" + stringId + ", " + fieldName + ", " + tableIds + ", " + isRel + ", " + isActive + ")";
	}
	
}
