/**
 * 
 */
package edu.ku.brc.specify.treeutils;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules;


/**
 * @author timo
 *
 *First shot at a utility for 'merging' tree nodes.
 *
 *If a taxon tree had an Order named Anordera and another Order named Anodera, a merge of Anodera into Anordera
 *would move Anodera's children to Anordera, recursively merging equivalent children.
 *
 *Initial version is intended to be quick and usable and testable. Does not use hibernate.
 *
 */
public class TreeMerger<N extends Treeable<N,D,I>,
			D extends TreeDefIface<N,D,I>,
			I extends TreeDefItemIface<N,D,I>>
{
	final TreeDefIface<N,D,I> treeDef;
	final DBTableInfo nodeTable;
	List<TreeMergerUIIFace<N,D,I>> listeners = new Vector<TreeMergerUIIFace<N,D,I>>();
	Connection connection = null;
	
	/**
	 * @param treeDef
	 */
	public TreeMerger(final TreeDefIface<N,D,I> treeDef)
	{
		this.treeDef = treeDef;
		nodeTable = getNodeTable();
	}
	
	/**
	 * @param listener
	 */
	public void addListener(TreeMergerUIIFace<N,D,I> listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * @param listener
	 */
	public void removeListener(TreeMergerUIIFace<N,D,I> listener)
	{
		listeners.remove(listener);
	}
	
	/**
	 * @return tableInfo for Node type
	 */
	public DBTableInfo getNodeTable()
	{
		return DBTableIdMgr.getInstance().getByClassName(treeDef.getNodeClass().getName());
	}
	
	/**
	 * @return name of parent id field for node table
	 */
	protected String getParentFld()
	{
		return "parentId";
	}
	
	/**
	 * @return name of the accepted field for node table
	 */
	protected String getAcceptedFld()
	{
		return "isaccepted";
	}
	
	/**
	 * @return name of the name field for node table
	 */
	public String getNameFld()
	{
		return "name";
	}
	
	/**
	 * @return
	 */
	public String getFullNameFld()
	{
		return "fullname";
	}
	
	/**
	 * @param parentId
	 * @return children of parent with parentId
	 */
	protected List<Object[]> getChildren(final Integer parentId)
	{
		return BasicSQLUtils.query(connection, "select " + nodeTable.getIdFieldName() + ", " + getNameFld() + ", "
				+ getAcceptedFld() + " from " + nodeTable.getName() + " where "
				+ getParentFld() + " = " + parentId);
	}
	
	
	/**
	 * @param child
	 * @param parentId
	 * 
	 * @return id of child of parentId with same name and accepted status as child, or null if no such child exists.
	 * 
	 * @throws Exception if more than matching child exists.
	 */
	protected Integer getMatch(final Object[] child, final Integer parentId) throws TreeMergeException
	{
		Boolean isAccepted = (Boolean )child[2];
		String sql = "select " + nodeTable.getIdFieldName() + " from " + nodeTable.getName() + " where "
			+ getNameFld() + " = " + BasicSQLUtils.getEscapedSQLStrExpr((String )child[1]) + " and "
			+ (!isAccepted ? "not " : "") + getAcceptedFld() + " and " + getParentFld() +  " = "
			+ parentId;
		Vector<Object[]> matches = BasicSQLUtils.query(connection, sql);
		if (matches.size() == 0)
		{
			return null;
		}
		if (matches.size() == 1)
		{
			return (Integer )matches.get(0)[0];
		}
		//XXX prompt user???
		throw new TreeMergeException(this, (String )child[1], parentId);  
	}
	
	/**
	 * @param toMergeId
	 * @param mergeIntoId
	 */
	protected void mergeTreeIntoTree(final Integer toMergeId, final Integer mergeIntoId) throws Exception
	{		
		for (TreeMergerUIIFace<N,D,I> face : listeners)
		{
			face.merging(toMergeId, mergeIntoId);
		}
		List<Object[]> children = getChildren(toMergeId);
		for (Object[] child : children)
		{
			Integer matchingChildId = getMatch(child, mergeIntoId);
			if (matchingChildId == null)
			{
				move((Integer )child[0], mergeIntoId);
			}	
			else
			{
				mergeTreeIntoTree((Integer )child[0], matchingChildId);
			}
		}
		mergeNodes(toMergeId, mergeIntoId);
		for (TreeMergerUIIFace<N,D,I> face : listeners)
		{
			face.merged(toMergeId, mergeIntoId);
		}
	}
	
	/**
	 * @param toMergeId
	 * @param mergeIntoId
	 * @throws Exception
	 */
	public void mergeTrees(final Integer toMergeId, final Integer mergeIntoId) throws Exception
	{
		boolean transOpen = false;
		connection = DBConnection.getInstance().createConnection();
		connection.setAutoCommit(false);
		//XXX more here to init transaction??
		transOpen = true;
		try
		{
			mergeTreeIntoTree(toMergeId, mergeIntoId);
			connection.commit();
			transOpen = false;
		}
		catch (Exception ex)
		{
			if (transOpen)
			{
				connection.rollback();
			}
			throw ex;
		}
		finally
		{
			connection.close();
			connection = null;
		}
	}
	
	/**
	 * @param toMergeId
	 * @param mergeIntoId
	 */
	protected void mergeNodes(final Integer toMergeId, final Integer mergeIntoId) throws Exception
	{
		preMerge(toMergeId, mergeIntoId);
		Statement stmt = connection.createStatement();
		try
		{
			stmt.execute("delete from " + nodeTable.getName() + " where " + nodeTable.getIdFieldName() 
					+ " = " + toMergeId);
		}
		finally
		{
			stmt.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected List<String> getPreMergeSql(final Integer toMergeId, final Integer mergeIntoId)
	{
		Vector<String> result = new Vector<String>();
		BaseTreeBusRules<N,D,I> busRules = (BaseTreeBusRules<N,D,I> )DBTableIdMgr.getInstance().getBusinessRule(treeDef.getNodeClass());
		if (busRules != null)
		{
			String[] rels = busRules.getAllRelatedTableAndColumnNames();	
			for (int i = 0; i < rels.length; i += 2)
			{
				result.add("update " + rels[i] + " set " + rels[i+1] + " = " + mergeIntoId 
						+ " where " + rels[i+1] + " = " + toMergeId);
			}
		}
		return result;
	}
	/**
	 * @param toMergeId
	 * @param mergeIntoId
	 * @throws Exception
	 */
	protected void preMerge(final Integer toMergeId, final Integer mergeIntoId) throws TreeMergeException, Exception
	{
		//business rules??? Use hibernate here??
		List<String> updaters = getPreMergeSql(toMergeId, mergeIntoId);
		if (updaters.size() > 0)
		{
			Statement stmt = connection.createStatement();
			for (String sql : updaters)
			{
				BasicSQLUtils.exeUpdateCmd(stmt, sql);
			}
		}		
		else 
		{
			throw new TreeMergeException(this, null, toMergeId, mergeIntoId, null, TreeMergeException.UNSPECIFIED);
		}
	}
	
	/**
	 * @param toMoveId
	 * @param parentId
	 * @throws Exception
	 * 
	 * Changes toMoveId's parentId to parentId.
	 */
	protected void move(final Integer toMoveId, final Integer parentId) throws Exception
	{
		String sql = "update " + nodeTable.getName() + " set " + getParentFld() + " = " + parentId 
			+ " where " + nodeTable.getIdFieldName() + " = " + toMoveId;
		Statement stmt = connection.createStatement();
		try
		{
			BasicSQLUtils.exeUpdateCmd(stmt, sql);
		}
		finally
		{
			stmt.close();
		}
		for (TreeMergerUIIFace<N,D,I> face : listeners)
		{
			face.moved(toMoveId, null, parentId);
		}
	}
}
