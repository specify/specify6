/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.tasks.ReportsBaseTask;
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
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 * 
 */
public class Uploader implements ActionListener, KeyListener
{
    // Phases in the upload process...
    protected final static String                   INITIAL_STATE            = "WB_UPLOAD_INITIAL_STATE";
    protected final static String                   CHECKING_REQS            = "WB_UPLOAD_CHECKING_REQS";
    protected final static String                   VALIDATING_DATA          = "WB_UPLOAD_VALIDATING_DATA";
    protected final static String                   READY_TO_UPLOAD          = "WB_UPLOAD_READY_TO_UPLOAD";
    protected final static String                   UPLOADING                = "WB_UPLOAD_UPLOADING";
    protected final static String                   SUCCESS                  = "WB_UPLOAD_SUCCESS";
    protected final static String                   RETRIEVING_UPLOADED_DATA = "WB_RETRIEVING_UPLOADED_DATA";
    protected final static String                   FAILURE                  = "WB_UPLOAD_FAILURE";
    protected final static String                   USER_INPUT               = "WB_UPLOAD_USER_INPUT";
    protected final static String                   UNDOING_UPLOAD           = "WB_UPLOAD_UNDO";
    protected final static String                   CLEANING_UP              = "WB_UPLOAD_CLEANUP";
    
    /**
     * Resource string Ids
     */
    protected final static String                   WB_CELL_LENGTH_EXCEPTION = "WB_CELL_LENGTH_EXCEPTION";
    protected final static String                   WB_TOO_MANY_ERRORS = "WB_TOO_MANY_ERRORS";
    protected final static String                   WB_UPLOAD_FORM_TITLE = "WB_UPLOAD_FORM_TITLE";
    protected final static String                   WB_UPLOAD_ROW_SKIPPED = "WB_UPLOAD_ROW_SKIPPED";
    protected final static String                   WB_UPLOAD_VIEW_RESULTS_TITLE = "WB_UPLOAD_VIEW_RESULTS_TITLE";
    
    /**
     * Maximum number of messages (validation errors) to display. Prevents un-responsiveness when a workbench is REALLY messed up.
     */
    protected final static int MAX_MSG_DISPLAY_COUNT = 800;
    
    /**
     * one of above statics
     */
    protected String                                currentOp;

    /**
     * The operation that preceded the currentOp
     */
    protected String                                previousOp = null;
    
    /**
     * the exception that killed the most recent op. null if most recent op was not murdered.
     * locked by this. Use setOpKiller and getOpKiller for access.
     */
    protected Exception                             opKiller;

    /**
     * used by bogusViewer
     */
    Map<String, Vector<Vector<String>>>             bogusStorages            = null;
    /**
     * Displays uploaded data. Roughly.
     */
    protected DB.BogusViewer                        bogusViewer              = null;

    protected DB                                    db;

    protected UploadData                            uploadData;

    /**
     * The WorkbenchPane for the uploading dataset.
     */
    protected WorkbenchPaneSS                       wbSS;

    protected Vector<UploadField>                   uploadFields;

    protected Vector<UploadTable>                   uploadTables;

    protected DirectedGraph<Table, Relationship>    uploadGraph;

    boolean                                         verbose                  = false;

    boolean                                         dataValidated            = false;

    protected UploadMainPanel                       mainPanel;

    /**
     * Problems with contents of cells in dataset.
     */
    protected Vector<UploadTableInvalidValue>       validationIssues         = null;
    /**
     * This object assigns default values for missing required fields and foreign keys. And provides
     * UI for viewing and changing the defaults.
     */
    MissingDataResolver                             resolver;

    /**
     * Required related classes that are not available in the dataset.
     */
    protected Vector<RelatedClassSetter>            missingRequiredClasses;
    /**
     * Required fields not present in the dataset.
     */
    protected Vector<DefaultFieldEntry> missingRequiredFields;

    /**
     * A list of recordsets containing the keys of all objects uploaded. A separate RecordSet will be
     * stored for each UploadTable.
     */
    protected Vector<RecordSet> recordSets = null;
    /**
     * While an upload is underway, this member will be provide access to the uploader.
     */
    protected static Uploader                       currentUpload            = null;

    /**
     * A unique identifier currently used to identify the upload. Currently not used. NOTE: Would it
     * be desirable to store info on imports - dataset imported, date, user, basic stats ???
     * 
     */
    protected String                                identifier;
    
    /**
     * The time of the upload. Used to create the identifier.
     */
    protected Calendar                              uploadTime = null;
    
    /**
     * The currently executing upload task.
     */
    protected UploaderTask                          currentTask = null;

    protected static final Logger                   log                      = Logger
                                                                                     .getLogger(Uploader.class);

    /**
     * @author timbo
     *
     * @code_status Alpha
     *
     * Stores information about rows that were not uploaded during an upload.
     */
    private class SkippedRow extends BaseUploadMessage
    {
        protected UploaderException cause;
        protected int               row;

        /**
         * @param cause
         * @param row
         */
        public SkippedRow(UploaderException cause, int row)
        {
            super(null);
            this.cause = cause;
            this.row = row;
        }

        /**
         * @return the cause
         */
        public UploaderException getCause()
        {
            return cause;
        }

        /*
         * (non-Javadoc)
         * 
         * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMessage#getRow()
         */
        @Override
        public int getRow()
        {
            return row;
        }

        /*
         * (non-Javadoc)
         * 
         * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMessage#getMsg()
         */
        @Override
        public String getMsg()
        {
            return String.format(getResourceString(WB_UPLOAD_ROW_SKIPPED), String.valueOf(getRow())) + ": " + cause.getMessage();
        }

        /*
         * (non-Javadoc)
         * 
         * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMessage#getData()
         */
        @Override
        public Object getData()
        {
            return cause;
        }
    }

    /**
     * Stores skipped row information for the most recent upload
     */
    protected Vector<SkippedRow>    skippedRows;

    /**
     * Stores messages that are generated during an upload
     */
    protected Vector<UploadMessage> messages;
    
    /**
     * Stores messages that have been generated since the last time the message ui was updated.
     */
    protected Vector<UploadMessage> newMessages;

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
     * the index of the currently processing row in the dataset.
     */
    protected int rowUploading;

    /**
     * @return rowUploading
     */
    public int getRow()
    {
        return rowUploading;
    }

    /**
     * @return the identifier.
     */
    public final String getIdentifier()
    {
        return identifier;
    }

    /**
     * @return the uploadTime
     */
    public Calendar getUploadTime()
    {
        return uploadTime;
    }
    /**
     * creates an identifier for an importer
     * 
     */
    protected void buildIdentifier()
    {
        uploadTime = new GregorianCalendar();
        identifier = uploadData.getWbRow(0).getWorkbench().getName() + "_" + uploadTime.get(Calendar.YEAR)
                + "-" + (uploadTime.get(Calendar.MONTH) + 1) + "-" + uploadTime.get(Calendar.DAY_OF_MONTH) + "_"
                + uploadTime.get(Calendar.HOUR_OF_DAY) + ":" + uploadTime.get(Calendar.SECOND);
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
                    && (result.getRelationship() == null && f.getRelationship() == null || (result
                            .getRelationship() != null
                            && f.getRelationship() != null && result.getRelationship().equals(
                            f.getRelationship())))) { return result; }
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
                    if (it != null) // ??
                    {
                        it.init();
                    }
                }
                if (it == null) { throw new UploaderException(
                        getResourceString("WB_UPLOAD_UPLOADTBL_BUILD_FAIL"),
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
                log.debug("could not find field in db: " + mapping.getTable() + "."
                        + mapping.getField());
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
            }
            catch (DirectedGraphException ex)
            {
                throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
            }
            // find the 'right' rel. ie: discard Agent ->> ModifiedByAgentID/CreatedByAgentID
            for (Relationship rel : rs)
            {
                if (!rel.getRelatedField().getName().equalsIgnoreCase("modifiedbyagentid")
                        && !rel.getRelatedField().getName().equalsIgnoreCase("createdbyagentid"))
                {
                    r = rel;
                    break;
                }
            }
            if (r != null)
            {
                Vector<ImportMappingRelFld> relFlds = mapping.getRelatedFields();
                for (int relF = 0; relF < relFlds.size(); relF++)
                {
                    Field fld = db.getSchema().getField(t2.getName(),
                            relFlds.get(relF).getFieldName());
                    int fldIdx = relFlds.get(relF).getFldIndex();
                    String wbFldName = relFlds.get(relF).getWbFldName();
                    UploadField newFld = new UploadField(fld, fldIdx, wbFldName, r);
                    newFld.setSequence(mapping.getSequence());
                    uploadFields.add(newFld);
                }
            }
            else
            {
                throw new UploaderException("could not find relationship for mapping.",
                        UploaderException.ABORT_IMPORT);
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
                UploadField newFld = new UploadField(fld, m.getIndex(), m.getWbFldName(), null);
                uploadFields.add(newFld);
                if (m.getClass() == UploadMappingDefRel.class)
                {
                    UploadMappingDefRel relM = (UploadMappingDefRel) m;
                    newFld.setSequence(relM.getSequence());
                    try
                    {
                        addMappingRelFlds(relM);
                        newFld.setIndex(-1);
                    }
                    catch (UploaderException ex)
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
            lines
                    .add(impF.getField().getTable().getName()
                            + "."
                            + impF.getField().getName()
                            + " ["
                            + Integer.toString(impF.getIndex())
                            + "] "
                            + (impF.getSequence() == null ? "" : " ("
                                    + impF.getSequence().toString() + ")"));
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
    public Uploader(DB db, UploadData importData, final WorkbenchPaneSS wbSS)
            throws UploaderException
    {
        this.db = db;
        this.uploadData = importData;
        this.wbSS = wbSS;
        this.uploadFields = new Vector<UploadField>(importData.getCols());
        this.missingRequiredClasses = new Vector<RelatedClassSetter>();
        this.missingRequiredFields = new Vector<DefaultFieldEntry>();
        this.skippedRows = new Vector<SkippedRow>();
        this.messages = new Vector<UploadMessage>();
        this.newMessages = new Vector<UploadMessage>();
        buildUploadFields();
        buildUploadTables();
        addEmptyUploadTables();
        buildUploadGraph();
        processTreeMaps();
        for (UploadTable ut : uploadTables)
        {
            ut.assignFldSetters();
        }
        orderUploadTables();
        buildUploadTableParents();
        reOrderUploadTables();
        currentUpload = this;
    }

    /**
     * Adds extra upload tables. Currently only adds Determination if necessary when Genus/Species
     * are selected. Also should add CollectingEvent if Locality and CollectionObject are present.
     * And others???
     */
    protected void addEmptyUploadTables() throws UploaderException
    {
        
        boolean genSpPresent = false, detPresent = false, locPresent = false, coPresent = false, cePresent = false;
        for (UploadTable ut : uploadTables)
        {
            if (ut.getTblClass().equals(Determination.class))
            {
                detPresent = true;
            }
            if (ut.getTblClass().equals(Locality.class))
            {
                locPresent = true;
            }
            if (ut.getTblClass().equals(CollectionObject.class))
            {
                coPresent = true;
            }
            if (ut.getTblClass().equals(CollectingEvent.class))
            {
                cePresent = true;
            }
        }
        if (!detPresent)
        {
            int maxSeq = 0;
            WorkbenchRow wbRow = uploadData.getWbRow(0);
            for (WorkbenchDataItem mapI : wbRow.getWorkbenchDataItems())
            {
                String fldName = mapI.getWorkbenchTemplateMappingItem().getFieldName();
                if (fldName.startsWith("genus") || fldName.startsWith("species")
                        || fldName.startsWith("variety") || fldName.startsWith("subspecies"))
                {
                    genSpPresent = true;
                    try
                    {
                        if (Integer.valueOf(fldName.substring(fldName.length() - 1)) > maxSeq)
                        {
                            maxSeq = Integer.valueOf(fldName.substring(fldName.length() - 1));
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        genSpPresent = false;
                    }
                }
            }
            if (genSpPresent)
            {
                UploadTable det = new UploadTable(db.getSchema().getTable("Determination"), null);
                for (int seq = 0; seq < maxSeq; seq++)
                {
                    UploadField fld = new UploadField(db.getSchema().getField("determination",
                            "collectionobjectid"), -1, null, null);
                    fld.setSequence(seq);
                    det.addField(fld);
                }
                det.init();
                uploadTables.add(det);
            }
        }
        if (!cePresent && locPresent && coPresent)
        {
            UploadTable ce = new UploadTable(db.getSchema().getTable("CollectingEvent"), null);
            ce.init();
            ce.addField(new UploadField(db.getSchema().getField("collectingevent",
                    "stationfieldnumber"), -1, null, null));
            uploadTables.add(ce);
        }
    }

    /**
     * Imposes additional ordering constraints created by the matchChildren property of UploadTable.
     * I.e. if A precedes B and B is in C.matchChildren, then A must precede C.
     */
    protected void reOrderUploadTables() throws UploaderException
    {
        SortedSet<Pair<UploadTable, UploadTable>> moves = new TreeSet<Pair<UploadTable, UploadTable>>(
                new Comparator<Pair<UploadTable, UploadTable>>()
                {
                    private boolean isAncestorOf(UploadTable t1, UploadTable t2)
                    {
                        log.debug("isAncestorOf(" + t1 + ", " + t2 + ")");
                        if (t1.equals(t2)) { return true; }
                        for (Vector<ParentTableEntry> ptes : t2.getParentTables())
                        {
                            for (ParentTableEntry pte : ptes)
                            {
                                if (isAncestorOf(t1, pte.getImportTable())) { return true; }
                            }
                        }
                        return false;
                    }

                    public int compare(Pair<UploadTable, UploadTable> p1,
                                       Pair<UploadTable, UploadTable> p2)
                    {
                        if (isAncestorOf(p1.getSecond(), p2.getSecond())) { return -1; }
                        if (isAncestorOf(p2.getSecond(), p1.getSecond())) { return 1; }
                        return 0;
                    }
                });
        for (UploadTable ut : uploadTables)
        {
            for (UploadTable mc : ut.getMatchChildren())
            {
                for (ParentTableEntry pte : mc.getAncestors())
                {
                    if (uploadTables.indexOf(ut) < uploadTables.indexOf(pte.getImportTable()))
                    {
                        moves.add(new Pair<UploadTable, UploadTable>(ut, pte.getImportTable()));
                    }

                }
            }
        }
        for (Pair<UploadTable, UploadTable> move : moves)
        {
            int fromIdx = uploadTables.indexOf(move.getSecond());
            int toIdx = uploadTables.indexOf(move.getFirst());
            if (toIdx > fromIdx)
            {
                log.error("Can't meet ordering constraints: "
                        + move.getSecond().getTable().getName() + ","
                        + move.getFirst().getTable().getName());
                throw new UploaderException("The Dataset is not uploadable.",
                        UploaderException.ABORT_IMPORT);
            }
            uploadTables.remove(fromIdx);
            uploadTables.insertElementAt(move.getSecond(), toIdx);
        }
    }

    /**
     * @throws UploaderException builds the uploadGraph.
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
                    uploadGraph.addEdge(edge.getPointA().getLabel(), edge.getPointB().getLabel(),
                            edge.getData());
                }
            }
        }
        catch (DirectedGraphException e)
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
        return treeMap.getTable()
                + Integer.toString(treeMap.getLevels().get(level).get(0).getRank());
    }

    /**
     * @param treeMap
     * @throws UploaderException adds Tables, ImportTables, ImportFields required by heirarchy
     *             represented by treeMap param.
     */
    protected void processTreeMap(UploadMappingDefTree treeMap) throws UploaderException
    {
        Table baseTbl = db.getSchema().getTable(treeMap.getTable());
        if (baseTbl == null) { throw new UploaderException(
                "Could not find base table for tree mapping.", UploaderException.ABORT_IMPORT); }
        Table parentTbl = null;
        UploadTableTree parentImpTbl = null;
        for (int level = 0; level < treeMap.getLevels().size(); level++)
        {
            // add new table to import graph for rank
            Table rankTbl = new Table(getTreeTableName(treeMap, level), baseTbl);
            try
            {
                uploadGraph.addVertex(new Vertex<Table>(rankTbl.getName(), rankTbl));
                if (parentTbl != null)
                {
                    Relationship rankRel = new Relationship(parentTbl.getKey(), rankTbl
                            .getField(treeMap.getParentField()), "OneToMany");
                    uploadGraph.addEdge(parentTbl.getName(), rankTbl.getName(), rankRel);
                }
            }
            catch (DirectedGraphException ex)
            {
                throw new UploaderException(ex);
            }
            parentTbl = rankTbl;

            // create UploadTable for new table

            UploadTableTree it = new UploadTableTree(rankTbl, baseTbl, parentImpTbl, treeMap
                    .getLevels().get(level).get(0).isRequired(), treeMap.getLevels().get(level)
                    .get(0).getRank(), treeMap.getLevels().get(level).get(0).getWbFldName());
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
                UploadField newFld2 = new UploadField(rankTbl.getField("rankId"), -1, null, null);
                newFld2.setRequired(true);
                newFld2.setValue(Integer.toString(treeMap.getLevels().get(level).get(0).getRank()));
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
                    Vertex<Table> relTblVertex = uploadGraph.getVertexByLabel(e.getPointB()
                            .getLabel());
                    if (relTblVertex != null)
                    {
                        String relFld1Name = e.getData().getField().getName();
                        Relationship rel = new Relationship(parentTbl.getField(relFld1Name), e
                                .getData().getRelatedField(), e.getData().getRelType());
                        try
                        {
                            uploadGraph.addEdge(parentTbl.getName(), relTblVertex.getLabel(), rel);
                        }
                        catch (DirectedGraphException ex)
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
                UploadMappingDefTree treeMap = (UploadMappingDefTree) uploadData.getMapping(m);
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
     * Handles 'parent-child' relationships between UploadTables
     */
    /**
     * @author timbo
     *
     * @code_status Alpha
     *
     */
    public class ParentTableEntry
    {
        /**
         * The parent UploadTable
         */
        protected UploadTable  importTable;
        /**
         * The relationship to the parent
         */
        protected Relationship parentRel;
        /**
         * The hibernate property name of the foreign key.
         */
        protected String       propertyName;
        /**
         * the setXXX method used to set objects of importTable's class to children.
         */
        protected Method       setter;

        /**
         * true if the parent is required.
         */
        protected boolean      required = false;
        
        /**
         * @param importTable
         * @param parentRel
         */
        public ParentTableEntry(UploadTable importTable, Relationship parentRel)
        {
            super();
            this.importTable = importTable;
            this.parentRel = parentRel;
            if (parentRel != null)
            {
                required = parentRel.getRelatedField().isForeignKey() && parentRel.getRelatedField().isRequired();
            }
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
         * @param setter the setter to set Also sets the propertyName.
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
            if (parentRel == null) { return importTable.getTblClass().getSimpleName(); }
            return parentRel.getRelatedField().getName();
        }

        /**
         * @return the propertyName
         */
        public final String getPropertyName()
        {
            return propertyName;
        }
        
        /**
         * @return the required
         */
        public boolean isRequired()
        {
            return required;
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
                    Vector<Relationship> rs = uploadGraph.getAllEdgeData(tv.getData(), it
                            .getTable());
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
            try
            {
                it.setParentTables(parentTables);
            }
            catch (ClassNotFoundException ex)
            {
                throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
            }
            catch (NoSuchMethodException ex)
            {
                throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
            }
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
        }
        catch (DirectedGraphException ex)
        {
            throw new UploaderException(ex);
        }
    }

    protected Vector<UploadTableInvalidValue> validateLengths(final UploadTable uploadTable)
    {
        Vector<UploadTableInvalidValue> result = new Vector<UploadTableInvalidValue>();
        for (Vector<UploadField> ufs : uploadTable.getUploadFields())
        {
            for (UploadField uf : ufs)
            {
                if (uf.getIndex() != -1)
                {
                    for (int r = 0; r < wbSS.getSpreadSheet().getRowCount(); r++)
                    {
                        String value = wbSS.getSpreadSheet().getValueAt(r, uf.getIndex())
                                .toString();
                        if (value.length() > wbSS.getColumnMaxWidth(uf.getIndex()))
                        {
                            result.add(new UploadTableInvalidValue(null, uploadTable, uf, r,
                                    new UploaderException(
                                            getResourceString(WB_CELL_LENGTH_EXCEPTION),
                                            UploaderException.INVALID_DATA)));
                        }
                    }
                }
            }
        }
        return result;
    }
    /**
     * Validates contents of all cells in dataset.
     */
    public void validateData()
    {
        dataValidated = false;
        setOpKiller(null);

        final Vector<UploadTableInvalidValue> issues = new Vector<UploadTableInvalidValue>();

        final UploaderTask validateTask = new UploaderTask(true, "WB_UPLOAD_CANCEL_MSG")
        {
            @Override
            public Object construct()
            {
                try
                {
                    int progress = 0;
                    initProgressBar(0, uploadTables.size(), true, 
                            getResourceString("WB_UPLOAD_VALIDATING") + " " + getResourceString("ERD_TABLE"), false);
                    for (UploadTable tbl : uploadTables)
                    {
                        setCurrentOpProgress(++progress, false);
                        issues.addAll(validateLengths(tbl));
                        issues.addAll(tbl.validateValues(uploadData));
                    }
                    Collections.sort(issues);
                    dataValidated = issues.size() == 0;
                    return dataValidated;
                }
                catch (Exception ex)
                {
                    setOpKiller(ex);
                    return false;
                }
            }

            @Override
            public void finished()
            {
                super.finished();
                validationIssues = issues;
                statusBar.setText("");
                if (cancelled)
                {
                    setCurrentOp(Uploader.INITIAL_STATE);
                }
                else if (dataValidated && resolver.isResolved())
                {
                    mainPanel.addMsg(new BaseUploadMessage(getResourceString("WB_DATASET_VALIDATED")));
                    setCurrentOp(Uploader.READY_TO_UPLOAD);
                }
                else
                {
                    mainPanel.addMsg(new BaseUploadMessage(getResourceString("WB_INVALID_DATASET")));
                    setCurrentOp(Uploader.USER_INPUT);
                }
            }

        };
        
        UIRegistry.getStatusBar().setText(getResourceString(Uploader.VALIDATING_DATA));
        validateTask.start();
        if (mainPanel == null)
        {
            initUI(Uploader.VALIDATING_DATA);
        }
        else
        {
            setCurrentOp(Uploader.VALIDATING_DATA);
        }
    }

    protected synchronized void cancelTask(final UploaderTask task)
    {
        boolean tooLate = true;
        if (!task.isDone())
        {
            tooLate = false;
            if (UIRegistry.displayConfirm(getResourceString("WB_UPLOAD_FORM_TITLE"),
                getResourceString(task.getCancelMsg()), getResourceString("YES"),
                getResourceString("NO"), JOptionPane.QUESTION_MESSAGE))
            {
                if (!task.isDone())
                {
                    task.interrupt();
                }
                else
                {
                    tooLate = true;
                }
            }
        }
        if (tooLate)
        {
            UIRegistry.displayErrorDlg(getResourceString("WB_UPLOAD_TASK_ALREADY_COMPLETE"));
        }
    }
    
    /**
     * @return a set of tables for which no fields are being imported, but which provide foreign
     *         keys for tables that do have fields being imported.
     * 
     * lots more to do here i think re agents (can occur in so many roles) and recursive tables.
     * also needs to distinguish between collectionObject -> CollectingEvent (missing) -> Locality
     * which is kind of bad and CollectionObject -> CollectingEvent (missing) -> Locality (missing)
     * which is useless but maybe ok.
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
            if (it.getTable() == t) { return true; }
        }
        return false;
    }

    /**
     * @param tbl
     * @return true if tbl is represented in the Workbench schema.
     */
    protected boolean isInWBSchema(final Table tbl)
    {
        return WorkbenchTask.getDatabaseSchema().getInfoById(tbl.getTableInfo().getTableId()) != null;
    }
    
    /**
     * @param tbl
     * @return true if tbl is in uploadGraph.
     */
    protected boolean isInUploadGraph(final Vertex<Table> tbl)
    {
        return uploadGraph.getVertexByLabel(tbl.getLabel()) != null;
    }
    
    /**
     * @param tbl
     * @return true if tbl's class implements Treeable.
     */
    protected boolean isTreeable(final Table tbl)
    {
        return Treeable.class.isAssignableFrom(tbl.getTableInfo().getClassObj());
        //return false;
    }
        
    /**
     * @param toMatch
     * @return matching vertex in uploadGraph.
     * Performs extra processing when Table represents a Treeable class.
     */
    protected Vertex<Table> getMatchingVertexInUpload(final Table toMatch)
    {
        if (isTreeable(toMatch))
        {
            UploadTableTree treeTbl = null;
            for (UploadTable ut : uploadTables)
            {
                if (ut instanceof UploadTableTree)
                {
                    UploadTableTree utt = (UploadTableTree)ut;
                    if (utt.getBaseTable().equals(toMatch))
                    {
                        if (treeTbl == null || treeTbl.getRank() < utt.getRank())
                        treeTbl = utt;
                    }
                }
            }
            if (treeTbl == null)
            {
                return null;
            }
            return uploadGraph.getVertexByData(treeTbl.getTable());
        }
        //else
        return uploadGraph.getVertexByData(toMatch);
    }
    /**
     * @param depth
     * @return groups of tables that can be added to the upload graph to make it a connected graph.
     * @throws DirectedGraphException
     */
    protected Vector<Vector<Table>> connectUploadGraph(final int depth) throws DirectedGraphException
    {
        //no rocket science whatsoever.
        //just try all ways to add one table. If no luck, then try all ways to add 2, 3, 4 up to depth tables.
        //This could become very very very inefficient if the number of tables in the Workbench Schema gets larger.
        Vector<Vector<Table>> result = new Vector<Vector<Table>>();
        DirectedGraph<Table, Relationship> dbGraph = this.db.getSchema().getGraph();
        for (Vertex<Table> newTbl : dbGraph.getVertices())
        {
            if (!isTreeable(newTbl.getData()) && isInWBSchema(newTbl.getData()))
            {
                if (!isInUploadGraph(newTbl))
                {
                    uploadGraph.addVertex(newTbl);
                    for (Vertex<Table> adj : dbGraph.getAdjacentVertices(newTbl))
                    {
                        //Vertex<Table> endPt = uploadGraph.getVertexByData(adj.getData());
                        Vertex<Table> endPt = getMatchingVertexInUpload(adj.getData());
                        if (endPt != null)
                        {
                            uploadGraph.addEdge(newTbl.getLabel(), endPt.getLabel());
                        }
                    }
                    for (Vertex<Table> adj : dbGraph.into(newTbl.getData()))
                    {
                        Vertex<Table> endPt = getMatchingVertexInUpload(adj.getData());
                        if (endPt != null)
                        {
                            uploadGraph.addEdge(endPt.getLabel(), newTbl.getLabel());
                        }
                    }
                    if (uploadGraph.isConnected())
                    {
                        Vector<Table> newTblResult = new Vector<Table>();
                        newTblResult.add(newTbl.getData());
                        result.add(newTblResult);
                    }
                    else if (depth - 1 > 0)
                    {
                        Vector<Vector<Table>> results = connectUploadGraph(depth - 1);
                        for (Vector<Table> tbls : results)
                        {
                            tbls.add(newTbl.getData());
                            result.add(tbls);
                        }
                    }
                    uploadGraph.removeVertex(newTbl);
                }
            }
        }
        return result;
    }
    /**
     * @return groups tables that, if added to the dataset, will probably make the dataset
     *         structurally sufficient for upload.
     */
    protected Vector<Vector<Table>> getMissingTbls() throws DirectedGraphException
    {
        Vector<Vector<Table>> result = new Vector<Vector<Table>>();
        int depth = 1;
        while (result.size() == 0 && depth < 4)
        {
            result.addAll(connectUploadGraph(depth++));
        }
        
        if (result.size() == 0)
        {
            result.add(null);
        }
        
        //It would make more sense to fix connectUploadGraph to not generate duplicate solutions,
        //but that would be hard.
        return removeDuplicateSolutions(result);
    }

    /**
     * @param solutions
     * @return a copy of solutions with only duplicate solutions (i.e same tables, different order) removed.
     */
    protected Vector<Vector<Table>> removeDuplicateSolutions(Vector<Vector<Table>> solutions)
    {
        SortedSet<Vector<Table>> sorted = new TreeSet<Vector<Table>>(
                new Comparator<Vector<Table>>()
                {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                     */
                    public int compare(Vector<Table> o1, Vector<Table> o2)
                    {
                        if (o1.size() == o2.size())
                        {
                            if (o1.containsAll(o2)) { return 0; }
                            // else
                            return o1.size() == 0 ? 0 /* ?? */: o1.get(0).getName().compareTo(
                                    o2.get(0).getName());
                        }
                        return o1.size() < o2.size() ? -1 : 1;
                        
                    }
                });
        for (Vector<Table> solution : solutions)
        {
            sorted.add(solution);
        }
        Vector<Vector<Table>> result = new Vector<Vector<Table>>();
        result.addAll(sorted);
        return result;
    }
    
    /**
     * @return true if the dataset can be uploaded.
     * 
     * Checks that the import mapping and graph are OK. Checks that all required data (TreeDefs,
     * TreeDefItems, DeterminationStatuses, etc) is present in the database.
     * 
     * Saves messages for each problem.
     */
    public Vector<UploadMessage> verifyUploadability() throws UploaderException,
            ClassNotFoundException
    {
        Vector<UploadMessage> errors = new Vector<UploadMessage>();
        try
        {
            if (!uploadGraph.isConnected())
            {                
                boolean first = true;
                for (Vector<Table> tbls : getMissingTbls())
                {
                    String msg = "";
                    if (first)
                    {
                        msg = getResourceString("WB_UPLOAD_MISSING_TBL");
                    }
                    else
                    {
                        msg += "    ";
                    }
                    if (tbls != null && tbls.size() > 0)
                    {
                        msg += " (";
                        for (int t=0; t<tbls.size(); t++)
                        {
                            if (t > 0)
                            {
                                msg += ", ";
                            }
                            msg += tbls.get(t).getTableInfo().getTitle();
                        }
                        msg += ")";
                    }
                    else
                    {
                        if (first)
                        {
                            msg += " (" + getResourceString("WB_UPLOAD_UNKNOWN_MISSING_DATA") + ")";
                        }
                        else
                        {
                            msg = "";
                        }
                    }
                    if (!msg.equals(""))
                    {
                        errors.add(new InvalidStructure(msg, tbls));
                    }
                    first = false;
                }

            }
        }
        catch (DirectedGraphException ex)
        {
            throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
        }

        errors.addAll(validateConsistency());

        // now find out what data is not available in the dataset and not available in the database
        // Considering such issues 'structural' for now.
        missingRequiredClasses.clear();
        missingRequiredFields.clear();
        Iterator<RelatedClassSetter> rces;
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
        }
        resolver = new MissingDataResolver(missingRequiredClasses, missingRequiredFields);
        for (RelatedClassSetter rcs : missingRequiredClasses)
        {
            if (!rcs.isDefined())
            {
                // Assume it is undefined because no related data exists in the database.
                // Also assuming (currently erroneously) that definition problems related to
                // choosing
                // from multiple existing related data have been resolved through user interaction.
                String tblName = DBTableIdMgr.getInstance().getByShortClassName(
                        rcs.getRelatedClass().getSimpleName()).getTitle();
                // a very vague message...
                String msg = getResourceString("WB_UPLOAD_MISSING_DBDATA") + ": " + tblName;
                errors.add(new InvalidStructure(msg, this));
            }
        }
        for (DefaultFieldEntry dfe : missingRequiredFields)
        {
            if (!dfe.isDefined())
            {
                // see note above for missignRequiredClasses iteration
                // another very vague message...
                String msg = getResourceString("WB_UPLOAD_MISSING_DBDATA") + ": "
                        + dfe.getUploadTbl().getTable().getTableInfo().getTitle() + "."
                        + dfe.getFldName(); // i18n (dfe.getFldName() is not using title nor wb
                                            // column header)
                errors.add(new InvalidStructure(msg, this));
            }
        }

        for (UploadTable t : uploadTables)
        {
            errors.addAll(t.verifyUploadability());
        }

        return errors;
    }

    /**
     * @return Vector containing messages for detected inconsistencies.
     * 
     * 
     */
    public Vector<UploadMessage> validateConsistency()
    {
        Vector<UploadMessage> result = new Vector<UploadMessage>();

        // Make sure that the tax tree levels included for each determination are consistent.
        // All need to include the same levels, for example [Genus 1, Species1, SubSpecies 1, Genus
        // 2, Species 2] is inconsistent because SubSpecies 2 is missing.
        // Since, right now, tax trees are the only trees that require this test.
        // Non-tree 1-manys like Collector are ok (but possibly not desirable?) if inconsistent:
        // [FirstName 1, LastName 1, LastName 2] works.
        Vector<TreeMapElement> maxTaxSeqLevel = null;
        for (int m = 0; m < uploadData.getCols(); m++)
        {
            if (uploadData.getMapping(m).getTable().equalsIgnoreCase("taxon"))
            {
                UploadMappingDefTree tmap = (UploadMappingDefTree) uploadData.getMapping(m);
                boolean seqSizeInconsistent = false;
                for (Vector<TreeMapElement> tmes : tmap.getLevels())
                {
                    if (tmes.size() > 1
                            || (maxTaxSeqLevel != null && maxTaxSeqLevel.get(0).getRank() < tmes
                                    .get(0).getRank()))
                    {
                        if (maxTaxSeqLevel == null)
                        {
                            maxTaxSeqLevel = tmes;
                        }
                        else if (maxTaxSeqLevel.size() != tmes.size())
                        {
                            seqSizeInconsistent = true;
                            if (maxTaxSeqLevel.size() < tmes.size())
                            {
                                maxTaxSeqLevel = tmes;
                            }
                        }
                    }
                }

                if (seqSizeInconsistent && maxTaxSeqLevel != null)
                {
                    for (Vector<TreeMapElement> tmes : tmap.getLevels())
                    {
                        if (tmes.get(0).getRank() > maxTaxSeqLevel.get(0).getRank()
                                && tmes.size() < maxTaxSeqLevel.size())
                        {
                            boolean[] seqsPresent = new boolean[maxTaxSeqLevel.size()];
                            for (int b = 0; b < seqsPresent.length; b++)
                                seqsPresent[b] = false;
                            for (TreeMapElement tme : tmes)
                            {
                                seqsPresent[tme.getSequence()] = true;
                            }
                            for (int s = 0; s < seqsPresent.length; s++)
                            {
                                if (!seqsPresent[s])
                                {
                                    String levelName = tmes.get(0).getWbFldName();
                                    // strip off trailing number (assuming we will never allow it to
                                    // be > 10) and trim.
                                    levelName = levelName.substring(0, levelName.length() - 2)
                                            .trim();
                                    String msg = getResourceString("WB_UPLOAD_MISSING_FLD") + ": "
                                            + levelName + " " + Integer.toString(s + 1);
                                    result.add(new InvalidStructure(msg, null));
                                }
                            }
                        }
                    }
                }
            }
        }

        if (maxTaxSeqLevel != null && maxTaxSeqLevel.size() > 1)
        {
            // check to see if Determination.isCurrent is either not present or always present.
            int isCurrentCount = 0;
            boolean[] isCurrentPresent = new boolean[maxTaxSeqLevel.size()];
            for (int b = 0; b < isCurrentPresent.length; b++)
                isCurrentPresent[b] = false;
            String isCurrentCaptionSample = null;
            for (int m = 0; m < uploadData.getCols(); m++)
            {
                if (uploadData.getMapping(m).getTable().equalsIgnoreCase("determination")
                        && uploadData.getMapping(m).getField().equalsIgnoreCase(
                                "determinationstatusid"))
                {
                    UploadMappingDefRel rMap = (UploadMappingDefRel) uploadData.getMapping(m);
                    for (ImportMappingRelFld fld : rMap.getRelatedFields())
                    {
                        if (fld.getFieldName().equalsIgnoreCase("iscurrent"))
                        {
                            isCurrentCount++;
                            isCurrentPresent[rMap.getSequence()] = true;
                            isCurrentCaptionSample = rMap.getWbFldName();
                        }
                    }
                }
            }
            if (isCurrentCount != 0 && isCurrentCount != maxTaxSeqLevel.size())
            {
                for (int c = 0; c < isCurrentPresent.length; c++)
                {
                    if (!isCurrentPresent[c])
                    {
                        String fldName;
                        // strip off trailing number (assuming we will never allow it to be > 10)
                        // and trim.
                        if (isCurrentCaptionSample != null)
                        {
                            fldName = isCurrentCaptionSample.substring(0,
                                    isCurrentCaptionSample.length() - 2).trim();
                        }
                        else
                        {
                            fldName = "Is Current"; // i18n (but isCurrentCaptionSample can't be
                                                    // null. Right.)
                        }
                        String msg = getResourceString("WB_UPLOAD_MISSING_FLD") + ": " + fldName
                                + " " + Integer.toString(c + 1);
                        result.add(new InvalidStructure(msg, null));
                    }
                }
            }
        }
        if (result.size() > 0)
        {
            currentUpload = null;
        }
        return result;
    }

    /**
     * @throws UploaderException
     * 
     * Sets up for upload.
     */
    protected void prepareToUpload()
    {
        for (UploadTable t : uploadTables)
        {
            t.prepareToUpload();
        }
        // But may want option to ONLY upload rows that were skipped...
        skippedRows.clear();
        messages.clear();
        newMessages.clear();
    }

    /**
     * @param opName
     * 
     * Puts UI into correct state for current upload phase.
     */
    public synchronized void setCurrentOp(final String opName)
    {
        previousOp = currentOp;
        currentOp = opName;
        if (mainPanel == null)
        {
            log.error("UI does not exist.");
            return;
        }
        setupUI(currentOp);
        setOpKiller(null);
    }

    protected synchronized void setOpKiller(final Exception killer)
    {
        opKiller = killer;
    }
    
    protected synchronized Exception getOpKiller()
    {
        return opKiller;
    }
    
    /**
     * @param min
     * @param max
     * @param paintString - true if the progress bar should display string description of progress
     * @param itemName - string description will be: "itemName x of max" (using English resource).
     * 
     * Initializes progress bar for upload actions. If min and max = 0, sets progress bar is
     * indeterminate.
     */
    protected void initProgressBar(final int min,
                                   final int max,
                                   final boolean paintString,
                                   final String itemName,
                                   final boolean useAppStatBar)
    {
        SwingUtilities.invokeLater(new Runnable() {
           public void run()
           {
               if (mainPanel == null)
               {
                   log.error("UI does not exist.");
                   return;
               }
               JProgressBar pb;
               if (useAppStatBar)
               {
                   pb = UIRegistry.getStatusBar().getProgressBar();
               }
               else
               {
                   pb = mainPanel.getCurrOpProgress();
               }
               pb.setVisible(true);
               if (min == 0 && max == 0)
               {
                   pb.setIndeterminate(true);
                   pb.setString("");
               }
               else
               {
                   if (pb.isIndeterminate())
                   {
                       pb.setIndeterminate(false);
                   }
                   pb.setStringPainted(paintString);
                   if (paintString)
                   {
                       pb.setName(itemName);
                   }
                   pb.setMinimum(min);
                   pb.setMaximum(max);
                   pb.setValue(min);
               }
           }
        });
    }

    /**
     * @param val
     * 
     * Sets progress bar progress.
     */
    protected void setCurrentOpProgress(final int val, final boolean useAppStatBar)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {

                if (mainPanel == null && !useAppStatBar)
                {
                    log.error("UI does not exist.");
                    return;
                }
                JProgressBar pb;
                if (useAppStatBar)
                {
                    pb = UIRegistry.getStatusBar().getProgressBar();
                }
                else
                {
                    pb = mainPanel.getCurrOpProgress();
                }
                if (!pb.isIndeterminate())
                {
                    int newVal = val == -1 ? Math.min(pb.getValue()+1, pb.getMaximum()) : val;
                    pb.setValue(newVal);
                    if (pb.isStringPainted())
                    {
                        pb.setString(String.format(getResourceString("WB_UPLOAD_PROGRESSBAR_TEXT"),
                                new Object[] { pb.getName(), Integer.toString(newVal),
                                        Integer.toString(pb.getMaximum()) }));
                    }
                }
            }
        });
    }

    protected void showUploadProgress(final int val)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                if (mainPanel == null)
                {
                    log.error("UI does not exist.");
                    return;
                }
                setCurrentOpProgress(val, false);
                for (UploadMessage newMsg : newMessages)
                {
                    mainPanel.addMsg(newMsg);
                    messages.add(newMsg);
                }
                newMessages.clear();
            }
        });
    }

    public void undoStep()
    {
        setCurrentOpProgress(-1, false);
    }
    
    protected void updateObjectsCreated()
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                mainPanel.updateObjectsCreated();
            }
        });
    }

    /**
     * @param initOp
     * 
     * builds upload ui with initial phase of initOp
     */
    protected void initUI(final String initOp)
    {
        buildMainUI();
        setCurrentOp(initOp);
    }

    public void startUI()
    {
        if (mainPanel == null)
        {
            log.error("Upload ui is null");
            return;
        }
        mainPanel.clearMsgs(new Class<?>[] {BaseUploadMessage.class});
        setCurrentOp(Uploader.INITIAL_STATE);
    }

    /**
     * Gets default values for all missing required classes (foreign keys) and local fields.
     */
    public void getDefaultsForMissingRequirements()
    {
        setOpKiller(null);
        final UploaderTask uploadTask = new UploaderTask(false, "")
        {
            boolean          success   = false;

            @Override
            public Object construct()
            {
                try
                {
                    missingRequiredClasses.clear();
                    missingRequiredFields.clear();
                    Iterator<RelatedClassSetter> rces;
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
                    resolver = new MissingDataResolver(missingRequiredClasses,
                            missingRequiredFields);
                    return null;
                }
                catch (Exception ex)
                {
                    setOpKiller(ex);
                    return false;
                }
            }

            @Override
            public void finished()
            {
                super.finished();
                statusBar.setText("");
                if (success)
                {
                    mainPanel.addMsg(new BaseUploadMessage(getResourceString("WB_REQUIRED_RETRIEVED")));
                }
                else
                {
                    mainPanel.addMsg(new BaseUploadMessage(getResourceString("WB_REQUIRED_RETRIEVED")));
                    setCurrentOp(Uploader.FAILURE);
                }
            }

        };

        UIRegistry.getStatusBar().setText(getResourceString("WB_UPLOAD_CHECKING_REQS"));
        uploadTask.start();
        initUI(Uploader.CHECKING_REQS);
    }

    /**
     * Called when dataset is saved.
     */
    public void refresh()
    {
        //wbSS.getWorkbench().forceLoad();
        uploadData.refresh(wbSS.getWorkbench().getWorkbenchRowsAsList());
        validateData();
    }

    /**
     * Called when dataset is closing. If shutter is not the WorkbenchPane for the dataSet
     * being uploaded, the call is ignored.
     * 
     * @param shutter
     * 
     * @return true if closed, else false.
     */
    public boolean closing(final WorkbenchPaneSS shutter)
    {
        if (shutter != wbSS)
        {
            return false;
        }
        if (mainPanel != null)
        {
            closeMainForm(false);
        }
        return true;
    }

    /**
     * @return count of total number of objects uploaded
     */
    protected Integer getUploadedObjects()
    {
        Integer result = 0;
        for (UploadTable ut : uploadTables)
        {
            result += ut.getUploadedRecs().size();
        }
        return result;
    }

    /**
     * Shuts down upload UI.
     * @param notifyWB - If true, notify this Uploader's WorkBench.
     */
    protected void closeMainForm(boolean notifyWB)
    {
        mainPanel.setVisible(false);
        mainPanel = null;
        closeUploadedDataViewers();
        currentUpload = null;
        if (notifyWB)
        {
            wbSS.uploadDone();
        }
    }

    public UploadMainPanel getMainPanel()
    {
        if (mainPanel == null)
        {
            initUI(INITIAL_STATE);
        }
        return mainPanel;
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

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     * 
     * Responds to user actions in UI.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals(UploadMainPanel.VALIDATE_CONTENT))
        {
            validateData();
        }
        else if (e.getActionCommand().equals(UploadMainPanel.DO_UPLOAD))
        {
            uploadIt();
        }
        else if (e.getActionCommand().equals(UploadMainPanel.VIEW_UPLOAD))
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
        else if (e.getActionCommand().equals(UploadMainPanel.VIEW_SETTINGS))
        {
            showSettings();
            if (currentOp.equals(Uploader.READY_TO_UPLOAD) && !resolver.isResolved())
            {
                setCurrentOp(Uploader.USER_INPUT);
            }
            else if (currentOp.equals(Uploader.USER_INPUT) && resolver.isResolved())
            {
                setCurrentOp(Uploader.READY_TO_UPLOAD);
            }
        }
        else if (e.getActionCommand().equals(UploadMainPanel.CLOSE_UI))
        {
            if (aboutToShutdown(wbSS))
            {
                closeMainForm(true);
            }
        }
        else if (e.getActionCommand().equals(UploadMainPanel.UNDO_UPLOAD))
        {
            if (UIRegistry.displayConfirm(getResourceString("WB_UPLOAD_FORM_TITLE"),
                    getResourceString("WB_UNDO_UPLOAD_MSG"), getResourceString("OK"),
                    getResourceString("CANCEL"), JOptionPane.QUESTION_MESSAGE))
            {
                undoUpload(true, false);
            }
        }
        else if (e.getActionCommand().equals(UploadMainPanel.TBL_DBL_CLICK))
        {
            mainPanel.getViewUploadBtn().setEnabled(canViewUpload(currentOp));
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
        else if (e.getActionCommand().equals(UploadMainPanel.TBL_CLICK))
        {
            mainPanel.getViewUploadBtn().setEnabled(canViewUpload(currentOp));
        }
        else if (e.getActionCommand().equals(UploadMainPanel.MSG_CLICK))
        {
            goToMsgWBCell((Component)e.getSource());
        }
        else if (e.getActionCommand().equals(UploadMainPanel.PRINT_INVALID))
        {
            printInvalidValReport();
        }
        else if (e.getActionCommand().equals(UploadMainPanel.CANCEL_OPERATION))
        {
            if (currentTask != null && currentTask.isCancellable())
            {
                cancelTask(currentTask);
            }
            else
            {
                log.info("ignoring action: " + e.getActionCommand());
            }
        }
        else
            log.error("Unrecognized action: " + e.getActionCommand());
    }

    /**
     * Called when the WorkbenchPaneSS for the uploaded dataset is shutting down, and when
     * the Upload UI 'Close' button is clicked.
     * 
     * @param shuttingDownSS - the dataset that is shutting down.
     * @return true if the Uploader can be closed, otherwise false.
     */
    public boolean aboutToShutdown(final WorkbenchPaneSS shuttingDownSS)
    {
        if (shuttingDownSS != wbSS)
        {
            return true;
        }
        if (currentTask != null)
        {
            JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), getResourceString("WB_UPLOAD_BUSY_CANNOT_CLOSE"));
            return false;
        }
        if (currentOp.equals(Uploader.SUCCESS) && getUploadedObjects() > 0)
        {
            boolean result = false;
            String msg = String.format(getResourceString("WB_UPLOAD_CONFIRM_SAVE"), wbSS.getWorkbench().getName());
            JFrame topFrame = (JFrame)UIRegistry.getTopWindow();
            int rv = JOptionPane.showConfirmDialog(topFrame,
                                                   msg,
                                                   getResourceString("WB_UPLOAD_FORM_TITLE"),
                                                   JOptionPane.YES_NO_CANCEL_OPTION);
            
            if (rv == JOptionPane.YES_OPTION)
            {
                saveRecordSets();
                result = true;
            }
            if (rv == JOptionPane.NO_OPTION)
            {
                undoUpload(false, true);
                result = true;
            }
            //else rv equals JOptionPane.CANCEL_OPTION or CLOSED_OPTION
            if (result)
            {
                for (UploadTable ut : uploadTables)
                {
                    try
                    {
                        ut.shutdown();
                    }
                    catch (UploaderException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                }
            }
            return result;
        }
        return true;
    }
    
    protected void showSettings()
    {
        boolean readOnly = !currentOp.equals(Uploader.READY_TO_UPLOAD)
                && !currentOp.equals(Uploader.USER_INPUT);
        UploadSettingsPanel usp = new UploadSettingsPanel(uploadTables);
        usp.buildUI(resolver, readOnly);
        CustomDialog cwin;
        if (!readOnly)
        {
            cwin = new CustomDialog((Frame) UIRegistry.getTopWindow(),
                    getResourceString("WB_UPLOAD_SETTINGS"), true, usp);
        }
        else
        {
            cwin = new CustomDialog((Frame) UIRegistry.getTopWindow(),
                    getResourceString("WB_UPLOAD_SETTINGS"), true, CustomDialog.OK_BTN, usp,
                    CustomDialog.OK_BTN);
        }

        cwin.setModal(true);
        UIHelper.centerAndShow(cwin);
        if (!cwin.isCancelled())
        {
            usp.getMatchPanel().apply();
        }
        cwin.dispose();
    }

    /**
     * @author timbo
     * 
     * @code_status Alpha
     * 
     * Datasource for printing validation issues for current upload.
     * 
     */
    class InvalidValueJRDataSource implements JRDataSource
    {
        protected int                                   rowIndex;
        protected final Vector<UploadTableInvalidValue> rows;

        public InvalidValueJRDataSource(final Vector<UploadTableInvalidValue> rows)
        {
            this.rows = rows;
            rowIndex = -1;
        }

        /*
         * (non-Javadoc)
         * 
         * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
         */
        public Object getFieldValue(final JRField field) throws JRException
        {
            if (field.getName().equals("row")) { return String.valueOf(rows.get(rowIndex).getRow()); }
            if (field.getName().equals("col")) { return String.valueOf(rows.get(rowIndex)
                    .getUploadFld().getWbFldName()); }
            if (field.getName().equals("description")) { return rows.get(rowIndex).getDescription(); }
            if (field.getName().equals("datasetName")) { return wbSS.getWorkbench().getName(); }
            if (field.getName().equals("cellData")) { return uploadData.get(rows.get(rowIndex)
                    .getRow(), rows.get(rowIndex).getUploadFld().getIndex()); }
            log.error("Unrecognized field Name: " + field.getName());
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see net.sf.jasperreports.engine.JRDataSource#next()
         */
        public boolean next() throws JRException
        {
            if (rowIndex >= rows.size() - 1) { return false; }
            rowIndex++;
            return true;
        }
    }

    /**
     * Launches ReportTask to display report of validation issues.
     */
    protected void printInvalidValReport()
    {
        if (validationIssues == null || validationIssues.size() == 0)
        {
            // this should never be called but just in case.
            log.error("no validationIssues");
            return;
        }
        InvalidValueJRDataSource src = new InvalidValueJRDataSource(validationIssues);
        final CommandAction cmd = new CommandAction(ReportsBaseTask.REPORTS,
                ReportsBaseTask.PRINT_REPORT, src);
        cmd.setProperty("title", "Validation Issues");
        cmd.setProperty("file", "upload_problem_report.jrxml");
        CommandDispatcher.dispatch(cmd);
    }

    /**
     * Moves to dataset cell corresponding to currently selected validation issue and starts editor.
     */
    protected void goToMsgWBCell(final Component c)
    {
        if (mainPanel == null) { throw new RuntimeException("Upload form does not exist."); }
        if (wbSS != null)
        {
            UploadMessage msg;
            if (c == mainPanel.getValidationErrorList())
            {
                msg = (UploadMessage) mainPanel.getValidationErrorList().getSelectedValue();
            }
            else
            {
                msg = (UploadMessage) mainPanel.getMsgList().getSelectedValue();
            }
            if (msg.getRow() != -1)
            {
                if (msg.getCol() == -1)
                {
                    wbSS.getSpreadSheet().scrollToRow(msg.getRow());
                    wbSS.getSpreadSheet().getSelectionModel().clearSelection();
                    wbSS.getSpreadSheet().getSelectionModel().setSelectionInterval(
                            msg.getRow() - 1, msg.getRow() - 1);
                }
                else
                {
                    Rectangle rect = wbSS.getSpreadSheet().getCellRect(msg.getRow(), msg.getCol(),
                            false);
                    wbSS.getSpreadSheet().scrollRectToVisible(rect);
                    if (msg instanceof UploadTableInvalidValue && msg.getCol() != -1)
                    {
                        wbSS.getSpreadSheet().editCellAt(msg.getRow(), msg.getCol(), null);
                        
                        //Now add this as a listener to the editorComponent to allow moving to next/prev
                        //invalid cell after ENTER/TAB/UP/DOWN
                        //Currently doesn't work. No KeyEvents make it to this.keyTyped().
                        //But alternate approach to enable the spreadsheet and intercept it's KeyEvents
                        //was too complex.
                        Component editor = wbSS.getSpreadSheet().getEditorComponent();
                        boolean addListener = true;
                        KeyListener[] listeners = editor.getKeyListeners();
                        for (int k=0; k<listeners.length; k++)
                        {
                            if (listeners[k] == this)
                            {
                                addListener = false;
                                break;
                            }
                        }
                        if (addListener)
                        {
                            editor.addKeyListener(this);
                        }
                        wbSS.getSpreadSheet().grabFocus();
                    }
                }
            }
        }
    }

    /**
     * Moves to and begins editing the next invalid WorkBench cell.
     * 
     * Not complete. Needs to limit selections to UploadTableInvalidValue objects.
     * 
     * See note in goToMsgWBCell re addKeyListener().
     */
    protected void goToNextInvalidCell(final Component c)
    {
        int sel = mainPanel.getMsgList().getSelectedIndex() + 1;
        if (sel >= mainPanel.getMsgList().getModel().getSize())
            sel = 0;
        if (sel != -1)
        {
            mainPanel.getMsgList().setSelectedIndex(sel);
            goToMsgWBCell(c);
        }
    }
    
    /**
     * Moves to and begins editing the previous invalid WorkBench cell.
     *
     * Not complete. Needs to limit selections to UploadTableInvalidValue objects.
     * 
     * See note in goToMsgWBCell re addKeyListener().
    */
    protected void goToPrevInvalidCell(final Component c)
    {
        int sel = mainPanel.getMsgList().getSelectedIndex() - 1;
        if (sel <= 0)
            sel = mainPanel.getMsgList().getModel().getSize()-1;
        if (sel != -1)
        {
            mainPanel.getMsgList().setSelectedIndex(sel);
            goToMsgWBCell(c);
        }
    }
    
    /**
     * Moves to and begins editing the first invalid WorkBench cell.
     *
     * Not complete. Needs to limit selections to UploadTableInvalidValue objects.
     * 
     * See note in goToMsgWBCell re addKeyListener().
     */
    protected void goToFirstInvalidCell(final Component c)
    {
        if (mainPanel.getMsgList().getModel().getSize() > 0)
        {
            mainPanel.getMsgList().setSelectedIndex(0);
            goToMsgWBCell(c);
        }
    }

    /**
     * Moves to and begins editing the last invalid WorkBench cell.
     *
     * Not complete. Needs to limit selections to UploadTableInvalidValue objects.
     * 
     * See note in goToMsgWBCell re addKeyListener().
     */
    protected void goToLastInvalidCell(final Component c)
    {
        if (c instanceof JList)
        {
            if (((JList)c).getModel().getSize() > 0)
            {
                ((JList)c).setSelectedIndex(((JList)c).getModel().getSize()-1);
                goToMsgWBCell(c);
            }
        }
    }

    /**
     * Builds form for upload UI.
     */
    protected void buildMainUI()
    {
        mainPanel = new UploadMainPanel();

        SortedSet<UploadInfoRenderable> uts = new TreeSet<UploadInfoRenderable>();
        for (UploadTable ut : uploadTables)
        {
            UploadInfoRenderable render = new UploadInfoRenderable(ut);
            if (uts.contains(render))
            {
                for (UploadInfoRenderable r : uts)
                {
                    if (r.equals(render))
                    {
                        r.addTable(ut);
                        break;
                    }
                }
            }
            else
            {
                uts.add(new UploadInfoRenderable(ut));
            }
        }
        mainPanel.addAffectedTables(uts.iterator());
        mainPanel.setActionListener(this);
    }

    /**
     * @param op
     * 
     * Sets up mainPanel for upload phase for op.
     */
    protected void setupUI(final String op)
    {
//        SwingUtilities.invokeLater(new Runnable() {
//           public void run()
//           {
               if (mainPanel == null)
               {
                   log.error("UI does not exist.");
                   return;
               }

               if (op.equals(Uploader.SUCCESS))
               {
                   if (mainPanel.getUploadTbls().getSelectedIndex() == -1)
                   {
                       // assuming list is not empty
                       mainPanel.getUploadTbls().setSelectedIndex(0);
                   }
               }

               if (op.equals(UPLOADING) || op.equals(SUCCESS))
               {
                   mainPanel.showUploadTblTbl();
               }
               else
               {
                   mainPanel.showUploadTblList();
               }
               
               mainPanel.getValidateContentBtn().setEnabled(canValidateContent(op));
               
               mainPanel.getCancelBtn().setEnabled(canCancel(op));
               mainPanel.getCancelBtn().setVisible(mainPanel.getCancelBtn().isEnabled());

               mainPanel.getDoUploadBtn().setEnabled(canUpload(op));

               mainPanel.getViewSettingsBtn().setEnabled(canViewSettings(op));

               mainPanel.getViewUploadBtn().setEnabled(canViewUpload(op));
               mainPanel.getViewUploadBtn().setVisible(mainPanel.getViewUploadBtn().isEnabled());

               mainPanel.getUndoBtn().setEnabled(canUndo(op));
               mainPanel.getUndoBtn().setVisible(mainPanel.getUndoBtn().isEnabled());

               mainPanel.getCloseBtn().setEnabled(canClose(op));

               mainPanel.getCurrOpProgress().setVisible(mainPanel.getCancelBtn().isVisible());
               
               String statText;
               if (previousOp != null && previousOp.equals(UNDOING_UPLOAD) && op.equals(FAILURE))
               {
                   statText = getResourceString("WB_UPLOAD_UNDO_FAILURE");
               }
               else
               {
                   statText = getResourceString(op);
               }
               Exception killer = getOpKiller();
               if (op.equals(Uploader.SUCCESS))
               {
                   statText += ". " + getUploadedObjects().toString() + " "
                           + getResourceString("WB_UPLOAD_OBJECT_COUNT") + ".";
                   if (killer != null)
                   {
                       log.debug("Hey. Wait a minute. The operation succeeded while dead. Is that not creepy?");
                   }
               }
               else if (op.equals(Uploader.FAILURE) && killer != null)
               {
                   if (StringUtils.isNotEmpty(killer.getLocalizedMessage()))
                   {
                       statText += ": " + killer;
                   }
                   else
                   {
                       statText += ": " + killer.getLocalizedMessage();
                   }
               }
               if (dotDotDot(op)) 
               {
                   statText += "...";
               }
               
               mainPanel.clearMsgs(new Class<?>[]{UploadTableInvalidValue.class});
               if (op.equals(USER_INPUT))
               {
                   mainPanel.addMsg(new UploadTableInvalidValue(statText, null, null, -1, null));
               }
               else
               {
                   mainPanel.addMsg(new BaseUploadMessage(statText));
               }
               
               if (validationIssues != null)
               {
                   for (int m=0; m<validationIssues.size() && m<MAX_MSG_DISPLAY_COUNT; m++)
                   {
                       mainPanel.addMsg(validationIssues.get(m));
                   }
                   if (validationIssues.size() > MAX_MSG_DISPLAY_COUNT)
                   {
                       log.info("Only displaying " + String.valueOf(MAX_MSG_DISPLAY_COUNT) + " of " 
                               + String.valueOf(validationIssues.size()) + " validation errors ");
                       JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), 
                               String.format(getResourceString(WB_TOO_MANY_ERRORS), String.valueOf(MAX_MSG_DISPLAY_COUNT),
                                           String.valueOf(validationIssues.size())), 
                               getResourceString(WB_UPLOAD_FORM_TITLE), 
                               JOptionPane.WARNING_MESSAGE,
                               null);
                   }
               }
               mainPanel.getPrintBtn().setEnabled(validationIssues != null && validationIssues.size() > 0);
//           }
//        });
    }

    /**
     * Opens view of uploaded data for selected table. Initializes viewer object if necessary.
     */
    protected void viewSelectedTable()
    {
        if (currentOp.equals(Uploader.SUCCESS))
        {
            viewUploadsAll();
//            if (mainPanel.getUploadTbls().getSelectedValue() != null)
//            {
//                if (bogusStorages != null)
//                {
//                    if (bogusViewer == null)
//                    {
//                        bogusViewer = db.new BogusViewer(bogusStorages);
//                    }
//                    if (bogusViewer != null)
//                    {
//                        bogusViewer.viewBogusTbl(((UploadInfoRenderable) mainPanel.getUploadTbls()
//                                .getSelectedValue()).getTableName(), true);
//                    }
//                }
//            }
        }
    }

    protected void viewUploadsAll()
    {
        new UploadRetriever().viewUploads(uploadTables, wbSS.getTask(), getResourceString(WB_UPLOAD_VIEW_RESULTS_TITLE));
    }
    
    protected void viewUpload(final UploadTable uploadTable)
    {
        List<UploadTable> lst = new LinkedList<UploadTable>();
        lst.add(uploadTable);
        new UploadRetriever().viewUploads(lst, wbSS.getTask(), getResourceString(WB_UPLOAD_VIEW_RESULTS_TITLE));
    }
    
    protected boolean dotDotDot(final String op)
    {
        return op.equals(Uploader.UPLOADING)
          || op.equals(Uploader.CHECKING_REQS)
          || op.equals(Uploader.CLEANING_UP)
          || op.equals(Uploader.RETRIEVING_UPLOADED_DATA)
          || op.equals(Uploader.VALIDATING_DATA)
          || op.equals(Uploader.USER_INPUT)
          || op.equals(Uploader.UNDOING_UPLOAD);
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
        return op.equals(Uploader.UPLOADING) || op.equals(Uploader.CHECKING_REQS)
                || op.equals(Uploader.VALIDATING_DATA)
                || op.equals(Uploader.RETRIEVING_UPLOADED_DATA);
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
    protected boolean canValidateContent(final String op)
    {
        return /*op.equals(Uploader.USER_INPUT) ||*/ op.equals(Uploader.INITIAL_STATE)
                || op.equals(Uploader.FAILURE);
    }

    /**
     * @param op
     * @return true if Close button is applicable in phase op.
     */
    protected boolean canClose(final String op)
    {
        return op.equals(Uploader.READY_TO_UPLOAD) || op.equals(Uploader.USER_INPUT)
                || op.equals(Uploader.SUCCESS) || op.equals(Uploader.INITIAL_STATE)
                || op.equals(Uploader.FAILURE);
    }

    /**
     * @param op
     * @return true if canViewSettings in phase op.
     */
    protected boolean canViewSettings(final String op)
    {
        return op.equals(Uploader.READY_TO_UPLOAD) || op.equals(Uploader.USER_INPUT)
        // || op.equals(Uploader.SUCCESS)
                // || op.equals(Uploader.INITIAL_STATE)
                || op.equals(Uploader.FAILURE);
    }

    /**
     * @param op
     * @return true if canViewUpload in phase op.
     */
    protected boolean canViewUpload(final String op)
    {
        return op.equals(Uploader.SUCCESS) && mainPanel.getUploadTbls().getSelectedIndex() != -1;
    }

    /**
     * Uploads dataset.
     */
    public void uploadIt() 
    {
        buildIdentifier();
        setOpKiller(null);
        prepareToUpload();

        final UploaderTask uploadTask = new UploaderTask(true, "WB_CANCEL_UPLOAD_MSG")
        {
            boolean success = false;
            
            @SuppressWarnings("synthetic-access")
            @Override
            public Object construct()
            {
                initProgressBar(0, uploadData.getRows(), true, 
                        getResourceString("WB_UPLOAD_UPLOADING") + " " + getResourceString("WB_ROW"), false);
                try
                {
                    for (rowUploading = 0; rowUploading < uploadData.getRows();)
                    {
                        if (cancelled)
                        {
                            break;
                        }
                        log.debug("uploading row " + String.valueOf(rowUploading));
                        setCurrentOpProgress(rowUploading + 1, false);
                        for (UploadTable t : uploadTables)
                        {
                            if (cancelled)
                            {
                                break;
                            }
                            try
                            {
                                uploadRow(t, rowUploading);
                            }
                            catch (UploaderException ex)
                            {
                                if (ex.getStatus() == UploaderException.ABORT_ROW)
                                {
                                    log.debug(ex.getMessage());
                                    abortRow(ex, rowUploading);
                                    break;
                                }
                                throw ex;
                            }
                            updateObjectsCreated();
                        }
                        rowUploading++;
                        showUploadProgress(rowUploading);
                    }
                    //But where is the best place to do this?
                    //Potentially the longest step.
                    //Need extra progress info...
                    for (UploadTable t : uploadTables)
                    {
                        t.finishUpload();
                    }
                }
                catch (Exception ex)
                {
                    setOpKiller(ex);
                    return false;
                }
                success = !cancelled;
                return success;
            }

            @Override
            public void finished()
            {
                super.finished();
                statusBar.setText("");
                if (success)
                {
                    setCurrentOp(Uploader.SUCCESS);
                }
                else
                {
                    mainPanel.clearObjectsCreated();
                    //undoUpload will clear opKiller, so save it and reassign, after call. (iffy?)
                    Exception savedOpKiller = getOpKiller();
                    undoUpload(false, false);
                    setOpKiller(savedOpKiller);
                    
                    if (!cancelled)
                    {
                        setCurrentOp(Uploader.FAILURE);
                    }
                }
            }

        };

        UIRegistry.getStatusBar().setText(getResourceString(Uploader.UPLOADING));
        uploadTask.start();
        if (mainPanel == null)
        {
            initUI(Uploader.UPLOADING);
        }
        else
        {
            setCurrentOp(Uploader.UPLOADING);
        }
    }

    protected synchronized void abortRow(UploaderException cause, int row)
    {
        log.debug("NOT undoing writes which have already occurred while processing aborted row");
        SkippedRow sr = new SkippedRow(cause, row + 1);
        skippedRows.add(sr);
        newMessages.add(sr);
    }

    public synchronized void addMsg(final UploadMessage msg)
    {
        newMessages.add(msg);
    }

    
    /**
     * @return uploadTables ordered such that unDo will work.
     * 
     * Currently this merely moves XXXAttributes tables to the end of the ordering.
     * This prevents problems caused by the DeleteOrphan annotation for the XXX->XXXAttribute relationships.
     * This is a horrible hack, but should work for now. If not, it could be achieved more effectively by
     * checking the annotations properties and re-ordering according.
     * 
     * 
     */
    protected List<UploadTable> reorderUploadTablesForUndo()
    {
        /*Actually, regardless of order, it seems impossible to undo coherently with the current
         * strange cascade definition on the many side of the CollectionObjectAttribute/CollectionObjects relationship.
         * 
         * So just leaving the ordering alone
         */
//        Vector<UploadTable> result = new Vector<UploadTable>(uploadTables.size());
      Vector<UploadTable> result = uploadTables;
//        for (UploadTable ut : uploadTables)
//        {
//            if (ut.getTable().getName().endsWith("Attribute"))
//            {
//                result.insertElementAt(ut, 0);
//            }
//            else
//            {
//                result.add(ut);
//            }
//
//        }
        
        for (UploadTable ut : result)
        {
            System.out.println(ut);
        }

        return result;
    }
    
    /**
     * Undoes the most recent upload.
     * 
     * Called in response to undo command from user, and by the program when an upload is cancelled
     * or fails.
     */
    public void undoUpload(final boolean isUserCmd, final boolean shuttingDown)
    {
        setOpKiller(null);
        
        if (shuttingDown)
        {
            //This process can take a long time and needs some kind of progress indication.
            UIRegistry.writeGlassPaneMsg(String.format(getResourceString("WB_UPLOAD_CLEANING_UP") + "...", 
                    wbSS.getWorkbench().getName()), WorkbenchTask.GLASSPANE_FONT_SIZE);
            try
            {
                List<UploadTable> fixedUp = reorderUploadTablesForUndo();
                try
                {
                    for (int ut = fixedUp.size() - 1; ut >= 0; ut--)
                    {
                        fixedUp.get(ut).undoUpload();
                    }
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), String.format(
                            getResourceString("WB_UPLOAD_CLEANUP_FAILED"), new Object[] {
                                    getResourceString((isUserCmd ? "WB_UPLOAD_UNDO_BTN"
                                            : "WB_UPLOAD_CLEANUP")), wbSS.getWorkbench().getName(),
                                    wbSS.getWorkbench().getName() }), getResourceString("Warning"),
                            JOptionPane.WARNING_MESSAGE);
                    setOpKiller(ex); //probably doesn't matter if shuttingDown but..
                    return;
                }
            }
            finally
            {
                UIRegistry.clearGlassPaneMsg();
            }
        }
        else
        {
            final UploaderTask undoTask = new UploaderTask(false, "")
            {
                boolean success = false;

                @Override
                public Object construct()
                {
                    try
                    {
                        if (isUserCmd)
                        {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                public void run()
                                {
                                    initProgressBar(0, getUploadedObjects(), true,
                                            getResourceString("WB_UPLOAD_UNDOING") + " "
                                                    + getResourceString("WB_UPLOAD_OBJECT"), false);
                                }
                            });
                        }
                        else
                        {
                            SwingUtilities.invokeAndWait(new Runnable()
                            {
                                public void run()
                                {
                                    initProgressBar(0, getUploadedObjects(), true,
                                            getResourceString("WB_UPLOAD_CLEANING_UP") + " "
                                                    + getResourceString("WB_UPLOAD_OBJECT"), false);
                                }
                            });
                        }
                        List<UploadTable> fixedUp = reorderUploadTablesForUndo();
                        for (int ut = fixedUp.size() - 1; ut >= 0; ut--)
                        {
                            //setCurrentOpProgress(fixedUp.size() - ut, false);
                            fixedUp.get(ut).undoUpload();
                        }
                        success = true;
                        return success;
                    }
                    catch (Exception ex)
                    {
                        setOpKiller(ex);
                        return false;
                    }
                }

                @Override
                public void finished()
                {
                    super.finished();
                    statusBar.setText("");
                    statusBar.getProgressBar().setVisible(false);
                    if (getOpKiller() != null)
                    {
                        JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), 
                                String.format(getResourceString("WB_UPLOAD_CLEANUP_FAILED"),
                                        new Object[] {getResourceString((isUserCmd ? "WB_UPLOAD_UNDO_BTN" : "WB_UPLOAD_CLEANUP")),
                                                        wbSS.getWorkbench().getName(), wbSS.getWorkbench().getName()
                                }),
                                getResourceString("Warning"), JOptionPane.WARNING_MESSAGE);

                    }
                    if (mainPanel != null)
                    {
                        mainPanel.clearObjectsCreated();
                        if (success)
                        {
                            setCurrentOp(Uploader.READY_TO_UPLOAD);
                        }
                        else
                        {
                            setCurrentOp(Uploader.FAILURE);
                        }
                    }
                }

            };
            if (recordSets != null)
            {
                recordSets.clear();
                recordSets = null;
            }
            UIRegistry.getStatusBar()
                    .setText(
                            getResourceString((isUserCmd ? Uploader.UNDOING_UPLOAD
                                    : Uploader.CLEANING_UP)));
            undoTask.start();
            setCurrentOp(isUserCmd ? Uploader.UNDOING_UPLOAD : Uploader.CLEANING_UP);
        }
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

    protected abstract class UploaderTask extends SwingWorker
    {
        protected final JStatusBar statusBar = UIRegistry.getStatusBar();
        protected boolean cancelled = false;
        protected boolean done = false;
        protected String cancelMsg = null;
        protected boolean cancellable = false;
        protected long startTime;
        protected long endTime;
        
        public UploaderTask(boolean cancellable, String cancelMsg)
        {
            super();
            this.cancellable = cancellable;
            this.cancelMsg = cancelMsg;
            currentTask = this;
        }
        
        @Override
        public void start()
        {
            startTime = System.nanoTime();
            super.start();
        }
        
        @Override
        public void interrupt()
        {
            super.interrupt();
            cancelled = true;
        }
        
        @Override
        public void finished()
        {
            super.finished();
            currentTask = null;
            done = true;
            endTime = System.nanoTime();
            log.debug("UploaderTask time elapsed: " + Long.toString((endTime-startTime)/1000000000L));
        }
        
        public synchronized boolean isDone()
        {
            return done;
        }
        
        public synchronized boolean isCancellable()
        {
            return cancellable;
        }
        
        public synchronized void setCancellable(boolean val)
        {
            cancellable = val;
        }
        
        public synchronized String getCancelMsg()
        {
            return cancelMsg;
        }
        
        public synchronized void setCancelMsg(final String msg)
        {
            cancelMsg = msg;
        }
    }
    /**
     * Builds viewer for uploaded data.
     */
    public void retrieveUploadedData()
    {
        bogusStorages = new HashMap<String, Vector<Vector<String>>>();
        setOpKiller(null);

        final UploaderTask retrieverTask = new UploaderTask(true, "WB_CANCEL_UPLOAD_MSG")
        {
            @Override
            public Object construct()
            {
                try
                {
//                    initProgressBar(0, uploadTables.size(), true, 
//                            getResourceString("WB_UPLOAD_RETRIEVING") + " " + getResourceString("ERD_TABLE"), false);
//                    for (int progress = 0; progress < uploadTables.size();)
//                    {
//                        if (cancelled)
//                        {
//                            break;
//                        }
//                        UploadTable ut = uploadTables.get(progress);
//                        setCurrentOpProgress(progress + 1, false);
//                        try
//                        {
//                            Vector<Vector<String>> vals = ut.printUpload();
//                            if (vals.size() > 0)
//                            {
//                                String title = ut.getWriteTable().getName();
//                                if (!bogusStorages.containsKey(title))
//                                {
//                                    bogusStorages.put(title, vals);
//                                }
//                                else
//                                {
//                                    // delete header
//                                    vals.remove(0);
//                                    bogusStorages.get(title).addAll(vals);
//                                }
//                            }
//                        }
//                        catch (InvocationTargetException ex)
//                        {
//                            log.error(ex);
//                        }
//                        catch (IllegalAccessException ex)
//                        {
//                            log.error(ex);
//                        }
//                        progress++;
//                    }
                    return true;
                }
                catch (Exception ex)
                {
                    setOpKiller(ex);
                    return false;
                }
            }

            @Override
            public void finished()
            {
                super.finished();
                statusBar.setText("");
                setCurrentOp(Uploader.SUCCESS);
                if (!cancelled)
                {
                    viewSelectedTable();
                    mainPanel.addMsg(new BaseUploadMessage(getResourceString("WB_UPLOAD_DATA_FETCHED")));
                }
                else
                {
                    bogusStorages = null;
                    mainPanel.addMsg(new BaseUploadMessage(getResourceString("RetrievalWB_UPLOAD_FETCH_CANCELLED cancelled")));
                }
            }

        };
        
        if (mainPanel == null)
        {
            initUI(Uploader.RETRIEVING_UPLOADED_DATA);
        }
        else
        {
            setCurrentOp(Uploader.RETRIEVING_UPLOADED_DATA);
        }
        UIRegistry.getStatusBar().setText(getResourceString(Uploader.RETRIEVING_UPLOADED_DATA));
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
            writeRow(t, row);
        }
        catch (UploaderException ex)
        {
            log.debug(ex.getMessage() + " (" + t.getTable().getName() + ", row "
                    + Integer.toString(row) + ")");
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
    protected void writeRow(final UploadTable t, int row) throws UploaderException
    {
        t.writeRow(row);
    }

    /**
     * Creates a recordset for each UploadTable containing the keys of all objects
     * uploaded.
     */
    protected void createRecordSets()
    {
        if (recordSets != null)
        {
            recordSets.clear();
        }
        else
        {
            recordSets = new Vector<RecordSet>(uploadTables.size());
        }

        for (UploadTable ut : uploadTables)
        {
            RecordSet rs = ut.getRecordSet();
            if (rs.getNumItems() > 0)
            {
                recordSets.add(rs);
            }
        }
        //combine recordSets with identical names...
        Collections.sort(recordSets, new Comparator<RecordSet>()
                {
                    public int compare(RecordSet rs1, RecordSet rs2)
                    {
                        return rs1.getName().compareTo(rs2.getName());
                    }
                });
        for (int rs=recordSets.size()-1; rs>0; rs--)
        {
            if (recordSets.get(rs).getName().equals(recordSets.get(rs-1).getName()))
            {
                recordSets.get(rs-1).addAll(recordSets.get(rs).getItems());
                for (RecordSetItemIFace rsi : recordSets.get(rs).getItems())
                {
                    recordSets.get(rs-1).addItem(rsi);
                }
                recordSets.remove(rs);
            }
        }
        //...end combine recordSets.
    }


    
    /**
     * Saves recordSets to the database.
     */
    protected void saveRecordSets()
    {
        if (recordSets == null || recordSets.size() == 0)
        {
            createRecordSets();
        }

        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            for (RecordSet rs : recordSets)
            {
                session.beginTransaction();
                try
                {
                    session.save(rs);
                    session.commit();
                }
                catch (Exception ex)
                {
                    session.rollback();
                    throw new RuntimeException(ex);
                }
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            session.close();
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    //@Override
    public void keyPressed(KeyEvent e)
    {
        //see note in goToMsgWBCell() re addKeyListener()
        //nuthin
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    //@Override
    public void keyReleased(KeyEvent e)
    {
        //see note in goToMsgWBCell() re addKeyListener()
        // nuthin
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    //@Override
    public void keyTyped(KeyEvent e)
    {
        //see note in goToMsgWBCell() re addKeyListener()
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ENTER || 
                key == KeyEvent.VK_TAB || 
                key == KeyEvent.VK_DOWN ||
                key == KeyEvent.VK_UP || (key == KeyEvent.VK_TAB && e.isShiftDown()) ||
                key == KeyEvent.VK_HOME ||
                key == KeyEvent.VK_END)
        {
            editInvalidCell(e);
            e.consume();
        }
    }

    /**
     * @param e
     * 
     * Moves to WB cell for appropriate InvalidValue and starts editing it.
     */
    protected void editInvalidCell(KeyEvent e)
    {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_TAB || key == KeyEvent.VK_DOWN)
            goToNextInvalidCell(e.getComponent());
        else if (key == KeyEvent.VK_UP || (key == KeyEvent.VK_TAB && e.isShiftDown()))
            goToPrevInvalidCell(e.getComponent());
        else if (key == KeyEvent.VK_HOME)
            goToFirstInvalidCell(e.getComponent());
        else if (key == KeyEvent.VK_END)
            goToLastInvalidCell(e.getComponent());
    }

}
