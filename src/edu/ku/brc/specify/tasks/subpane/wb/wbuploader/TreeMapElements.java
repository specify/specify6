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
 */
public class TreeMapElements
{
    final Vector<TreeMapElement> elements;
    
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
    
    
    public TreeMapElements(Vector<TreeMapElement> elements) throws Exception
    {
        this.elements = elements;
        if (!validateRank())
        {
            throw new Exception("TreeMapElements: inconsistent ranks");
        }
    }
    
    public TreeMapElement getElement(int index)
    {
        return elements.get(index);
    }
    
    public int size()
    {
        return elements != null ? elements.size() : 0;
    }
    
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
    
    public int getRank()
    {
        if (size() == 0)
        {
            return -1;
        }
        
        return elements.get(0).getRank();
    }
    
    public String getWbFldName()
    {
        if (size() == 0)
        {
            return null;
        }
        
        return elements.get(0).getWbFldName();
    }
    
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
    
    public boolean isRequired()
    {
        if (size() == 0)
        {
            return false;
        }
        
        return elements.get(0).isRequired();
        
    }
}
