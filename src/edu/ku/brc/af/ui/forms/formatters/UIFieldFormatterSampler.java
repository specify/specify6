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
import edu.ku.brc.specify.datamodel.UserGroupScope;
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
	
	static DBTableInfo userGroupScopeFakeTableInfo = new DBTableInfo(-1, UserGroupScope.class.getCanonicalName(), "UserGroupScope", "", "");
	
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
	public boolean isValid(UIFieldFormatterIFace formatter) throws UIFieldFormatterInvalidatesExistingValueException
	{
	    if (formatter != null)
	    {
    		if (!ready)
    		{
    			// could not get samples from DB for some reason, so just bypass test quietly
    			return true;
    		}
    		
    		for (Object obj : results)
    		{
    			if (obj == null || !formatter.isValid(obj.toString()))
    			{
    				throw new UIFieldFormatterInvalidatesExistingValueException(
    						"Format invalidates existing field value.", formatter.toPattern(), obj == null ? "NULL" : obj.toString());
    			}
    		}
    		return true;
    		
	    } else
	    {
	        log.error("Formatter was null!");
	    }
	    return false;
	}
	
	protected void processSamples()
	{
		String sql = getSql();
		if (sql != null)
		{
		    SQLExecutionProcessor sqlProc = new SQLExecutionProcessor(this, sql);
		    sqlProc.start();
		}
	}
	
	public void exectionDone(final SQLExecutionProcessor process, final java.sql.ResultSet rs)
	{
		// process result set
		try
		{
			results.clear();
			while (rs.next())
			{
				results.add(rs.getObject(1));
			}
			rs.close();
			ready = true;
		}
		catch (SQLException e) 
		{
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UIFieldFormatterSampler.class, e);
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
	    DBTableInfo tblInfo   = fieldInfo.getTableInfo();
		String      tableName = tblInfo.getName();
		if (tblInfo != null)
		{
		    DBFieldInfo colMemIdField = tblInfo.getFieldByColumnName("CollectionMemberID");
    		String      fieldName     = fieldInfo.getName();
    		String      joins	      = tableName.equals("collectionobject")? "" : getJoins();
    		String      sql           = "SELECT " + tableName + "." + fieldName + " " + 
    					                "FROM " + tableName + joins + " " +
    					                (colMemIdField != null ? ("WHERE " + tableName + ".CollectionMemberID  = COLMEMID") : "");
    		sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
    		//System.out.println(sql + "\n");
    		return sql;
		}
		return null;
	}
	
	protected String getJoins()
	{
		StringBuilder joins = new StringBuilder();
		
		List<Pair<DBTableInfo, DBRelationshipInfo>> path = getShortestPath();
		for (Pair<DBTableInfo, DBRelationshipInfo> node : path)
		{
			DBRelationshipInfo rel = node.second;
			DBTableInfo firstTable = node.first;
			DBTableInfo secondTable = DBTableIdMgr.getInstance().getByClassName(rel.getClassName());
			
//			System.out.println("");
//			System.out.println("FirstTable:   " + firstTable.getName());
//			System.out.println("SecondTable:  " + secondTable.getName());
//			System.out.println("Relationship: " + rel.getName());
//			System.out.println("Class name:   " + rel.getClassName());
//			System.out.println("Join table:   " + rel.getJoinTable());
//			System.out.println("Col Name:     " + rel.getColName());
//			System.out.println("Other side:   " + rel.getOtherSide());
//			System.out.println("Rel Type:     " + rel.getType().toString());
//			System.out.println("");

			String sql = "";
			if (rel.getType() == RelationshipType.OneToMany)
			{
				// must find the other direction of the relationship
				rel = secondTable.getRelationshipByName(rel.getOtherSide());

				sql = " INNER JOIN " + secondTable.getName() + 
				" ON " + secondTable.getName() + "." + rel.getColName() + " = " + 
				firstTable.getName() + "." + firstTable.getIdColumnName();
			}
			else if (rel.getType() == RelationshipType.ManyToOne || 
			         rel.getType() == RelationshipType.OneToOne)
			{
				sql = " INNER JOIN " + secondTable.getName() + 
				" ON " + firstTable.getName() + "." + rel.getColName() + " = " + 
				secondTable.getName() + "." + secondTable.getIdColumnName();
			}
			else if (rel.getType() == RelationshipType.ManyToMany)
			{
			    String joinTable = firstTable.getName() + "_" + secondTable.getName();
			    
			    // XXX in the case of agent_discipline table, the field name in the join table (DisciplineID) isn't the same as in the discipline table (UserGroupScopeId)
			    // also, there's no such information on the DBRelationshipInfo instance, so we treat this case separately
			    String joinTableSecondIdColumnName = secondTable.getIdColumnName();
			    if ("agent_discipline".equals(joinTable))
			    {
			        joinTableSecondIdColumnName = "DisciplineID";
			    }
			    
                sql = " INNER JOIN " + joinTable + " ON " + firstTable.getName() + "." +
                    firstTable.getIdColumnName() + " = " + joinTable + "." + firstTable.getIdColumnName() + 
                    " INNER JOIN " + secondTable.getName() + " ON " + secondTable.getName() + "." + 
                    secondTable.getIdColumnName() + " = " + joinTable + "." + joinTableSecondIdColumnName;

                // System.out.println(sql);
			}
			else
			{
			    // don't know what to do, really
			   log.warn("Relationship Type: " + rel.getType());
			}
			
			joins.append(sql);
		}
		return joins.toString();
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
		// stores the distance between the source table and the given table
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

		// continue until we reach the destination vertex
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
				DBTableInfo neighborTable = getTableInfo(relationship.getClassName()); 

                // System.out.println(String.format("Dist: %4d | Link: %30s - %s", distanceToCurrent, currentVertex.getName(), neighborTable.getName()));

				if (visited.contains(neighborTable) ||
					neighborTable.getName().equals(currentVertex.getName()))
				{
					// already visited or it is a relationships to the same table
//                    System.out.println(String.format("Already visited: %30s | curr=**** | dist=%4d", neighborTable.getName(), distanceToCurrent));
					continue;
				}
				
				// compute distance to neighbor as distance to the current node plus 1
				Integer distanceToNeighbor = distance.get(neighborTable);
				if (distanceToNeighbor == null || 
					distanceToCurrent + 1 < distanceToNeighbor)
				{
//				    System.out.println(String.format("neighborTable:   %30s | curr=%4d | dist=%4d", neighborTable.getName(), distanceToNeighbor , distanceToCurrent));
					distance.put(neighborTable, new Integer(distanceToCurrent + 1));
					previous.put(neighborTable, new Pair<DBTableInfo, DBRelationshipInfo>(currentVertex, relationship));
				}
				
				// check if we reached the destination
				if ("collectionobject".equals(neighborTable.getName()))
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

	/**
	 * Returns the DBTableInfo corresponding to the class name provided.
	 * Returns a fake DBTableInfo to represent the UserGroupScope table.
	 * 
	 * @param className
	 * @return
	 */
	private DBTableInfo getTableInfo(final String className) {
        
        // treat UserGroupScope as a special table
        if (UserGroupScope.class.getCanonicalName().equals(className))
        {
            // UserGroupScope doesn't have a table of its own, but it is composed of the
            // tables associated with its sub-classes: institution, discipline, division, collection. 
            // We will return a fake DBTableInfo instance that represents a dead-end in the graph,
            // just to fool the shortest path algorithm.
            return userGroupScopeFakeTableInfo; 
        }
        return DBTableIdMgr.getInstance().getByClassName(className); 
    }
}
