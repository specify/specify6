/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.af.tasks.subpane.formeditor;

import java.util.Vector;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 23, 2007
 *
 */
public class Control implements ControlIFace
{
    protected String type;
    protected String desc;
    
    protected Vector<Attr>       attrs       = new Vector<Attr>();
    protected Vector<Param>      params      = new Vector<Param>();
    protected Vector<SubControl> subcontrols = new Vector<SubControl>();
    
    public Control()
    {
        
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

    /**
     * @return the attrs
     */
    public Vector<Attr> getAttrs()
    {
        return attrs;
    }

    /**
     * @param attrs the attrs to set
     */
    public void setAttrs(Vector<Attr> attrs)
    {
        this.attrs = attrs;
    }

    /**
     * @return the desc
     */
    public String getDesc()
    {
        return desc;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc)
    {
        this.desc = desc;
    }

    /**
     * @return the params
     */
    public Vector<Param> getParams()
    {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(Vector<Param> params)
    {
        this.params = params;
    }

    /**
     * @return the subcontrols
     */
    public Vector<SubControl> getSubcontrols()
    {
        return subcontrols;
    }

    /**
     * @param subcontrols the subcontrols to set
     */
    public void setSubcontrols(Vector<SubControl> subcontrols)
    {
        this.subcontrols = subcontrols;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return type;
    }
    
}
