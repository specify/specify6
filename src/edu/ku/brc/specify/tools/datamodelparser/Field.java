package edu.ku.brc.specify.tools.datamodelparser;

public class Field
{

    private String name;
    private String type;
    private String column;
    private String notNull;
    private String length;
    
    public Field()
    {
        
    }
    
    public Field(String aName, 
                    String aType, 
                    String aColumn,
                    String aLen)
    {
        name = aName;
        type = aType;
        column = aColumn;
        length = aLen;
    }
    

    public String getColumn() 
    {
        return column;
    }


    public void setColumn(String column) 
    {
        this.column = column;
    }


    public String getLength() 
    {
        return length;
    }


    public void setLength(String length) 
    {
        this.length = length;
    }


    public String getName() 
    {
        return name;
    }


    public void setName(String name) 
    {
        this.name = name;
    }


    public String getNotNull() 
    {
        return notNull;
    }


    public void setNotNull(String notNull) 
    {
        this.notNull = notNull;
    }


    public String getType() 
    {
        return type;
    }


    public void setType(String type) 
    {
        this.type = type;
    }


    public String toString()
    {
        return name;
    }
}
