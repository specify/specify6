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
package edu.ku.brc.specify.tools.datamodelgenerator;

import edu.ku.brc.specify.tools.schemalocale.LocalizedStrIFace;

/**
 * Create relationship data.
 * 
 * @code_status Alpha
 * 
 * @author megkumin
 *
 */
public class Relationship implements Comparable<Relationship>
{

    protected String   type;
    protected String   className;
    protected Table    classObj;
    protected String   columnName;
    protected String   relationshipName;
    protected String   joinTableName;
    
    protected String   otherSideName;
    protected boolean  isRequired;
    protected boolean  isUpdatable;
    protected boolean  isSave          = false;
    protected boolean  isLikeManyToOne;
    
    protected LocalizedStrIFace desc;
    protected LocalizedStrIFace nameDesc;
    
    /**
     * @param type
     * @param className
     * @param columnName
     * @param relationshipName
     */
    public Relationship(String type, String className, String columnName, String relationshipName)
    {
        this.type = type;
        this.className = className;
        this.columnName = columnName;
        this.relationshipName = relationshipName;
        this.isLikeManyToOne = false;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return
     * String
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * @return
     * String
     */
    public String getType()
    {
        return type;
    }
    
    /**
     * @return
     * String
     */
    public String getColumnName()
    {
        return columnName;
    }    
    
    /**
     * @return
     * String
     */
    public String getRelationshipName()
    {
        return relationshipName;
    }   
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return type + " - " +className;
    }
    
    /**
     * @return
     * Table
     */
    public Table getClassObj()
    {
        return classObj;
    }

    /**
     * @param classObj
     * void
     */
    public void setClassObj(Table classObj)
    {
        this.classObj = classObj;
    }
    
    /**
     * @param columnName
     * void
     */
    public void setColumnName(String columnName)
    {
        this.columnName = columnName;
    }    
    
    /**
     * @param relationshipName
     * void
     */
    public void setRelationshipName(String relationshipName)
    {
        this.relationshipName = relationshipName;
    }

    /**
     * @return the otherSideName
     */
    public String getOtherSideName()
    {
        return otherSideName;
    }

    public String getJoinTableName()
    {
        return joinTableName;
    }

    public void setJoinTableName(String joinTableName)
    {
        this.joinTableName = joinTableName;
    }

    /**
     * @param otherSideName the otherSideName to set
     */
    public void setOtherSideName(String otherSideName)
    {
        this.otherSideName = otherSideName;
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
     * @param isUpdatable the isUpdatable to set
     */
    public void setUpdatable(boolean isUpdatable)
    {
        this.isUpdatable = isUpdatable;
    }

    /**
     * @return the desc
     */
    public LocalizedStrIFace getDesc()
    {
        return desc;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(LocalizedStrIFace desc)
    {
        this.desc = desc;
    }

    /**
     * @return the nameDesc
     */
    public LocalizedStrIFace getNameDesc()
    {
        return nameDesc;
    }

    /**
     * @param nameDesc the nameDesc to set
     */
    public void setNameDesc(LocalizedStrIFace nameDesc)
    {
        this.nameDesc = nameDesc;
    }

    public boolean isSave()
    {
        return isSave;
    }

    public void setSave(boolean isSave)
    {
        this.isSave = isSave;
    }
    
    /**
     * @return the isLikeManyToOne
     */
    public boolean isLikeManyToOne()
    {
        return isLikeManyToOne;
    }

    /**
     * @param isLikeManyToOne the isLikeManyToOne to set
     */
    public void setLikeManyToOne(boolean isLikeManyToOne)
    {
        this.isLikeManyToOne = isLikeManyToOne;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Relationship o)
    {
        return relationshipName.compareTo(o.relationshipName);
    }
    
}
