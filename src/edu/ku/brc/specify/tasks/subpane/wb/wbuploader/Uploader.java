/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.tasks.subpane.wb.graph.DirectedGraph;
import edu.ku.brc.specify.tasks.subpane.wb.graph.DirectedGraphException;
import edu.ku.brc.specify.tasks.subpane.wb.graph.Edge;
import edu.ku.brc.specify.tasks.subpane.wb.graph.Vertex;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Field;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Relationship;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Table;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMappingDefRel.ImportMappingRelFld;


/**
 * @author timo
 * 
 */
public class Uploader
{
	protected DB db;

	protected UploadData uploadData;

	protected Vector<UploadField> uploadFields;

	protected Vector<UploadTable> uploadTables;

	protected DirectedGraph<Table, Relationship> uploadGraph;
    
    boolean verbose = false;
    
    protected DataProviderSessionIFace importSession = null;
    
    
    /**
     *  While an upload is underway, this member will be provide access to the uploader.
     */
    protected static Uploader currentUpload = null;
    
    /**
     *  A unique identifier currently used to identify uploaded records (stored in the lastEditedBy field).
     *  (NOTE: Would it be desirable to store info on imports - dataset imported, date, user, basic stats ??? 
     *  - this means lastEditedBy storage (which is only good till somebody edits an imported record) is not good enough)
     */
    protected String identifier;

    private static final Logger log = Logger.getLogger(Uploader.class);
       
    
    /**
     * @return the currentUpload;
     */
    public static Uploader getCurrentUpload()
    {
        return currentUpload;
    }
    
    /**
     * @return the identifier.
     */
    public final String getIdentifier()
    {
        return identifier;
    }
    
    /**
     * creates a unique (?) identifier for an importer
     * Currently stored in lastEditedBy field. Mostly used for debugging purposes.
     */
    protected void buildIdentifier()
    {
        Date now = new Date(System.currentTimeMillis());
        identifier =  "Upload " /*+ uploadData.getWbRow(0).getWorkbench().getName() */ + now.toString();
    }
    
    /**
     * @param f
     * @return an existing importTable that is suitable for f
     */
    protected UploadTable getUploadTable(UploadField f)
	{
		for (UploadTable result : uploadTables)
		{
			if (result.getTable().getName().equals(f.getField().getTable().getName()) 
					&& (result.getRelationship() == null && f.getRelationship() == null || (result.getRelationship() != null && f.getRelationship() != null && result.getRelationship().equals(f.getRelationship()))))
			{
				return result;
			}
		}
		return null;
	}

	/**
	 * @throws UploaderException
     * 
     * builds uploadTables member based on uploadFields' contents
	 */
	protected void buildUploadTables() throws UploaderException
	{
		uploadTables = new Vector<UploadTable>();
		for (UploadField f : uploadFields)
		{
			if (f.getField() != null)
            {
                UploadTable it = getUploadTable(f);
                boolean addIt = it == null;
                if (addIt)
                {
                    it = new UploadTable(f.getField().getTable(), f.getRelationship());
                    it.init();
                }
                if (it == null) { throw new UploaderException("failed to construct import table.",
                        UploaderException.ABORT_IMPORT); }
                it.addField(f);
                if (addIt)
                {
                    uploadTables.add(it);
                }
            }
 		}
	}


	/**
	 * @param mapping
	 * @throws UploaderException
     * 
     * Adds elements to uploadFields as required for relationship described in mapping. 
	 */
	protected void addMappingRelFlds(UploadMappingDefRel mapping) throws UploaderException
	{
		if (mapping.getSequenceFld() != null)
		{
			Field fld = db.getSchema().getField(mapping.getTable(), mapping.getSequenceFld());
            if (fld == null)
            {
                log.debug("could not find field in db: " + mapping.getTable() + "." + mapping.getField());
            }
            UploadField newFld = new UploadField(fld, -1, null);
			newFld.setSequence(mapping.getSequence());
			newFld.setValue(mapping.getSequence().toString());
			uploadFields.add(newFld);
		}
		Table t1 = db.getSchema().getTable(mapping.getTable());
		Table t2 = db.getSchema().getTable(mapping.getRelatedTable());
		for (ImportMappingRelFld fld : mapping.getLocalFields())
		{
			Field dbFld = t1.getField(fld.getFieldName());
            if (dbFld == null)
            {
                log.debug("could not find field in db: " + t1.getName() + "." + fld.getFieldName());
            }
            UploadField newFld = new UploadField(dbFld, fld.getFldIndex(), null);
			newFld.setSequence(mapping.getSequence());
			uploadFields.add(newFld);
		}
		if (mapping.getRelatedFields().size() > 0)
		{
			Relationship r;
			try
			{
				r = db.getGraph().getEdgeData(t1, t2);
				if (r == null)
				{
					r = db.getGraph().getEdgeData(t2, t1);
				}
			} catch (DirectedGraphException ex)
			{
				throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
			}
			if (r != null)
			{
				Vector<ImportMappingRelFld> relFlds = mapping
						.getRelatedFields();
				for (int relF = 0; relF < relFlds.size(); relF++)
				{
					UploadField newFld = new UploadField(db.getSchema()
							.getField(t2.getName(),
									relFlds.get(relF).getFieldName()), relFlds
							.get(relF).getFldIndex(), r);
					newFld.setSequence(mapping.getSequence());
					uploadFields.add(newFld);
				}
			} else
			{
				throw new UploaderException(
						"could not find relationship for mapping.", UploaderException.ABORT_IMPORT);
			}
		}
	}
    
	
	
    /**
     * @throws UploaderException
     * 
     * builds ImportFields required for import and adds them to uploadFields member. 
     */
    protected void buildUploadFields() throws UploaderException
	{
		for (int f = 0; f < uploadData.getCols(); f++)
		{
			UploadMappingDef m = uploadData.getMapping(f);
			if (m.getClass() != UploadMappingDefTree.class)
			{
				Field fld = this.db.getSchema().getField(m.getTable(), m.getField());
                if (fld == null)
                {
                    log.debug("could not find field in db: " + m.getTable() + "." + m.getField());
                }
                UploadField newFld = new UploadField(fld, m.getIndex(),
						null);
				uploadFields.add(newFld);
				if (m.getClass() == UploadMappingDefRel.class)
				{
					UploadMappingDefRel relM = (UploadMappingDefRel) m;
					newFld.setSequence(relM.getSequence());
					try
					{
						addMappingRelFlds(relM);
						newFld.setIndex(-1);
					} catch (UploaderException ex)
					{
						throw ex;
					}
				}
			}
		}
	}
    
    /**
     * @return a "printout" of the uploadFields member.
     */
    public Vector<String> printUploadFields()
    {
        Vector<String> lines = new Vector<String>();
        for (UploadField impF : uploadFields)
        {
            lines.add(impF.getField().getTable().getName()
                    + "."
                    + impF.getField().getName()
                    + " ["
                    + Integer.toString(impF.getIndex())
                    + "] "
                    + (impF.getSequence() == null ? "" : " (" + impF.getSequence().toString()
                            + ")"));
        }
        return lines;
    }
    
    /**
     * @return a printout of info about the uploadGraph
     * 
     * @throws DirectedGraphException
     */
    public Vector<String> printGraphInfo() throws DirectedGraphException
    {
        Vector<String> lines = new Vector<String>();
            lines.add("vertices:");
            for (Vertex<Table> v : uploadGraph.getVertices())
            {
                lines.add("   " + v.getLabel());
            }
            Vector<String> graphEdges = uploadGraph.listEdges();
            lines.add("");
            lines.add("edges:");
            for (String e : graphEdges)
            {
                lines.add(e);
            }
            lines.add("");
            lines.add("Graph sources:");
            Set<Vertex<Table>> sources = uploadGraph.sources();
            for (Vertex<Table> tbl : sources)
            {
                lines.add("   " + tbl.getLabel());
            }
            lines.add("");
            if (uploadGraph.isStronglyConnected())
            {
                lines.add("graph is strongly connected.");
            }
            else
            {
                lines.add("graph is not strongly connected.");
            }
         return lines;
    }
	
    /**
     * @return a "printout" of the uploadTables member.
     */
    public Vector<String> printUploadTables()
    {
        Vector<String> lines = new Vector<String>();
        for (UploadTable impT : uploadTables)
        {
            lines.add(impT.getTable().getName());
            for (Vector<UploadField> seq : impT.getUploadFields())
            {
                for (UploadField f : seq)
                {
                    lines.add("       "
                            + f.getField().getName()
                            + (f.getSequence() == null ? "" : " (" + f.getSequence().toString()
                                    + ")"));
                }
            }
        }
        return lines;
    }
    
	/**
	 * @param db
	 * @param uploadData
	 * @throws UploaderException
	 */
	public Uploader(DB db, UploadData importData) throws UploaderException
	{
		this.db = db;
		this.uploadData = importData;
		this.uploadFields = new Vector<UploadField>(importData.getCols());

		try
		{
			buildUploadFields();
		}
		catch (UploaderException ex)
		{
			throw ex;
		}
		buildUploadTables();
		buildUploadGraph();
		processTreeMaps();
		orderUploadTables();
        buildUploadTableParents();
 	}

	/**
	 * @throws UploaderException
     * builds the uploadGraph.
	 */
	protected void buildUploadGraph() throws UploaderException
	{
		uploadGraph = new DirectedGraph<Table, Relationship>();
		try
		{
			for (UploadTable t : uploadTables)
			{
				String label = t.getTable().getName();
				if (uploadGraph.getVertexByLabel(label) == null)
				{
					uploadGraph.addVertex(new Vertex<Table>(label, t.getTable()));
				}
			}
            for (Edge<Table, Relationship> edge : db.getGraph().getEdges())
            {
                Vector<UploadTable> its1 = getUploadTable(edge.getPointA().getData());
                Vector<UploadTable> its2 = getUploadTable(edge.getPointB().getData());
                if (its1.size() > 0 && its2.size() > 0)
                {
                    uploadGraph.addEdge(edge.getPointA().getLabel(), edge.getPointB().getLabel(), edge.getData());
                }
            }
		} catch (DirectedGraphException e)
		{
            log.debug(e);
            throw new UploaderException(e, UploaderException.ABORT_IMPORT);
		}
	}
    
	
	
    /**
     * @param treeMap
     * @param level
     * @return a name for table representing data with rank represented by level param.
     */
    protected String getTreeTableName(final UploadMappingDefTree treeMap, final int level)
	{
		return treeMap.getTable() + Integer.toString(treeMap.getLevels().get(level).get(0).getRank());
	}
		
	/**
	 * @param treeMap
	 * @throws UploaderException
     * adds Tables, ImportTables, ImportFields required by heirarchy represented by treeMap param.
	 */
	protected void processTreeMap(UploadMappingDefTree treeMap) throws UploaderException
	{
		Table baseTbl = db.getSchema().getTable(treeMap.getTable());
		if (baseTbl == null)
		{
			throw new UploaderException(
					"Could not find base table for tree mapping.",
					UploaderException.ABORT_IMPORT);
		}
		Table parentTbl = null;
		UploadTableTree parentImpTbl = null;
		for (int level = 0; level < treeMap.getLevels().size(); level++)
		{
			// add new table to import graph for rank
            Table rankTbl = new Table(getTreeTableName(treeMap, level), baseTbl);
			try
			{
				uploadGraph.addVertex(new Vertex<Table>(rankTbl.getName(),
						rankTbl));
				if (parentTbl != null)
				{
					Relationship rankRel = new Relationship(parentTbl.getKey(),
							rankTbl.getField(treeMap.getParentField()),
							"OneToMany");
					uploadGraph.addEdge(parentTbl.getName(), rankTbl.getName(),
							rankRel);
				}
			} catch (DirectedGraphException ex)
			{
				throw new UploaderException(ex);
			}
			parentTbl = rankTbl;

			// create UploadTable for new table

			UploadTableTree it = new UploadTableTree(rankTbl, baseTbl,
					parentImpTbl, treeMap.getLevels().get(level).get(0).isRequired(),
                    treeMap.getLevels().get(level).get(0).getRank());
            it.init();
			
			// add ImportFields for new table
			for (int seq = 0; seq < treeMap.getLevels().get(level).size(); seq++)
			{
				UploadField newFld1 = new UploadField(rankTbl.getField(treeMap
						.getField()), treeMap.getLevels().get(level).get(seq)
						.getIndex(), null);
				newFld1.setRequired(true);
				newFld1.setSequence(seq);
				uploadFields.add(newFld1);
				UploadField newFld2 = new UploadField(rankTbl.getField("rankId"),
						-1, null);
				newFld2.setRequired(true);
				newFld2.setValue(Integer.toString(treeMap.getLevels()
						.get(level).get(0).getRank()));
				newFld2.setSequence(seq);
				uploadFields.add(newFld2);

				// add UploadTable for new table

				it.addField(newFld1);
				it.addField(newFld2);
			}
			uploadTables.add(it);
			parentImpTbl = it;
		}

		// add relationships from base table to other tables

		if (parentTbl != null)
		{
            for (Edge<Table, Relationship> e : db.getGraph().getEdges())
            {
                if (e.getPointA().getData().equals(baseTbl))
                {
                    Vertex<Table> relTblVertex = uploadGraph.getVertexByLabel(e.getPointB().getLabel());
                    if (relTblVertex != null)
                    {
                        String relFld1Name = e.getData().getField().getName();
                        Relationship rel = new Relationship(parentTbl.getField(relFld1Name), e.getData().getRelatedField(), e.getData().getRelType());
                        try
                        {
                            uploadGraph.addEdge(parentTbl.getName(), relTblVertex.getLabel(), rel);
                        } catch (DirectedGraphException ex)
                        {
                            throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
                        }
                    }
                }
            }
		}
	}
    
	
	/**
	 * @throws UploaderException
     * 
     * processes UploadMappingDefTree objects in uploadData.
	 */
	protected void processTreeMaps() throws UploaderException
	{
		for (int m = 0; m < uploadData.getCols(); m++)
		{
			if (uploadData.getMapping(m).getClass() == UploadMappingDefTree.class)
			{
				UploadMappingDefTree treeMap = (UploadMappingDefTree) uploadData
						.getMapping(m);
				processTreeMap(treeMap);
			}
		}
	}
    
	
	/**
	 * @param t
	 * @return ImportTables with Table equal to t.
	 */
	protected Vector<UploadTable> getUploadTable(Table t)
	{
		SortedSet<UploadTable> its = new TreeSet<UploadTable>();
		for (UploadTable it : uploadTables)
		{
			if (it.getTable().equals(t))
			{
				its.add(it);
			}
		}
		return new Vector<UploadTable>(its);
	}
    
    public class ParentTableEntry
    {
        protected UploadTable importTable;
        protected Relationship  parentRel;
         /**
         * The hibernate property name of the foreign key.
         */
        protected String propertyName;
        /**
         * the setXXX method used to set objects of importTable's class to children.
         */
        protected Method setter;
        /**
         * @param importTable
         * @param parentRel
         */
        public ParentTableEntry(UploadTable importTable, Relationship parentRel)
        {
            super();
            this.importTable = importTable;
            this.parentRel = parentRel;
        }
        /**
         * @return the importTable
         */
        public final UploadTable getImportTable()
        {
            return importTable;
        }
        /**
         * @return the parentRel
         */
        public final Relationship getParentRel()
        {
            return parentRel;
        }
        /**
         * @return the setter
         */
        public final Method getSetter()
        {
            return setter;
        }
        /**
         * @param setter the setter to set
         * Also sets the propertyName.
         */
        public final void setSetter(Method setter)
        {
            this.setter = setter;
            if (this.setter.getName().startsWith("set"))
            {
                this.propertyName = UploadTable.deCapitalize(this.setter.getName().substring(3));
            }
            else
            {
                this.propertyName = UploadTable.deCapitalize(this.setter.getName());
            }
        }
        public final String getForeignKey()
        {
            if (parentRel == null)
            {
                return importTable.getTblClass().getSimpleName();
            }
            return parentRel.getRelatedField().getName();
        }
        /**
         * @return the propertyName
         */
        public final String getPropertyName()
        {
            return propertyName;
        }
    }

    protected void buildUploadTableParents() throws UploaderException
    {
        for (UploadTable it : uploadTables)
        {
            Vector<Vector<ParentTableEntry>> parentTables = new Vector<Vector<ParentTableEntry>>();
            Set<Vertex<Table>> tbls = uploadGraph.into(it.getTable());
            for (Vertex<Table> tv : tbls)
            {
                try
                {
                    Relationship r = uploadGraph.getEdgeData(tv.getData(), it.getTable());
                    Vector<UploadTable> impTs = getUploadTable(tv.getData());
                    Vector<ParentTableEntry> entries = new Vector<ParentTableEntry>();
                    for (UploadTable impT : impTs)
                    {
                        if (impT.getRelationship() == null || r.equals(impT.getRelationship()))
                        {
                            impT.setHasChildren(true);
                            entries.add(new ParentTableEntry(impT, r));
                        }
                    }
                    parentTables.add(entries);
                }
                catch (DirectedGraphException ex)
                {
                    throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
                }
            }
            it.setParentTables(parentTables);
        }
    }
    
	/**
     * @throws UploaderException
     * 
     * Orders uploadTables according to dependencies in uploadGraph.
     */
	protected void orderUploadTables() throws UploaderException
	{
		try
		{
			Vector<Vertex<Table>> topoSort = uploadGraph.getTopoSort();
			Vector<UploadTable> newTables = new Vector<UploadTable>();
			for (Vertex<Table> v : topoSort)
			{
				Vector<UploadTable> its = getUploadTable(v.getData());
				for (UploadTable it : its)
				{
					newTables.add(it);
					uploadTables.remove(it);
				}
				if (uploadTables.size() == 0)
				{
					break;
				}
			}
			uploadTables = newTables;
		} catch (DirectedGraphException ex)
		{
			throw new UploaderException(ex);
		}
	}
    

	/**
	 * @return true if everything's OK.
	 */
	public boolean validateData()
	{
		for (int r = 0; r < uploadData.getRows(); r++)
		{
			for (int c = 0; c < uploadData.getCols(); c++)
			{
				if (!uploadFields.get(c).validate(uploadData.get(r, c)))
				{
					return false;
				}
			}
		}
		return true;
	}
    

	/**
	 * @return a set of tables for which no fields are being imported, but which provide foreign keys for tables that do have fields being imported.
	 * 
	 * lots more to do here i think re agents (can occur in so many roles) and recursive tables.
	 * also needs to distinguish between collectionObject -> CollectingEvent (missing) -> Locality 
	 * which is kind of bad and CollectionObject -> CollectingEvent (missing) -> Locality (missing) which is useless but maybe ok.
	 */
	public Set<Table> checkForMissingTables()
	{
		Set<Table> result = new HashSet<Table>();
		for (UploadTable t : uploadTables)
		{
			Set<Vertex<Table>> ins = uploadGraph.into(t.getTable());
			for (Vertex<Table> in : ins)
			{
				if (!uploadTableIsPresent(in.getData()))
				{
					result.add(in.getData());
				}
			}
		}
		return result;
	}
    
	
	/**
	 * @param t
	 * @return true if there is an UploadTable defined for t.
	 */
	protected boolean uploadTableIsPresent(final Table t)
	{
		for (UploadTable it : uploadTables)
		{
			if (it.getTable() == t)
			{
				return true;
			}
		}
		return false;
	}
    
	
	/**
	 * @return true if the import mapping and graph are OK.
	 */
	public boolean validateStructure() throws UploaderException
	{
        try
        {
            if (!uploadGraph.isConnected())
            {
                System.out.println("some tables are missing or something.");
                return false;
            }
        }
        catch (DirectedGraphException ex)
        {
            throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
        }
        for (UploadTable t : uploadTables)
        {
            if (!t.isImportable()) { return false; }
        }
        return true;
    }
    
	/**
	 * @return
	 * @throws ClassNotFoundException
	 */
	protected boolean getMissingData() throws ClassNotFoundException, NoSuchMethodException
    {
       for (UploadTable t : uploadTables)
        {
            for (UploadTable.RelatedClassEntry rce : t.getMissingReqRelClasses())
            {
                if (!meetRelClassRequirement(rce, t)) { return false; }
            }
            for (UploadTable.DefaultFieldEntry dfe : t.getMissingRequiredFlds())
            {
                if (!meetFldRequirement(dfe, t)) { return false; }
            }
        }
        return true;
    }
    
    /**
     * @param dfe
     * @param t
     * @return
     */
    protected boolean meetFldRequirement(UploadTable.DefaultFieldEntry dfe, UploadTable t)
    {
        if (t.getTblClass() == edu.ku.brc.specify.datamodel.Agent.class)
        {
            if (dfe.getFldName().equalsIgnoreCase("agenttype"))
            {
                System.out.println("setting Agent.AgentType to 0");
                dfe.setDefaultValue(new Byte("0"));
                return true;
            }
        }
        if (t.getTblClass() == edu.ku.brc.specify.datamodel.PrepType.class)
        {
            if (dfe.getFldName().equalsIgnoreCase("IsLoanable"))
            {
                System.out.println("setting PrepType.IsLoanable to true");
                dfe.setDefaultValue(true);
                return true;
            }
        }
        if (t.getTblClass() == edu.ku.brc.specify.datamodel.AccessionAgent.class)
        {
            if (dfe.getFldName().equalsIgnoreCase("Role"))
            {
                System.out.println("setting AccessionAgent.Role to \"Receiver\"");
                dfe.setDefaultValue("Receiver");
                return true;
            }
        }
        if (t.getTblClass() == edu.ku.brc.specify.datamodel.ReferenceWork.class)
        {
            if (dfe.getFldName().equalsIgnoreCase("ReferenceWorkType"))
            {
                System.out.println("setting ReferenceWork.ReferenceWorkType to 1");
                dfe.setDefaultValue(new Byte("1"));
                return true;
            }
        }
        System.out.println("unable to meet requirement: " + t.getTblClass().getSimpleName() + "." + dfe.getFldName());
        return false;
    }
    
    /**
     * @param rce
     * @param t
     * @return
     */
    protected boolean meetRelClassRequirement(UploadTable.RelatedClassEntry rce, UploadTable t) 
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            List data = session.getDataList(rce.getRelatedClass());
            if (data.size() > 0)
            {
                Object id = ((DataModelObjBase) data.get(0)).getId();
                System.out
                        .println("setting " + rce.getRelatedClass().getSimpleName() + " to " + id);
                t.addRelatedClassDefault(rce, id);
                return true;
            }
        }
        finally
        {
            session.close();
        }
        System.out.println("unable to meet requirement: " + t.getTblClass().getSimpleName() + "<->"
                + rce.getRelatedClass().getSimpleName());
        return false;
    }
        
	
    public void validate() throws UploaderException
    {
        if (!validateStructure()) { throw new UploaderException("invalid structure",
                UploaderException.ABORT_IMPORT); }
        if (!validateData()) { throw new UploaderException("invalid data",
                UploaderException.ABORT_IMPORT); }
        try
        {
            if (!getMissingData()) { throw new UploaderException(
                    "unable to meet requirements.", UploaderException.ABORT_IMPORT); }
        }
        catch (ClassNotFoundException cnfEx)
        {
            throw new UploaderException(cnfEx, UploaderException.ABORT_IMPORT);
        }
        catch (NoSuchMethodException nsmeEx)
        {
            throw new UploaderException(nsmeEx, UploaderException.ABORT_IMPORT);
        }
    }
    /**
	 * @throws UploaderException 
     * 
     * imports.
	 */
	public void uploadIt() throws UploaderException
    {
        buildIdentifier();
        currentUpload = this;
        boolean committed = false;
        //DataProviderSessionIFace importSession = DataProviderFactory.getInstance().createSession();
        //importSession = DataProviderFactory.getInstance().createSession();
       try
        {
            //importSession.beginTransaction();
            for (UploadTable t : uploadTables)
            {
                t.prepareToUpload();
            }
            for (int r = 0; r < uploadData.getRows(); r++)
            {
                for (UploadTable t : uploadTables)
                {
                    try
                    {
                        uploadRow(t, r);
                    }
                    catch (UploaderException ex)
                    {
                        if (ex.getAbortStatus() != UploaderException.ABORT_IMPORT)
                        {
                            log.debug(ex.getMessage());
                            break;
                        }
                        throw ex;
                    }

                }
            }
            //importSession.commit();
            committed = true;
        }
        catch (Exception ex)
        {
            if (ex.getClass() != UploaderException.class)
            {
                throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
            }
            throw (UploaderException)ex;
        }
        finally
        {
            if (!committed)
            {
                //importSession.rollback();
                //rollback doesn't rollback - need to associate UploadTable sessions with importSession ???
                undoUpload();
            }
            currentUpload = null;
            //importSession.close();
            //importSession = null;
        }
    }
    
	/**
	 * Undoes the most recent upload.
     * 
     *  This is currently intended to be used as a debugging aid.
	 */
	public void undoUpload()
    {
        for (int ut = uploadTables.size()-1; ut >= 0; ut--)
        {
            uploadTables.get(ut).undoUpload(identifier);
        }
    }
    
    public void printUpload() throws IllegalAccessException, InvocationTargetException
    {
        Vector<String> printed = new Vector<String>();
        for (UploadTable ut : uploadTables)
        {
            if (!printed.contains(ut.getWriteTable().getName()))
            {
                System.out.println(ut.getWriteTable().getName());
                Vector<Vector<String>> vals = ut.printUpload(identifier);
                for (Vector<String> row : vals)
                {
                    for (String val : row)
                    {
                        System.out.print(val + ", ");
                    }
                    System.out.println();
                }
                printed.add(ut.getWriteTable().getName());
                System.out.println();
            }
        }
    }
    
    public void viewUpload() throws IllegalAccessException, InvocationTargetException
    {
        Map<String, Vector<Vector<String>>> storages = new HashMap<String, Vector<Vector<String>>>();
        for (UploadTable ut : uploadTables)
        {
            if (!storages.containsKey(ut.getWriteTable().getName()))
            {
                Vector<Vector<String>> vals = ut.printUpload(identifier);
                if (vals.size() > 0)
                {
                    storages.put(ut.getWriteTable().getName(), vals);
                }
            }
        }
        DB.BogusViewer bv = db.new BogusViewer(storages);
        bv.viewBogus();
    }
    
	/**
     * @param t
     * @param row
     * @throws UploaderException
     * 
     * imports data in row belonging to t's Table.
     */
	protected void uploadRow(final UploadTable t, int row) throws UploaderException
	{
		for (UploadField field : uploadFields)
		{
			if (field.getField().getTable().equals(t.getTable()))
			{
				if (field.getIndex() != -1)
				{
                    uploadCol(field, uploadData.get(row, field.getIndex()));
				}
			}
		}
		try
		{
			writeRow(t);
		} catch (UploaderException ex)
		{
			log.debug(ex.getMessage() + " (" + t.getTable().getName() + ", row " + Integer.toBinaryString(row) + ")");
            throw ex;
		}
	}
    

	/**
	 * @param f
	 * @param val
	 * 
     * imports val to f.
	 */
	protected void uploadCol(final UploadField f, final String val)
	{
		if (f != null)
		{
			f.setValue(val);
		}
	}

	/**
	 * @param t
	 * @throws UploaderException
     * 
     * writes data (if necessary) for t.
	 */
	protected void writeRow(final UploadTable t) throws UploaderException
    {
        t.writeRow();
    }
        
	public void dumpBogus()
	{
		db.dumpBogus();
	}

    /**
     * @return the importSession
     */
    public final DataProviderSessionIFace getImportSession()
    {
        return importSession;
    }
}
