/**
 * 
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
 * @author timo
 *
 */
public class TreeRebuilder<T extends Treeable<T, D, I>, 
				D extends TreeDefIface<T, D, I>, 
				I extends TreeDefItemIface<T, D, I>> extends TreeTraversalWorker<T, D, I> 
{
    protected FullNameBuilder<T,D,I> fullNameBuilder;
    protected final int minRank;
    protected QueryIFace updateNodeQuery = null;
    protected final boolean doNodeNumbers;
    protected final boolean doFullNames;
    
	/**
	 * @param treeDef
	 * @param traversalSession
	 * @param minRank
	 * @param doNodeNumbers
	 * @param doFullNames
	 */
	public TreeRebuilder(final D treeDef, final DataProviderSessionIFace traversalSession, 
			final int minRank, final boolean doNodeNumbers, final boolean doFullNames) 
	{
		super(treeDef);
		this.minRank = minRank;
		this.doNodeNumbers = doNodeNumbers;
		this.doFullNames = doFullNames;
	}
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Boolean doInBackground() throws Exception 
	{
        traversalSession = DataProviderFactory.getInstance().createSession();
        try
        {
            traversalSession.beginTransaction();
            buildChildrenQuery();
            buildUpdateNodeQuery();
            T root = getTreeRoot();
            initProgress();
            initCacheInfo();
            fullNameBuilder = new FullNameBuilder<T,D,I>(treeDef);
            rebuildTree(new TreeNodeInfo(root.getTreeId(), root.getRankId(), root.getName()), 
            		new LinkedList<TreeNodeInfo>(), 1);
            traversalSession.commit();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NodeNumberer.class, e);
            return false;
        }
        finally
        {
            traversalSession.close();
        }
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.treeutils.TreeTraversalWorker#buildChildrenQuery()
     */
    @Override
    protected void buildChildrenQuery()
    {
        String childrenSQL = "select " + getNodeKeyFldName() + ", " + getNodeTblName() + ".Name, RankId from " + getNodeTblName()
                + " where " + getNodeParentFldName() + " =:parentArg  order by name";
        childrenQuery = traversalSession.createQuery(childrenSQL, true);
    }

	/**
	 * @param node
	 * @param parents
	 * @param nodeNumber
	 * @return highest nodenumber in the tree under node.
	 * @throws Exception
	 * 
	 * Traverses the tree rooted at node and assigns new node numbers and/or fullnames
	 */
	protected int rebuildTree(TreeNodeInfo node, LinkedList<TreeNodeInfo> parents, int nodeNumber) throws Exception {
        int nn = nodeNumber;
		List<?> children = getChildrenInfo(node);
        if (doFullNames) 
        {
        	parents.addLast(node);
        }
        while (children.size() > 0)
        {
            Object child = children.get(0);
            Object[] childInfo = (Object[] )child;
            nn = rebuildTree(new TreeNodeInfo((Integer )childInfo[0], (Integer )childInfo[2], (String )childInfo[1]),
        			parents, nn + 1);
        	children.remove(0);
        	child = null;
       }
       children = null;
       String fullName = null;
       if (doFullNames) 
       {
           parents.removeLast();
           fullName = fullNameBuilder.buildFullName(node, parents);
       }
       writeNode(node.getId(), fullName, nodeNumber, nn);
       incrementProgress();
       checkCache();
       return nn;
	}	   
    
    /**
     * @param node
     * @return list of node's children
     */
    protected List<?> getChildrenInfo(final TreeNodeInfo node)
    {
        childrenQuery.setParameter("parentArg", node.getId());
        return childrenQuery.list();
    }

    /**
     * @param nodeId
     * @param fullName
     * @param nodeNumber
     * @param highestChildNodeNumber
     * @throws Exception
     * 
     * sets parameters for updataNodeQuery and executes it.
     */
    protected void writeNode(int nodeId, String fullName, int nodeNumber, int highestChildNodeNumber) throws Exception
    {
    	updateNodeQuery.setParameter("keyArg", nodeId);
    	if (doFullNames) 
    	{
    		updateNodeQuery.setParameter("fnArg", fullName);
    	}
    	if (doNodeNumbers) 
    	{
            updateNodeQuery.setParameter("nnArg", nodeNumber);
            updateNodeQuery.setParameter("hcnArg", highestChildNodeNumber);    		
    	}
    	updateNodeQuery.executeUpdate();
    }
    
    /**
     * Builds query used to write nodes to the db.
     */
    protected void buildUpdateNodeQuery()
	{
		String updateSQL = "update " + getNodeTblName() + " set ";
		if (doNodeNumbers) 
		{
			updateSQL += "NodeNumber=:nnArg, HighestChildNodeNumber=:hcnArg";
			if (doFullNames) 
			{
				updateSQL += ", ";
			}
		}
		if (doFullNames) 
		{
			updateSQL += "FullName=:fnArg";
		}
		updateSQL += " where " + getNodeKeyFldName() + "=:keyArg";
		updateNodeQuery = traversalSession.createQuery(updateSQL, true);
	}
    
}
