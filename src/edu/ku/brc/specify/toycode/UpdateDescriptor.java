/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.toycode;

import java.util.Vector;

import com.thoughtworks.xstream.XStream;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 14, 2008
 *
 */
public class UpdateDescriptor
{
    protected String baseUrl = "";
    protected Vector<UpdateEntry> entries = new Vector<UpdateEntry>();
    
    /**
     * 
     */
    public UpdateDescriptor()
    {
        super();
    }

    /**
     * @return the baseUrl
     */
    public String getBaseUrl()
    {
        return baseUrl;
    }

    /**
     * @param baseUrl the baseUrl to set
     */
    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    /**
     * @return the entries
     */
    public Vector<UpdateEntry> getEntries()
    {
        return entries;
    }

    /**
     * @param entries the entries to set
     */
    public void setEntries(Vector<UpdateEntry> entries)
    {
        this.entries = entries;
    }
    
    public static void config(XStream xstream)
    {
        xstream.alias("updateDescriptor", UpdateDescriptor.class);
        xstream.useAttributeFor(UpdateDescriptor.class, "baseUrl");
        xstream.addImplicitCollection(UpdateDescriptor.class, "entries");
    } 
    
}
