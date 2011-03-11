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
package edu.ku.brc.af.ui.forms.validation;

import org.apache.commons.lang.StringUtils;

/**
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jun 1, 2010
 *
 */
public class TypeSearchInfo implements Comparable<TypeSearchInfo>
{
    protected int    tableId;
    protected String name;
    protected String displayColumns;
    protected String searchFieldName;
    protected String format;
    protected String uiFieldFormatterName;
    protected String dataObjFormatterName;
    protected String sqlTemplate = null;
    protected Boolean isSystem;

    /**
     * @param tableId
     * @param name
     * @param displayColumns
     * @param searchFieldName
     * @param format
     * @param uiFieldFormatterName
     * @param dataObjFormatterName
     */
    public TypeSearchInfo(int    tableId,
                          String name,
                          String displayColumns,
                          String searchFieldName,
                          String format,
                          String uiFieldFormatterName,
                          String dataObjFormatterName,
                          Boolean isSystem)
    {
        this.tableId              = tableId;
        this.name                 = name;
        this.displayColumns       = displayColumns;

        this.searchFieldName      = StringUtils.isNotBlank(searchFieldName) ? searchFieldName : null;
        this.format               = StringUtils.isNotBlank(format) ? format : null;
        this.uiFieldFormatterName = StringUtils.isNotBlank(uiFieldFormatterName) ? uiFieldFormatterName : null;
        this.dataObjFormatterName = StringUtils.isNotBlank(dataObjFormatterName) ? dataObjFormatterName : null;
        this.isSystem             = isSystem;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return
     */
    public int getTableId()
    {
        return tableId;
    }

    /**
     * @return
     */
    public String getDisplayColumns()
    {
        return displayColumns;
    }

    /**
     * @return
     */
    public String getFormat()
    {
        return format;
    }

    /**
     * @return the uiFieldFormatterName
     */
    public String getUiFieldFormatterName()
    {
        return uiFieldFormatterName;
    }

    /**
     * @return the dataObjFormatterName
     */
    /**
     * @return
     */
    public String getDataObjFormatterName()
    {
        return dataObjFormatterName;
    }

    public String getSearchFieldName()
    {
        return searchFieldName;
    }

    /**
     * @return the sqlTemplate
     */
    /**
     * @return
     */
    public String getSqlTemplate()
    {
        return sqlTemplate;
    }

    /**
     * @param sqlTemplate the sqlTemplate to set
     */
    public void setSqlTemplate(String sqlTemplate)
    {
        if (StringUtils.isNotBlank(sqlTemplate)) this.sqlTemplate = sqlTemplate;
    }
    
    /**
     * @return the isSystem
     */
    public Boolean isSystem()
    {
        return isSystem;
    }

    /**
     * @param isSystem the isSystem to set
     */
    public void setSystem(Boolean isSystem)
    {
        if (StringUtils.isNotBlank(sqlTemplate)) this.isSystem = isSystem;
    }

    private String getStr(final String str)
    {
        return str == null ? "" : str;
    }
    
    /**
     * @return the isSystem
     */
    public Boolean getIsSystem()
    {
        return isSystem;
    }

    /**
     * @param isSystem the isSystem to set
     */
    public void setIsSystem(Boolean isSystem)
    {
        this.isSystem = isSystem;
    }

    /**
     * @param tableId the tableId to set
     */
    public void setTableId(int tableId)
    {
        this.tableId = tableId;
    }

    /**
     * @param displayColumns the displayColumns to set
     */
    public void setDisplayColumns(String displayColumns)
    {
        this.displayColumns = displayColumns;
    }

    /**
     * @param searchFieldName the searchFieldName to set
     */
    public void setSearchFieldName(String searchFieldName)
    {
        this.searchFieldName = searchFieldName;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format)
    {
        this.format = format;
    }

    /**
     * @param uiFieldFormatterName the uiFieldFormatterName to set
     */
    public void setUiFieldFormatterName(String uiFieldFormatterName)
    {
        this.uiFieldFormatterName = uiFieldFormatterName;
    }

    /**
     * @param dataObjFormatterName the dataObjFormatterName to set
     */
    public void setDataObjFormatterName(String dataObjFormatterName)
    {
        this.dataObjFormatterName = dataObjFormatterName;
    }

    /**
     * @return
     */
    public String getXML()
    {
        String xmlFmt = "    <typesearch tableid=\"%d\"  name=\"%s\" searchfield=\"%s\" displaycols=\"%s\" format=\"%s\" uifieldformatter=\"%s\" dataobjformatter=\"%s\" system=\"%s\"";
        String xml    = String.format(xmlFmt, tableId, name, getStr(searchFieldName), getStr(displayColumns), getStr(format), getStr(uiFieldFormatterName), getStr(dataObjFormatterName), isSystem.toString());
        if (StringUtils.isNotEmpty(sqlTemplate))
        {
            xml += ">\n        " + sqlTemplate + "\n";
            xml += "    </typesearch>\n";
        } else
        {
            xml += "/>\n";
        }
        
        return xml;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return name + (isSystem ? " (System)" : "");
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(TypeSearchInfo o)
    {
        return name.compareTo(o.getName());
    }
    
}
