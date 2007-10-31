/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.apache.log4j.Logger;

import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPaneSS;
import edu.ku.brc.specify.tasks.subpane.wb.graph.DirectedGraph;
import edu.ku.brc.specify.tasks.subpane.wb.graph.DirectedGraphException;
import edu.ku.brc.specify.tasks.subpane.wb.graph.Edge;
import edu.ku.brc.specify.tasks.subpane.wb.graph.Vertex;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Field;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Relationship;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Table;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMappingDefRel.ImportMappingRelFld;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable.DefaultFieldEntry;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable.RelatedClassEntry;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable.UploadTableInvalidValue;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;


/**
 * @author timo
 * 
 */
public class Uploader implements ActionListener, WindowStateListener
{
    //Phases in the upload process...
    protected static String CHECKING_REQS = "WB_UPLOAD_CHECKING_REQS";
    protected static String VALIDATING_DATA = "WB_UPLOAD_VALIDATING_DATA";
    protected static String READY_TO_UPLOAD = "WB_UPLOAD_READY_TO_UPLOAD";
    protected static String UPLOADING = "WB_UPLOAD_UPLOADING";
    protected static String SUCCESS = "WB_UPLOAD_SUCCESS";
    protected static String RETRIEVING_UPLOADED_DATA = "WB_RETRIEVING_UPLOADED_DATA";
    protected static String FAILURE = "WB_UPLOAD_FAILURE";
    protected static String USER_INPUT = "WB_UPLOAD_USER_INPUT";
    protected static String UNDOING_UPLOAD = "WB_UPLOAD_UNDO";
    
    /**
     * one of above statics
     */
    protected String currentOp;
    

    /**
     * used by bogusViewer
     */
    Map<String, Vector<Vector<String>>> bogusStorages = null;    
    /**
     * Displays uploaded data. Roughly.
     */
    protected DB.BogusViewer bogusViewer = null;
    
    protected DB db;

	protected UploadData uploadData;
    
    /**
     * The WorkbenchPane for the uploading dataset. 
     */
    protected WorkbenchPaneSS wbSS;

	protected Vector<UploadField> uploadFields;

	protected Vector<UploadTable> uploadTables;

	protected DirectedGraph<Table, Relationship> uploadGraph;
    
    boolean verbose = false;
    
    boolean dataValidated = false;
    
    protected UploadMainForm mainForm;
    
    /**
     * Problems with contents of cells in dataset.
     */
    protected Vector<UploadTableInvalidValue> validationIssues = null;
    /**
     * This object assigns default values for missing required fields and foreign keys.
     * And provides UI for viewing and changing the defaults.
     */
    MissingDataResolver resolver;
    
    /**
     * Required related classes that are not available in the dataset.
     */
    protected Vector<UploadTable.RelatedClassEntry> missingRequiredClasses;
    /**
     * Required fields not present in the dataset.
     */
    protected Vector<UploadTable.DefaultFieldEntry> missingRequiredFields;
        
    /**
     *  While an upload is underway, this member will be provide access to the uploader.
     */
    protected static Uploader currentUpload = null;
    
    /**
     *  A unique identifier currently used to identify the upload. Currently not used.
     *  NOTE: Would it be desirable to store info on imports - dataset imported, date, user, basic stats ??? 
     * 
     */
    protected String identifier;

    private static final Logger log = Logger.getLogger(Uploader.class);
   
    /**
     * @return dataValidated
     */
    public boolean getDataValidated()
    {
        return dataValidated;
    }
    
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
     * creates an identifier for an importer
     * 
     */
    protected void buildIdentifier()
    {
        Date now = new Date(System.currentTimeMillis());
        identifier =  "Upload " + uploadData.getWbRow(0).getWorkbench().getName() + now.toString();
    }
    
    /**
     * @param f
     * @return an existing importTable that contains f
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
            UploadField newFld = new UploadField(fld, -1, mapping.getWbFldName(), null);
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
            UploadField newFld = new UploadField(dbFld, fld.getFldIndex(), fld.getWbFldName(), null);
			newFld.setSequence(mapping.getSequence());
			uploadFields.add(newFld);
		}
		if (mapping.getRelatedFields().size() > 0)
		{
			Relationship r = null;
            Vector<Relationship> rs;
			try
			{
				rs = db.getGraph().getAllEdgeData(t1, t2);
				if (rs.size() == 0)
				{
					rs = db.getGraph().getAllEdgeData(t2, t1);
				}
			} catch (DirectedGraphException ex)
			{
				throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
			}
            //find the 'right' rel. ie: discard Agent ->> ModifiedByAgentID/CreatedByAgentID
            for (Relationship rel : rs)
            {
                if (!rel.getRelatedField().getName().equalsIgnoreCase("modifiedbyagentid") && !rel.getRelatedField().getName().equalsIgnoreCase("createdbyagentid")) 
                {
                    r = rel;
                    break;
                }
            }
			if (r != null)
			{
				Vector<ImportMappingRelFld> relFlds = mapping
						.getRelatedFields();
				for (int relF = 0; relF < relFlds.size(); relF++)
				{
					Field fld = db.getSchema().getField(t2.getName(),relFlds.get(relF).getFieldName());
                    int fldIdx = relFlds.get(relF).getFldIndex();
                    String wbFldName = relFlds.get(relF).getWbFldName();
                    UploadField newFld = new UploadField(fld, fldIdx, wbFldName, r);
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
                UploadField newFld = new UploadField(fld, m.getIndex(), m.getWbFldName(),
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
	public Uploader(DB db, UploadData importData, final WorkbenchPaneSS wbSS) throws UploaderException
	{
		this.db = db;
		this.uploadData = importData;
        this.wbSS = wbSS;
		this.uploadFields = new Vector<UploadField>(importData.getCols());
        this.missingRequiredClasses = new Vector<UploadTable.RelatedClassEntry>();
        this.missingRequiredFields = new Vector<UploadTable.DefaultFieldEntry>();
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
                Field fld = rankTbl.getField(treeMap.getField());
                int fldIdx = treeMap.getLevels().get(level).get(seq).getIndex();
                String wbFldName = treeMap.getLevels().get(level).get(seq).getWbFldName();
                UploadField newFld1 = new UploadField(fld, fldIdx, wbFldName, null);
				newFld1.setRequired(true);
				newFld1.setSequence(seq);
				uploadFields.add(newFld1);
				UploadField newFld2 = new UploadField(rankTbl.getField("rankId"),
						-1, null, null);
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
    
    /**
     * @author timbo
     *
     * @code_status Alpha
     *
     *Handles 'parent-child' relationships between UploadTables
     */
    public class ParentTableEntry
    {
        /**
         * The parent UploadTable
         */
        protected UploadTable importTable;
        /**
         * The relationship to the parent
         */
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
    

    /**
     * @throws UploaderException
     * 
     * Determines 'parent' UploadTables for each UploadTable
     */
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
                    Vector<Relationship> rs = uploadGraph.getAllEdgeData(tv.getData(), it.getTable());
                    for (Relationship r : rs)
                    {
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
     *  Validates contents of all cells in dataset.
     */
    public void validateData()
    {       
        dataValidated = false;
        
        final Vector<UploadTableInvalidValue> issues = new Vector<UploadTableInvalidValue>();
        
        final SwingWorker validateTask = new SwingWorker()
        {
            final JStatusBar statusBar = UIRegistry.getStatusBar();
 
            @Override
            public void interrupt()
            {
                super.interrupt();
            }
                        
            @SuppressWarnings("synthetic-access")
            @Override
            public Object construct()
            {
                int progress = 0;
                initProgressBar(0, uploadTables.size());
                for (UploadTable tbl : uploadTables)
                {
                    setCurrentOpProgress(++progress);
                    issues.addAll(tbl.validateValues(uploadData));
                }
                dataValidated = issues.size() == 0;
                return new Boolean(dataValidated);
            }
            
            @Override
            public void finished()
            {
                validationIssues = issues;
                if (dataValidated && resolver.isResolved())
                {
                    statusBar.setText(getResourceString("WB_DATASET_VALIDATED")); 
                    setCurrentOp(Uploader.READY_TO_UPLOAD);
                }
                else
                {
                    setCurrentOp(Uploader.USER_INPUT);
                    statusBar.setText(getResourceString("WB_INVALID_DATASET"));
                }
            }

        };
        validateTask.start();
        if (mainForm == null)
        {
            initUI(Uploader.VALIDATING_DATA);
            UIHelper.centerAndShow(mainForm);
        }
        else
        {
            setCurrentOp(Uploader.VALIDATING_DATA);
        }
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
     * @throws UploaderException
     * 
     * Sets up for upload.
     */
    public void prepareToUpload() throws UploaderException
    {
        try
        {
            for (UploadTable t : uploadTables)
            {
                t.prepareToUpload();
            }
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
     * @param opName
     * 
     * Puts UI into correct state for current upload phase.
     */
    protected synchronized void setCurrentOp(final String opName)
    {
        currentOp = opName;
        if (mainForm == null)
        {
            log.error("UI does not exist.");
            return;
        }
        setupUI(currentOp);
    }
    
    
    /**
     * @param min
     * @param max
     * 
     * Initializes progress bar for upload actions. 
     * If min and max = 0, sets progress bar is indeterminate.
     */
    protected synchronized void initProgressBar(int min, int max)
    {
        if (mainForm == null)
        {
            log.error("UI does not exist.");
            return;
        }
        JProgressBar pb = mainForm.getCurrOpProgress();
        pb.setVisible(true);
        if (min == 0 && max == 0)
        {
            pb.setIndeterminate(true);
        }
        else
        {
            if (pb.isIndeterminate())
            {
                pb.setIndeterminate(false);
            }
            pb.setMinimum(min);
            pb.setMaximum(max);
            pb.setValue(min);
        }
        pb.setString("");
    }
    
    
    /**
     * @param val
     * 
     * Sets progress bar progress.
     */
    protected synchronized void setCurrentOpProgress(int val)
    {
        if (mainForm == null)
        {
            log.error("UI does not exist.");
            return;
        }
        if (!mainForm.getCurrOpProgress().isIndeterminate())
        {
            mainForm.getCurrOpProgress().setValue(val);
        }
    }
    
    /**
     * @param initOp
     * 
     * builds upload ui witn initial phase of initOp
     */
    protected void initUI(final String initOp)
    {
        buildMainUI();
        setCurrentOp(initOp);
        mainForm.pack();
    }
    
    
    
    /**
     * Gets default values for all missing required classes (foreign keys) and local fields. 
     */
    public void getDefaultsForMissingRequirements()
    {        
        final SwingWorker uploadTask = new SwingWorker()
        {
            final JStatusBar statusBar = UIRegistry.getStatusBar();
            boolean success = false;                        
            @SuppressWarnings("synthetic-access")
            @Override
            public Object construct()
            {
                UIRegistry.writeGlassPaneMsg(String.format(getResourceString("WB_UPLOAD_VALIDATING_DATASET"),
                        new Object[] { "" }), WorkbenchTask.GLASSPANE_FONT_SIZE);
                missingRequiredClasses.clear();
                missingRequiredFields.clear();
                Iterator<RelatedClassEntry> rces;
                Iterator<DefaultFieldEntry> dfes;
                for (UploadTable t : uploadTables)
                {
                    try 
                    {
                        rces = t.getRelatedClassDefaults();
                    }
                    catch (ClassNotFoundException ex)
                    {
                        log.error(ex);
                        return null;
                    }
                    while (rces.hasNext())
                    {
                        missingRequiredClasses.add(rces.next());
                    }

                    try
                    {
                        dfes = t.getMissingRequiredFlds();
                    }
                    catch (NoSuchMethodException ex)
                    {
                        log.error(ex);
                        return null;
                    }
                    while (dfes.hasNext())
                    {
                        missingRequiredFields.add(dfes.next());
                    }
                    success = true;
                }
                resolver = new MissingDataResolver(missingRequiredClasses, missingRequiredFields);
                return null;
            }
            
            @Override
            public void finished()
            {
                UIRegistry.clearGlassPaneMsg();
                if (success)
                {
                    statusBar.setText(getResourceString("WB_REQUIRED_RETRIEVED")); 
                    validateData();
                }
                else
                {
                    setCurrentOp(Uploader.FAILURE);
                }
            }

        };

        uploadTask.start();
        initUI(Uploader.CHECKING_REQS);
        mainForm.setAlwaysOnTop(true);
        UIHelper.centerAndShow(mainForm);
    }
    
      
    /**
     * Called when dataset is saved. 
     */
    public void refresh()
    {
        wbSS.getWorkbench().forceLoad();
        validateData();
    }
    
    
    /**
     * Called when dataset is closing.
     */
    public void closing()
    {
        if (mainForm != null)
        {
            closeMainForm();
        }
    }
    
    
    /**
     * Shuts down upload UI.
     */
    protected void closeMainForm()
    {
        mainForm.setVisible(false);
        mainForm.dispose();
        mainForm = null;
        closeUploadedDataViewers();
    }
    
    
    /**
     * Closes views of uploaded data.
     */
    protected void closeUploadedDataViewers()
    {
        if (bogusViewer != null)
        {
            bogusViewer.closeViewers();
            bogusViewer = null;
        }
    }
    
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     * 
     * Responds to user actions in UI.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals(UploadMainForm.DO_UPLOAD))
        {
            uploadIt();
        }
        else if (e.getActionCommand().equals(UploadMainForm.VIEW_UPLOAD))
        {
            if (currentOp.equals(Uploader.SUCCESS))
            {
                if (bogusStorages == null)
                {
                    retrieveUploadedData();
                }
                else
                {
                    viewSelectedTable();
                }
            }
        }
        else if (e.getActionCommand().equals(UploadMainForm.VIEW_SETTINGS))
        {
            resolver.resolve(!currentOp.equals(Uploader.READY_TO_UPLOAD)
                    && !currentOp.equals(Uploader.USER_INPUT));
            if (currentOp.equals(Uploader.READY_TO_UPLOAD) && !resolver.isResolved())
            {
                setCurrentOp(Uploader.USER_INPUT);
            }
            else if (currentOp.equals(Uploader.USER_INPUT) && resolver.isResolved())
            {
                setCurrentOp(Uploader.READY_TO_UPLOAD);
            }
        }
        else if (e.getActionCommand().equals(UploadMainForm.CLOSE_UI))
        {
            closeMainForm();
            wbSS.uploadDone();
        }
        else if (e.getActionCommand().equals(UploadMainForm.UNDO_UPLOAD))
        {
            undoUpload();
            closeMainForm();
            wbSS.uploadDone();
        }
        else if (e.getActionCommand().equals(UploadMainForm.CANCEL_OPERATION))
        {
            //System.out.println(UploadMainForm.CANCEL_OPERATION);
        }
        else if (e.getActionCommand().equals(UploadMainForm.TBL_DBL_CLICK))
        {
            mainForm.getViewUploadBtn().setEnabled(canViewUpload(currentOp));
            if (currentOp.equals(Uploader.SUCCESS))
            {
                if (bogusStorages == null)
                {
                    retrieveUploadedData();
                }
                else
                {
                    viewSelectedTable();
                }
            }
        }
        else if (e.getActionCommand().equals(UploadMainForm.TBL_CLICK))
        {
            mainForm.getViewUploadBtn().setEnabled(canViewUpload(currentOp));
        }
        else if (e.getActionCommand().equals(UploadMainForm.INVALID_VAL_CLICK))
        {
             goToInvalidWBCell();
        }
    }
    
    
    /**
     * Moves to dataset cell corresponding to currently selected validation issue and starts editor.
     */
    protected void goToInvalidWBCell()
    {
        if (mainForm == null)
        {
            throw new RuntimeException("Upload form does not exist.");
        }
        if (wbSS != null)
        {
            UploadTableInvalidValue invalid = validationIssues.get(mainForm.getInvalidVals().getSelectedIndex());
            Rectangle rect = wbSS.getSpreadSheet().getCellRect(invalid.getRowNum(), invalid.getUploadFld().getIndex(), false);
            wbSS.getSpreadSheet().scrollRectToVisible(rect);
            wbSS.getSpreadSheet().editCellAt(invalid.getRowNum(), invalid.getUploadFld().getIndex(), null);
            wbSS.getSpreadSheet().grabFocus();
        }
    }
    
    
    public void windowStateChanged(WindowEvent e)
    {
        if (e.getNewState() == WindowEvent.WINDOW_CLOSING)
        {
            System.out.println("Closing");
        }
        else if (e.getNewState() == WindowEvent.WINDOW_ACTIVATED)
        {
            System.out.println("Activated");
        }
    }
    
    
    /**
     * Builds form for upload UI.
     */
    protected void buildMainUI()
    {
        mainForm = new UploadMainForm();
        mainForm.buildUI();
         
        SortedSet<String> tblNames = new TreeSet<String>();
        for (UploadTable ut : uploadTables)
        {
            tblNames.add(ut.getWriteTable().getName());
        }
        DefaultListModel tbls = new DefaultListModel();
        for (String tblName : tblNames)
        {
            tbls.addElement(tblName);
        }
        mainForm.getUploadTbls().setModel(tbls);
        mainForm.setActionListener(this);
        mainForm.addWindowStateListener(this);
    }
    
    
    /**
     * @param op
     * 
     * Sets up mainForm for upload phase for op.
     */
    protected synchronized void setupUI(final String op)
    {
        if (mainForm == null)
        {
            log.error("UI does not exist.");
            return;
        }

        if (op.equals(Uploader.SUCCESS))
        {
            if (mainForm.getUploadTbls().getSelectedIndex() == -1)
            {
                //assuming list is not empty
                mainForm.getUploadTbls().setSelectedIndex(0);
            }
        }

        mainForm.getCancelBtn().setEnabled(canCancel(op));
        mainForm.getCancelBtn().setVisible(mainForm.getCancelBtn().isEnabled());
        mainForm.getDoUploadBtn().setEnabled(canUpload(op));
        mainForm.getDoUploadBtn().setVisible(mainForm.getDoUploadBtn().isEnabled());
        mainForm.getViewSettingsBtn().setEnabled(canViewSettings(op));
        mainForm.getViewSettingsBtn().setVisible(mainForm.getViewSettingsBtn().isEnabled());
        mainForm.getViewUploadBtn().setEnabled(canViewUpload(op));
        mainForm.getViewUploadBtn().setVisible(mainForm.getViewUploadBtn().isEnabled());
        mainForm.getUndoBtn().setEnabled(canUndo(op));
        mainForm.getUndoBtn().setVisible(mainForm.getUndoBtn().isEnabled());
        mainForm.getCloseBtn().setEnabled(canClose(op));
        
        DefaultListModel invalids = new DefaultListModel();
        if (validationIssues != null)
        {
            for (UploadTableInvalidValue invalid : validationIssues)
            {
                invalids.addElement(invalid);
            }
        }
        mainForm.getInvalidVals().setModel(invalids);
        mainForm.getInvalidValPane().setVisible(invalids.size() > 0);
        
        mainForm.getCurrOpProgress().setVisible(mainForm.getCancelBtn().isVisible());
        
        mainForm.getCurrOpLbl().setText(getResourceString(op));
    }
    
    
    /**
     * Opens view of uploaded data for selected table.
     * Initializes viewer object if necessary. 
     */
    protected void viewSelectedTable()
    {
        if (currentOp.equals(Uploader.SUCCESS))
        {
            if (mainForm.getUploadTbls().getSelectedValue() != null)
            {
                if (bogusStorages != null)
                {
                    if (bogusViewer == null)
                    {
                        bogusViewer = db.new BogusViewer(bogusStorages);
                    }
                    if (bogusViewer != null)
                    {
                        bogusViewer.viewBogusTbl(mainForm.getUploadTbls().getSelectedValue().toString(), true);
                    }
                }
            }
        }
    }
    
    
    /**
     * @param op
     * @return true if canUndo in phase op.
     */
    protected boolean canUndo(final String op)
    {
        return op.equals(Uploader.SUCCESS);
    }
    
    /**
     * @param op
     * @return true if canCancel in phase op.
     */
    protected boolean canCancel(final String op)
    {
        return op.equals(Uploader.UPLOADING)
          || op.equals(Uploader.CHECKING_REQS)
          || op.equals(Uploader.VALIDATING_DATA);
    }
    
    /**
     * @param op
     * @return true if canUpload in phase op.
     */
    protected boolean canUpload(final String op)
    {
        return op.equals(Uploader.READY_TO_UPLOAD);
    }
    
    /**
     * @param op
     * @return true if canClose in phase op.
     */
    protected boolean canClose(final String op)
    {
        return op.equals(Uploader.READY_TO_UPLOAD)
         || op.equals(Uploader.USER_INPUT)
         || op.equals(Uploader.SUCCESS)
         || op.equals(Uploader.FAILURE);
    }

    /**
     * @param op
     * @return true if canViewSettings in phase op.
     */
    protected boolean canViewSettings(final String op)
    {
        return op.equals(Uploader.READY_TO_UPLOAD)
        || op.equals(Uploader.USER_INPUT)
        || op.equals(Uploader.SUCCESS)
        || op.equals(Uploader.FAILURE);
    }

    /**
     * @param op
     * @return true if canViewUpload in phase op.
     */
    protected boolean canViewUpload(final String op)
    {
        return op.equals(Uploader.SUCCESS) && mainForm.getUploadTbls().getSelectedIndex() != -1;
    }
    
    
    /**
     * Uploads dataset.
     */
    public void uploadIt() 
    {
        buildIdentifier();
        currentUpload = this;
        
        final SwingWorker uploadTask = new SwingWorker()
        {
            final JStatusBar statusBar = UIRegistry.getStatusBar();
            boolean success = false;
            boolean cancelled = false;
            boolean holdIt = false;
            
            @Override
            public void interrupt()
            {
                //this will hold the upload process until the confirm dlg is closed
                holdIt = true;
                if (UIRegistry.displayConfirm(getResourceString("WB_CANCEL_UPLOAD_TITLE"), 
                        getResourceString("WB_CANCEL_UPLOAD_MSG"), 
                        getResourceString("OK"),
                        getResourceString("Cancel"), 
                        JOptionPane.QUESTION_MESSAGE))
                {
                    super.interrupt();
                    cancelled = true;
                }
                holdIt = false;
            }
                        
            @SuppressWarnings("synthetic-access")
            @Override
            public Object construct()
            {
                UIRegistry.writeGlassPaneMsg(String.format(getResourceString("WB_UPLOADING_DATASET"),
                        new Object[] { "" }), WorkbenchTask.GLASSPANE_FONT_SIZE);
                initProgressBar(0, uploadData.getRows());
                try
                {
                    for (int r = 0; r < uploadData.getRows();)
                    {
                        if (!holdIt)
                        {
                            for (UploadTable t : uploadTables)
                            {
                                if (cancelled)
                                {
                                    break;
                                }
                                setCurrentOpProgress(r + 1);
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
                            r++;
                        }
                    }
                }
                catch (Exception ex)
                {
                    return ex;
                }
                success = !cancelled;
                return Boolean.valueOf(success);
            }
            
            @Override
            public void finished()
            {
                if (success)
                {
                    statusBar.setText(getResourceString(SUCCESS)); 
                    setCurrentOp(Uploader.SUCCESS);
                }
                else 
                {
                    for (int ut = uploadTables.size()-1; ut >= 0; ut--)
                    {
                        uploadTables.get(ut).undoUpload();
                    }
                    if (cancelled)
                    {
                        statusBar.setText(getResourceString("WB_UPLOAD_CANCELLED"));
                        setCurrentOp(Uploader.READY_TO_UPLOAD);
                    }
                    else
                    {
                        statusBar.setText(getResourceString(Uploader.FAILURE)); 
                        setCurrentOp(Uploader.FAILURE);
                    }
                }
                UIRegistry.clearGlassPaneMsg();
            }

        };

        JButton cancelBtn = mainForm.getCancelBtn();
        cancelBtn.addActionListener(new ActionListener()
        {
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent ae)
            {
                log.debug("Stopping the dataset upload thread");
                uploadTask.interrupt();
            }
        });

        uploadTask.start();
        if (mainForm == null)
        {
            initUI(Uploader.UPLOADING);
            UIHelper.centerAndShow(mainForm);
        }
        else
        {
            setCurrentOp(Uploader.UPLOADING);
        }
    }
    
    
    
	/**
     * Undoes the most recent upload.
     * 
     * This is currently intended to be used as a debugging aid.
     */
	public void undoUpload()
    {
        final SwingWorker undoTask = new SwingWorker()
        {
            final JStatusBar statusBar = UIRegistry.getStatusBar();
            boolean success = false;
                                    
            @SuppressWarnings("synthetic-access")
            @Override
            public Object construct()
            {
                UIRegistry.writeGlassPaneMsg(String.format(getResourceString("WB_UPLOAD_UNDO"),
                        new Object[] { "" }), WorkbenchTask.GLASSPANE_FONT_SIZE);
                initProgressBar(0, 0);
                for (int ut = uploadTables.size()-1; ut >= 0; ut--)
                {
                    uploadTables.get(ut).undoUpload();
                }
                success = true;
                return Boolean.valueOf(success);
            }
            
            @Override
            public void finished()
            {
                UIRegistry.clearGlassPaneMsg();
                if (success)
                {
                    statusBar.setText(getResourceString("WB_UPLOAD_ROLLEDBACK"));
                    setCurrentOp(Uploader.READY_TO_UPLOAD);
                }
                else 
                {
                        statusBar.setText(getResourceString("WB_UPLOAD_ROLLBACK_FAILURE")); 
                        setCurrentOp(Uploader.FAILURE);
                }
            }

        };
        undoTask.start();
        setCurrentOp(Uploader.UNDOING_UPLOAD);
    }
    
    
    /**
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * 
     * prints listing of uploaded data to System.out.
     */
    public void printUpload() throws IllegalAccessException, InvocationTargetException
    {
        for (UploadTable ut : uploadTables)
        {
            System.out.println(ut.getWriteTable().getName());
            Vector<Vector<String>> vals = ut.printUpload();
            for (Vector<String> row : vals)
            {
                for (String val : row)
                {
                    System.out.print(val + ", ");
                }
                System.out.println();
            }
        }
    }
    

    /**
     * Builds viewer for uploaded data.
     */
    public void retrieveUploadedData() 
    {
        bogusStorages = new HashMap<String, Vector<Vector<String>>>();

        final SwingWorker retrieverTask = new SwingWorker()
        {
            final JStatusBar statusBar = UIRegistry.getStatusBar();
            boolean cancelled = false;
            boolean holdIt = false;
            
            @Override
            public void interrupt()
            {
                //this will hold the display process until the confirm dlg is closed
                holdIt = true;
                if (UIRegistry.displayConfirm(getResourceString("WB_CANCEL_UPLOAD_RETRIEVE_TITLE"), 
                        getResourceString("WB_CANCEL_UPLOAD_RETRIEVE_MSG"), 
                        getResourceString("OK"),
                        getResourceString("Cancel"), 
                        JOptionPane.QUESTION_MESSAGE))
                {
                    super.interrupt();
                    cancelled = true;
                }
                holdIt = false;
            }
                        
            @SuppressWarnings("synthetic-access")
            @Override
            public Object construct()
            {
                initProgressBar(0, uploadTables.size());
                for (int progress = 0; progress < uploadTables.size();)
                {
                    if (cancelled)
                    {
                        break;
                    }
                    if (!holdIt)
                    {
                        UploadTable ut = uploadTables.get(progress);
                        setCurrentOpProgress(progress + 1);
                        try
                        {
                            Vector<Vector<String>> vals = ut.printUpload();
                            if (vals.size() > 0)
                            {
                                if (!bogusStorages.containsKey(ut.getWriteTable().getName()))
                                {
                                    bogusStorages.put(ut.getWriteTable().getName(), vals);
                                }
                                else
                                {
                                    // delete header
                                    vals.remove(0);
                                    bogusStorages.get(ut.getWriteTable().getName()).addAll(vals);
                                }
                            }
                        }
                        catch (InvocationTargetException ex)
                        {
                            log.error(ex);
                        }
                        catch (IllegalAccessException ex)
                        {
                            log.error(ex);
                        }
                        progress++;
                    }
                }
               return null;
            }
            
            @Override
            public void finished()
            {
                setCurrentOp(Uploader.SUCCESS);
                if (!cancelled)
                {
                    viewSelectedTable();
                    statusBar.setText(getResourceString("WB_UPLOAD_DATA_FETCHED")); 
                    //undoUpload(); 
                }
                else
                {
                    bogusStorages = null;
                    statusBar.setText(getResourceString("RetrievalWB_UPLOAD_FETCH_CANCELLED cancelled"));
                    //undoUpload();
                }
                UIRegistry.clearGlassPaneMsg();
            }

        };
        if (mainForm == null)
        {
            initUI(Uploader.RETRIEVING_UPLOADED_DATA);
            UIHelper.centerAndShow(mainForm);
        }
        else
        {
            setCurrentOp(Uploader.RETRIEVING_UPLOADED_DATA);
        }
        retrieverTask.start();
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
            

}
