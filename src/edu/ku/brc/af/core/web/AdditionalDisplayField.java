/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.af.core.web;


/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 24, 2007
 *
 */
public class AdditionalDisplayField implements Comparable<AdditionalDisplayField>
{
    protected Integer index;
    protected String  type;
    protected String  label;
    protected String  level;
    protected String  fieldName;
    
    public AdditionalDisplayField(Integer index, String type, String label, String level, String fieldName)
    {
        super();
        this.index = index;
        this.label = label;
        this.level = level;
        this.fieldName = fieldName;
        this.type = type;
    }
    
    /**
     * @return the index
     */
    public Integer getIndex()
    {
        return index;
    }

    /**
     * @return the labelField
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * @return the level
     */
    public String getLevel()
    {
        return level;
    }

    /**
     * @return the fieldName
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    public int compareTo(AdditionalDisplayField obj)
    {
        return index.compareTo(obj.index);
    }
    
    public boolean isSet()
    {
        return type.equals("set");
    }
}
