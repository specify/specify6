/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.fielddesc;

import java.util.List;
import java.util.Vector;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 4, 2007
 *
 */
public class Field
{
    protected String  name;
    protected String  type;
    /*protected String  column;
    protected int  length;
    protected String  indexName = null;
    protected boolean isRequired;
    protected boolean isUpdatable;
    protected boolean isUnique;*/
    
    protected List<Desc> descs = new Vector<Desc>();
    
    /*public Field(String name, String type, String column, int length, String indexName,
                 boolean isRequired, boolean isUpdatable, boolean isUnique)
     {
         super();
         this.name = name;
         this.type = type;
         this.column = column;
         this.length = length;
         this.indexName = indexName;
         this.isRequired = isRequired;
         this.isUpdatable = isUpdatable;
         this.isUnique = isUnique;
     }*/

    public Field(String name, String type)
     {
         super();
         this.name = name;
         this.type = type;
     }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @return the descs
     */
    public List<Desc> getDescs()
    {
        return descs;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @param descs the descs to set
     */
    public void setDescs(List<Desc> descs)
    {
        this.descs = descs;
    }
    
    public String toString()
    {
        return name;
    }
    
}
