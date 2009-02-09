/**
 * 
 */
package edu.ku.brc.specify.dbsupport;

import java.util.HashMap;

import edu.ku.brc.specify.datamodel.TreeDefIface;

import edu.ku.brc.specify.dbsupport.TreeDefStatus;

/**
 * @author timo
 *
 */
public class TreeDefStatusMgr 
{
	
	protected static HashMap<Integer, TreeDefStatus> stats = new HashMap<Integer, TreeDefStatus>();
	
	/**
	 * @param treeDef
	 * @return TreeDefStatus object for treeDef
	 */
	protected static TreeDefStatus getStatus(TreeDefIface<?,?,?> treeDef)
	{
		TreeDefStatus treeDefStatus = stats.get(treeDef.getTreeDefId());
		if (treeDefStatus == null)
		{
			treeDefStatus = new TreeDefStatus(treeDef);
			stats.put(treeDef.getTreeDefId(), treeDefStatus);
		}
		if (treeDefStatus == null)
		{
			throw new RuntimeException("Unable to create Tree Definition Status Object.");
		}
		return treeDefStatus;
	}
	
	/**
	 * @param treeDef
	 * @return isNodeNumbersAreUpToDate for treeDef
	 */
	public static boolean isNodeNumbersAreUpToDate(TreeDefIface<?,?,?> treeDef)
	{
		return getStatus(treeDef).isNodeNumbersAreUpToDate();
	}
	
	/**
	 * @param treeDef
	 * @param nodeNumbersAreUpToDate
	 */
	public static void setNodeNumbersAreUpToDate(TreeDefIface<?,?,?> treeDef, boolean nodeNumbersAreUpToDate)
	{
		getStatus(treeDef).setNodeNumbersAreUpToDate(nodeNumbersAreUpToDate);
	}
	
	/**
	 * @param treeDef
	 * @return isRenumberingNodes for treeDef
	 */
	public static boolean isRenumberingNodes(TreeDefIface<?,?,?> treeDef)
	{
		return getStatus(treeDef).isRenumberingNodes();
	}

	/**
	 * @param treeDef
	 * @param renumberingNodes
	 */
	public static void setRenumberingNodes(TreeDefIface<?,?,?> treeDef, boolean renumberingNodes)
	{
		getStatus(treeDef).setRenumberingNodes(renumberingNodes);
	}

	/**
	 * @param treeDef
	 * @return isDoNodeNumberUpdates for treeDef
	 */
	public static boolean isDoNodeNumberUpdates(TreeDefIface<?,?,?> treeDef)
	{
		return getStatus(treeDef).isDoNodeNumberUpdates();
	}

	/**
	 * @param treeDef
	 * @param doNodeNumberUpdates
	 */
	public static void setDoNodeNumberUpdates(TreeDefIface<?,?,?> treeDef, boolean doNodeNumberUpdates)
	{
		getStatus(treeDef).setDoNodeNumberUpdates(doNodeNumberUpdates);
	}
	
	/**
	 * @param treeDef
	 * @return isUploadInProgress for treeDef
	 */
	public static boolean isUploadInProgress(TreeDefIface<?,?,?> treeDef)
	{
		return getStatus(treeDef).isUploadInProgress();
	}

	/**
	 * @param treeDef
	 * @param uploadInProgress
	 */
	public static void setUploadInProgress(TreeDefIface<?,?,?> treeDef, boolean uploadInProgress)
	{
		getStatus(treeDef).setUploadInProgress(uploadInProgress);
	}

}
