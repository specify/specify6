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

import java.util.List;

import javax.swing.JDialog;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 * Runs tree node number update task.
 */
public class NodeNumberer<T extends Treeable<T, D, I>, D extends TreeDefIface<T, D, I>, I extends TreeDefItemIface<T, D, I>>
        extends TreeTraversalWorker<T, D, I>
{

    protected QueryIFace updateNodeQuery = null;
    protected JDialog progDlg = null;

    /**
     * @param treeDef
     */
    public NodeNumberer(final D treeDef)
    {
        super(treeDef);
    }

    /**
     * @param progDlg the progDlg to set.
     */
    public void setProgDlg(final JDialog progDlg)
    {
        this.progDlg = progDlg;
    }
    
    
    /* (non-Javadoc)
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done()
    {
        super.done();
        if (progDlg != null)
        {
            progDlg.setVisible(false);
        }
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
            traversalSession.beginTransaction();
            buildReNumberingQueries();
            T root = getTreeRoot();
            initProgress();
            initCacheInfo();
            reNumberNodesFaster(root.getTreeId(), 1);
            traversalSession.commit();
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
     * @return highest child node number.
     * @throws Exception
     * 
     * recursively walks tree and numbers nodes.
     */
    protected int reNumberNodesFaster(int nodeId, int nodeNumber) throws Exception
    {
        List<?> children = getChildIds(nodeId);
        int nn = nodeNumber;
        while (children.size() > 0)
        //for (Object child : children)
        {
            Object child = children.get(0);
        	nn = reNumberNodesFaster((Integer) child, nn + 1);
        	children.remove(0);
        	child = null;
        }
        children = null;
        writeNode(nodeId, nodeNumber, nn);
        incrementProgress();
        checkCache();
        return nn;
    }

    /**
     * @param nodeId
     * @param nodeNumber
     * @param highestChildNodeNumber
     * @throws Exception
     * 
     * Writes node number info to database.
     */
    protected void writeNode(int nodeId, int nodeNumber, int highestChildNodeNumber)
            throws Exception
    {
    	updateNodeQuery.setParameter("keyArg", nodeId);
        updateNodeQuery.setParameter("nnArg", nodeNumber);
        updateNodeQuery.setParameter("hcnArg", highestChildNodeNumber);
    	
        updateNodeQuery.executeUpdate();
    }

    /**
     * Creates queries used during number process.
     */
    protected void buildReNumberingQueries()
    {
        buildChildrenQuery();
        buildUpdateNodeQuery();
    }


    /**
     * creates query to update node number field.
     */
    protected void buildUpdateNodeQuery()
	{
		String updateSQL = "update "
				+ getNodeTblName()
				+ " set NodeNumber=:nnArg, HighestChildNodeNumber=:hcnArg where "
				+ getNodeKeyFldName() + "=:keyArg";
		updateNodeQuery = traversalSession.createQuery(updateSQL, true);
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.treeutils.TreeTraversalWorker#clearCache()
	 */
	@Override
	protected void clearCache() throws Exception
	{
		/* Every time updateNodeQuery.executeUpdate() is executed, an entry
		 * is added to the hibernate session.actionQueue.executions data structure.
		 * For large trees, out of memory errors occur.
		 * 
		 * Attempts to combine multiple node updates into one updateNodeQuery.executeUpdate() failed.
		 *
		 * Even when a transaction was not opened, the executions structure was filled (besides, with hibernate,
		 * session updates MUST be in a transaction or they do not actually get written to the db).
		 * 
		 * So, periodic commits are required. This means that an entire tree update cannot be rolled back, but
		 * in theory, the tree was not in correct shape before the rebuild began, so this is not so serious an issue.
		 */
		traversalSession.commit();
		traversalSession.beginTransaction();
	}
    
    
}
