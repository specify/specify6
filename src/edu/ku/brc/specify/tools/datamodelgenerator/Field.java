package edu.ku.brc.specify.tools.datamodelgenerator;

import org.apache.commons.lang.StringUtils;


/**
 * Create field data.
 * 
 * @code_status Alpha
 * 
 * @author megkumin
 *
 */
public class Field
{
    protected String name;
    protected String type;
    protected String column;
    protected String length;
    protected String indexName = null;
    
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
        this.name = name;
        this.type = type;
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

    
    
}
