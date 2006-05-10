package edu.ku.brc.specify.tools.datamodelparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

public class Table
{

    private String name;
    private String table;
    private String lazy;
    private String tableId;
    //Hashtable<Integer, String> hash = new Hashtable<Integer, String>();
    Hashtable<String, Integer> hash = new Hashtable<String, Integer>();
    private Collection<Field> fields = new ArrayList<Field>();
    private Collection<Relationship> relationships = new ArrayList<Relationship>();
    
    public Table()
    {
        
    }

    public Table(String aName, String aTable, String aLazy, Hashtable<String, Integer> hash )
    {
        name  = aName;
        table = aTable;
        lazy  = aLazy;
        hash = hash;

        Integer i = hash.get(name);
        	if(i!=null) {
        	tableId = i.toString();
        }
        //else {
        //  tableId = ""+ altClassId + "";
        //}
        //	System.out.println("hash.put(\"" + aName +"\", "+tableId+");");
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

    public String getTableId() {
    		return tableId;
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
    
    public void setTableId(String tableId) {
    		this.tableId = tableId;
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
