/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.datamodel;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Describes a 'standard' tree level such as Genus, Species, Country...
 */
public class TreeDefItemStandardEntry
{
    protected final String name;
    protected final int rank;
    /**
     * @param name
     * @param rank
     */
    public TreeDefItemStandardEntry(String name, int rank)
    {
        super();
        this.name = name;
        this.rank = rank;
    }
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }
    /**
     * @return the rank
     */
    public int getRank()
    {
        return rank;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getName();
    }
    
    
}
