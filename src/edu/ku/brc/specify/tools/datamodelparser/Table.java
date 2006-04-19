package edu.ku.brc.specify.tools.datamodelparser;

import java.util.*;

public class Table
{

    private String name;
    private String table;
    private String lazy;
    
    private Collection<Field> fields = new ArrayList<Field>();
    private Collection<Relationship> relationships = new ArrayList<Relationship>();
    
    public Table()
    {
        
    }

    public Table(String aName, String aTable, String aLazy)
    {
        name  = aName;
        table = aTable;
        lazy  = aLazy;
    }

    public String getLazy() 
    {
        return lazy;
    }

    public void setLazy(String lazy) 
    {
        this.lazy = lazy;
    }

    public String getName() 
    {
        return name;
    }


    protected String createShortName(String aName)
    {
        int inx = aName.lastIndexOf('.');
        if (inx != -1)
        {
            return aName.substring(inx+1, aName.length());
        } else
        {
            return aName;
        }
        
    }
    
    public void setName(String name) 
    {
        this.name = name;
    }

    public String getTable() 
    {
        return table;
    }

    public void setTable(String table) 
    {
        this.table = table;
    }
    
    public void addField(Field aField)
    {
        fields.add(aField);
    }
    
    public void addRelationship(Relationship rel)
    {
        relationships.add(rel);
    }
    
    
    public Collection<Field> getFields()
    {
        return fields;
    }
    
    public Collection<Relationship> getRelationships()
    {
        return relationships;
    }
    
}
