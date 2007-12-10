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
public class SubControl implements ControlIFace
{
    protected String type;
    protected String dsp;
    protected String desc;
    protected Vector<Attr>  attrs  = new Vector<Attr>();
    protected Vector<Param> params = new Vector<Param>();

    public SubControl()
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
     * @return the dsp
     */
    public String getDsp()
    {
        return dsp;
    }


    /**
     * @param dsp the dsp to set
     */
    public void setDsp(String dsp)
    {
        this.dsp = dsp;
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return type;
    }   
}
