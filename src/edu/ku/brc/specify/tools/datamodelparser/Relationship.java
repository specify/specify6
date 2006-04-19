package edu.ku.brc.specify.tools.datamodelparser;

public class Relationship
{

    protected String   type;
    protected String   className;
    protected Table classObj;
    protected String columnName;
    protected String relationshipName;
    
    
    public Relationship(String type, String className)
    {
        this.type = type;
        this.className = className;
        this.columnName = "";
        this.relationshipName = "";
    }

    public Relationship(String type, String className, String columnName, String relationshipName)
    {
        this.type = type;
        this.className = className;
        this.columnName = columnName;
        this.relationshipName = relationshipName;
    }

    public String getClassName()
    {
        return className;
    }

    public String getType()
    {
        return type;
    }
    
    public String getColumnName()
    {
        return columnName;
    }    
    
    public String getRelationshipName()
    {
        return relationshipName;
    }        
    public String toString()
    {
        return type + " - " +className;
    }

    public Table getClassObj()
    {
        return classObj;
    }

    public void setClassObj(Table classObj)
    {
        this.classObj = classObj;
    }
    
    public void setColumnName(String columnName)
    {
        this.columnName = columnName;
    }    
    public void setRelationshipName(String relationshipName)
    {
        this.relationshipName = relationshipName;
    }           
    
}
