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
package edu.ku.brc.specify.dbsupport;

import java.util.HashMap;

import edu.ku.brc.specify.datamodel.TreeDefIface;

/**
 * @author timo
 *
 */
public class TreeDefStatusMgr 
{
	
	protected static HashMap<Class<?>, TreeDefStatus> stats = new HashMap<Class<?>, TreeDefStatus>();
	
	/**
	 * @param treeDef
	 * @return TreeDefStatus object for treeDef
	 */
	protected static TreeDefStatus getStatus(TreeDefIface<?,?,?> treeDef)
	{
		TreeDefStatus treeDefStatus = stats.get(treeDef.getNodeClass());
		if (treeDefStatus == null)
		{
			treeDefStatus = new TreeDefStatus(treeDef);
			stats.put(treeDef.getNodeClass(), treeDefStatus);
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

	/**
	 * @param treeDef
	 * @return true if lock succeeds, else return false.
	 */
	public static boolean lockTree(TreeDefIface<?,?,?> treeDef, TaskSemaphoreMgrCallerIFace lockCallback)
	{
		return getStatus(treeDef).lockTree(lockCallback);
	}
	
	/**
	 * @param treeDef
	 * @return true if unlock succeeds, else return false.
	 */
	public static boolean unlockTree(TreeDefIface<?,?,?> treeDef)
	{
		return getStatus(treeDef).unlockTree();
	}
}
