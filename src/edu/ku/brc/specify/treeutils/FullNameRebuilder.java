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
package edu.ku.brc.specify.treeutils;

import java.util.LinkedList;
import java.util.List;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 * @author Administrator
 *
 */
public class FullNameRebuilder<T extends Treeable<T, D, I>, D extends TreeDefIface<T, D, I>, I extends TreeDefItemIface<T, D, I>> 
	extends TreeTraversalWorker<T,D,I> {

    protected QueryIFace updateNodeQuery = null;
    protected final int minRank;
    protected final boolean ownsSession;
    protected FullNameBuilder<T,D,I> fullNameBuilder;

	/**
	 * @param treeDef
	 */
	public FullNameRebuilder(final D treeDef, final DataProviderSessionIFace traversalSession, final int minRank) 
	{
		super(treeDef);
		this.traversalSession = traversalSession;
		this.ownsSession = traversalSession == null;
		this.minRank = minRank;
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	public Boolean doInBackground() throws Exception 
	{
        if (ownsSession)
        {
        	traversalSession = DataProviderFactory.getInstance().createSession();
        }
        try
        {
            if (ownsSession)
            {
            	traversalSession.beginTransaction();
            }
            buildQueries();
            T root = getTreeRoot();
            initProgress();
            initCacheInfo();
            fullNameBuilder = new FullNameBuilder<T,D,I>(treeDef);
            rebuildFullNames(new TreeNodeInfo(root.getTreeId(), root.getRankId(), root.getName(), root.getIsAccepted()), new LinkedList<TreeNodeInfo>());
            if (ownsSession)
            {
            	traversalSession.commit();
            }
            return true;
        }
        catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NodeNumberer.class, e);
            return false;
        }
        finally
        {
            if (ownsSession)
            {
            	traversalSession.close();
            }
        }
	}
    /**
     * @param nodeId
     * @param nodeNumber
     * @return highest child node number.
     * @throws Exception
     * 
     * recursively walks tree and builds full names.
     */
    protected void rebuildFullNames(TreeNodeInfo node, LinkedList<TreeNodeInfo> parents) throws Exception
    {
        List<?> children = getChildrenInfo(node);
        if (node.getRank() >= minRank)
        {
        	writeNode(node.getId(), fullNameBuilder.buildFullName(node, parents));
        }
        parents.addLast(node);
        for (Object child : children)
        {
            Object[] childInfo = (Object[] )child;
        	rebuildFullNames(new TreeNodeInfo((Integer )childInfo[0], (Integer )childInfo[2], (String )childInfo[1],
        			(Boolean )childInfo[3]),
        			parents);
        }
        parents.removeLast();
        incrementProgress();
        checkCache();
    }
    /**
     * @param parentId
     * @return list of NodeInfo objects for children of parentId.
     */
    protected List<?> getChildrenInfo(final TreeNodeInfo node)
    {
        childrenQuery.setParameter("parentArg", node.getId());
        return childrenQuery.list();
    }

    /**
     * Creates queries used during process.
     */
    protected void buildQueries()
    {
        buildChildrenQuery();
        buildUpdateNodeQuery();
    }


    /**
     * creates query to update fullname field.
     */
    protected void buildUpdateNodeQuery()
    {
        String updateSQL = "update " + getNodeTblName()
                + " set FullName=:fnArg where "
                + getNodeKeyFldName() + "=:keyArg";
        updateNodeQuery = traversalSession.createQuery(updateSQL, true);
    }

    /**
     * @param nodeId
     * @param nodeNumber
     * @param highestChildNodeNumber
     * @throws Exception
     * 
     * Writes node number info to database.
     */
    protected void writeNode(int nodeId, String fullName)
            throws Exception
    {
        updateNodeQuery.setParameter("keyArg", nodeId);
        updateNodeQuery.setParameter("fnArg", fullName);
        updateNodeQuery.executeUpdate();
    }

    /**
     * Creates query used to retrieve children.
     */
    @Override
    protected void buildChildrenQuery()
    {
        String childrenSQL = "select " + getNodeKeyFldName() + ", " + getNodeTblName() + ".Name, RankId from " + getNodeTblName()
                + " where " + getNodeParentFldName() + " =:parentArg  order by name";
        childrenQuery = traversalSession.createQuery(childrenSQL, true);
    }
}
