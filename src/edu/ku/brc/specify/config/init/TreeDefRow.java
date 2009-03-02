/*
 * Copyright (C) 2009  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.config.init;

import com.thoughtworks.xstream.XStream;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 1, 2009
 *
 */
public class TreeDefRow 
{
    protected String  defName;
    protected int     rank;
    protected boolean isEnforced;
    protected boolean isInFullName;
    protected boolean isRequired;
    protected String  separator;
    
    /**
     * @param defName
     * @param rank
     * @param isEnforced
     * @param isInFullName
     */
    public TreeDefRow(String defName, 
                      int rank, 
                      boolean isEnforced, 
                      boolean isInFullName, 
                      boolean isRequired, 
                      String separator)
    {
        super();
        this.defName = defName;
        this.rank = rank;
        this.isEnforced = isEnforced;
        this.isInFullName = isInFullName;
        this.isRequired = isRequired;
        this.separator = separator;
    }
    /**
     * @return the defName
     */
    public String getDefName()
    {
        return defName;
    }
    /**
     * @return the rank
     */
    public int getRank()
    {
        return rank;
    }
    /**
     * @return the isEnforced
     */
    public boolean isEnforced()
    {
        return isEnforced;
    }
    /**
     * @return the isInFullName
     */
    public boolean isInFullName()
    {
        return isInFullName;
    }
    /**
     * @return the isEditable
     */
    public boolean isRequired()
    {
        return isRequired;
    }
    /**
     * @param isEnforced the isEnforced to set
     */
    public void setEnforced(boolean isEnforced)
    {
        this.isEnforced = isEnforced;
    }
    /**
     * @param isInFullName the isInFullName to set
     */
    public void setInFullName(boolean isInFullName)
    {
        this.isInFullName = isInFullName;
    }
    /**
     * @return the separator
     */
    public String getSeparator()
    {
        return separator;
    }
    /**
     * @param separator the separator to set
     */
    public void setSeparator(String separator)
    {
        this.separator = separator;
    }
    
    /**
     * Configures the XStream for I/O.
     * @param xstream the stream
     */
    public static void configXStream(final XStream xstream)
    {
        xstream.useAttributeFor(TreeDefRow.class, "defName");
        xstream.useAttributeFor(TreeDefRow.class, "rank");
        xstream.useAttributeFor(TreeDefRow.class, "isEnforced");
        xstream.useAttributeFor(TreeDefRow.class, "isInFullName");
        xstream.useAttributeFor(TreeDefRow.class, "isRequired");
        xstream.useAttributeFor(TreeDefRow.class, "separator");
    }
}