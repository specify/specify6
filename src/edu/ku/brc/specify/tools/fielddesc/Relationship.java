/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tools.fielddesc;

import java.util.Vector;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 24, 2007
 *
 */
public class Relationship extends Table implements Cloneable
{
    protected String type;

    public Relationship(String name, String type)
     {
         super(name);
         this.type = type;
     }

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Relationship rel = (Relationship)super.clone();
        rel.name = name;
        rel.type = type;
        
        rel.names = new Vector<Name>();
        rel.descs = new Vector<Desc>();
        
        for (Name nm : names)
        {
            rel.names.add((Name)nm.clone());
        }

        for (Desc d : descs)
        {
            rel.descs.add((Desc)d.clone());
        }
        return rel;
    }
}
