/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.util.Collections;
import java.util.Comparator;
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
        Collections.sort(elements, new Comparator<TreeMapElement>() {

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(TreeMapElement o1, TreeMapElement o2) {
				
				return o1.getSequence().compareTo(o2.getSequence());
			}            	
        });
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
     * @return
     */
    public Boolean isLowerSubTree() {
    	if (size() == 0) {
    		return null;
    	}
    	return elements.get(0).isLowerSubTree();
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
