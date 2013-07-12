/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.ui.treetables;

import java.util.Iterator;
import java.util.Set;

import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 * (Original author JDS)
 *
 * @code_status Beta
 *
 * Jan 23, 2008
 *
 */
public class TreeNode
{
    protected String   name;
    protected String   fullName;
    protected boolean  hasChildren;
    protected int      id;
    protected int      parentId;
    protected Class<?> dataObjClass;
    protected int      rank;
    protected int      parentRank;
    protected int      nodeNumber;
    protected int      highestChildNodeNumber;
    
    /** This field will hold a number representing the number of associated records for this node.
     *  For example, if this field represents a Taxon record, the <code>associatedRecordCount</code> would
     *  be the number of CollectionObject records that refer to this Taxon.  For TreeNodes representing
     *  Geography records, this number might represent the number of CollectingEvents that were in this
     *  geographical area.
     */
    protected boolean isCalcCount            = false;
    protected boolean hasCalcCount           = false;
    protected boolean isCalcCount2           = false;
    protected boolean hasCalcCount2          = false;
    protected int     associatedRecordCount  = -1;
    protected int     associatedRecordCount2 = -1;
    protected Integer acceptedParentId;
    protected String  acceptedParentFullName;
    protected Set<Pair<Integer,String>> synonymIdsAndNames;
    
    // Transient for faster rendering
    protected Boolean hasVisualChildren = null; 
    protected String  tooltipText       = null;
    
    /**
     * @param name
     * @param fullName
     * @param id
     * @param parentId
     * @param rank
     * @param parentRank
     * @param hasChildren
     * @param acceptedParentId
     * @param acceptedParentFullName
     * @param synonymIdsAndNames
     */
    public TreeNode(final String name, 
                    final String fullName, 
                    final int id, 
                    final int parentId, 
                    final int rank, 
                    final int parentRank, 
                    final boolean hasChildren, 
                    final Integer acceptedParentId, 
                    final String acceptedParentFullName, 
                    final Set<Pair<Integer,String>> synonymIdsAndNames)
    {
        super();
        this.name                   = name;
        this.fullName               = fullName;
        this.id                     = id;
        this.parentId               = parentId;
        this.rank                   = rank;
        this.parentRank             = parentRank;
        this.hasChildren            = hasChildren;
        this.acceptedParentId       = acceptedParentId;
        this.acceptedParentFullName = acceptedParentFullName;
        this.synonymIdsAndNames     = synonymIdsAndNames;
    }

    /**
     * @return
     */
    public Class<?> getDataObjClass()
    {
        return dataObjClass;
    }

    /**
     * @param dataObjClass
     */
    public void setDataObjClass(final Class<?> dataObjClass)
    {
        this.dataObjClass = dataObjClass;
    }

    /**
     * @return
     */
    public boolean isHasChildren()
    {
        return hasChildren;
    }

    /**
     * @param hasChildren
     */
    public void setHasChildren(final boolean hasChildren)
    {
        this.hasChildren = hasChildren;
    }

    /**
     * @return
     */
    public int getId()
    {
        return id;
    }

    /**
     * @param id
     */
    public void setId(final int id)
    {
        this.id = id;
    }

    /**
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     */
    public void setName(final String name)
    {
        tooltipText = null;

        this.name = name;
    }

    /**
     * @return
     */
    public String getFullName()
    {
        return fullName;
    }

    /**
     * @param fullName
     */
    public void setFullName(final String fullName)
    {
        tooltipText = null;

        this.fullName = fullName;
    }

    /**
     * @return the parent node's ID, or, if this node has no parent, this node's ID
     */
    public int getParentId()
    {
        return parentId;
    }

    /**
     * @param parentId
     */
    public void setParentId(final int parentId)
    {
        this.parentId = parentId;
    }

    /**
     * @return the parent node's rank, or -1 if this node doesn't have a parent
     */
    public int getParentRank()
    {
        return parentRank;
    }

    /**
     * @param parentRank
     */
    public void setParentRank(final int parentRank)
    {
        this.parentRank = parentRank;
    }

    /**
     * @return
     */
    public int getRank()
    {
        return rank;
    }

    /**
     * @param rank
     */
    public void setRank(final int rank)
    {
        this.rank = rank;
    }

    /**
     * @return
     */
    public int getAssociatedRecordCount()
    {
        return associatedRecordCount;
    }

    /**
     * @param associatedRecordCount
     */
    public void setAssociatedRecordCount(final int associatedRecordCount)
    {
        this.associatedRecordCount = associatedRecordCount;
    }

    /**
     * @return the associatedRecordCount2
     */
    public int getAssociatedRecordCount2()
    {
        return associatedRecordCount2;
    }

    /**
     * @param associatedRecordCount2 the associatedRecordCount2 to set
     */
    public void setAssociatedRecordCount2(final int associatedRecordCount2)
    {
        this.associatedRecordCount2 = associatedRecordCount2;
    }

    /**
     * @return the isCalcCount
     */
    public synchronized boolean isCalcCount()
    {
        return isCalcCount;
    }

    /**
     * @param isCalcCount the isCalcCount to set
     */
    public synchronized void setCalcCount(boolean isCalcCount)
    {
        this.isCalcCount = isCalcCount;
    }

    /**
     * @return the hasCalcCount
     */
    public synchronized boolean isHasCalcCount()
    {
        return hasCalcCount;
    }

    /**
     * @param hasCalcCount the hasCalcCount to set
     */
    public synchronized void setHasCalcCount(boolean hasCalcCount)
    {
        this.hasCalcCount = hasCalcCount;
    }

    /**
     * @return the isCalcCount2
     */
    public synchronized boolean isCalcCount2()
    {
        return isCalcCount2;
    }

    /**
     * @param isCalcCount2 the isCalcCount2 to set
     */
    public synchronized void setCalcCount2(boolean isCalcCount2)
    {
        this.isCalcCount2 = isCalcCount2;
    }

    /**
     * @return the hasCalcCount2
     */
    public synchronized boolean isHasCalcCount2()
    {
        return hasCalcCount2;
    }

    /**
     * @param hasCalcCount2 the hasCalcCount2 to set
     */
    public synchronized void setHasCalcCount2(boolean hasCalcCount2)
    {
        this.hasCalcCount2 = hasCalcCount2;
    }

    /**
     * @return
     */
    public synchronized boolean shouldCalcCount()
    {
        //don't do counts for taxon synonyms
        if (dataObjClass != null && dataObjClass.equals(Taxon.class) && acceptedParentId != null)
        {
            return false;
        }
        
        boolean isCalc = (isCalcCount || hasCalcCount) && (isCalcCount2 || hasCalcCount2);
        return !isCalc;
    }
    
    /**
     * @return
     */
    public Integer getAcceptedParentId()
    {
        return acceptedParentId;
    }

    /**
     * @return
     */
    public String getAcceptedParentFullName()
    {
        return acceptedParentFullName;
    }

    /**
     * @param acceptedParentFullName
     */
    public void setAcceptedParentFullName(final String acceptedParentFullName)
    {
        tooltipText = null;

        this.acceptedParentFullName = acceptedParentFullName;
    }

    /**
     * @param acceptedParentId
     */
    public void setAcceptedParentId(final Integer acceptedParentId)
    {
        this.acceptedParentId = acceptedParentId;
    }

    /**
     * @return
     */
    public Set<Pair<Integer, String>> getSynonymIdsAndNames()
    {
        return synonymIdsAndNames;
    }
    
    /**
     * @return the hasVisualChildren
     */
    public Boolean hasVisualChildren()
    {
        return hasVisualChildren;
    }

    /**
     * @param hasVisualChildren the hasVisualChildren to set
     */
    public void setHasVisualChildren(final Boolean hasVisualChildren)
    {
        this.hasVisualChildren = hasVisualChildren;
    }

    /**
     * @return the tooltipText
     */
    public String getTooltipText()
    {
        return tooltipText;
    }

    /**
     * @param tooltipText the tooltipText to set
     */
    public void setTooltipText(String tooltipText)
    {
        this.tooltipText = tooltipText;
    }

    /**
     * @param synonymNodeId
     */
    public void removeSynonym(final Integer synonymNodeId)
    {
        tooltipText = null;
        
        Iterator<Pair<Integer,String>> iter = synonymIdsAndNames.iterator();
        while( iter.hasNext() )
        {
            if (iter.next().first.equals(synonymNodeId))
            {
                iter.remove();
                return;
            }
        }
    }

    public void setSynonymIdsAndNames(Set<Pair<Integer, String>> synonymIdsAndNames)
    {
        this.synonymIdsAndNames = synonymIdsAndNames;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String memoryLocation = "00000000" + Integer.toHexString(hashCode());
        memoryLocation = memoryLocation.substring(memoryLocation.length() - 8);
        return getClass().getSimpleName() + "@0x" + memoryLocation + ": " + name + ", " + id + ", " + rank + ", " + parentId + ", " + parentRank;
    }

    /**
     * @param synonymId
     * 
     * Removes item with id equal tosynonymId from list of synonyms for this node.
     * 
     * @return true if node was removed.
     */
    public boolean removeSynonym(int synonymId)
    {
        boolean result = false;
        for (Pair<Integer, String> syn : synonymIdsAndNames)
        {
            if (syn.getFirst().equals(synonymId))
            {
                synonymIdsAndNames.remove(syn);
                result = true;
                break;
            }
        }
        return result;
    }
}
