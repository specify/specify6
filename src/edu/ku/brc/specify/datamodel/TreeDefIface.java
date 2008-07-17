/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.datamodel;

import java.util.List;
import java.util.Set;

import edu.ku.brc.util.Nameable;

public interface TreeDefIface<N extends Treeable<N,D,I>,
                              D extends TreeDefIface<N,D,I>,
                              I extends TreeDefItemIface<N,D,I>>
	                            extends Nameable
{
    /**
     * An indicator that node full names should start with highest order
     * nodes and continue to the lowest order nodes.
     * @see #REVERSE
     */
    public static final int FORWARD = 1;
    /**
     * An indicator that node full names should start with lowest order
     * nodes and continue to the highest order nodes.
     * @see #FORWARD
     */
    public static final int REVERSE = -1;

    public void initialize();
	
	public Integer getTreeDefId();
	public void setTreeDefId(Integer id);
	
	public String getRemarks();
	public void setRemarks(String remarks);
	
	public Set<N> getTreeEntries();
	public void setTreeEntries(Set<N> treeEntries);
	
	public Set<I> getTreeDefItems();
	public void setTreeDefItems(Set<I> treeDefItems);
	
    public Integer getFullNameDirection();
    public void setFullNameDirection(Integer direction);
    
	public Class<N> getNodeClass();
	
	public I getDefItemByRank(Integer rank);
		
	/**
	 * @return true if the node numbers in the tree are up to date.
	 */
	public boolean getNodeNumbersAreUpToDate();
	/**
	 * @param arg 
	 */
	public void setNodeNumbersAreUpToDate(boolean arg);
	
	/**
	 * Walks the entire tree and assigns node numbers for every item.
	 */
	public void updateAllNodes(DataModelObjBase rootObj) throws Exception;
	
	/**
	 * @return true if node numbers are to be kept up to date at all times.
	 * (i.e. updated in business rule execution.)
	 */
	public boolean getDoNodeNumberUpdates();
	/**
	 * @param arg - true if node numbers should be kept up to date at all times.
	 */
	public void setDoNodeNumberUpdates(boolean arg);
	
	/**
	 * @return true if a workbench upload is (possibly) modifying the tree.
	 */
	public boolean isUploadInProgress();
	
	/**
	 * @param arg - true if a workbench upload is (possibly) modifying the tree.
	 */
	public void setUploadInProgress(boolean arg);
	
	   
    /**
     * The rank id of the level of the tree that items below it can be synonymized to. For example,
     * all ranks below Species can be synonymized up to Species, but not above. A call to this for the
     * Taxon tree would return the Species Rank Id. Returning a '-1' means it doesn't matter.
     * @return the rank id of the level of the tree that items below it can be synonymized to.
     */
    public int getSynonymizedLevel();
    
    /**
     * @return a List of 'Standard' levels for the tree in order from lowest rank to highest.
     * Such as Continent, Country, State, County for a Geography Tree.  
     */
    public List<TreeDefItemStandardEntry> getStandardLevels();
    
    /**
     * When a new TreeDefItem with no children is created, it's rank can be determined by adding the value returned by this function
     * to it's parent's rank.
     * 
     * @return rank increment.
     */
    public int getRankIncrement();
	
}
