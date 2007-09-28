package edu.ku.brc.specify.tools.datamodelgenerator;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.tools.fielddesc.LocalizedStrIFace;


/**
 * Create field data.
 * 
 * @code_status Alpha
 * 
 * @author megkumin
 *
 */
public class Field implements Comparable<Field>
{
    protected String  name;
    protected String  type;
    protected String  column;
    protected String  length;
    protected String  indexName = null;
    protected boolean isRequired;
    protected boolean isUpdatable;
    protected boolean isUnique;
    
    protected LocalizedStrIFace desc;
    protected LocalizedStrIFace nameDesc;
    
    /**
     * @param name the name of the field	
     * @param type the type of the field
     * @param column the name of the column this field maps to in the database
     * @param length the length of the field
     */
    public Field(String name, 
                    String type, 
                    String column,
                    String length)
    {
        this.name   = name;
        this.type   = type;
        this.column = column;
        this.length = length;
    }
    
    /**
     * @return the database column
     */
    public String getColumn() 
    {
        return column;
    }

    /**
     * @param column sets the database column
     */
    public void setColumn(String column) 
    {
        this.column = column;
    }


    /**
     * @return the lenght of the field as a String
     */
    public String getLength() 
    {
        return length;
    }

    /**
     * @param length the length as a String
     */
    public void setLength(String length) 
    {
        this.length = length;
    }

    /**
     * @return the field name
     */
    public String getName() 
    {
        return name;
    }

    /**
     * @param name sets the field name
     */
    public void setName(String name) 
    {
        this.name = name;
    }
    
    /**
     * @return
     */
    public String getType() 
    {
        return type;
    }

    /**
     * @param type
     */
    public void setType(String type) 
    {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return name;
    }

    /**
     * @return the isIndexed
     */
    public boolean isIndexed()
    {
        return StringUtils.isNotEmpty(indexName);
    }

    /**
     * @return the indexName
     */
    public String getIndexName()
    {
        return indexName;
    }

    /**
     * @param indexName the indexName to set
     */
    public void setIndexName(String indexName)
    {
        this.indexName = indexName;
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

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Field arg0)
    {
        return name.compareTo(arg0.name);
    }

    /**
     * @return the isUnique
     */
    public boolean isUnique()
    {
        return isUnique;
    }

    /**
     * @param isUnique the isUnique to set
     */
    public void setUnique(boolean isUnique)
    {
        this.isUnique = isUnique;
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

    
}
