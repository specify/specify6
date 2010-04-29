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
package edu.ku.brc.specify.toycode.mexconabio;


/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Apr 12, 2010
 *
 */
public class FieldDef
{
    enum DataType {eText, eNumber, eDate, eTime, eMemo}

    private String   name;
    private String   origName;
    private DataType type;
    private boolean  isNullable;
    private int      maxSize = Integer.MIN_VALUE;
    private boolean  isDouble = false;
    
    /**
     * 
     */
    public FieldDef()
    {
        super();
    }
    
    /**
     * @param name
     * @param type
     * @param isNullable
     */
    public FieldDef(String name, String origName, DataType type, boolean isNullable, boolean isDouble)
    {
        super();
        this.name       = name;
        this.origName   = origName;
        this.type       = type;
        this.isNullable = isNullable;
        this.isDouble   = isDouble;
    }
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * @return the origName
     */
    public String getOrigName()
    {
        return origName;
    }

    /**
     * @return the type
     */
    public DataType getType()
    {
        return type;
    }
    /**
     * @return the isNullable
     */
    public boolean isNullable()
    {
        return isNullable;
    }
    
    /**
     * @return the maxSize
     */
    public int getMaxSize()
    {
        return maxSize;
    }
    
    public void setMaxSize(final int len)
    {
        if (len > maxSize)
        {
            maxSize = len;
        }
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
     
    /**
     * @param origName the origName to set
     */
    public void setOrigName(String origName)
    {
        this.origName = origName;
    }

    /**
     * @param type the type to set
     */
    public void setType(DataType type)
    {
        this.type = type;
    }
    /**
     * @param isNullable the isNullable to set
     */
    public void setNullable(boolean isNullable)
    {
        this.isNullable = isNullable;
    }

    /**
     * @return the isDouble
     */
    public boolean isDouble()
    {
        return isDouble;
    }

    /**
     * @param isDouble the isDouble to set
     */
    public void setDouble(boolean isDouble)
    {
        this.isDouble = isDouble;
    }

    /**
     * @return the isDouble
     */
    public boolean isNumber()
    {
        return type == DataType.eNumber;
    }  
}