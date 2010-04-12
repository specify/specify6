/**
 * 
 */
package edu.ku.brc.specify.tools.export;

import org.dom4j.Element;

import edu.ku.brc.helpers.XMLHelper;

/**
 * @author timo
 *
 */
public class MappedFieldInfo 
{
	protected final String stringId;
	protected final String fieldName;
	protected final String tableIds;
	protected final boolean isRel;
	protected final boolean isActive;
	
	public MappedFieldInfo(String stringId, String fieldName, String tableIds, boolean isRel, boolean isActive) {
		super();
		this.stringId = stringId;
		this.fieldName = fieldName;
		this.tableIds = tableIds;
		this.isRel = isRel;
		this.isActive = isActive;
	}

	public MappedFieldInfo(String stringId, String fieldName, String tableIds, boolean isRel) 
	{
		this(stringId, fieldName, tableIds, isRel, true);
	}

	public MappedFieldInfo(Element def)
	{
		this(XMLHelper.getAttr(def, "specify_field", null), 
				XMLHelper.getAttr(def, "fieldname", null),
				XMLHelper.getAttr(def, "table_path", null),
				XMLHelper.getAttr(def, "is_relationship", false),
				XMLHelper.getAttr(def, "active", true));
	}
	
	public String getStringId() 
	{
		return stringId;
	}

	public String getFieldName() 
	{
		return fieldName;
	}

	public String getTableIds() 
	{
		return tableIds;
	}
	
	public boolean isRel()
	{
		return isRel;
	}

	public boolean isActive()
	{
		return isActive;
	}

	public String toXML()
	{
		return "specify_field=\"" + stringId + "\" fieldname=\"" + fieldName + "\" table_path=\""
			+ tableIds + "\" is_relationship=\"" + (isRel ? "true" : "false") + "\" active=\""
			+ (isActive ? "true" : "false") + "\""; 
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
