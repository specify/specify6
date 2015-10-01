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
package edu.ku.brc.specify.datamodel;

import java.util.List;
import java.util.Set;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
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
	 * @param rootObj
	 * @param useProgDlg
	 * @param lockedByCaller - main tree lock handled by caller
	 * 
	 * @return true if update completes without errors, else false
	 * 
	 * @throws Exception
	 * 
	 * Walks the entire tree and assigns node numbers for every item.
	 *
	 */
	public boolean updateAllNodeNumbers(DataModelObjBase rootObj, final boolean useProgDlg, 
			final boolean lockedByCaller) throws Exception;
	
	/**
	 * @param rootObj
	 * @throws Exception
	 * 
	 * Walks the entire tree and builds the FullName for every item.
	 */
	public boolean updateAllFullNames(DataModelObjBase rootObj, final boolean useProgDlg,
			final boolean lockedByCaller, int minRank) throws Exception;
	
	/**
	 * @param rootObj
	 * @param useProgDlg
	 * @param lockedByCaller
	 * 
	 * @return true if update completes without errors, else false
	 * 
	 * @throws Exception
	 * 
	 * Walks the entire tree and assigns node numbers and fullname for every item.
	 */
	public boolean updateAllNodes(DataModelObjBase rootObj, final boolean useProgDlg, 
			final boolean lockedByCaller) throws Exception;
	
    /**
     * @param rootObj
     * @param useProgDlg
     * @param lockedByCaller
     * @param traversalLockedByCaller  true if tree traversal locks are already set.
     * @param checkForOtherLogins   if true, require user to confirm update if other users are logged in.
     * 
     * @return true if update completes without errors, else false
     * 
     * Walks the entire tree and assigns node numbers and fullname for every item.
     * 
     * @throws Exception
     */
    public boolean updateAllNodes(final DataModelObjBase rootObj, final boolean useProgDlg, 
            final boolean lockedByCaller, final boolean traversalLockedByCaller, 
            final boolean checkForOtherLogins) throws Exception;

    /**
     * @param rootObj
     * @param useProgDlg
     * @param lockedByCaller
     * @param traversalLockedByCaller
     * @param checkForOtherLogins
     * @param theSession
     * @return
     * @throws Exception
     */
    public boolean updateAllNodes(final DataModelObjBase rootObj, final boolean useProgDlg, 
            final boolean lockedByCaller, final boolean traversalLockedByCaller, 
            final boolean checkForOtherLogins, final DataProviderSessionIFace theSession) throws Exception;

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
     * Taxon tree would return the Species Rank Id. 
     * Returning '-1' means it doesn't matter.
     * @return the rank id of the level of the tree that items below it can be synonymized to.
     */
    public int getSynonymizedLevel();
    
    /**
     * 
     * @return true if synonymy is supported
     */
    public boolean isSynonymySupported();
    
    /**
     * @return a List of 'Standard' levels for the tree in order from lowest rank to highest.
     * Such as Continent, Country, State, County for a Geography Tree.  
     */
    public List<TreeDefItemStandardEntry> getStandardLevels();
    
    /**
     * @param levelRank
     * @return true if there is a required standard level with rank == levelRank.
     */
    public boolean isRequiredLevel(int levelRank);
    
    /**
     * When a new TreeDefItem with no children is created, it's rank can be determined by adding the value returned by this function
     * to it's parent's rank.
     * 
     * @return rank increment.
     */
    public int getRankIncrement();
	
}
