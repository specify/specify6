/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.weblink;

import com.thoughtworks.xstream.XStream;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 13, 2008
 *
 */
public class WebLinkUsedBy
{
    protected String tableName;
    protected String fieldName;
    
    /**
     * @param tableName
     * @param fieldName
     */
    public WebLinkUsedBy(String tableName, String fieldName)
    {
        this.tableName = tableName;
        this.fieldName = fieldName;
    }
    
    public WebLinkUsedBy(String tableName)
    {
        this.tableName = tableName;
        this.fieldName = null;
    }
    
    public WebLinkUsedBy() // needed for XML Marshaling
    {
    }
    
    /**
     * @return the tableName
     */
    public String getTableName()
    {
        return tableName;
    }
    /**
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }
    /**
     * @return the fieldName
     */
    public String getFieldName()
    {
        return fieldName;
    }
    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }
    
    /**
     * @param xstream
     */
    public static void configXStream(final XStream xstream)
    {
        xstream.alias("usedby", WebLinkUsedBy.class); //$NON-NLS-1$

    }  
}
