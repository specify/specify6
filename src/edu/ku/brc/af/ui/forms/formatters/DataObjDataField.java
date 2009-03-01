/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.af.ui.forms.formatters;

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;

/**
 * This describes a single field that is used as part of a DataObjectFormatter.
 * The fields are appended together to construct the final value.
 * 
 * @author rods
 * 
 * @code_status Complete.
 * 
 * Created Date: Jan 17, 2007
 * 
 */
public class DataObjDataField implements Cloneable
{
	protected String                 name;
	protected Class<?>               type;
	protected String                 format;
	protected String                 sep;
	protected String                 uiFieldFormatterName;
	protected String                 dataObjFormatterName;
	protected DBTableInfo            tableInfo;
	protected DBFieldInfo            fieldInfo;
	protected DBRelationshipInfo     relInfo;
	protected DataObjSwitchFormatter objFormatter;
	protected UIFieldFormatterIFace  uiFieldFormatter;
	

	public DataObjDataField(final String name, 
	                        final Class<?> type,
			                final String format, 
			                final String sep,
			                final String dataObjFormatterName, 
			                final String uiFieldFormatterName) {
		super();

		this.name = name;
		this.type = type;
		this.format = format;
		this.sep = sep;
		this.dataObjFormatterName = dataObjFormatterName;
		this.uiFieldFormatterName = uiFieldFormatterName;

		// table info is set during parent (DataObjDataFieldFormat) construction
	}

	public void setDbInfo(DBTableInfo tableInfo, DBFieldInfo fieldInfo, DBRelationshipInfo relInfo)
	{
		this.tableInfo = tableInfo;
		this.fieldInfo = fieldInfo;
		this.relInfo   = relInfo;
	}

	public void setObjFormatter(DataObjSwitchFormatter objFormatter)
    {
        this.objFormatter = objFormatter;
    }

    public UIFieldFormatterIFace getUiFieldFormatter()
	{
		return uiFieldFormatter;
	}

	public void setUiFieldFormatter(UIFieldFormatterIFace uiFieldFormatter)
	{
		this.uiFieldFormatter = uiFieldFormatter;
	}
	
	public void setSep(String sep)
	{
		this.sep = sep;
	}

	public boolean isPureField()
	{
		return (StringUtils.isEmpty(getUiFieldFormatterName()) && 
				StringUtils.isEmpty(getDataObjFormatterName()));
	}
	
	/**
	 * @param sb
	 */
	public void toXML(final StringBuilder sb)
	{
	    String spaces = "                ";
        sb.append(spaces);
        sb.append("<field");

        // omit type for Strings
        if (type != null && type != String.class)
        {
            xmlAttr(sb, "type", DataObjFieldFormatMgr.getInstance().getStrForType(type));
        }
        
        xmlAttr(sb, "format",    format);
        xmlAttr(sb, "sep",       sep);
        xmlAttr(sb, "formatter", dataObjFormatterName);
        xmlAttr(sb, "uifieldformatter", uiFieldFormatterName);
        sb.append(">");
        sb.append(name);
        sb.append("</field>\n");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() 
	{
		String prefix = "";
		if (StringUtils.isNotEmpty(sep))
		{
			prefix = sep;
		}

		if (objFormatter != null)
        {
            return prefix + "[" + objFormatter.getTitle() + "." + objFormatter.getFieldName() + "]";
        }
        
		return prefix + "[" + tableInfo.getTitle() + "." + fieldInfo.getTitle() + "]";
	}

	public String getFormat() {
		return format;
	}

	public String getName() {
		return name;
	}

	public String getSep() {
		return sep;
	}

	public Class<?> getType() {
		return type;
	}

	public String getDataObjFormatterName() {
		return dataObjFormatterName;
	}

	/**
	 * @return the uiFieldFormatter
	 */
	public String getUiFieldFormatterName() {
		return uiFieldFormatterName;
	}

	public void setUiFieldFormatterName(String uiFieldFormatterName) {
		this.uiFieldFormatterName = uiFieldFormatterName;
	}

	public DBTableInfo getTableInfo() {
		return tableInfo;
	}

	public void setTableInfo(DBTableInfo tableInfo) {
		this.tableInfo = tableInfo;
	}

	public DBRelationshipInfo getRelInfo() {
		return relInfo;
	}

	public void setRelInfo(DBRelationshipInfo relInfo) {
		this.relInfo = relInfo;
	}

	public DBFieldInfo getFieldInfo() {
		return fieldInfo;
	}

	public void setFieldInfo(DBFieldInfo fieldInfo) {
		this.fieldInfo = fieldInfo;
	}
	
	/**
	 * @param tableInfo
	 */
	public void setTableAndFieldInfo(final DBTableInfo tableInfo)
	{
		setTableInfo(tableInfo);

		if (StringUtils.isNotEmpty(dataObjFormatterName))
		{
			// XXX is it ok to get this info from static instance instead of cached version?
			objFormatter = DataObjFieldFormatMgr.getInstance().getFormatter(dataObjFormatterName);
		}
		
		String[] parts = name.split("\\.");
		if (parts.length == 2)
		{
			// there's a dot on field name, which means it represents a field in a related table
			// split name into relation.fieldName
			setRelInfo(tableInfo.getRelationshipByName(parts[0]));
			DBTableInfo otherTable = DBTableIdMgr.getInstance().getByClassName(relInfo.getClassName());
	        setFieldInfo(otherTable.getFieldByName(parts[1]));
			return;
		}

		// else 
		setRelInfo(tableInfo.getRelationshipByName(name));
		if (relInfo != null)
		{
			// relationship was not mentioned (lacked the dot) but it is one anyway
			// get field info from relationship table
			return;
		}
		
		// else
		setFieldInfo(tableInfo.getFieldByName(name));
	}
	
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        DataObjDataField ddf = (DataObjDataField)super.clone();
        return ddf;
    }
}