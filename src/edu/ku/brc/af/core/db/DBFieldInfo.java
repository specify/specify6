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
package edu.ku.brc.af.core.db;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;

/**
 * Represents a field in the database schema.
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 3, 2007
 *
 */
public class DBFieldInfo extends DBInfoBase implements DBTableChildIFace
{
    protected static final Logger log = Logger.getLogger(DBFieldInfo.class);
    
    protected DBTableInfo tableInfo;
    protected String      column;
    protected String      type;
    protected int         length;
    protected boolean     isRequired;
    protected boolean     isRequiredInSchema;
    protected boolean     isUpdatable;
    protected boolean     isUnique;
    protected boolean     isIndexed;
    protected boolean     isPartialDate;
    protected String      datePrecisionName;
    
    // Transient
    protected String                pickListName = null;
    protected UIFieldFormatterIFace formatter    = null;
    protected String                formatStr    = null;
    protected Class<?>              dataClass    = null;
    protected String                webLinkName  = null;
    
    
    /**
     * @param tableInfo
     * @param column
     * @param cls
     */
    public DBFieldInfo(final DBTableInfo tableInfo, 
                       final String column,
                       final Class<?> cls)
    {
        this(tableInfo, column, null, null, 0, false, false, false, false, false, null);
        this.dataClass = cls;
    }
    
    /**
     * @param tableInfo
     * @param column
     * @param name
     * @param type
     * @param length
     * @param isRequired
     * @param isUpdatable
     * @param isUnique
     * @param isIndexed
     * @param isPartialDate
     * @param datePrecisionName
     */
    public DBFieldInfo(final DBTableInfo tableInfo, 
                       final String column, 
                       final String name, 
                       final String type, 
                       final int length, 
                       final boolean isRequired, 
                       final boolean isUpdatable, 
                       final boolean isUnique, 
                       final boolean isIndexed, 
                       final boolean isPartialDate, 
                       final String datePrecisionName)
    {
        super(name);
        
        this.tableInfo = tableInfo;
        this.column = column;
        this.type = type;
        this.length = length;
        this.isRequired = isRequired;
        this.isUpdatable = isUpdatable;
        this.isUnique = isUnique;
        this.isIndexed = isIndexed;
        this.isPartialDate = isPartialDate;
        this.datePrecisionName = datePrecisionName;
    }

    public String getColumn()
    {
        return column;
    }

    public int getLength()
    {
        return length;
    }

    public DBTableInfo getTableInfo()
    {
        return tableInfo;
    }

    public String getType()
    {
        return type;
    }
    
    public String toString()
    {
        return StringUtils.isNotEmpty(title) ? title : column;
    }
    
    /**
     * @return the isRequired
     */
    public boolean isRequired()
    {
        return isRequired;
    }

    /**
     * @param isRequired the isRequired to set
     */
    public void setRequired(boolean isRequired)
    {
        this.isRequired = isRequired;
    }

    /**
     * @return the isUpdatable
     */
    public boolean isUpdatable()
    {
        return isUpdatable;
    }

    /**
     * @return the isUnique
     */
    public boolean isUnique()
    {
        return isUnique;
    }

    /**
     * @return the isIndexed
     */
    public boolean isIndexed()
    {
        return isIndexed;
    }
    
    /**
     * @return the formatter
     */
    public UIFieldFormatterIFace getFormatter()
    {
        return formatter;
    }

    /**
     * @param formatter the formatter to set
     */
    public void setFormatter(UIFieldFormatterIFace formatter)
    {
        this.formatter = formatter;
    }

    /**
     * @return the formatStr
     */
    public String getFormatStr()
    {
        return formatStr;
    }

    /**
     * @param formatStr the formatStr to set
     */
    public void setFormatStr(String formatStr)
    {
        this.formatStr = formatStr;
    }

    /**
     * @return the pickListName
     */
    public String getPickListName()
    {
        return pickListName;
    }

    /**
     * @param pickListName the pickListName to set
     */
    public void setPickListName(String pickListName)
    {
        this.pickListName = pickListName;
    }

    /**
     * @return the webLinkName
     */
    public String getWebLinkName()
    {
        return webLinkName;
    }

    /**
     * @param webLinkName the webLinkName to set
     */
    public void setWebLinkName(String webLinkName)
    {
        this.webLinkName = webLinkName;
    }

    /**
     * @return the isRequiredInSchema
     */
    public boolean isRequiredInSchema()
    {
        return isRequiredInSchema;
    }

    /**
     * @param isRequiredInSchema the isRequiredInSchema to set
     */
    public void setRequiredInSchema(boolean isRequiredInSchema)
    {
        this.isRequiredInSchema = isRequiredInSchema;
    }

    /**
     * @return the isPartialDate
     */
    public boolean isPartialDate()
    {
        return isPartialDate;
    }

    /**
     * @return the datePrecisionName
     */
    public String getDatePrecisionName()
    {
        return datePrecisionName;
    }

    public Class<?> getDataClass() {
        if (dataClass == null) {
            if (StringUtils.isNotEmpty(type)) {
                if (type.equals("calendar_date")) { //$NON-NLS-1$
                    dataClass = Calendar.class;
                    
                } else if (type.equals("text")) { //$NON-NLS-1$
                    dataClass = String.class;
                    
                } else if (type.equals("boolean")) { //$NON-NLS-1$
                    dataClass = Boolean.class;
                    
                } else if (type.equals("byte")) { //$NON-NLS-1$
                    dataClass = Byte.class;
                } else {
                    try {
                        dataClass = Class.forName(type);
                        
                    } catch (Exception e) {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DBFieldInfo.class, e);
                        log.error(e);
                    }
                }
            }
        }
        return dataClass;
    }
}
