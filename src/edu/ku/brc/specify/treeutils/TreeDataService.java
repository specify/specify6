/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.treeutils;

import java.util.List;
import java.util.Set;

import edu.ku.brc.dbsupport.CustomQueryListener;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.ui.treetables.TreeNode;
import edu.ku.brc.util.Pair;

/**
 * This interface defines a session facade for all clients needing access to tree-structured data.
 * 
 * @author jstewart
 * @code_status Complete
 * @param <T> an implementation class of {@link Treeable}
 * @param <D> an implementation class of {@link TreeDefIface}
 * @param <I> an implementation class of {@link TreeDefItemIface}
 */
public interface TreeDataService <T extends Treeable<T,D,I>,
									D extends TreeDefIface<T,D,I>,
									I extends TreeDefItemIface<T,D,I>>
{
	
	public final static int SUCCESS = 0;
	public final static int CANCELLED = 1;
	public final static int ERROR = 2;
	
	/**
     * Finds a tree node in the given tree with the given name.
     * 
	 * @param treeDef the tree to search
	 * @param name the node name to search for
	 * @return a {@link List} of matching nodes
	 */
	public List<T> findByName(D treeDef, String name, boolean isExact);
	
    /**
     * Returns the root node of the given tree.
     * 
	 * @param treeDef the tree to inspect
	 * @return the root node
	 */
	public T getRootNode(D treeDef);
	
    public T getNodeById(Class<?> clazz, int id);
    
    /**
     * Returns all of the trees of the given class.
     * 
	 * @param treeDefClass a class implementing {@link TreeDefIface}
	 * @return a {@link List} of trees of the given class
	 */
	public List<D> getAllTreeDefs(Class<D> treeDefClass);
	
    /**
     * Returns the tree having the given class and ID.
     * 
	 * @param defClass the tree class
	 * @param defId the tree ID
	 * @return the tree definition
	 */
	public D getTreeDef(Class<D> defClass, int defId);
    
    /**
     * Returns the {@link Set} of the children of the given node.
     * 
     * @param parent the node for which to gather the children
     * @return the {@link Set} of child nodes
     */
    public Set<T> getChildNodes(T parent);
    
    public List<TreeNode> getChildTreeNodes(T parent);
    
    /**
     * Returns the number of descendants of the given node.
     * 
     * @param node the node to inspect
     * @return the number of descendants
     */
    public int getDescendantCount(T node);
    
    /**
     * @param clazz
     * @param id
     * @param listener
     */
    public void calcRelatedRecordCount(final Class<?> clazz, 
                                       final int       id, 
                                       final CustomQueryListener listener);
    
    /**
     * Deletes the given node from the DB.
     * 
     * @param node the node to be deleted
     * @throws Exception when anything prevents the transaction from success
     * @return false if the transaction should be aborted, true otherwise
     */
    public boolean deleteTreeNode(T node);

    /**
     * @param objects
     */
    public void refresh(Object ... objects);
    
    /**
     * @param rankID
     * @param treeDef
     * @return
     */
    public int countNodesAtLevel(int rankID, D treeDef);
    
    /**
     * @param levelSkippedRank
     * @param treeDef
     * @return
     */
    public List<String> nodesSkippingOverLevel(int levelSkippedRank, D treeDef);
    
    /**
     * @param rankID
     * @param treeDef
     * @return
     */
    public List<String> nodeNamesAtLevel(int rankID, D treeDef);
    
    /**
     * @param newNode
     * @param session
     * @return
     * @throws Exception
     */
    public boolean updateNodeNumbersAfterNodeAddition(T newNode, DataProviderSessionIFace session) throws Exception;
    
    
    /**
     * @param node
     * @param session
     * @return
     * @throws Exception
     */
    public boolean updateNodeNumbersAfterNodeEdit(final T node, final DataProviderSessionIFace session) throws Exception;

    /**
     * @param deletedNode
     * @param session
     * @return
     * @throws Exception
     */
    public boolean updateNodeNumbersAfterNodeDeletion(T deletedNode, DataProviderSessionIFace session) throws Exception;
    
    /**
     * Moves the given node from its current parent to the given new parent node.  This can
     * also be used to attach a new node to a parent.  The node to be moved doesn't not need
     * to have a non-null parent before the call.
     * 
     * @param node the node to be moved
     * @param newParent the new parent node
     * @throws Exception when anything prevents the transaction from success
     * 
     * return SUCCESS, CANCEL or ERROR.
     */
    public int moveTreeNode(T node, T newParent);
    
    /**
     * Creates a logical link between two nodes.  This link's meaning is dependent on the type, T.  Some
     * implementations of T will ignore this call.  Others might setup any sort of association between the
     * two nodes <code>source</code> and <code>destination</code>.
     * 
     * @param source any node
     * @param destination any node
     * @return a localized, human-readable status message, or null
     */
    public String synonymize(T source, T destination);
    
    
    /**
     * Updates (clears) values of AcceptedParent and Accepted fields.
     * And for some types (current only Taxon) additional changes in other tables (Determination for Taxon) may be made.
     * 
     * @param node 
     * @return a localized, human-readable status message, or null
     */
    public String unSynonymize(T node);
    
    /**
     * @param node
     * @return
     */
    public Set<T> getSynonyms(T node);
    
    /**
     * @param clazz
     * @param nodeId
     * @return
     */
    public Set<Pair<Integer,String>> getSynonymIdsAndNames(Class<?> clazz, Integer nodeId);
    
    /**
     * Initializes any data associated with the given node.
     * 
     * @param node any tree node
     */
    public void initializeRelatedObjects(T node);
}
