package edu.ku.brc.specify.tools.datamodelgenerator;

/**
 * Create relationship data.
 * 
 * @code_status Alpha
 * 
 * @author megkumin
 *
 */
public class Relationship
{

    protected String   type;
    protected String   className;
    protected Table    classObj;
    protected String   columnName;
    protected String   relationshipName;
    
    protected String   otherSideName;
    protected boolean  isRequired;
    protected boolean  isUpdatable;
    
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
    
}
