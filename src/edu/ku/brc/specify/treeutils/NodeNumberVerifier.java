/* Copyright (C) 2009, University of Kansas Center for Research
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
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 * This class verifies that the ancestry returned using queries on nodenumber and highestchildnumbers matches ancestry obtained
 * by walking tree to root for every node in a tree.
 */
public class NodeNumberVerifier<T extends Treeable<T, D, I>, D extends TreeDefIface<T, D, I>, I extends TreeDefItemIface<T, D, I>>
        extends TreeTraversalWorker<T, D, I>
{
    /**
     * @param treeDef
     */
    public NodeNumberVerifier(final D treeDef)
    {
        super(treeDef);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    public Boolean doInBackground()
    {
        traversalSession = DataProviderFactory.getInstance().createSession();
        try
        {
            buildVerificationQueries();
            T root = getTreeRoot();
            LinkedList<Integer> parentIds = new LinkedList<Integer>();
            initProgress();
            verifyNodes(root.getTreeId(), root.getNodeNumber(), parentIds);
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
            traversalSession.close();
        }

    }

    /**
     * @param nodeId
     * @param nodeNumber
     * @param parentIds
     * @throws Exception
     * 
     * Recursively walks tree and verifies nodes.
     */
    protected void verifyNodes(int nodeId, int nodeNumber, LinkedList<Integer> parentIds)
            throws Exception
    {
        checkNode(nodeId, nodeNumber, parentIds);
        incrementProgress();
        List<?> children = getChildIds(nodeId);
        parentIds.push(nodeId);
        for (Object child : children)
        {
            Object childId = ((Object[]) child)[0];
            Object nodeNum = ((Object[]) child)[1];
            verifyNodes((Integer) childId, (Integer) nodeNum, parentIds);
        }
        parentIds.pop();
    }

    /**
     * @param nodeId
     * @param nodeNumber
     * @param parentIds
     * @throws Exception
     * 
     * checks that node's ancestry obtained through node number data matches path to root in parentIds.
     */
    protected void checkNode(int nodeId, int nodeNumber, List<Integer> parentIds) throws Exception
    {
        List<?> ancestors = getAncestors(nodeNumber);
        if (ancestors.size() != parentIds.size()) { throw new Exception(String.valueOf(nodeId)); }
        for (int a = 0; a < ancestors.size(); a++)
        {
            Object ancestor = ancestors.get(a);
            Integer parentId = parentIds.get(a);
            // System.out.println("checking " + nodeId + "," + nodeNumber + " - " + parentId + "=" +
            // ancestor + "?");
            if (parentId == null)
            {
                if (ancestor != null) { throw new Exception(String.valueOf(nodeId)); }
            }
            else
            {
                if (!parentId.equals(ancestor)) { throw new Exception(String.valueOf(nodeId)); }
            }
        }
    }

    /**
     * @param nodeNumber
     * @return ancestors of node to root of tree.
     */
    protected List<?> getAncestors(int nodeNumber)
    {
        ancestorQuery.setParameter("descendantArg", nodeNumber);
        return ancestorQuery.list();
    }

    /**
     * Creates query to obtained children.
     */
    protected void buildChildrenQueryForVerification()
    {
        String childrenSQL = "select " + getNodeKeyFldName() + ", nodenumber from "
                + getNodeTblName() + " where " + getNodeParentFldName()
                + " =:parentArg  order by name";
        childrenQuery = traversalSession.createQuery(childrenSQL, true);
    }

    /**
     * Creates ancestor query.
     */
    protected void buildAncestorQuery()
    {
        String ancestorSQL = "select "
                + getNodeKeyFldName()
                + " from "
                + getNodeTblName()
                + " where "
                + ":descendantArg between nodenumber and highestchildnodenumber  and :descendantArg <> nodenumber and "
                + getNodeTreeFldName() + "=" + treeDef.getTreeDefId() + " order by rankId desc";
        ancestorQuery = traversalSession.createQuery(ancestorSQL, true);
    }

    /**
     * Creates queries used during the verification process.
     */
    protected void buildVerificationQueries()
    {
        buildChildrenQueryForVerification();
        buildAncestorQuery();
    }
}
