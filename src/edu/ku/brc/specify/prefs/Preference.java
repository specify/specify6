package edu.ku.brc.specify.prefs;

import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.lang.builder.ToStringBuilder;

import edu.ku.brc.specify.exceptions.UIException;


/** @author Hibernate CodeGenerator */
public class Preference implements PrefIFace 
{
    private Hashtable<String, PrefChangeListener> prefsChangedListeners = new Hashtable<String, PrefChangeListener>();
    
    private PrefGroupIFace group;
    
    /** nullable persistent field */
    private String name;

    /** nullable persistent field */
    private String value;

    /** nullable persistent field */
    private String valueType;

    /** default constructor */
    public Preference() 
    {
    }
    
    /** full constructor */
    public Preference(String name, String value, String valueType) 
    {
        this.name = name;
        this.value = value;
        this.valueType = valueType;
    }

    public boolean isGroup()
    {
        return false;
    }

    public String getName() 
    {
        return this.name;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public String getValue() 
    {
        return this.value;
    }

    public void setValue(String value) 
    {
        this.value = value;
        preferncesHaveChanged();
    }

    public String getValueType() 
    {
        return this.valueType;
    }

    public void setValueType(String valueType) 
    {
        this.valueType = valueType;
    }
    
    public PrefGroupIFace getGroup() 
    {
        return group;
    }

    public void setGroup(PrefGroupIFace group) 
    {
        this.group = group;
    }

    /**
     * 
     * @param aName
     * @param aPCL
     * @throws UIException
     */
    public void registerPrefChangeListener(String aName, PrefChangeListener aPCL) throws UIException
    {
        if (prefsChangedListeners.containsKey(aName))
        {
           throw new UIException("PrefChangeListener with Name["+aName+"] has already been registered."); 
        }
        prefsChangedListeners.put(aName, aPCL);
    }
    
    /**
     * 
     * @param aName
     * @param aPCL
     * @throws UIException
     */
    public void unregisterPrefChangeListener(String aName, PrefChangeListener aPCL) throws UIException
    {
        PrefChangeListener acl = prefsChangedListeners.get(aName);
        if (acl == null)
        {
           throw new UIException("Couldn't find PrefChangeListener with Name["+aName+"]."); 
        }
        prefsChangedListeners.remove(acl);
    }
    
    /**
     * Notify all listeners that the prefs have changes
     *
     */
    public void preferncesHaveChanged()
    {
        for (Enumeration e=prefsChangedListeners.elements();e.hasMoreElements();)
        {
            ((PrefChangeListener)e.nextElement()).prefsChanged(this);
        }
    }    

}
