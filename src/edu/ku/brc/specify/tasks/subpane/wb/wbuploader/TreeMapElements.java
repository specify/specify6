/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.util.Vector;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * This class manages lists of TreeMapElements used in Tree mappings for the uploader.
 * 
 * It allows easy access to properties which are stored in individual TreeMapElement objects, but which
 * should be equal for all objects in a TreeMapElements instance.
 */
public class TreeMapElements
{
    final Vector<TreeMapElement> elements;
    
    /**
     * @return true if all members have the same rank. Otherwise, return false.
     */
    protected boolean validateRank()
    {
        if (size() > 0)
        {
            int rank = elements.get(0).getRank();
            for (TreeMapElement tme : elements)
            {
                if (tme.getRank() != rank) 
                { 
                    return false; 
                }
            }
        }
        return true;
    }
    
    
    /**
     * @param elements
     * @throws Exception
     */
    public TreeMapElements(Vector<TreeMapElement> elements) throws Exception
    {
        this.elements = elements;
        if (!validateRank())
        {
            throw new Exception("TreeMapElements: inconsistent ranks");
        }
    }
    
    /**
     * @param index
     * @return the TreeMapElement at index.
     */
    public TreeMapElement getElement(int index)
    {
        return elements.get(index);
    }
    
    /**
     * @return the number elements contained.
     */
    public int size()
    {
        return elements != null ? elements.size() : 0;
    }
    
    /**
     * @return the maximum one-to-many 'sequence' (e.g. Genus 1, Genus 2) value.
     */
    public int getMaxSeq()
    {
        if (size() == 0)
        {
            return -1;
        }
        
        int result = -1;
        for (TreeMapElement tme : elements)
        {
            if (tme.getSequence() > result)
            {
                result = tme.getSequence();
            }
        }
        return result;
    }
    
    /**
     * @return rank of TreeMapElements contained.
     */
    public int getRank()
    {
        if (size() == 0)
        {
            return -1;
        }
        
        return elements.get(0).getRank();
    }
    
    /**
     * @return the column header for the workbench column that 'triggered' the creation of this object.
     */
    public String getWbFldName()
    {
        if (size() == 0)
        {
            return null;
        }
        
        return elements.get(0).getWbFldName();
    }
    
    /**
     * @return an boolean array of size getMaxSeq() with each element corresponding to a sequence and having
     * a value of true iff this object contains TreeMapElements of that sequence.     
     */
    public boolean[] getSeqs()
    {
        if (size() == 0)
        {
            return null;
        }
        
        boolean[] result = new boolean[getMaxSeq()+1];
        for (int i = 0; i <= getMaxSeq(); i++)
        {
            result[i] = false;
        }
        for (TreeMapElement tme : elements)
        {
            result[tme.getSequence()] = true;
        }
        return result;
    }
    
    /**
     * @return the isRequired for TreeMapElements contained.
     */
    public boolean isRequired()
    {
        if (size() == 0)
        {
            return false;
        }
        
        return elements.get(0).isRequired();
        
    }
}
