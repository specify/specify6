/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ku.brc.af.ui.forms.formatters;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo.RelationshipType;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.SQLExecutionListener;
import edu.ku.brc.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.util.Pair;

/**
 * This class is responsible for getting samples of existing values for a given 
 * table field to prevent a new format to invalidate existing data 
 *
 * @author Ricardo
 *
 */
public class UIFieldFormatterSampler implements SQLExecutionListener
{
    protected static final Logger log = Logger.getLogger(UIFieldFormatterSampler.class);

    protected DBFieldInfo fieldInfo;
	protected List<Object> results;
	protected boolean ready;
	
	/**
	 * Constructor
	 * 
	 * @param fieldInfo
	 */
	public UIFieldFormatterSampler(DBFieldInfo fieldInfo)
	{
		this.results 	= new ArrayList<Object>();
		this.fieldInfo 	= fieldInfo;
		this.ready 		= false;

		processSamples();
	}
	
	/**
	 * Check whether the given formatter invalidates any of the existing field samples.
	 * If it does, return one of the samples that it invalidates.
	 * If it doesn't, return null
	 * 
	 * @param formatter formatter being checked for validity against field value samples
	 * @return A field sample that is invalidated by the formatter or null if no sample is invalidated
	 */
	public boolean isValid(UIFieldFormatterIFace formatter)
		throws UIFieldFormatterInvalidatesExistingValueException
	{
		if (!ready)
		{
			// could not get samples from DB for some reason, so just bypass test quietly
			return true;
		}
		
		for (Object obj : results)
		{
			if (!formatter.isValid(obj.toString()))
			{
				throw new UIFieldFormatterInvalidatesExistingValueException(
						"Format invalidates existing field value.", formatter.toPattern(), obj.toString());
			}
		}
		return true;
	}
	
	protected void processSamples()
	{
		String sql = getSql();
		SQLExecutionProcessor sqlProc = new SQLExecutionProcessor(this, sql);
		sqlProc.start();
	}
	
	public void exectionDone(final SQLExecutionProcessor process, final java.sql.ResultSet rs)
	{
		// process result set
		try
		{
			results.clear();
			while (rs.next())
			{
				results.add(rs.getObject(0));
			}
			rs.close();
			ready = true;
		}
		catch (SQLException e) 
		{
			results.clear();
			ready = false;
		}
	}
	
    public void executionError(final SQLExecutionProcessor process, final Exception ex)
    {
    	log.debug("Error processing SQL to get field value samples. " + ex.getMessage());
    	ex.printStackTrace();
    	ready = false;
    }
	
	protected String getSql()
	{
		String tableName = fieldInfo.getTableInfo().getName().toLowerCase();
		String fieldName = fieldInfo.getName();
		String joins	 = tableName.equals("collectionobject")? "" : getJoins();
		String sql = "SELECT " + tableName + "." + fieldName + " " + 
					"FROM " + tableName + joins + " " +
					"WHERE collectionobject.CollectionMemberID = COLMEMID";
		sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
		//System.out.println(sql + "\n");
		return sql;
	}
	
	protected String getJoins()
	{
		String joins = "";
		List<Pair<DBTableInfo, DBRelationshipInfo>> path = getShortestPath();
		for (Pair<DBTableInfo, DBRelationshipInfo> node : path)
		{
			DBRelationshipInfo rel = node.second;
			DBTableInfo firstTable = node.first;
			DBTableInfo secondTable = DBTableIdMgr.getInstance().getByClassName(rel.getClassName());
			
			String sql = "";
			if (rel.getType() == RelationshipType.OneToMany)
			{
				// must find the other direction of the relationship
				rel = secondTable.getRelationshipByName(rel.getOtherSide());

				sql = " INNER JOIN " + secondTable.getName() + 
				" ON " + secondTable.getName() + "." + rel.getColName() + " = " + 
				firstTable.getName() + "." + firstTable.getIdColumnName();
			}
			else if (rel.getType() == RelationshipType.ManyToOne)
			{
				sql = " INNER JOIN " + secondTable.getName() + 
				" ON " + firstTable.getName() + "." + rel.getColName() + " = " + 
				secondTable.getName() + "." + secondTable.getIdColumnName();
			}
			
			joins += sql + "";
			
/*			System.out.println("");
			System.out.println("FirstTable:   " + firstTable.getName());
			System.out.println("SecondTable:  " + secondTable.getName());
			System.out.println("Relationship: " + rel.getName());
			System.out.println("Class name:   " + rel.getClassName());
			System.out.println("Join table:   " + rel.getJoinTable());
			System.out.println("Col Name:     " + rel.getColName());
			System.out.println("Other side:   " + rel.getOtherSide());
			System.out.println("Rel Type:     " + rel.getType().toString());
			System.out.println("");
*/
		}
		return joins;
	}
	
	/**
	 * Finds the shortest path between the given table and the collectionobject table.
	 * It uses Dijkstra's algorithm to compute shortest path between tables.
	 * Graph is defined by table, field and relationship information provided by DBTableIdMgr class
	 * Vertices (or nodes) are the tables (represented by DBTableInfo objects) and edges are the 
	 * relationships between them (represented by DBRelationshipInfo objects). All distances are 1. 
	 * See: http://en.wikipedia.org/wiki/Dijkstra%27s_algorithm
	 */
	protected List<Pair<DBTableInfo, DBRelationshipInfo>> getShortestPath()
	{
		// hash that stored the distance between the source table and the given table
		Hashtable<DBTableInfo, Integer> distance = new Hashtable<DBTableInfo, Integer>();

		// list of previous vertex to a given vertex (table) 
		Hashtable<DBTableInfo, Pair<DBTableInfo, DBRelationshipInfo>> previous = 
			new Hashtable<DBTableInfo, Pair<DBTableInfo, DBRelationshipInfo>>();

		// set that marks the visited tables
		HashSet<DBTableInfo> visited = new HashSet<DBTableInfo>();

		// stack that holds vertices to be visited
		Stack<DBTableInfo> stack = new Stack<DBTableInfo>();
		
		// push source vertex into the stack and set distance to itself to zero
		stack.push(fieldInfo.getTableInfo());
		distance.put(fieldInfo.getTableInfo(), 0);

		// continue until we reach the destination vertix
		boolean destinationReached = false;
		DBTableInfo currentVertex = null;
		while (stack.size() > 0 && !destinationReached)
		{
			// get current vertex from stack
			currentVertex = stack.pop();

			// mark current vertex as visited
			visited.add(currentVertex);

			// distance so far
			Integer distanceToCurrent = distance.get(currentVertex);
	
			// visit each neighbor of the current vertex that hasn't been visited yet
			List<DBRelationshipInfo> relationships = currentVertex.getRelationships();
			for (DBRelationshipInfo relationship : relationships)
			{
				DBTableInfo neighborTable = DBTableIdMgr.getInstance().getByClassName(relationship.getClassName()); 

				if (visited.contains(neighborTable) ||
					neighborTable.getName().equals(currentVertex.getName()))
				{
					// already visited or it is a relationships to the same table
					continue;
				}
				
				// compute distance to neighbor as distance to the current node plus 1
				Integer distanceToNeighbor = distance.get(neighborTable);
				if (distanceToNeighbor == null || 
					distanceToCurrent + 1 < distanceToNeighbor)
				{
					distance.put(neighborTable, new Integer(distanceToCurrent + 1));
					previous.put(neighborTable, new Pair<DBTableInfo, DBRelationshipInfo>(currentVertex, relationship));
				}
				
				// check if we reached the destination
				if (neighborTable.getName().equals("collectionobject"))
				{
					// destination reached: bail out
					currentVertex = neighborTable;
					destinationReached = true;
					break;
				}

				// not the destination table: just add neighbor to list of nodes to visit
				stack.push(neighborTable);
			}
		}
		
		// list of relationships that define the shortest path between source and destination tables
		List<Pair<DBTableInfo, DBRelationshipInfo>> path = new ArrayList<Pair<DBTableInfo, DBRelationshipInfo>>();

		if (destinationReached)
		{
			Pair<DBTableInfo, DBRelationshipInfo> node = previous.get(currentVertex);
			while (node != null)
			{
				path.add(0, node);
				DBTableInfo relTable = DBTableIdMgr.getInstance().getInfoByTableName(node.first.getName()); 
				node = previous.get(relTable);
			}
		}
		
		return path;
	}
}
