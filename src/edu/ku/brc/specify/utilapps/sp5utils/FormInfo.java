/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
package edu.ku.brc.specify.utilapps.sp5utils;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.db.DBTableInfo;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 8, 2009
 *
 */
public class FormInfo
{
    private String tableName;
    private String formVersion;
    private String formType;
    
    protected DBTableInfo tblInfo;
    
    ArrayList<FormFieldInfo> fields = new ArrayList<FormFieldInfo>();

    /**
     * @param tableName
     * @param formVersion
     * @param formType
     */
    public FormInfo(final String tableName, final String formVersion, final String formType, final DBTableInfo tblInfo)
    {
        super();
        
        this.tableName   = tableName;
        this.formVersion = formVersion;
        this.formType    = formType;
        this.tblInfo     = tblInfo;
    }
    
    public boolean isFull()
    {
        return formVersion != null && formVersion.equalsIgnoreCase("full");
    }
    
    /**
     * @return the tblInfo
     */
    public DBTableInfo getTblInfo()
    {
        return tblInfo;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getUniqueKey(tableName, formVersion, formType);
    }

    /**
     * @return the tableName
     */
    public String getTableName()
    {
        return tableName;
    }

    /**
     * @return the formVersion
     */
    public String getFormVersion()
    {
        return formVersion;
    }

    /**
     * @return the formType
     */
    public String getFormType()
    {
        return formType;
    }

    /**
     * @return the fields
     */
    public ArrayList<FormFieldInfo> getFields()
    {
        return fields;
    }
    
    /**
     * @param tableName
     * @param formVersion
     * @param formType
     * @return
     */
    public static String getUniqueKey(final String tableName, final String formVersion, final String formType)
    {
        return tableName + "/" + formVersion + (StringUtils.isNotEmpty(formType) ? ("/" + formType) : "");
    }
    
    /**
     * Configures the XStream for I/O.
     * @param xstream the stream
     */
    public static void configXStream(final XStream xstream)
    {
        // Aliases
        xstream.alias("form",       FormInfo.class); //$NON-NLS-1$
        
        xstream.aliasAttribute(FormInfo.class, "tableName",   "tableName"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(FormInfo.class, "formVersion", "formVersion"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(FormInfo.class, "formType",    "formType"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}