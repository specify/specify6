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
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.tasks.subpane.wb.graph.DirectedGraph;
import edu.ku.brc.specify.tasks.subpane.wb.graph.DirectedGraphException;
import edu.ku.brc.specify.tasks.subpane.wb.graph.Vertex;
import edu.ku.brc.specify.tasks.subpane.wb.schema.DBSchema;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Field;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Relationship;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Table;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Given a database schema, this class builds a DirectedGraph of the schema (with modifications) for use in workbench uploads. 
 */
public class GraphBuilder
{
    /**
     * The DBTableIdMgr instance for the database being graphed
     */
    private DBTableIdMgr                       schema;
    /**
     * The DBSchema representing the database being graphed.
     */
    private DBSchema scheme;
    /**
     * The graph constructed for the database.
     */
    private DirectedGraph<Table, Relationship> g;
    /**
     * short class names of tables to exclude from graph
     */
    private SortedSet<String> excludeTbls; 
    private static final Logger log = Logger.getLogger(GraphBuilder.class);
        
    
    /**
     * @param schema
     * @param scheme
     * @param excludeTbls
     */
    public GraphBuilder(final DBTableIdMgr schema, final DBSchema scheme, String[] excludeTbls)
    {
        this.schema = schema;
        this.scheme = scheme;
        this.buildExcludeTables(excludeTbls);
    }
    
    /**
     * @param aSchema
     * @return a graph of the schema, modified for use during workbench uploads
     * @throws DirectedGraphException
     */
    public DirectedGraph<Table, Relationship> buildGraph()
            throws DirectedGraphException
    {
        try
        {
            g = new DirectedGraph<Table, Relationship>();

            // add vertices
            for (DBTableInfo tbl : schema.getTables())
            {
                if (includeTable(tbl))
                {
                    g.addVertex(buildTableVertex(tbl));
                }
            }

            // add edges
            for (Vertex<Table> vTbl : g.getVertices())
            {
                DBTableInfo tbl = vTbl.getData().getTableInfo();
                for (DBRelationshipInfo rel : tbl.getRelationships())
                {
                    String relTblName = getRelShortName(rel);
                    if (relTblName != null && g.getVertexByLabel(relTblName) != null && includeEdge(tbl.getShortClassName().toLowerCase(), relTblName, rel))
                    {
                        Relationship relationship = getOneToManyRelationship(tbl, rel);
                        if (relationship != null && !isSystemRelationship(relationship))
                        {
                            g.addEdge(relTblName, tbl.getShortClassName().toLowerCase(), relationship);
                        }
                    }
                }
            }
            return g;
        }
        catch (DirectedGraphException ex)
        {
            throw ex;
        }
    }

    /**
     * @param tblName
     * @param relTblName
     * @param rel
     * @return false if the edge should be included in the graph
     */
    private boolean includeEdge(final String tblName, final String relTblName, final DBRelationshipInfo rel)
    {
        //only one-to-manys are needed
        //if (rel.getType() != DBRelationshipInfo.RelationshipType.OneToMany)
        if (rel.getType() != DBRelationshipInfo.RelationshipType.ManyToOne)
        {
            return false;
        }
        //no cycles
        if (tblName.equals(relTblName))
        {
            return false;
        }
        return true;
    }
    
    /**
     * @param tbl
     * @return true if tbl should be included in graph
     */
    private boolean includeTable(final DBTableInfo tbl)
    {
        return  !excludeTbls.contains(tbl.getShortClassName().toLowerCase());
    }
    
    /**
     * @param r
     * @return true if r involves a 'System' field that is populated by the application.
     * 
     */
    protected boolean isSystemRelationship(final Relationship r)
    {
        return r.getField().getName().equalsIgnoreCase("ModifiedByAgentID")
            || r.getField().getName().equalsIgnoreCase("CreatedByAgentID")
            || r.getRelatedField().getName().equalsIgnoreCase("ModifiedByAgentID")
            || r.getRelatedField().getName().equalsIgnoreCase("CreatedByAgentID")
        //more to come???
            ;
    }

    /**
     * @param tbl
     * @param rel
     * @return a Relationship object corresponding to a TableRelationship
     */
    private Relationship getOneToManyRelationship(final DBTableInfo tbl,
                                         final DBRelationshipInfo rel)
    {
        try
        {
            DBTableInfo oneSideTblInfo = schema.getByClassName(rel.getClassName());
            Table oneSideTbl = scheme.getTable(oneSideTblInfo.getShortClassName());
            Field oneSideFld = oneSideTbl.getField(oneSideTblInfo.getIdColumnName());
            Table manySideTbl = scheme.getTable(tbl.getShortClassName());
            String manySideFldName = rel.getColName() == null ? oneSideFld.getName() : rel.getColName();
            Field manySideFld = manySideTbl.getField(manySideFldName);
            if (oneSideFld == null || manySideFld == null)
            {
                log.debug(rel.getName() + ": one of the fields is null."); 
            }
            return new Relationship(oneSideFld, manySideFld, "OneToMany");
        }
        catch (RuntimeException ex)
        {
            log.debug("relationship " + rel.getName() + "could not be added to database graph.");
            return null;
        }
    }

    /**
     * @param rel
     * @return rel's shortname or null in case of difficulty
     */
    private String getRelShortName(final DBRelationshipInfo rel)
    {
        try
        {
            return schema.getByClassName(rel.getClassName()).getShortClassName().toLowerCase();
        } catch (RuntimeException ex)
        {
            return null;
        }
    }

    /**
     * @param tbl
     * @return Vertex for tbl
     */
    private Vertex<Table> buildTableVertex(DBTableInfo tbl)
    {
        Table table = scheme.getTable(tbl.getShortClassName());
        if (table == null)
        {
            log.debug("unable to find " + tbl.getShortClassName() + " in DBSchema.");
            return null;
        }
        return new Vertex<Table>(tbl.getShortClassName().toLowerCase(), table);
    }
    
    /**
     * @param badTbls 
     * 
     * builds a sortedSet of tables to exclude from graph.
     */
    private void buildExcludeTables(final String[] badTbls)
    {
        excludeTbls = new TreeSet<String>();
        for (String tbl : badTbls)
        {
            excludeTbls.add(tbl);
        }
    }
}
