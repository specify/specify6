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

import java.util.Vector;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 4, 2007
 *
 */
public class Field extends Table implements Cloneable
{
    protected String  type;

    public Field(String name, String type)
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
        Field field = (Field)super.clone();
        field.name = name;
        field.type = type;
        
        field.names = new Vector<Name>();
        field.descs = new Vector<Desc>();
        
        for (Name nm : names)
        {
            field.names.add((Name)nm.clone());
        }

        for (Desc d : descs)
        {
            field.descs.add((Desc)d.clone());
        }
        return field;
    }
}
