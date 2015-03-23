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
package edu.ku.brc.specify.conversion;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 20, 2009
 *
 */
public class FieldMetaData
{
    protected String  name;
    protected String  type;
    protected boolean isDate;
    protected boolean isPrecision;
    protected boolean isString;
    
    protected int sqlType;

    public FieldMetaData(String name, 
                         String type,
                         boolean isDate,
                         boolean isPrecision,
                         boolean isString)
    {
        this.name        = name;
        this.type        = type;
        this.isDate      = isDate;
        this.isPrecision = isPrecision;
        this.isString    = isString;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    /**
     * @return the isDate
     */
    public boolean isDate()
    {
        return isDate;
    }

    /**
     * @return the hasPrecision
     */
    public boolean isPrecision()
    {
        return isPrecision;
    }

    /**
     * @return the isString
     */
    public boolean isString()
    {
        return isString;
    }

    /**
     * @param isDate the isDate to set
     */
    public void setDate(boolean isDate)
    {
        this.isDate = isDate;
    }

    /**
     * @param isPrecision the isPrecision to set
     */
    public void setPrecision(boolean isPrecision)
    {
        this.isPrecision = isPrecision;
    }

    /**
     * @return the sqlType
     */
    public int getSqlType()
    {
        return sqlType;
    }

    /**
     * @param sqlType the sqlType to set
     */
    public void setSqlType(int sqlType)
    {
        this.sqlType = sqlType;
    }
}