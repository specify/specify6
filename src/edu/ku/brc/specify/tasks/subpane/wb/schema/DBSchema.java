package edu.ku.brc.specify.tasks.subpane.wb.schema;

import java.util.Collection;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.specify.tasks.subpane.wb.graph.DirectedGraph;
import edu.ku.brc.specify.tasks.subpane.wb.graph.DirectedGraphException;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.DB;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.GraphBuilder;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * This class pretty much just a wrapper for the DBTableIdMgr class, with methods added to
 * access information (i.e. foreign key names) needed to simplify workbench uploads.
 * 
 */
public class DBSchema
{
    /**
     * The tables in the schema. 
     */
    protected TreeMap<String, Table> tables;
    /**
     * The relatinoships between tables.
     */
    protected Vector<Relationship>   relationships;
    /**
     * The underlying DBTableIdMgr instance.
     */
    protected final DBTableIdMgr     scheme;
    private static final Logger      log = Logger.getLogger(DBSchema.class);
    /**
     * The DB object that owns this schema.
     */
    protected DB                     db;

    public DBSchema(final DBTableIdMgr scheme, final DB db)
    {
        this.scheme = scheme;
        this.db = db;
        //setupSchema();
        buildSchema();
    }

    /**
     * Builds schema based on DBTableIdMgr instance stored in this.scheme.
     */
    protected void buildSchema()
    {
        tables = new TreeMap<String, Table>();
        for (DBTableInfo tbl : scheme.getTables())
        {
            Table newTbl = new Table(this, tbl);
            tables.put(newTbl.getName().toLowerCase(), newTbl);
        }
        // add foreign keys
        for (Table tbl : getTables())
        {
            Class<?> tblClass = tbl.getTableInfo().getClassObj();
            if (Treeable.class.isAssignableFrom(tblClass))
            {
                log.debug("adding foreign key: " + tbl.getName() + ".parentid");
                tbl.addField(new Field("parentid", tbl.getTableInfo().getIdType()));
            }
            for (DBRelationshipInfo rel : tbl.getTableInfo().getRelationships())
            {
                String fld2Name = rel.getColName();
                if (fld2Name == null)
                {
                    fld2Name = tbl.getTableInfo().getIdFieldName();
                }
                Field fld2 = tbl.getField(fld2Name);
                if (fld2 == null)
                {
                    log.debug("adding foreign key: " + tbl.getName() + "." + fld2Name);
                    tbl.addField(new Field(fld2Name, tbl.getTableInfo().getIdType(), rel.isRequired()));
                }
            }
        }
        relationships = new Vector<Relationship>();
        // don't actually need relationships if DBTableIdMgr is present
    }


    /**
     * @param tblName
     * @param fldName
     * @return the Field object named fldName from the table named tblNamed.
     */
    public Field getField(final String tblName, final String fldName)
    {
        Table t = getTable(tblName);
        if (t == null) { return null; }
        return t.getField(fldName);
    }

    /**
     * @return the tables as a collection.
     */
    public Collection<Table> getTables()
    {
        return tables.values();
    }

    /**
     * @param tblName
     * @return the table named tblName.
     */
    public Table getTable(final String tblName)
    {
        return tables.get(tblName.toLowerCase());
    }

    /**
     * @return a graph representing the schema.
     * @throws DirectedGraphException
     */
    public DirectedGraph<Table, Relationship> getGraph() throws DirectedGraphException
    {
        //if (scheme == null) { return getFakeGraph(); }
        // there are probably a whole bunch of tables that will never be importable but for
        // now...
        String[] badTbls = { "appresource", "appresourcedata", "appresourcedefault", "datatype",
                /*"geographytreedef", "geographytreedefitem",*/ "picklist", "specifyuser", /*"taxontreedef", "taxontreedefitem",*/ "spprincipal", "viewsetobj", "workbench",
                "workbenchdataitem", "workbenchrow", "workbenchrowimage", "workbenchtemplate",
                "workbenchtemplatemappingitem" };
        GraphBuilder gb = new GraphBuilder(scheme, this, badTbls);
        return gb.buildGraph();
    }
}
