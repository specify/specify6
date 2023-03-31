/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.af.ui.forms.formatters;

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

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
    private static final Logger log = Logger.getLogger(DataObjDataField.class);
    
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

		this.name                 = name;
		this.type                 = type;
		this.format               = format;
		this.sep                  = sep;
		this.dataObjFormatterName = dataObjFormatterName;
		this.uiFieldFormatterName = uiFieldFormatterName;

		// table info is set during parent (DataObjDataFieldFormat) construction
	}

    public void setDbInfo(DBTableInfo tableInfo, DBFieldInfo fieldInfo, DBRelationshipInfo relInfo, boolean isInitial)
    {
        this.tableInfo = tableInfo;
        this.fieldInfo = fieldInfo;
        this.relInfo   = relInfo;
        
        if (!isInitial && fieldInfo == null && relInfo == null)
        {
            setTableAndFieldInfo(tableInfo);
        }
    }

    public void setDbInfo(DBTableInfo tableInfo, DBFieldInfo fieldInfo, DBRelationshipInfo relInfo)
    {
        setDbInfo(tableInfo, fieldInfo, relInfo, false);
    }

	public DataObjSwitchFormatter getObjFormatter()
	{
		return objFormatter;
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
        //this is a very specific fix for bug #185. Probably should add general xml special char handling to xml helper class...
        xmlAttr(sb, "sep",
				sep == null ? null : sep.replace("&","&amp;").replace("amp;amp;", "amp;"));
        //...end fix
		//xmlAttr(sb, "sep", sep);
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

		if (StringUtils.isNotEmpty(name)) {
		    return prefix + "[" + name + "]";
		}
		
		if (objFormatter != null)
        {
            return prefix + "[" + objFormatter.getTitle() + "." + objFormatter.getFieldName() + "]";
        }
        
		return prefix + "[" + tableInfo.getTitle() + "." + fieldInfo.getTitle() + "]";
	}

	public String getFormat()
    {
        return format;
    }

    public String getName()
    {
        return name;
    }

	public String getSep() 
	{
		return sep;
	}

	public Class<?> getType() 
	{
		return type;
	}

	public String getDataObjFormatterName() 
	{
		return dataObjFormatterName;
	}

	/**
	 * @return the uiFieldFormatter
	 */
	public String getUiFieldFormatterName() 
	{
		return uiFieldFormatterName;
	}

	public void setUiFieldFormatterName(String uiFieldFormatterName) 
	{
		this.uiFieldFormatterName = uiFieldFormatterName;
	}

	public DBTableInfo getTableInfo() 
	{
		return tableInfo;
	}

	public void setTableInfo(DBTableInfo tableInfo) 
	{
		this.tableInfo = tableInfo;
	}

	public DBRelationshipInfo getRelInfo() 
	{
		return relInfo;
	}

	public void setRelInfo(DBRelationshipInfo relInfo) 
	{
		this.relInfo = relInfo;
	}

	public DBFieldInfo getFieldInfo() 
	{
		return fieldInfo;
	}

	public void setFieldInfo(DBFieldInfo fieldInfo) 
	{
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
		    if (StringUtils.isNotEmpty(parts[0]) && StringUtils.isNotEmpty(parts[1]))
		    {
    			// there's a dot on field name, which means it represents a field in a related table
    			// split name into relation.fieldName
    		    DBRelationshipInfo reltnInfo = tableInfo.getRelationshipByName(parts[0]);
    		    if (reltnInfo != null)
    		    {
    		        setRelInfo(reltnInfo);
    		        
        			DBTableInfo otherTable = DBTableIdMgr.getInstance().getByClassName(reltnInfo.getClassName());
        			if (otherTable != null)
        			{
        			    DBFieldInfo fi = otherTable.getFieldByName(parts[1]);
        			    if (fi != null)
        			    {
        			        setFieldInfo(fi);
        			    } else
                        {
                            log.error("Couldn't find fieldinfo for ["+parts[1]+"]");
                        }
        			} else
        			{
        			    log.error("Couldn't find tableinfo for ["+reltnInfo.getClassName()+"]");
        			}
    		    } else
    		    {
    		        log.error("Couldn't find Rel Info for ["+parts[0]+"]");
    		    }
		    }
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
