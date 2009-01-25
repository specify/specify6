/**
 * 
 */
package edu.ku.brc.specify.treeutils;

import java.util.List;

import edu.ku.brc.dbsupport.DataProviderFactory;
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


	/**
	 * @param treeDef
	 */
	public FullNameRebuilder(final D treeDef) 
	{
		super(treeDef);
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	public Boolean doInBackground() throws Exception 
	{
        traversalSession = DataProviderFactory.getInstance().createSession();
        try
        {
            traversalSession.beginTransaction();
            buildQueries();
            T root = getTreeRoot();
            initProgress();
            rebuildFullNames(root.getTreeId());
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
     * recursively walks tree and builds full names.
     */
    protected void rebuildFullNames(int nodeId) throws Exception
    {
        List<?> children = getChildIds(nodeId);
        T node = traversalSession.get(this.treeDef.getNodeClass(), nodeId);
        writeNode(nodeId, TreeHelper.generateFullname(node));
        traversalSession.evict(node);
        node = null;
        for (Object child : children)
        {
            rebuildFullNames((Integer) child);
        }
        incrementProgress();
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
        // nodeNumberSession.beginTransaction();
        try
        {
            updateNodeQuery.executeUpdate();
        }
        finally
        {
            // nodeNumberSession.commit();
        }
    }

}
