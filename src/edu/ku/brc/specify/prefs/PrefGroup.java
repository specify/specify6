package edu.ku.brc.specify.prefs;

import java.io.Serializable;
import java.util.Date;
import java.util.*;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class PrefGroup implements PrefGroupIFace 
{
    protected Hashtable<String, PrefIFace> preferences;
    
    /** nullable persistent field */
    private String name;
    

    /** default constructor */
    public PrefGroup() 
    {
    }

    /** full constructor */
    public PrefGroup(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return this.name;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public Hashtable<String, PrefIFace> getPreferences() 
    {
        return this.preferences;
    }
    
    public boolean isGroup()
    {
        return true;
    }


}
