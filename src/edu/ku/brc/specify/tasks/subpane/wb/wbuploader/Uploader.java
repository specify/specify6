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
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ServiceInfo;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.BusinessRulesIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.helpers.ImageMetaDataHelper;
import edu.ku.brc.specify.SpecifyUserTypes;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.AttachmentOwnerIFace;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.FieldNotebook;
import edu.ku.brc.specify.datamodel.FieldNotebookPage;
import edu.ku.brc.specify.datamodel.FieldNotebookPageSet;
import edu.ku.brc.specify.datamodel.GeoCoordDetail;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.LocalityDetail;
import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchRowExportedRelationship;
import edu.ku.brc.specify.datamodel.WorkbenchRowImage;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr.SCOPE;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr.USER_ACTION;
import edu.ku.brc.specify.tasks.DataEntryTask;
import edu.ku.brc.specify.tasks.InteractionsTask;
import edu.ku.brc.specify.tasks.RecordSetTask;
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
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 * 
 */
public class Uploader implements ActionListener, KeyListener
{
    private static boolean                       debugging                    = false;

    // Phases in the upload process...
    protected final static String                INITIAL_STATE                = "WB_UPLOAD_INITIAL_STATE";
    protected final static String                CHECKING_REQS                = "WB_UPLOAD_CHECKING_REQS";
    protected final static String                VALIDATING_DATA              = "WB_UPLOAD_VALIDATING_DATA";
    public final static String                	 READY_TO_UPLOAD              = "WB_UPLOAD_READY_TO_UPLOAD";
    protected final static String                UPLOADING                    = "WB_UPLOAD_UPLOADING";
    public final static String                	 SUCCESS                      = "WB_UPLOAD_SUCCESS";
    protected final static String                SUCCESS_PARTIAL              = "WB_UPLOAD_SUCCESS_PARTIAL";
    protected final static String                RETRIEVING_UPLOADED_DATA     = "WB_RETRIEVING_UPLOADED_DATA";
    protected final static String                FAILURE                      = "WB_UPLOAD_FAILURE";
    protected final static String                USER_INPUT                   = "WB_UPLOAD_USER_INPUT";
    protected final static String                UNDOING_UPLOAD               = "WB_UPLOAD_UNDO";
    protected final static String                CLEANING_UP                  = "WB_UPLOAD_CLEANUP";
    
    /**
     * Resource string Ids
     */
    protected final static String                   WB_CELL_LENGTH_EXCEPTION = "WB_CELL_LENGTH_EXCEPTION";
    protected final static String                   WB_TOO_MANY_ERRORS = "WB_TOO_MANY_ERRORS";
    protected final static String                   WB_UPLOAD_FORM_TITLE = "WB_UPLOAD_FORM_TITLE";
    protected final static String                   WB_UPLOAD_ROW_SKIPPED = "WB_UPLOAD_ROW_SKIPPED";
    protected final static String                   WB_UPLOAD_VIEW_RESULTS_TITLE = "WB_UPLOAD_VIEW_RESULTS_TITLE";
    
    //Upload lock check results
    public final static int                     NO_LOCK      = 0;
    public final static int                     LOCK_REMOVED = 1;
    public final static int                     LOCK_IGNORED = 2;
    public final static int                     LOCKED       = 3;
    public final static int						LOCK_FAILED  = 4;
    
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
    //Map<String, Vector<Vector<String>>>             bogusStorages            = null;
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
    
    protected java.util.Collection<WorkbenchTemplateMappingItem> workbenchTemplateMappingItems = null;

    protected Vector<UploadField>                   uploadFields;

    protected Vector<UploadTable>                   uploadTables;

    protected DirectedGraph<Table, Relationship>    uploadGraph;

    boolean                                         verbose                  = false;

    boolean                                         dataValidated            = false;

    protected UploadMainPanel                       mainPanel;
    
    /**
     * While editing invalid cells, this is added as a listener to the WorkbenchPaneSS's spreadsheet cell editor component.
     * The cell editor component is currently always the same object.
     */
    protected List<Component>                       keyListeningTo           = new LinkedList<Component>();

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
     * A unique identifier currently used to identify the upload. NOTE: Would it
     * be desirable to store info on imports - dataset imported, date, user, basic stats ???
     * 
     */
    protected String                                identifier = "uploader";
    
    /**
     * The time of the upload. Used to create the identifier.
     */
    protected Calendar                              uploadTime = null;
    
    /**
     * The currently executing upload task.
     */
    protected UploaderTask                          currentTask = null;

    /**
     * the index of the currently processing row in the dataset.
     */
    protected int rowUploading;

    /**
     * the index of the row to start uploading at 
     */
    protected int uploadStartRow;
    
    protected AtomicInteger updateTableId = new AtomicInteger(-1);
    
    /**
     * The workbenchrowimages for the current workbench row
     */
    protected List<WorkbenchRowImage> 				imagesForRow = new Vector<WorkbenchRowImage>();
    
    /**
     * Attachments created during the upload
     */
    protected List<UploadedRecordInfo>              newAttachments = new Vector<UploadedRecordInfo>();
    
    protected UploadRetriever						uploadedObjectViewer = null;
    
    protected boolean 	                            additionalLocksSet = false;
    protected static final Logger                   log                      = Logger.getLogger(Uploader.class);
   
      
    private class SkippedAttachment extends BaseUploadMessage
    {
    	protected int row;
    	
    	public SkippedAttachment(String msg, int row)
    	{
    		super(msg);
    		this.row = row;
    	}

		/* (non-Javadoc)
		 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.BaseUploadMessage#getRow()
		 */
		@Override
		public int getRow()
		{
			return row;
		}
    	
    	
    }
    /**
     * @author timbo
     *
     * @code_status Alpha
     *
     * Stores information about rows that were not uploaded during an upload.
     */
    @SuppressWarnings("unused")
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
            return String.format(getResourceString(WB_UPLOAD_ROW_SKIPPED), String.valueOf(getRow()+1)) + ": " + cause.getMessage();
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
     * @param services
     * @return list of services with services modified or deleted according to
     * requirements of upload status.
     */
    public List<ServiceInfo> filterServices(final List<ServiceInfo> services)
    {
        List<ServiceInfo> result =  new Vector<ServiceInfo>();
        for (int s = 0; s < services.size(); s++)
        {
            ServiceInfo service = services.get(s);
            if (includeService(service))
            {
                if (service.getTask() instanceof DataEntryTask || service.getTask() instanceof InteractionsTask)
                {
                    try
                    {
                        ServiceInfo newService = (ServiceInfo )service.clone();
                        newService.getCommandAction().setProperty("readonly", true);
                        service = newService;
                    }
                    catch (CloneNotSupportedException ex)
                    {
                        log.error(ex);
                        continue;
                    }
                }
                result.add(service);
            }
        }
        return result;
    }
    
    protected boolean includeService(final ServiceInfo service)
    {
        return !(service.getTask() instanceof RecordSetTask);
    }
    

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
     * @param name
     * @return UploadTable named name.
     */
    public UploadTable getUploadTableByName(final String name)
    {
    	for (UploadTable result : uploadTables)
    	{
    		if (result.getTable().getName().equalsIgnoreCase(name))
    		{
    			return result;
    		}
    	}
    	return null;
    }
    
    /**
     * @param name
     * @return UploadTable named name.
     */
    public UploadTable getUploadTableByNameRel(final String name, final String relTblName)
    {
    	for (UploadTable result : uploadTables)
    	{
    		if (result.getTable().getName().equalsIgnoreCase(name))
    		{
    			if (relTblName == null)
    			{
    				return result;
    			}
    			Relationship rel = result.getRelationship();
    			if (rel != null)
    			{
    				if (rel.getRelatedField().getTable().getTableInfo().getName().equalsIgnoreCase(relTblName))
    				{
    					return result;
    				}
    			}
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
            logDebug(f.getWbFldName());
            if (f.getField() != null)
            {
                UploadTable it = getUploadTable(f);
                boolean addIt = it == null;
                if (addIt)
                {
                    it = new UploadTable(this, f.getField().getTable(), f.getRelationship());
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
        for (UploadTable ut : uploadTables)
        {
            ut.findPrecisionDateFields();
        }
    }

    protected void logDebug(Object toLog)
    {
        if (debugging)
        {
            log.debug(toLog);
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
                logDebug("could not find field in db: " + mapping.getTable() + "."
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
                logDebug("could not find field in db: " + t1.getName() + "." + fld.getFieldName());
            }
            UploadField newFld = new UploadField(dbFld, fld.getFldIndex(), fld.getWbFldName(), null);
            newFld.setSequence(mapping.getSequence());
            uploadFields.add(newFld);
        }
        if (mapping.getRelatedFields().size() > 0)
        {
            //Relationship r = null;
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
            //Actually it seems the modifiedby and createdby 'system' relationships get filtered out during graph creation,
            //so the filtering isn't necessarily necessary.
            for (int r = rs.size() - 1; r > -1; r--)
            {
                if (rs.get(r).getRelatedField().getName().equalsIgnoreCase("modifiedbyagentid")
                            || rs.get(r).getRelatedField().getName().equalsIgnoreCase("createdbyagentid"))
                {
                        rs.remove(r);
                }
            }

//            for (Relationship rel : rs)
//            {
//            	if (!rel.getRelatedField().getName().equalsIgnoreCase("modifiedbyagentid")
//                        && !rel.getRelatedField().getName().equalsIgnoreCase("createdbyagentid"))
//                {
//                    r = rel;
//                    break;
//                }
//            }
            if (rs.size() > 0)
            {
            	for (Relationship r : rs)
            	{
            		if (r.getRelatedField().getName().equalsIgnoreCase(mapping.getField()))
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
        				return;
            		}
            	}
            }
            throw new UploaderException("could not find relationship for mapping.",
                 UploaderException.ABORT_IMPORT);
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
                    logDebug("could not find field in db: " + m.getTable() + "." + m.getField());
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

    public Uploader(DB db, UploadData importData, final WorkbenchPaneSS wbSS, boolean isValidator) throws UploaderException
    {
    	this(db, importData, wbSS, wbSS.getWorkbench().getWorkbenchTemplate().getWorkbenchTemplateMappingItems(), isValidator);
    }

    /**
     * @param db
     * @param uploadData
     * @throws UploaderException
     */
    public Uploader(DB db, UploadData importData, final WorkbenchPaneSS wbSS, final java.util.Collection<WorkbenchTemplateMappingItem> wbItems,
    		boolean isValidator)
            throws UploaderException
    {
        this.db = db;
        this.uploadData = importData;
        this.wbSS = wbSS;
        this.workbenchTemplateMappingItems = wbItems;
        this.uploadFields = new Vector<UploadField>(importData.getCols());
        this.missingRequiredClasses = new Vector<RelatedClassSetter>();
        this.missingRequiredFields = new Vector<DefaultFieldEntry>();
        this.skippedRows = new Vector<SkippedRow>();
        this.messages = new Vector<UploadMessage>();
        this.newMessages = new Vector<UploadMessage>();
        buildUploadFields();
        buildUploadTables();
        addEmptyUploadTables();
        addRequiredUploadTables(); 
        buildUploadGraph();
        processTreeMaps();
        for (UploadTable ut : uploadTables)
        {
            logDebug("assigningFldSetters: " + ut.getTable().getName());
            ut.assignFldSetters();
        }
        orderUploadTables();
        buildUploadTableParents();
        reOrderUploadTables();
        if (!isValidator)
        {	
        	currentUpload = this;
        }
//        for (UploadTable ut : uploadTables){
//        	System.out.print(ut + " " + ut.isMatchChild() + " ");
//        	if (ut.getParentTables() != null && ut.getParentTables().size() > 0)
//        	{
//        		System.out.println(ut.getParentTables().get(0).size());
//        	} else
//        	{
//        		System.out.println("0");
//        	}
//        }
//        System.out.println("end of upload tables");
//        System.out.println(getRootTable());
    }

    /**
     * @param ut child whose parents will be traced
     * @param rps current list of parents
     */
    protected void processParentsForRootTableSearch(UploadTable ut, Set<UploadTable> rps)
    {
		for (Vector<ParentTableEntry> ptes : ut.getParentTables())
		{
			for (ParentTableEntry pte : ptes)
			{
				UploadTable p = pte.getImportTable();
				if (!p.getSpecialChildren().contains(ut))
				{
					rps.add(p);
					processParentsForRootTableSearch(p, rps);
				}
			}
		}
    	
    }
    
    /**
     * @return the 'root' or 'main' table for a dataset
     * 
     * This will be the table that has no one-to-many or one-to-one child tables or 'owns' all its child tables. 
     */
    protected UploadTable getRootTable()
    {
    	//First, construct all tables that are 'owned' by other tables and their non-'owner' parents.
    	HashSet<UploadTable> ruledChildrenAndTheirReqs = new HashSet<UploadTable>();
    	for (UploadTable ut : uploadTables)
    	{
    		if (ut.isMatchChild())
    		{
    			ruledChildrenAndTheirReqs.add(ut);
    			processParentsForRootTableSearch(ut, ruledChildrenAndTheirReqs);
    		}
    	}
    	//Then move backwards through the ALREADY ordered upload tables until we get to a table 
    	//that is not in the list constructed above 
    	for (int t = uploadTables.size() - 1; t >= 0; t--)
    	{
    		if (!uploadTables.get(t).isMatchChild() && !ruledChildrenAndTheirReqs.contains(uploadTables.get(t)))
    		{
    			return uploadTables.get(t);
    		}
    	}
    	return null;
    }
    
    /**
     * @param mapI
     * @return true if mapI maps a taxonomic level for determination
     * 
     */
    protected boolean isDetTaxLevelMapping(WorkbenchTemplateMappingItem mapI)
    {
        //XXX this is pretty dumb. Probably would be better to add original table
    	//info when UploadMappingDefTree objects are created.
    	//This code will have to be changed if/when tree levels are created on-the-fly
    	//from TreeDefinitions.
    	if (!mapI.getTableName().equalsIgnoreCase("determination"))
        {
        	return false;
        }
    	String fldName = mapI.getFieldName();
        return fldName.startsWith("kingdom")
        	|| fldName.startsWith("phylum") 
        	|| fldName.startsWith("subphylum") 
        	|| fldName.startsWith("superclass") 
        	|| fldName.startsWith("class") 
        	|| fldName.startsWith("subclass") 
        	|| fldName.startsWith("infraclass") 
        	|| fldName.startsWith("superorder") 
        	|| fldName.startsWith("order") 
        	|| fldName.startsWith("suborder") 
        	|| fldName.startsWith("infraorder") 
        	|| fldName.startsWith("superfamily") 
        	|| fldName.startsWith("family") 
        	|| fldName.startsWith("subfamily") 
        	|| fldName.startsWith("tribe") 
        	|| fldName.startsWith("subtribe") 
        	|| fldName.startsWith("genus") 
        	|| fldName.startsWith("subgenus") 
        	|| fldName.startsWith("species")
            || fldName.startsWith("subspecies")
            || fldName.startsWith("variety") 
        	|| fldName.startsWith("forma"); 
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
            for (WorkbenchTemplateMappingItem mapI : workbenchTemplateMappingItems)
            {
                if (isDetTaxLevelMapping(mapI))
                {
                    genSpPresent = true;
                    try
                    {
                        String fldName = mapI.getFieldName();
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
                UploadTable det = new UploadTable(this, db.getSchema().getTable("Determination"), null);
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
            UploadTable ce = new UploadTable(this, db.getSchema().getTable("CollectingEvent"), null);
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
                        logDebug("isAncestorOf(" + t1 + ", " + t2 + ")");
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
                        if (p1.getFirst() == p2.getFirst() && p1.getSecond() == p2.getSecond())
                        {
                            return 0;
                        }
                        if (isAncestorOf(p1.getSecond(), p2.getSecond())) 
                        { 
                            return -1; 
                        }
                        if (isAncestorOf(p2.getSecond(), p1.getSecond())) 
                        { 
                            return 1; 
                        }
                        return 0;
                    }
                });
        if (debugging)
        {
            logDebug("reOrderUploadTables(): initial order:");
            for (UploadTable ut : uploadTables)
            {
                logDebug("   " + ut.getTable().getName());
            }
        }
        for (UploadTable ut : uploadTables)
        {            
            logDebug("Table: " + ut.getTable().getName());
            for (UploadTable mc : ut.getSpecialChildren())
            {
                if (ut.needToMatchChild(mc.getTblClass()))
                {
                	logDebug("  Child: " + mc.getTable().getName());
                	for (ParentTableEntry pte : mc.getAncestors())
                	{
                		if (uploadTables.indexOf(ut) < uploadTables.indexOf(pte.getImportTable()))
                		{
                			logDebug("reordering: " + pte.getImportTable().getTable().getName() + " must precede " + ut.getTable().getName());
                			moves.add(new Pair<UploadTable, UploadTable>(ut, pte.getImportTable()));
                		}
                	}
                }
            }
        }
        for (Pair<UploadTable, UploadTable> move : moves)
        {
            int fromIdx = uploadTables.indexOf(move.getSecond());
            int toIdx = uploadTables.indexOf(move.getFirst());
            this.logDebug("reording: " + move.getSecond().getTable().getName() + "(" + fromIdx + ") -> " + move.getFirst().getTable().getName() + "(" + toIdx + ")");
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
            logDebug(e);
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
                + Integer.toString(treeMap.getLevels().get(level).getRank());
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
            logDebug(treeMap.getLevels().get(level).getWbFldName());
            // add new table to import graph for rank
            Table rankTbl = new Table(getTreeTableName(treeMap, level), baseTbl);
            try
            {
                uploadGraph.addVertex(new Vertex<Table>(rankTbl.getName(), rankTbl));
                if (parentTbl != null)
                {
                    Relationship rankRel = new Relationship(parentTbl.getKey(), rankTbl
                            .getField(treeMap.getParentField()), Relationship.REL_ONETOMANY);
                    uploadGraph.addEdge(parentTbl.getName(), rankTbl.getName(), rankRel);
                }
            }
            catch (DirectedGraphException ex)
            {
                throw new UploaderException(ex);
            }
            parentTbl = rankTbl;

            // create UploadTable for new table

            UploadTableTree it = new UploadTableTree(this, rankTbl, baseTbl, parentImpTbl, treeMap
                    .getLevels().get(level).isRequired(), treeMap.getLevels().get(level)
                    .getRank(), treeMap.getLevels().get(level).getWbFldName());
            it.init();

            // add ImportFields for new table
            //for (int seq = 0; seq < treeMap.getLevels().get(level).size(); seq++)
            int prevSeq = -1;
            //for (TreeMapElement item : treeMap.getLevels().get(level))
            for (int i = 0; i < treeMap.getLevels().get(level).size(); i++) 
            {
                TreeMapElement item = treeMap.getLevels().get(level).getElement(i);
                int seq = item.getSequence();
                Field fld = rankTbl.getField(item.getFldName());
                int fldIdx = item.getIndex();
                String wbFldName = item.getWbFldName();
                UploadField newFld1 = new UploadField(fld, fldIdx, wbFldName, null);
                if (item.isRequired())
                {
                    newFld1.setRequired(true);
                }
                newFld1.setSequence(seq);
                uploadFields.add(newFld1);
                
                UploadField newFld2 = null;
                if (prevSeq != seq)
                {
                    newFld2 = new UploadField(rankTbl.getField("rankId"), -1, null, null);
                    newFld2.setRequired(true);
                    newFld2.setValue(Integer.toString(item.getRank()));
                    newFld2.setSequence(seq);
                    uploadFields.add(newFld2);
                }

                it.addField(newFld1);
                if (newFld2 != null)
                {
                    it.addField(newFld2);
                }
                
                prevSeq = seq;
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
                        if (isRelationshipImplemented(rel, relTblVertex.getData()))
                        {
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
    /**
     * @author timo
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
         * the methods used to set and get objects of importTable's class to children.
         */
        protected Method       setter;
        protected Method       getter;

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

        /**
         * @param getter
         */
        public final void setGetter(Method getter)
        {
        	this.getter = getter;
        	
        }
        
        /**
         * @return the getter
         */
        public final Method getGetter()
        {
        	return this.getter;
        	
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
     * @param r
     * @return true is r is working
     */
    protected boolean isRelationshipImplemented(Relationship r, Table t)
    {
    	boolean result = true;
    	if (t.getName().equalsIgnoreCase("collectingeventattribute"))
    	{
    		if (r.getRelatedField().getName().equalsIgnoreCase("HostTaxonID"))
    		{
    			result = false;
    		}
    	}
    	else if (t.getName().equalsIgnoreCase("paleocontext"))
    	{
    		if (r.getRelatedField().getName().equalsIgnoreCase("ChronosStratEndID"))
    		{
    			result = false;
    		}
    		else if (r.getRelatedField().getName().equalsIgnoreCase("BioStratID"))
    		{
    			result = false;
    		}
    		
    	} else if (t.getName().equalsIgnoreCase("determination"))
    	{
    		if (r.getRelatedField().getName().equalsIgnoreCase("PreferredTaxonID"))
    		{
    			result = false;
    		}
    	}
    	if (!result)
    	{
			log.debug("Ignoring relationship: " + r.getField().getName() + ":" + r.getRelatedField().getName());
    	}
    	return result;
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
                        if (isRelationshipImplemented(r, tv.getData()))
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

    /**
     * Currently this just adds CollectingEvent if it is not present if the current collection
     * has embedded CollectingEvents
     */
    protected void addRequiredUploadTables() throws UploaderException
    {
    	if (AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent())
    	{
    		boolean hasCO = false;
    		boolean hasCE = false;
    		for (UploadTable t : uploadTables)
    		{
    			if (t.getTblClass().equals(CollectionObject.class))
    			{
    				hasCO = true;
    			} else if (t.getTblClass().equals(CollectingEvent.class))
    			{
    				hasCE = true;
    			}
    		}
    		if (!hasCE && hasCO)
    		{
    	        UploadTable ce = new UploadTable(this, db.getSchema().getTable("CollectingEvent"), null);
    	        ce.init();
    	        ce.addField(new UploadField(db.getSchema().getField("collectingevent",
    	               "stationfieldnumber"), -1, null, null));
    			uploadTables.add(ce);
    		}
    	}
    }
    	
    /**
     * @param uploadTable
     * @param rowToValidate
     * @param colToValidate
     * @return
     */
    protected Vector<UploadTableInvalidValue> validateLengths(final UploadTable uploadTable, 
    		final int rowToValidate, final int colToValidate)
    {
        Vector<UploadTableInvalidValue> result = new Vector<UploadTableInvalidValue>();
        int startRow = rowToValidate != -1 ? rowToValidate : 0;
        int endRow = rowToValidate != -1 ? rowToValidate + 1 : wbSS.getSpreadSheet().getRowCount();
        for (Vector<UploadField> ufs : uploadTable.getUploadFields())
        {
            for (UploadField uf : ufs)
            {
                if (uf.getIndex() != -1)
                {
                    for (int r = startRow; r < endRow; r++)
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
     * @param row
     * @param col
     * @return list of invalid values
     */
    public Vector<UploadTableInvalidValue> validateData(int row, int col)
    {
        Vector<UploadTableInvalidValue> result = new Vector<UploadTableInvalidValue>();
    	//XXX figure out which table is associated with col.
        for (UploadTable tbl : uploadTables)
        {
            result.addAll(validateLengths(tbl, row, col));
            tbl.validateRowValues(row, uploadData, result);
        }
        //XXX not the right place!!!!!!!!
//        try 
//        {
//        	checkChangedData(row);
//        } catch (Exception ex)
//        {
//        	ex.printStackTrace();
//        }
    	return result;
    }
    
    public Vector<UploadField> checkChangedData(int row) throws Exception
    {
    	
    	getRootTable().loadExportedRecord(/*row, */wbSS.getWorkbench().getRow(row).getRecordId());
    	
    	Vector<UploadField> result = new Vector<UploadField>();
    	for (UploadTable ut : uploadTables)
    	{
    		result.addAll(ut.getChangedFields(row));
    	}
    	if (result.size() > 0)
    	{
    		//for (UploadField fld : result)
    		//{
    			//System.out.println(fld.getWbFldName());
    		//}
    	}
    	return result;
    }
    
    /**
     * @param ut
     * @param recID primary key value for record in ut
     * @return true if other records than the current exported rec for child tables use 
     * the specified ut record.
     */
    public boolean recIsShared(final UploadTable ut, final Integer recID)
    {
    	int utIndex = -1;
    	boolean result = false;
    	for (int t = 0; t < uploadTables.size(); t++)
    	{
    		if (ut == uploadTables.get(t))
    		{
    			utIndex = t;
    			break;
    		}
    	}
    	if (utIndex != -1)
    	{
    		for (int t = utIndex+1; t < uploadTables.size(); t++)
    		{
    			UploadTable tbl = uploadTables.get(t);
    			ParentTableEntry pte = tbl.getParentTableEntry(ut);
    			if (pte != null)
    			{
    				String sql = "select count(*) from " + tbl.getTblClass().getSimpleName().toLowerCase() 
    					+ " where " + pte.getParentRel().getRelatedField() + " = " 
    					+ ut.getExportedRecord().getId() + " and " 
    					+ tbl.getTable().getTableInfo().getPrimaryKeyName() + " != "
    					+ tbl.getExportedRecord().getId();
    				Integer cnt = BasicSQLUtils.getCount(sql);
    				if (cnt != null && cnt > 0)
    				{
    					return true;
    				} else if (recIsShared(tbl, tbl.getExportedRecord().getId()))
    				{
    					return true;
    				}
    			}
    		}
    	}
    	
    	return result;
    }
    
    /**
     * @param col
     * @return
     */
    protected Vector<Pair<Integer, UploadTable>> getUploadTablesForColForMatch(int col, List<UploadTableInvalidValue> invalidCols)
    {
    	Vector<Pair<Integer, UploadTable>> result = new Vector<Pair<Integer, UploadTable>>();
    	Set<Integer> badCols = new HashSet<Integer>();
    	for (UploadTableInvalidValue utiv : invalidCols)
    	{
    		badCols.add(utiv.getCol());
    	}
    	if (col == -1)
    	{
    		for (UploadTable ut : uploadTables)
    		{
				for (int seq = 0; seq < ut.getUploadFields().size(); seq++)
				{
					if (ut.isMatchable(badCols, seq))
					{
						//only need to add lowest rank for trees
						if (!(ut instanceof UploadTableTree) || 
    						((UploadTableTree )ut).getChild() == null)
						{
    						result.add(new Pair<Integer, UploadTable>(seq, ut));
    					}
    				}
    			}
    		}
    		return result;
    	} else
    	{
    		for (UploadTable ut: uploadTables)
    		{
    			int seq = 0;
    			for (Vector<UploadField> flds : ut.getUploadFields())
    			{
    				if (ut.isMatchable(badCols, seq))
    				{
    					for (UploadField fld : flds)
    					{
    						if (fld.getIndex() == col)
    						{
    							result.add(new Pair<Integer, UploadTable>(seq, ut));
    							return result;
    						}
    					}
    				}
    				seq++;
    			}
    		}
    		return result;
    	}
    }
    
    /**
     * @param row
     * @param col -1 -> match all cols
     * @return
     */
    public List<UploadTableMatchInfo> matchData(int row, int col, List<UploadTableInvalidValue> invalidCols) throws UploaderException,
		InvocationTargetException, IllegalAccessException, ParseException,
		NoSuchMethodException, InstantiationException, SQLException
    {
    	//Whereas, a match or non-match in any column X depends on matches or non-matches for columns whose tables are parents to X's table, and
    	//whereas, a match or non-match in X can change the match status for columns whose tables are children of X's tables, and
    	//whereas, a match or non-match in X can even change the match status for columns whose tables are parents of the children of X's tables
    	//for example a change to species can affect determination which can effect collection object,
    	//therefore it is best simply not to try to do matching on a col by col basis.
    	int matchCol = -1;  //match entire row
    		
    	Set<Integer> invalidColNums = new HashSet<Integer>();
    	for (UploadTableInvalidValue iv : invalidCols)
    	{
    		invalidColNums.add(iv.getCol());
    	}
    	
    	if (matchCol == -1)
    	{
        	List<UploadTableMatchInfo> result = getRootTable().getMatchInfo(row, 0, invalidColNums);
        	return result;
    	}
    	
    	return null;
    	
//    	Vector<UploadTableMatchInfo> result = new Vector<UploadTableMatchInfo>();
//    	for (Pair<Integer, UploadTable> utSeq : getUploadTablesForColForMatch(matchCol, invalidCols))
//    	{
//    		result.add(utSeq.getSecond().getMatchInfo(row, utSeq.getFirst()));
//    	}
//    	return result;
    }
    
    /**
     * @param col
     * @return
     */
    public DBFieldInfo getFieldInfoForCol(int col)
    {
    	for (UploadTable ut : uploadTables)
    	{
    		for (Vector<UploadField> ufs : ut.getUploadFields())
    		{
    			for (UploadField uf : ufs)
    			{
    				if (uf.getIndex() == col && uf.getField() != null)
    				{
    					return uf.getField().getFieldInfo();
    				}
    			}
    		}
    	}
    	return null;
    }
    
    /**
     * Sets default match status display settings.
     * 
     */
    //XXX Need to base the 'checkMatchInfo' settings on user-prefs.
    public void setDefaultMatchStatus()
    {
    	for (UploadTable t : uploadTables)
    	{
    		if (t instanceof UploadTableTree)
    		{
    			t.setCheckMatchInfo(true);
    		} else if (Agent.class.isAssignableFrom(t.getTblClass()))
    		{
    			t.setCheckMatchInfo(true);
    		} else if (Locality.class.isAssignableFrom(t.getTblClass())
    				|| LocalityDetail.class.isAssignableFrom(t.getTblClass())
    				|| GeoCoordDetail.class.isAssignableFrom(t.getTblClass()))
    		{
    	        //XXX testing!
    			if (AppPreferences.getRemote().getBoolean("WB_HighlightNewLocRecs", true) || AppPreferences.getRemote().getBoolean("WB_HighlightNewCERecs", false))
    	        {
    	        	t.setCheckMatchInfo(true);
    	        }
    		} else if (CollectingEvent.class.isAssignableFrom(t.getTblClass())) 
    		{
    			if (AppPreferences.getRemote().getBoolean("WB_HighlightNewCERecs", false))
    	        {
    	        	t.setCheckMatchInfo(true);
    	        }
    			
    		} else
    		{
    			t.setCheckMatchInfo(false);
    		}
    	}
    }
    
//    public void getMatchPrefs()
//    {
//    	List<Class<?>>
//    }
    
    /**
     * Validates contents of all cells in dataset.
     */
    public boolean validateData(boolean doInBackground)
    {
        dataValidated = false;
        setOpKiller(null);

        final Vector<UploadTableInvalidValue> issues = new Vector<UploadTableInvalidValue>();

        final UploaderTask validateTask = new UploaderTask(true, "WB_UPLOAD_CANCEL_MSG")
        {
            @Override
            public Object doInBackground()
            {
                start();
            	try
                {
                    int progress = 0;
                    initProgressBar(0, uploadTables.size(), true, 
                            getResourceString("WB_UPLOAD_VALIDATING") + " " + getResourceString("ERD_TABLE"), false);
                    for (UploadTable tbl : uploadTables)
                    {
                        setCurrentOpProgress(++progress);
                        issues.addAll(validateLengths(tbl, -1, -1));
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
            public void done()
            {
                super.done();
                validationIssues = issues;
                statusBar.setText("");
                if (cancelled)
                {
                    setCurrentOp(Uploader.INITIAL_STATE);
                }
                else if (dataValidated && resolver.isResolved() && mainPanel != null)
                {
                    mainPanel.addMsg(new BaseUploadMessage(getResourceString("WB_DATASET_VALIDATED")));
                    setCurrentOp(Uploader.READY_TO_UPLOAD);
                }
                else if (mainPanel != null)
                {
                    mainPanel.addMsg(new BaseUploadMessage(getResourceString("WB_INVALID_DATASET")));
                    setCurrentOp(Uploader.USER_INPUT);
                }
            }

//			/* (non-Javadoc)
//			 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader.UploaderTask#cancelTask()
//			 */
//			@Override
//			public synchronized void cancelTask()
//			{
//				super.cancelTask();
//				interrupt();
//			}

            
        };
        
        SwingUtilities.invokeLater(new Runnable() {

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
		        UIRegistry.getStatusBar().setText(getResourceString(Uploader.VALIDATING_DATA));
			}
        });
        boolean result = true;
        validateTask.execute();
        if (!doInBackground)
        {
        	try
        	{
        		result = (Boolean )validateTask.get();
        	} catch (ExecutionException ex)
        	{
        		//hopefully it will be clear to caller that something went wrong?
        	} catch (InterruptedException ex)
        	{
        		//hopefully it will be clear to caller that something went wrong?
        	}
        	//validateTask.finished();
        	//validateTask.
        }
        return result;
    }

    protected synchronized void cancelTask(final UploaderTask task)
    {
        boolean tooLate = true;
        if (!task.isDone())
        {
            tooLate = false;
//            boolean undo = UIRegistry.displayConfirm(getResourceString("WB_UPLOAD_FORM_TITLE"),
//                getResourceString(task.getCancelMsg()), getResourceString("YES"),
//                getResourceString("NO"), JOptionPane.QUESTION_MESSAGE);
            if (!task.isDone())
            {
                task.setUndo(false);
                task.cancelTask();
            }
            else
            {
                tooLate = true;
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
     * @return the uploadData
     */
    public UploadData getUploadData()
    {
    	return uploadData;
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
     * @return true if the upload is updating existing records
     */
    public boolean isUpdateUpload()
    {
    	UploadTable root = getRootTable();
    	return wbSS.getWorkbench().getExportedFromTableName() != null && root != null && root.isUpdateMatches();
    }
    
    /**
     * @return true if the update upload can be performed.
     */
    protected boolean isValidUpdateUpload()
    {
    	//currently this just checks for 1-many tables, which is a temporary restriction, but
    	//it is possible that some tables just won't be updatable.
//    	for (UploadTable ut : uploadTables)
//    	{
//    		if (ut.getUploadFields().size() > 1)
//    		{
//    			return false;
//    		} else 
//    		{
//    			if (ut.getTblClass().equals(Determination.class)
//    					|| ut.getTblClass().equals(Collector.class)
//    					|| ut.getTblClass().equals(Preparation.class)
//    					|| ut.getTblClass().equals(CollectionObjectAttribute.class)
//    					|| ut.getTblClass().equals(CollectingEventAttribute.class)
//    					|| ut.getTblClass().equals(AccessionAuthorization.class)
//    					|| ut.getTblClass().equals(CollectionObjectCitation.class)
//    					|| ut.getTblClass().equals(LocalityCitation.class)
//    					|| ut.getTblClass().equals(DNASequence.class)
//    					)
//    			{
//    				return false;
//    			}		
//    		}
//    	}
    	return true;
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
                        int size1 = o1 == null ? 0 : o1.size();
                        int size2 = o2 == null ? 0 : o2.size();
                    	if (o1 != null && o2 != null && size1 == size2)
                        {
                            if (o1.containsAll(o2)) { return 0; }
                            // else
                            if (o1.size() == 0) return 0;
                            int id1 = o1.get(0).getTableInfo().getTableId();
                            int id2 = o2.get(0).getTableInfo().getTableId();
                            return id1 < id2 ? -1 : (id1 == id2 ? 0 : 1);
                        }
                        return size1 < size2 ? -1 : 1;
                        
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
     * @return true if images are attached to any row
     */
    protected boolean attachmentsPresent()
    {
    	for (int r = 0; r < this.uploadData.getRows(); r++)
    	{
    		Set<WorkbenchRowImage> images = uploadData.getWbRow(r).getWorkbenchRowImages();
    		if (images != null && images.size() > 0)
    		{
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * @return list of attachable table classes in order of precedence.
     */
    protected Vector<Class<?>> getAttachableTables()
    {
    	Vector<Class<?>> result = new Vector<Class<?>>(8);
    	result.add(FieldNotebook.class);
    	result.add(FieldNotebookPageSet.class);
    	result.add(FieldNotebookPage.class);
    	result.add(Taxon.class);
    	result.add(Accession.class);
    	result.add(Locality.class);
    	result.add(CollectingEvent.class);
    	result.add(CollectionObject.class);
    	return result;
    }
    
 
    /**
     * @return a list of uploadtables that can have attachments in the current upload.
     * 
     *  Not currently used, but will be when/if we allow users to choose which tables
     *  attachments apply to.
     */
    public List<UploadTable> getAttachableTablesInUse()
    {
    	List<UploadTable> result = new Vector<UploadTable>();
    	for (UploadTable ut : uploadTables)
    	{
    		if (AttachmentOwnerIFace.class.isAssignableFrom(ut.getTblClass()))
    		{
        		//System.out.println(ut.getTblTitle());
    			if (attachmentsSupported(ut))
    			{
    	    		//System.out.println("   attachable: " + ut.getTblTitle());
    				result.add(ut);
    			}
    		}
    	}
    	return result;
    }

    /**
     * @param tblClass
     * @return true if attachments can be uploaded for tblClass
     * 
     * Lots of UI work needs to be done before we can support attachments
     * for 1-many situations (collectors, determiners, preps, determinations)
     */
    protected boolean attachmentsSupported(UploadTable uploadTable)
    {
    	//Only allow attachments to agent for agent-only dataset
    	Class<?> tblClass = uploadTable.getTblClass();
    	if (tblClass.equals(Agent.class))
    	{
    		for (UploadTable ut : uploadTables)
    		{
    			if (!ut.getTblClass().equals(Agent.class) && !ut.getTblClass().equals(Address.class))
    			{
    				return false;
    			}
    		}
    		return true;
    	} 
    	
    	//Only allow attachments to taxon for taxon-only dataset
    	if (tblClass.equals(Taxon.class))
    	{
    		for (UploadTable ut : uploadTables)
    		{
    			if (!ut.getTblClass().equals(Taxon.class))
    			{
    				return false;
    			}
    		}
    		return true;
    	}
    	
    	
    	//Exclude tables that can have multiple records per row in a dataset 
    	for (List<ParentTableEntry> ptes : uploadTable.getParentTables())
    	{
    		for (ParentTableEntry pte : ptes)
    		{
    			if (pte.getImportTable().needToMatchChild(tblClass)  && uploadTable.getUploadFields().size() > 1)
    			{
    				return false;
    			}
    		}
    	}
    	
    	
    	return true;
    }
    /**
     * @return list of attachable table names.
     */
    protected String getAttachableStr()
    {
    	String result = "";
    	Set<Class<?>> clss = new HashSet<Class<?>>();
    	List<UploadTable> uts = getAttachableTablesInUse();
    	for (UploadTable ut : uts)
    	{
    		clss.add(ut.getTblClass());
    	}
    	for (Class<?> cls : clss)
    	{
    		DBTableInfo info = DBTableIdMgr.getInstance().getByClassName(cls.getName());
    		if (info != null)
    		{
    			if (!StringUtils.isBlank(result))
    			{
    				result += ", ";
    			}
    			result += info.getTitle();
    		}
    	}
    	return result;
    }
    
    /**
     * @return the table to attach images to.
     */
    public UploadTable getAttachToTable()
    {
    	int priority = -1;
    	Vector<Class<?>> attachable = getAttachableTables();
    	UploadTable result = null;
    	boolean isAgentOnly = true;
    	for (UploadTable ut : uploadTables)
    	{
    		if (!ut.getTblClass().equals(Agent.class) || ut.getTblClass().equals(Address.class))
    		{
    			isAgentOnly = false;
    		}
    		int newPriority = attachable.indexOf(ut.getTblClass());
    		if (newPriority > priority)
    		{
    			priority = newPriority;
    			result = ut;
    		}
    	}
    	if (result instanceof UploadTableTree)
    	{
    		while (((UploadTableTree )result).getChild() != null)
    		{
    			result = ((UploadTableTree )result).getChild();
    		}
    	}
    	if (result == null && isAgentOnly)
    	{
    		for (UploadTable ut : uploadTables)
    		{
    			if (ut.getTblClass().equals(Agent.class))
    			{
    				return ut;
    			}
    		}
    	}
    	return result;
    }
    
    /**
     * @param wri
     * @return table to attach wri to
     */
    public UploadTable getAttachToTable(WorkbenchRowImage wri)
    {
    	if (wri.getAttachToTableName() == null)
    	{
    		return getAttachToTable();
    	}
    	
    	String attachToStrs[] = wri.getAttachToTableName().split("\\.");
    	String tblName = attachToStrs[0];
    	String relTblName = attachToStrs.length > 1 ? attachToStrs[1] : null; 
    	
    	UploadTable result = getUploadTableByNameRel(tblName, relTblName);
    	if (result == null)
    	{
    		result = getAttachToTable();
    		String msg = "row " + rowUploading + ": " + wri.getAttachToTableName() + " is not mapped."
    			+ " Attaching image to " 
    			+ (result != null ? result.toString() : /*but this CANNOT happen*/ "default") + " table.";
    		log.warn(msg);
    	}
    	return result;
    }
    
    /**
     * @return true if attachments are OK.
     */
    protected boolean verifyAttachments()
    {
    	if (!attachmentsPresent())
    	{
    		return true;
    	}
    	
    	return getAttachToTable() != null;
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
            Vector<Vector<Table>> missingTbls = new Vector<Vector<Table>>();
            
            //check that parents exist for one-to-one children (which are required to be defined as many-to-one parents 
            //in hibernate)
            for (UploadTable t : uploadTables)
            {
                if (t.isOneToOneChild() && !t.getHasChildren() && !(t.getTblClass().equals(LocalityDetail.class) || t.getTblClass().equals(GeoCoordDetail.class)))
                {
                    Vector<Vertex<Table>> vs = db.getGraph().getAdjacentVertices(new Vertex<Table>(t.getTable().getName(), t.getTable()));
                    Vector<Table> tbls = new Vector<Table>();
                    for (Vertex<Table> vertex : vs)
                    {
                        tbls.add(vertex.getData());
                    }
                    missingTbls.add(tbls);
                }
            }
            if (!uploadGraph.isConnected())
            {     
               missingTbls.addAll(getMissingTbls());   
            }
            if (missingTbls.size() > 0)
            {
                Vector<Pair<String, Vector<Table>>> missingTblHints = new Vector<Pair<String, Vector<Table>>>();
            	int h = 1;
                for (Vector<Table> tbls : missingTbls)
                {
                    String msg = "";
                    if (tbls != null && tbls.size() > 0)
                    {
                        msg += " ";
                        for (int t=0; t<tbls.size(); t++)
                        {
                            if (t > 0)
                            {
                                msg += ", ";
                            }
                            msg += tbls.get(t).getTableInfo().getTitle();
                        }
                    }
                    if (!msg.equals(""))
                    {
                    	missingTblHints.add(new Pair<String, Vector<Table>>(
                    			String.format(getResourceString("WB_UPLOAD_MISSING_TBL_HINT"), h++, msg), 
                    			tbls));
                    }
                }
                if (missingTblHints.size() > 0)
                {
                    errors.add(new BaseUploadMessage(getResourceString("WB_UPLOAD_MISSING_TBL_HINTS")));
                    for (Pair<String, Vector<Table>> hint : missingTblHints)
                    {
                    	errors.add(new InvalidStructure("   " + hint.getFirst(), hint.getSecond()));
                    }
                }
                else 
                {
                	errors.add(new BaseUploadMessage(getResourceString("WB_UPLOAD_MISSING_TBL_NO_HINTS")));
                }
            }
        }
        catch (DirectedGraphException ex)
        {
            throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
        }
        
        errors.addAll(validateConsistency());

        if (!verifyAttachments())
        {
        	String msg = String.format(UIRegistry.getResourceString("WB_UPLOAD_NO_ATTACHABLES"), getAttachableStr());
        	errors.add(new BaseUploadMessage(msg));
        }
        
        //if tables are missing return now, because spurious errors may be generated.
        if (errors.size() != 0)
        {
        	return errors;
        }
        
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
        
        Vector<DefaultFieldEntry> undefinedDfes = new Vector<DefaultFieldEntry>();
        for (DefaultFieldEntry dfe : missingRequiredFields)
        {
            if (!dfe.isDefined())
            {
                undefinedDfes.add(dfe);
            }
        }
        //now remove possibly confusing or redundant dfes.
        Collections.sort(undefinedDfes, new Comparator<DefaultFieldEntry>(){

            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(DefaultFieldEntry o1, DefaultFieldEntry o2)
            {
                int result = o1.getUploadTbl().getTable().getName().compareTo(o2.getUploadTbl().getTable().getName());
                if (result != 0)
                {
                    return result;
                }
                boolean o1IsUserFld = o1.getUploadFld() == null || o1.getUploadFld().getIndex() != -1;
                boolean o2IsUserFld = o2.getUploadFld() == null || o2.getUploadFld().getIndex() != -1;
                if (o1IsUserFld == o2IsUserFld)
                {
                    return (o1.getFldName().compareTo(o2.getFldName()));
                }
                if (o1IsUserFld)
                {
                    return -1;
                }
                return 1;
            }
            
        });
        UploadTable currentTbl = null;
        Vector<DefaultFieldEntry> dfes4Tbl = new Vector<DefaultFieldEntry>();
        Vector<DefaultFieldEntry> dfes2Remove = new Vector<DefaultFieldEntry>();
        for (DefaultFieldEntry dfe : undefinedDfes)
        {
            if (dfe.getUploadTbl() != currentTbl)
            {
                if (dfes4Tbl.size() > 1)
                {
                    boolean gotAUserFld = false;
                    for (DefaultFieldEntry tblDfe : dfes4Tbl)
                    {
                        boolean isAUserFld = tblDfe.getUploadFld() == null || tblDfe.getUploadFld().getIndex() != -1;
                        gotAUserFld = gotAUserFld || isAUserFld;
                        if (!isAUserFld && gotAUserFld)
                        {
                            //remove weird fields if there are other non-weird fields from the table
                            dfes2Remove.add(tblDfe);
                        }
                    }
                }
                dfes4Tbl.clear();
                currentTbl = dfe.getUploadTbl();                            
            }
            dfes4Tbl.add(dfe);
        }
        if (dfes4Tbl.size() > 1)
        {
            boolean gotAUserFld = false;
            for (DefaultFieldEntry tblDfe : dfes4Tbl)
            {
                boolean isAUserFld = tblDfe.getUploadFld() == null || tblDfe.getUploadFld().getIndex() != -1;
                gotAUserFld = gotAUserFld || isAUserFld;
                if (!isAUserFld && gotAUserFld)
                {
                    //remove weird fields if there are other non-weird(or weird) fields from the table
                    dfes2Remove.add(tblDfe);
                }
            }
        }
        for (DefaultFieldEntry dfe : dfes2Remove)
        {
            undefinedDfes.remove(dfe);
        }
        for (DefaultFieldEntry dfe : undefinedDfes)
        {
            // see note above for missignRequiredClasses iteration
            // another very vague message...
            String msg = getResourceString("WB_UPLOAD_MISSING_DBDATA") + ": "
                    + dfe.getUploadTbl().getTable().getTableInfo().getTitle() + "."
                    + dfe.getFldName(); // i18n (dfe.getFldName() is not using title nor wb
                                        // column header)
            errors.add(new InvalidStructure(msg, this));
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

        try
        {
            // Make sure that the tax tree levels included for each determination are consistent.
            // All need to include the same levels, for example [Genus 1, Species1, SubSpecies 1,
            // Genus
            // 2, Species 2] is inconsistent because SubSpecies 2 is missing.
            // Since, right now, tax trees are the only trees that require this test.
            // Non-tree 1-manys like Collector are ok (but possibly not desirable?) if inconsistent:
            // [FirstName 1, LastName 1, LastName 2] works.
            TreeMapElements maxTaxSeqLevel = null;
            for (int m = 0; m < uploadData.getCols(); m++)
            {
                if (uploadData.getMapping(m).getTable().equalsIgnoreCase("taxon"))
                {
                    UploadMappingDefTree tmap = (UploadMappingDefTree) uploadData.getMapping(m);
                    boolean seqSizeInconsistent = false;
                    for (int l = 0; l < tmap.getLevels().size(); l++)
                    {
                        TreeMapElements tmes = tmap.getLevels().get(l);
                        if (tmes.getMaxSeq() > 0 || maxTaxSeqLevel != null
                                && maxTaxSeqLevel.getRank() < tmes.getRank())
                        {
                            if (maxTaxSeqLevel == null)
                            {
                                maxTaxSeqLevel = tmes;
                            }
                            else if (maxTaxSeqLevel.getMaxSeq() != tmes.getMaxSeq())
                            {
                                seqSizeInconsistent = true;
                                if (maxTaxSeqLevel.getMaxSeq() < tmes.getMaxSeq())
                                {
                                    maxTaxSeqLevel = tmes;
                                }
                            }

                        }

                        boolean[] seqs = tmes.getSeqs();
                        for (int s = 0; s < seqs.length; s++)
                        {
                            if (!seqs[s])
                            {
                                String msg = String.format(
                                        getResourceString("WB_UPLOAD_MISSING_SEQ"), tmes
                                                .getWbFldName());
                                result.add(new InvalidStructure(msg, null));
                            }
                        }
                    }

                    if (seqSizeInconsistent && maxTaxSeqLevel != null)
                    {
                        for (int l = 0; l < tmap.getLevels().size(); l++)
                        {
                            TreeMapElements tmes = tmap.getLevels().get(l);
                            if (tmes.getRank() > maxTaxSeqLevel.getRank()
                                    && tmes.getMaxSeq() < maxTaxSeqLevel.getMaxSeq())
                            {
                                for (int s = tmes.getMaxSeq() + 1; s <= maxTaxSeqLevel.getMaxSeq(); s++)
                                {
                                    String msg = String.format(
                                            getResourceString("WB_UPLOAD_MISSING_SEQ"), tmes
                                                    .getWbFldName());
                                    result.add(new InvalidStructure(msg, null));
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
                            && uploadData.getMapping(m).getField().equalsIgnoreCase("iscurrent"))
                    {
                        isCurrentCount++;
                        isCurrentPresent[((UploadMappingDefRel) uploadData.getMapping(m))
                                .getSequence()] = true;
                        isCurrentCaptionSample = uploadData.getMapping(m).getWbFldName();
                    }
                }
                if (isCurrentCount != 0 && isCurrentCount != maxTaxSeqLevel.size())
                {
                    for (int c = 0; c < isCurrentPresent.length; c++)
                    {
                        if (!isCurrentPresent[c])
                        {
                            String fldName;
                            // strip off trailing number (assuming we will never allow it to be >
                            // 10)
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
                            String msg = getResourceString("WB_UPLOAD_MISSING_FLD") + ": "
                                    + fldName + " " + Integer.toString(c + 1);
                            result.add(new InvalidStructure(msg, null));
                        }
                    }
                }
            }

            return result;
        }
        finally
        {
            if (result.size() > 0)
            {
                currentUpload = null;
            }
        }
    }

    /**
     * @param row
     * @return
     */
    public synchronized Integer getRowRecordId(int row) throws UploaderException
    {
    	try
    	{
    		return wbSS.getWorkbench().getRow(row).getRecordId();
    	} catch(Exception ex)
    	{
    		throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
    	}
    }
    
    protected void setUpdateTableId(Integer updateTblId)
    {
    	this.updateTableId.set(updateTblId == null ? -1 : updateTblId);
    }
    
    protected Integer getUpdateTableId()
    {
    	int got = this.updateTableId.get();
    	return got >= 0 ? got : null;
    }
    /**
     * @throws UploaderException
     * 
     * Sets up for upload.
     */
    protected void prepareToUpload() throws UploaderException
    {
        //XXX What is the dbTableId field in workbench for? ExportedFromTableName may not even be necessary. Based on use of dbTableId in recordset
    	//it looks like it was designed to for exported records...
        Integer updateTblId = isUpdateUpload() ? DBTableIdMgr.getInstance().getIdByClassName(wbSS.getWorkbench().getExportedFromTableName()) : null;
        setUpdateTableId(updateTblId);
    	if (currentOp != SUCCESS_PARTIAL)
        {
            for (UploadTable t : uploadTables)
            {
                t.setMatchRecordId(t.isMatchRecordId() || (updateTblId != null && updateTblId == t.getTable().getTableInfo().getTableId()));
                if (updateTblId != null && updateTblId == CollectionObject.getClassTableId() 
                		&& t.getTable().getTableInfo().getTableId() == CollectingEvent.getClassTableId()
                		&& AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent())
                {
                	t.setMatchRecordId(true);
                }
                t.setUpdateMatches(isUpdateUpload());
                t.prepareToUpload();
            }
            newAttachments.clear();
            // But may want option to ONLY upload rows that were skipped...
            skippedRows.clear();
            messages.clear();
            uploadStartRow = 0;
        } else
        {
        	uploadStartRow = rowUploading;
        	if (wbSS.getWorkbench().getRow(rowUploading).getUploadStatus() == WorkbenchRow.UPLD_SUCCESS)
        	{
        		uploadStartRow++; //never should be more than row behind.
        	}
        }
        if (uploadedObjectViewer != null)
        {
        	uploadedObjectViewer.closeView();
        }
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
    
    protected boolean indeterminateProgress = false;
    protected boolean useAppStatBar = false;
    protected int maxProgVal = 0;
    protected int minProgVal = 0;
    
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
                                   final boolean useAppProgress)
    {
        SwingUtilities.invokeLater(new Runnable() {
           public void run()
           {
               if (!useAppStatBar && mainPanel == null)
               {
                   log.error("UI does not exist.");
                   return;
               }
               minProgVal = min;
               maxProgVal = max;
               indeterminateProgress = minProgVal == 0 && maxProgVal == 0;
               useAppStatBar = useAppProgress;
               if (useAppStatBar)
               {
                   if (indeterminateProgress)
                   {
                       UIRegistry.getStatusBar().setIndeterminate("UPLOADER", indeterminateProgress);
                   }
                   else
                   {
                       UIRegistry.getStatusBar().setProgressRange("UPLOADER", minProgVal, maxProgVal);
                   }
               }
               else
               {
                   JProgressBar pb = mainPanel.getCurrOpProgress();
                   pb.setVisible(true);
                   if (indeterminateProgress)
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
                       pb.setMinimum(minProgVal);
                       pb.setMaximum(maxProgVal);
                       pb.setValue(minProgVal);
                   }}
               }
        });
    }

    /**
     * @param val
     * 
     * Sets progress bar progress.
     */
    protected void setCurrentOpProgress(final int val)
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
                if (!indeterminateProgress)
                {
                    if (useAppStatBar && !indeterminateProgress)
                    {
                        if (val == -1)
                        {
                            UIRegistry.getStatusBar().incrementValue("UPLOADER");
                        }
                        else
                        {
                            UIRegistry.getStatusBar().setValue("UPLOADER", val);
                        }
                    }
                    else
                    {
                        JProgressBar pb = mainPanel.getCurrOpProgress();
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
                setCurrentOpProgress(val);
                synchronized (Uploader.this)
                {
                	for (UploadMessage newMsg : newMessages)
                	{
                		mainPanel.addMsg(newMsg);
                		messages.add(newMsg);
                	}
                	newMessages.clear();
                }
            }
        });
    }

    public void undoStep()
    {
        setCurrentOpProgress(-1);
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
            public Object doInBackground()
            {
                start();
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
            public void done()
            {
                super.done();
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
        uploadTask.execute();
        initUI(Uploader.CHECKING_REQS);
    }

    /**
     * Called when dataset is saved.
     */
    public void refresh()
    {
        //wbSS.getWorkbench().forceLoad();
    	if (currentOp.equals(USER_INPUT)) 
    	{
    		uploadData.refresh(wbSS.getWorkbench().getWorkbenchRowsAsList());
    		validateData(true);
    	}
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
            //NOTE the uploadedRecs structure in UploadTable is cleared at the beginning up each upload,
        	//after an undo, uploadedRecs will still contain the records that were 'undone'.
            result += ut.getUploadedRecs().size();
        }
        return result;
    }

    /**
     * Shuts down upload UI.
     * @param notifyWB - If true, notify this Uploader's WorkBench.
     */
    public void closeMainForm(boolean notifyWB)
    {
        try
        {
            logDebug("closing main form");
            mainPanel.setVisible(false);
            mainPanel = null;
            closeUploadedDataViewers();

            for (Component c : keyListeningTo)
            {
                logDebug("removing key listener");
                c.removeKeyListener(this);
            }
            keyListeningTo.clear();

            if (notifyWB)
            {
                wbSS.uploadDone();
            }
        }
        finally
        {
            currentUpload = null;
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
            validateData(true);
        }
        else if (e.getActionCommand().equals(UploadMainPanel.DO_UPLOAD))
        {
            if (isUpdateUpload())
            {
            	if (!isValidUpdateUpload())
            	{
            		UIRegistry.showError("This dataset contains mappings which are not updateable");
            		return;
            	}
            	if (!UIRegistry.displayConfirm("Ready to Update", "This update cannot be undone. Are you sure you want to continue?", "Yes", "No", JOptionPane.WARNING_MESSAGE))
            	{
            		return;
            	}
            }
        	uploadIt(true);
        }
        else if (e.getActionCommand().equals(UploadMainPanel.VIEW_UPLOAD))
        {
            if (currentOp.equals(Uploader.SUCCESS) || currentOp.equals(Uploader.SUCCESS_PARTIAL))
            {
            	viewAllObjectsCreatedByUpload();
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
            if (aboutToShutdown(null))
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
                undoUpload(true, false, true);
            }
        }
        else if (e.getActionCommand().equals(UploadMainPanel.TBL_DBL_CLICK))
        {
            mainPanel.getViewUploadBtn().setEnabled(canViewUpload(currentOp));
            if (currentOp.equals(Uploader.SUCCESS) || currentOp.equals(Uploader.SUCCESS_PARTIAL))
            {
            	viewAllObjectsCreatedByUpload();
            }
        }
        else if (e.getActionCommand().equals(UploadMainPanel.TBL_CLICK))
        {
            mainPanel.getViewUploadBtn().setEnabled(canViewUpload(currentOp));
        }
        else if (e.getActionCommand().equals(UploadMainPanel.MSG_CLICK))
        {
            goToMsgWBCell((Component)e.getSource(), false);
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
        if (shuttingDownSS != null && shuttingDownSS != wbSS)
        {
            return true;
        }
        if (currentTask != null || 
        		(shuttingDownSS != null && (currentOp.equals(Uploader.SUCCESS)  || currentOp.equals(Uploader.SUCCESS_PARTIAL)) && getUploadedObjects() > 0))
        {
            JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), getResourceString("WB_UPLOAD_BUSY_CANNOT_CLOSE"));
            return false;
        }
        
        boolean result = true;

        if (uploadedObjectViewer != null)
        {
        	uploadedObjectViewer.closeView();
        }

        if (result && shuttingDownSS == null && 
        		(currentOp.equals(Uploader.SUCCESS)  || currentOp.equals(Uploader.SUCCESS_PARTIAL)) && getUploadedObjects() > 0 && !isUpdateUpload())
        {
            result = false;
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
                wbSS.saveObject();
            }
            else if (rv == JOptionPane.NO_OPTION)
            {
                undoUpload(shuttingDownSS == null, true, true);
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
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(Uploader.class, ex);
                        throw new RuntimeException(ex);
                    }
                }
            }
        } 
        if (additionalLocksSet)
        {
        	freeAdditionalLocks();
        }
        return result; 
    }
    
    protected void showSettings()
    {
        boolean readOnly = !(currentOp.equals(Uploader.READY_TO_UPLOAD)  || currentOp.equals(Uploader.SUCCESS_PARTIAL))
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
    protected void goToMsgWBCell(final Component c, boolean stopEdit)
    {
        if (mainPanel == null) 
        { 
            throw new RuntimeException("Upload form does not exist."); 
        }
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
            
            if (msg == null)
            {
                logDebug("gotToMsgWBCell: null message");
                return;
            }
            
            if (msg.getRow() != -1)
            {
                if (msg.getCol() == -1)
                {
                    wbSS.getSpreadSheet().scrollToRow(msg.getRow());
                    wbSS.getSpreadSheet().getSelectionModel().clearSelection();
                    wbSS.getSpreadSheet().getSelectionModel().setSelectionInterval(
                            msg.getRow(), msg.getRow());
                }
                else
                {
                    wbSS.getSpreadSheet().getSelectionModel().clearSelection();
                    Rectangle rect = wbSS.getSpreadSheet().getCellRect(msg.getRow(), msg.getCol(),
                            false);
                    wbSS.getSpreadSheet().scrollRectToVisible(rect);
                    if (msg instanceof UploadTableInvalidValue && msg.getCol() != -1)
                    {
                        if (!stopEdit)
                        {

                            wbSS.getSpreadSheet().editCellAt(msg.getRow(), msg.getCol(), null);

                            // Now, if necessary, add this as a listener to the editorComponent to
                            // allow moving to
                            // next/prev
                            // invalid cell after ENTER/TAB/UP/DOWN
                            Component editor = wbSS.getSpreadSheet().getEditorComponent();
                            boolean addListener = true;
                            KeyListener[] listeners = editor.getKeyListeners();
                            for (int k = 0; k < listeners.length; k++)
                            {
                                if (listeners[k] instanceof Uploader)
                                {
                                    if (listeners[k] == this)
                                    {
                                        logDebug("already listening to spreadsheet editor");
                                        addListener = false;
                                        break;
                                    }
                                    // should never get here, but just in case:
                                    logDebug("removing previous listener");
                                    editor.removeKeyListener(listeners[k]);
                                }
                            }
                            if (addListener)
                            {
                                logDebug("adding this as listener to spreadsheet editor");
                                editor.addKeyListener(this);
                                this.keyListeningTo.add(editor);
                            }
                            editor.requestFocusInWindow();
                        }
                        else
                        {
                            if (wbSS.getSpreadSheet().getCellEditor() != null)
                            {
                                wbSS.getSpreadSheet().getCellEditor().stopCellEditing();
                            }
                        }
                    }
                }
            }
        }
        if (mainPanel == null) 
        { 
            throw new RuntimeException("Upload form does not exist."); 
        }
    }

    /**
     * Moves to and begins editing the next invalid WorkBench cell.
     * 
     * Not complete. Needs to limit selections to UploadTableInvalidValue objects.
     * 
     * See note in goToMsgWBCell re addKeyListener().
     */
    protected void goToNextInvalidCell()
    {
        logDebug("goToNextInvalidCell");
        int sel = mainPanel.getValidationErrorList().getSelectedIndex() + 1;
        if (sel >= mainPanel.getValidationErrorList().getModel().getSize())
            sel = 1; // first msg is explanatory
        if (sel != -1)
        {
            boolean stopEditing = sel == mainPanel.getValidationErrorList().getSelectedIndex();
            mainPanel.getValidationErrorList().setSelectedIndex(sel);
            logDebug("Going to msg " + sel);
            goToMsgWBCell(mainPanel.getValidationErrorList(), stopEditing);
        }
    }
    
    /**
     * Moves to and begins editing the previous invalid WorkBench cell.
     *
     * Not complete. Needs to limit selections to UploadTableInvalidValue objects.
     * 
     * See note in goToMsgWBCell re addKeyListener().
    */
    protected void goToPrevInvalidCell()
    {
        int sel = mainPanel.getValidationErrorList().getSelectedIndex() - 1;
        if (sel < 1) //first msg is explanatory
            sel = mainPanel.getValidationErrorList().getModel().getSize()-1;
        if (sel != -1)
        {
            boolean stopEdit = sel == mainPanel.getValidationErrorList().getSelectedIndex();
            mainPanel.getValidationErrorList().setSelectedIndex(sel);
            goToMsgWBCell(mainPanel.getValidationErrorList(), stopEdit);
        }
    }
    
    /**
     * Moves to and begins editing the first invalid WorkBench cell.
     *
     * Not complete. Needs to limit selections to UploadTableInvalidValue objects.
     * 
     * See note in goToMsgWBCell re addKeyListener().
     */
    protected void goToFirstInvalidCell()
    {
        if (mainPanel.getValidationErrorList().getModel().getSize() > 0)
        {
            mainPanel.getValidationErrorList().setSelectedIndex(0);
            goToMsgWBCell(mainPanel.getValidationErrorList(), false);
        }
    }

    /**
     * Moves to and begins editing the last invalid WorkBench cell.
     *
     * Not complete. Needs to limit selections to UploadTableInvalidValue objects.
     * 
     * See note in goToMsgWBCell re addKeyListener().
     */
    protected void goToLastInvalidCell()
    {
        if (mainPanel.getValidationErrorList().getModel().getSize() > 0)
        {
            mainPanel.getValidationErrorList().setSelectedIndex(
                    mainPanel.getValidationErrorList().getModel().getSize() - 1);
            goToMsgWBCell(mainPanel.getValidationErrorList(), false);
        }
    }

    /**
     * Builds form for upload UI.
     */
    protected void buildMainUI()
    {
        mainPanel = new UploadMainPanel(isUpdateUpload());

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

    //XXX debugging junk!
    int thirdTime = 0; 
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
               
               if (op.equals(Uploader.RETRIEVING_UPLOADED_DATA))
               {
            	   //There's really nothing to do in this case anymore
            	   return;
               }
               
               int uploadedObjects = getUploadedObjects();
               
               if (op.equals(Uploader.SUCCESS) || op.equals(Uploader.SUCCESS_PARTIAL))
               {
                   if (mainPanel.getUploadTbls().getSelectedIndex() == -1)
                   {
                       // assuming list is not empty
                       mainPanel.getUploadTbls().setSelectedIndex(0);
                   }
               }

               if (op.equals(Uploader.SUCCESS) || op.equals(Uploader.SUCCESS_PARTIAL)) 
               {
            	   if (uploadedObjects > 0 && !isUpdateUpload())
            	   {
            		   mainPanel.closeBtn.setText(getResourceString("WB_UPLOAD.COMMIT"));
            	   }
            	   else
            	   {
            		   mainPanel.closeBtn.setText(getResourceString("CLOSE"));
            	   }
               }
               
               if (op.equals(Uploader.READY_TO_UPLOAD))
               {
            	   mainPanel.closeBtn.setText(getResourceString("CLOSE"));
               }
               
               
               if (op.equals(UPLOADING) || op.equals(SUCCESS) || op.equals(Uploader.SUCCESS_PARTIAL))
               {
                   mainPanel.showUploadTblTbl();
               }
               else if (uploadedObjects == 0 || op.equals(Uploader.READY_TO_UPLOAD))
               {
                   mainPanel.showUploadTblList();
               }
               
               mainPanel.getValidateContentBtn().setEnabled(canValidateContent(op));
               
               mainPanel.getCancelBtn().setEnabled(canCancel(op));
               if (mainPanel.getCancelBtn().isEnabled())
               {
            	   if (op.equals(UPLOADING))
            	   {
            		   mainPanel.getCancelBtn().setText(getResourceString("WB_UPLOAD_PAUSE"));
            	   }
            	   else
            	   {
            		   mainPanel.getCancelBtn().setText(getResourceString("WB_UPLOAD_CANCEL"));
            	   }
               }
            	   
               mainPanel.getCancelBtn().setVisible(mainPanel.getCancelBtn().isEnabled());

               mainPanel.getDoUploadBtn().setEnabled(canUpload(op));

               mainPanel.getViewSettingsBtn().setEnabled(canViewSettings(op));

               mainPanel.getViewUploadBtn().setEnabled(canViewUpload(op) && uploadedObjects > 0);
               mainPanel.getViewUploadBtn().setVisible(mainPanel.getViewUploadBtn().isEnabled());

               mainPanel.getUndoBtn().setEnabled(canUndo(op) && uploadedObjects > 0);
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
               if (op.equals(Uploader.SUCCESS) || op.equals(Uploader.SUCCESS_PARTIAL))
               {
                   statText += ". " + getUploadedObjects().toString() + " "
                           + getResourceString("WB_UPLOAD_OBJECT_COUNT") + ".";
                   if (killer != null)
                   {
                       logDebug("Hey. Wait a minute. The operation succeeded while dead. Is that not creepy?");
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
                       statText += ": " + killer.getClass().getName();
                   }
               }
               if (dotDotDot(op)) 
               {
                   statText += "...";
               }
               
               mainPanel.clearMsgs(new Class<?>[]{UploadTableInvalidValue.class});
               if (op.equals(USER_INPUT))
               {
                   mainPanel.addMsg(new UploadTableInvalidValue(statText, null, -1, null));
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
                       thirdTime = thirdTime + 1;
                       SwingUtilities.invokeLater(new Runnable(){
                    	   @Override
                    	   public void run()
                    	   {
                               JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), 
                                       String.format(getResourceString(WB_TOO_MANY_ERRORS), String.valueOf(MAX_MSG_DISPLAY_COUNT),
                                                   String.valueOf(validationIssues.size())), 
                                       getResourceString(WB_UPLOAD_FORM_TITLE), 
                                       JOptionPane.WARNING_MESSAGE,
                                       null);
                    	   }
                       });
                   }
               }
               mainPanel.getPrintBtn().setEnabled(validationIssues != null && validationIssues.size() > 0);
//           }
//        });
    }

    /**
     * Opens view of uploaded (newly created) objects for all tables. 
     * NOTE: Currently does not include attachments.
     */
    protected void viewAllObjectsCreatedByUpload()
    {
        if (currentOp.equals(Uploader.SUCCESS) || currentOp.equals(Uploader.SUCCESS_PARTIAL)
        		|| currentOp.equals(Uploader.RETRIEVING_UPLOADED_DATA))
        {
            viewUploadsAll();
        }
    }

    protected void viewUploadsAll()
    {
        if (uploadedObjectViewer == null)
        {
        	uploadedObjectViewer = new UploadRetriever();
        }
    	uploadedObjectViewer.viewUploads(uploadTables, wbSS.getTask(), getResourceString(WB_UPLOAD_VIEW_RESULTS_TITLE));
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
        return (op.equals(Uploader.SUCCESS) || op.equals(Uploader.SUCCESS_PARTIAL))
         && !isUpdateUpload(); //can't currently undo update uploads
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
        return op.equals(Uploader.READY_TO_UPLOAD) || op.equals(Uploader.SUCCESS_PARTIAL) ;
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
                || op.equals(Uploader.SUCCESS) || op.equals(Uploader.SUCCESS_PARTIAL)
                || op.equals(Uploader.INITIAL_STATE) || op.equals(Uploader.FAILURE);
    }

    /**
     * @param op
     * @return true if canViewSettings in phase op.
     */
    protected boolean canViewSettings(final String op)
    {
        return op.equals(Uploader.READY_TO_UPLOAD) || op.equals(Uploader.USER_INPUT) || op.equals(Uploader.SUCCESS_PARTIAL)
        // || op.equals(Uploader.SUCCESS)
                // || op.equals(Uploader.INITIAL_STATE)
                || op.equals(Uploader.FAILURE);
    }

    /**
     * @return all tables whose ParentTableEntries contain parent
     */
    public List<UploadTable> getChildren(final UploadTable parent)
    {
    	Vector<UploadTable> result = new Vector<UploadTable>();
    	for (UploadTable ut : uploadTables)
    	{
    		//Assuming this method is called by UploadTable created by this Uploader
    		//so pointer comparisons are OK.
    		if (ut != parent)
    		{
    			for (Vector<ParentTableEntry> ptes : ut.getParentTables())
    			{
    				boolean broke = false;
    				for (ParentTableEntry pte : ptes)
    				{
    					if (pte.getImportTable() == parent)
    					{
    						result.add(ut);
    						broke = true;
    						break;
    					}
    				}
    				if (broke)
    				{
    					break;
    				}
    			}
    		}
    	}
    	return result;
    }

    /**
     * @param op
     * @return true if canViewUpload in phase op.
     */
    protected boolean canViewUpload(final String op)
    {
        return (op.equals(Uploader.SUCCESS) || op.equals(Uploader.SUCCESS_PARTIAL)) && mainPanel.getUploadTbls().getSelectedIndex() != -1;
    }

    /**
     * Uploads dataset.
     */
    public void uploadIt(boolean doInBackground) 
    {
        try
        {
        	buildIdentifier();
            setOpKiller(null);
            prepareToUpload();

            final UploaderTask uploadTask = new UploaderTask(true, "WB_CANCEL_UPLOAD_MSG")
            {
                boolean success = false;
                boolean paused = false;
                Integer updateTblId = null;
                UploadTable exportedTable = null;
                
                /**
                 * @throws UploaderException
                 */
                protected void setupExportedTable() throws UploaderException
                {
                	if (updateTblId != null)
                	{
                		for (UploadTable ut : uploadTables)
                		{
                			if (ut.getTable().getTableInfo().getTableId() == updateTblId)
                			{
                				if (exportedTable == null)
                				{
                					exportedTable = ut;
                				} else
                				{
                					throw new UploaderException("Unable to determine base exported table", UploaderException.ABORT_IMPORT);
                				}
                			}
                		}
                	}
                }
                
                /**
                 * 
                 */
                protected void setExportedRecordIds() throws Exception
                {
                	DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                	Class<?> cls = DBTableIdMgr.getInstance().getInfoById(updateTblId).getClassObj();
                	try
                	{
                		DataModelObjBase obj = (DataModelObjBase )session.get(cls, getRowRecordId(rowUploading));
                		if (obj != null)
                		{
                			obj.forceLoad();
                		}
                		exportedTable.setExportedRecordId(obj);
                	} finally
                	{
                		session.close();
                	}
                	
                }
                
                @SuppressWarnings("synthetic-access")
                @Override
                public Object doInBackground()
                {
                    start();
                	initProgressBar(0, uploadData.getRows(), true, 
                        getResourceString("WB_UPLOAD_UPLOADING") + " " + getResourceString("WB_ROW"), false);
                    try
                    {
                    	updateTblId = getUpdateTableId();
                    	setupExportedTable();
                        for (rowUploading = uploadStartRow; rowUploading < uploadData.getRows();)
                        {
                        	boolean rowAborted = false;
                        	if (cancelled)
                        	{
                        		paused = true;
                        		break;
                        	}
                        	logDebug("uploading row "
								+ String.valueOf(rowUploading));

                        	if (rowUploading == 0)
                        	{
                        		showUploadProgress(1);
                        	}

                        	if (!uploadData.isEmptyRow(rowUploading))
                        	{
                            	imagesForRow.clear();
                            	if (updateTblId != null)
                            	{
                            		setExportedRecordIds();
                            	}
                            	imagesForRow.addAll(uploadData.getWbRow(rowUploading).getWorkbenchRowImages());
                        		for (UploadTable t : uploadTables)
                        		{
                        			if (cancelled)
                        			{
                        				break;
                        			}
                        			try
                        			{
                        				if (wbSS.getWorkbench()
											.getRow(rowUploading)
											.getUploadStatus() != WorkbenchRow.UPLD_SUCCESS)
                        				{
                        					uploadRow(t, rowUploading);
                        				} else
                        				{
                        					throw new UploaderException(
												getResourceString("WB_UPLOAD_ROW_ALREADY_UPLOADED"),
												UploaderException.ABORT_ROW);
                        				}
                        			} catch (UploaderException ex)
                        			{
                        				if (ex.getStatus() == UploaderException.ABORT_ROW)
                        				{
                        					logDebug(ex.getMessage());
                        					abortRow(ex, rowUploading);
                        					rowAborted = true;
                        					break;
                        				}
                        				throw ex;
                        			}
                        			updateObjectsCreated();
                        		}
                        	}

                            if (!rowAborted)
                            {
                            	wbSS.getWorkbench().getRow(rowUploading).setUploadStatus(
                            			WorkbenchRow.UPLD_SUCCESS);
                            }
                            if (!cancelled)
                            {
                            	rowUploading++;
                            }
                            showUploadProgress(rowUploading);
                        }
                    }
                    catch (Exception ex)
                    {
                        setOpKiller(ex);
                        return false;
                    }
                    success = !cancelled || (cancelled && paused);
                    return success;
                }

                @Override
                public void done()
                {
                    try
                    {
                    	//for (UploadTable t : uploadTables)
                    	for (int t = uploadTables.size() - 1; t >= 0; t--)
                    	{
                    		uploadTables.get(t).finishUpload(cancelled && !paused);
                    	}
                    } catch (Exception ex)
                    {
                    	success = false;
                    	setOpKiller(ex);
                    }
                    super.done();
                    statusBar.setText("");
                    if (success)
                    {
                        if (!paused)
                        {
                        	setCurrentOp(Uploader.SUCCESS);
                        } else
                        {
                        	setCurrentOp(Uploader.SUCCESS_PARTIAL);
                        }
                    }
                    else
                    {
                        mainPanel.clearObjectsCreated();
                        //undoUpload will clear opKiller, so save it and reassign, after call. (iffy?)
                        Exception savedOpKiller = getOpKiller();
                        undoUpload(false, false, undo);
                        setOpKiller(savedOpKiller);

                        if (!cancelled)
                        {
                            setCurrentOp(Uploader.FAILURE);
                        }
                    }
                }

            };

            UIRegistry.getStatusBar().setText(getResourceString(Uploader.UPLOADING));
            uploadTask.execute();
            if (mainPanel == null)
            {
                initUI(Uploader.UPLOADING);
            }
            else
            {
                setCurrentOp(Uploader.UPLOADING);
            }
            
            if (!doInBackground)
            {
            	try
            	{
            		uploadTask.get();
            	} catch (ExecutionException ex)
            	{
            		//hopefully it will be clear to caller that something went wrong?
            	} catch (InterruptedException ex)
            	{
            		//hopefully it will be clear to caller that something went wrong?
            	}
            	//uploadTask.finished();
            }
        }
        catch (UploaderException ex)
        {
            setOpKiller(ex);
        }
    }

    /**
     * @param cause
     * @param row
     * @throws UploaderException
     */
    protected synchronized void abortRow(UploaderException cause, int row) throws UploaderException
    {
        //logDebug("NOT undoing writes which have already occurred while processing aborted row");
        logDebug("undoing writes which have already occurred while processing aborted row");
        try 
        {
            List<UploadTable> fixedUp = reorderUploadTablesForUndo();
            boolean isEmbeddedCE = AppContextMgr.getInstance().getClassObject(
                    Collection.class).getIsEmbeddedCollectingEvent();
            try
            {
                AppContextMgr.getInstance().getClassObject(Collection.class)
                        .setIsEmbeddedCollectingEvent(false);
                for (int ut = fixedUp.size() - 1; ut >= 0; ut--)
                {
                    // setCurrentOpProgress(fixedUp.size() - ut, false);
                    logDebug("aborting " + fixedUp.get(ut).getTable().getName());
                    fixedUp.get(ut).abortRow(row);
                }
    			updateObjectsCreated();
            }
            finally
            {
                AppContextMgr.getInstance().getClassObject(Collection.class)
                        .setIsEmbeddedCollectingEvent(isEmbeddedCE);
            }
        } catch (Exception e)
        {
        	throw new UploaderException(e, UploaderException.ABORT_IMPORT);
        }
        
        if (cause instanceof UploaderMatchSkipException)
        {
            wbSS.getWorkbench().getRow(rowUploading).setUploadStatus(WorkbenchRow.UPLD_SKIPPED);
        }
        else
        {
            WorkbenchRow wbr = wbSS.getWorkbench().getRow(rowUploading);
            if (wbr.getUploadStatus() != WorkbenchRow.UPLD_SUCCESS)
            {
            	wbr.setUploadStatus(WorkbenchRow.UPLD_FAILED);
            }
        }
        SkippedRow sr = new SkippedRow(cause, row);
        skippedRows.add(sr);
        newMessages.add(sr);
    }

    /**
     * @param msg
     */
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
        //It is currently not necessary to do any re-ordering.
        return uploadTables;
    }
    
    /**
     * Undoes the most recent upload.
     * 
     * Called in response to undo command from user, and by the program when an upload is cancelled
     * or fails.
     */
    public void undoUpload(final boolean isUserCmd, final boolean shuttingDown, final boolean completeUndo)
    {
        setOpKiller(null);

        final UploaderTask undoTask = new UploaderTask(false, "")
        {
            boolean success = false;
            boolean removeObjects = completeUndo;
            Vector<UploadTable> undone = new Vector<UploadTable>();
            
            @Override
            public Object doInBackground()
            {
                start();
            	if (removeObjects)
                {
                    try
                    {
                        if (isUserCmd)
                        {
                            SwingUtilities.invokeAndWait(new Runnable()
                            {
                                public void run()
                                {
                                    initProgressBar(0, getUploadedObjects(), true,
                                            getResourceString("WB_UPLOAD_UNDOING") + " "
                                                    + getResourceString("WB_UPLOAD_OBJECT"),
                                            shuttingDown);
                                }
                            });
                        }
                        else
                        {
                            SwingUtilities.invokeLater(new Runnable()
                            {
                                public void run()
                                {
                                    initProgressBar(0, getUploadedObjects(), true,
                                            getResourceString("WB_UPLOAD_CLEANING_UP") + " "
                                                    + getResourceString("WB_UPLOAD_OBJECT"),
                                            shuttingDown);
                                }
                            });
                        }
                        List<UploadTable> fixedUp = reorderUploadTablesForUndo();
                        boolean isEmbeddedCE = AppContextMgr.getInstance().getClassObject(
                                Collection.class).getIsEmbeddedCollectingEvent();
                        undoAttachments();
                        try
                        {
                            AppContextMgr.getInstance().getClassObject(Collection.class)
                                    .setIsEmbeddedCollectingEvent(false);
                            for (int ut = fixedUp.size() - 1; ut >= 0; ut--)
                            {
                                // setCurrentOpProgress(fixedUp.size() - ut, false);
                                logDebug("undoing " + fixedUp.get(ut).getTable().getName());
                                fixedUp.get(ut).undoUpload(true);
                                undone.add(fixedUp.get(ut));
                            }
                            success = true;
                            return success;
                        }
                        finally
                        {
                            AppContextMgr.getInstance().getClassObject(Collection.class)
                                    .setIsEmbeddedCollectingEvent(isEmbeddedCE);
                        }
                    }
                    catch (Exception ex)
                    {
                        setOpKiller(ex);
                        return false;
                    }
                }
                //else
                success = true;
                return success;
            }

            @Override
            public void done()
            {
                if (removeObjects)
                {
                	try
                	{
                		for (UploadTable ut : undone)
                		{
                			ut.finishUndoUpload();
                		}
                	} catch (Exception ex)
                	{
                		setOpKiller(ex);
                		success = false;
                	}
                }
                
            	super.done();
                
                if (removeObjects)
                {
                    for (WorkbenchRow wbRow : wbSS.getWorkbench().getWorkbenchRows())
                    {
                        wbRow.setUploadStatus(WorkbenchRow.UPLD_NONE); 
                    }
                    wbSS.setChanged(false);
                }
                
                statusBar.setText("");
                statusBar.setProgressDone("UPLOADER");
                if (shuttingDown)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            UIRegistry.clearSimpleGlassPaneMsg();

                        }
                    });
                }
                if (getOpKiller() != null)
                {
                    JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), String.format(
                            getResourceString("WB_UPLOAD_CLEANUP_FAILED"), new Object[] {
                                    getResourceString((isUserCmd ? "WB_UPLOAD_UNDO_BTN"
                                            : "WB_UPLOAD_CLEANUP")), wbSS.getWorkbench().getName(),
                                    wbSS.getWorkbench().getName() }), getResourceString("WARNING"),
                            JOptionPane.WARNING_MESSAGE);

                }
                if (mainPanel != null)
                {
                    mainPanel.clearObjectsCreated();
                    if (success)
                    {
                        if (removeObjects)
                        {
                            setCurrentOp(Uploader.READY_TO_UPLOAD);
                        }
                        else
                        {
                            setCurrentOp(Uploader.SUCCESS_PARTIAL);
                        }
                    }
                    else
                    {
                        setCurrentOp(Uploader.FAILURE);
                    }
                }
                if (shuttingDown && !isUserCmd)
                {
                	wbSS.decShutdownLock();
                	wbSS.shutdown();
                }
            }

        };
        if (recordSets != null)
        {
            recordSets.clear();
            recordSets = null;
        }
        UIRegistry.displayStatusBarText(getResourceString((isUserCmd ? Uploader.UNDOING_UPLOAD
                : Uploader.CLEANING_UP)));
        if (shuttingDown)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    UIRegistry.writeSimpleGlassPaneMsg(String.format(
                            getResourceString("WB_UPLOAD_CLEANING_UP") + "...", wbSS.getWorkbench()
                                    .getName()), WorkbenchTask.GLASSPANE_FONT_SIZE);

                }
            });
        }
        if (shuttingDown && !isUserCmd)
        {
            wbSS.incShutdownLock();
        }
        undoTask.execute();
        setCurrentOp(isUserCmd ? Uploader.UNDOING_UPLOAD : Uploader.CLEANING_UP);
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

    protected abstract class UploaderTask extends javax.swing.SwingWorker<Object, Object>
    {
        protected final JStatusBar statusBar = UIRegistry.getStatusBar();
        protected boolean cancelled = false;
        protected boolean undo = false;
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
        
        public void start()
        {
            startTime = System.nanoTime();
        }
                
        @Override
        public void done()
        {
            super.done();
            currentTask = null;
            done = true;
            endTime = System.nanoTime();
            logDebug("UploaderTask time elapsed: " + Long.toString((endTime-startTime)/1000000000L));
        }
        
        /**
         * Cancels the task. Subclasses need to take specific action
         * such as interrupting thread or whatever.
         */
        public synchronized void cancelTask()
        {
        	cancelled = true;
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

        /**
         * @return the undo
         */
        public synchronized boolean isUndo()
        {
            return undo;
        }

        /**
         * @param undo the undo to set
         */
        public synchronized void setUndo(boolean undo)
        {
            this.undo = undo;
        }
    }
    /**
     * Builds viewer for uploaded data.
     */
    public void retrieveUploadedData()
    {
        //bogusStorages = new HashMap<String, Vector<Vector<String>>>();
        final String savedOp = currentOp;
        setOpKiller(null);

        final UploaderTask retrieverTask = new UploaderTask(true, "WB_CANCEL_UPLOAD_MSG")
        {
            @Override
            public Object doInBackground()
            {
                start();
            	try
                {
                    //
                	return true;
                }
                catch (Exception ex)
                {
                    setOpKiller(ex);
                    return false;
                }
            }

            @Override
            public void done()
            {
                super.done();
                statusBar.setText("");
                setCurrentOp(savedOp);
                if (!cancelled)
                {
                    viewAllObjectsCreatedByUpload();
                    mainPanel.addMsg(new BaseUploadMessage(getResourceString("WB_UPLOAD_DATA_FETCHED")));
                }
                else
                {
                    //bogusStorages = null;
                    mainPanel.addMsg(new BaseUploadMessage(getResourceString("RetrievalWB_UPLOAD_FETCH_CANCELLED cancelled")));
                }
            }

			/* (non-Javadoc)
			 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader.UploaderTask#cancelTask()
			 */
//			@Override
//			public synchronized void cancelTask()
//			{
//				super.cancelTask();
//				interrupt();
//			}
            

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
        retrieverTask.execute();
    }

    /**
     * @param t
     * @param row
     */
    public void loadRow(final UploadTable t, int row)
    {
        for (UploadField field : uploadFields)
        {
            logDebug("   uploading field: " + field.getWbFldName());
            //System.out.println("   uploading field: " + field.getWbFldName());
        	if (field.getField().getTable().equals(t.getTable()))
            {
                if (field.getIndex() != -1)
                {
                    uploadCol(field, uploadData.get(row, field.getIndex()));
                }
            }
        }
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
        loadRow(t, row);
    	try
        {
            writeRow(t, row);
        }
        catch (UploaderException ex)
        {
            //ex.getCause().printStackTrace();
        	logDebug(ex.getMessage() + " (" + t.getTable().getName() + ", row "
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
        Set<WorkbenchRowImage> imagesToAttach = new HashSet<WorkbenchRowImage>();
        for (int i = imagesForRow.size() -1; i >= 0; i--)
        {
        	WorkbenchRowImage wri = imagesForRow.get(i);
        	if (getAttachToTable(wri) == t)
        	{
        		imagesToAttach.add(wri);
        		imagesForRow.remove(i);
        	}
        }
        attachImages(t, imagesToAttach);
    }

    /**
     * @param cls
     * @return an initialized instance of the appropriate OjbectAttachmentIFace implementation.
     */
    protected ObjectAttachmentIFace<? extends DataModelObjBase> getAttachmentObject(final Class<?> cls)
    {
    	ObjectAttachmentIFace<? extends DataModelObjBase> result = null;
    	
    	// Redesigned to handle anytime of Attachment upload
    	Exception ex       = null;
    	String   className = cls.getName() + "Attachment";
        try
        {
            Class<?> createClass = Class.forName(className);
            result = (ObjectAttachmentIFace<?>)createClass.newInstance();
            if (result != null)
            {
                ((DataModelObjBase)result).initialize();
            }            
        } catch (ClassNotFoundException e)
        {
            ex = e;
            e.printStackTrace();
        } catch (InstantiationException e)
        {
            ex = e;
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            ex = e;
            e.printStackTrace();
        }

        if (ex != null)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(Uploader.class, ex);
        }

    	return result;
    }
    
    /**
     * @param t
     * @param images
     * @throws UploaderException
     * 
     * Attaches images to the current record in t.
     */
    @SuppressWarnings("unchecked")
    protected void attachImages(final UploadTable t,
			final Set<WorkbenchRowImage> images) throws UploaderException
	{
		if (images.size() == 0)
		{
			return;
		}
		
    	AttachmentOwnerIFace<?> rec = (AttachmentOwnerIFace<?>) t
				.getCurrentRecord(0);
		
		//
    	if (rec == null && t instanceof UploadTableTree)
		{
			rec = (AttachmentOwnerIFace<?>)t.getParentRecord(0, t);
		}
		
		if (rec /*still*/ == null)
		{
			String msg = String.format(UIRegistry.getResourceString("Uploader.AttachToRecordMissing"), getRow() + 1, t.toString());
			addMsg(new SkippedAttachment(msg, getRow()));
			return; //maybe the row was not uploaded for some reason
		}
		
		
		
	    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
		boolean tblTransactionOpen = false;
		try
		{
			session.attach(rec);
			session.beginTransaction();
			tblTransactionOpen = true;
			Set<ObjectAttachmentIFace<?>> attachees = (Set<ObjectAttachmentIFace<?>>) rec.getAttachmentReferences();
			Vector<ObjectAttachmentIFace<?>> currentAttachees = new Vector<ObjectAttachmentIFace<?>>();
			int ordinal = 0;
			for (WorkbenchRowImage image : images)
			{
				Attachment attachment = new Attachment();
				attachment.initialize();
				attachment.setOrigFilename(image.getCardImageFullPath());
				attachment.setTableId(rec.getAttachmentTableId());
				File dummy = new File(image.getCardImageFullPath());
				if (!dummy.exists())
				{
					addMsg(new SkippedAttachment(String.format(getResourceString("Uploader.AttachFileMissing"), getRow()+1, image.getCardImageFullPath()),
									getRow()));
					continue;
				}
				String title = dummy.getName();
				if (title.length() > 64)
				{
					title = title.substring(0,64);
				}
				attachment.setTitle(title);
                attachment.setFileCreatedDate(ImageMetaDataHelper.getEmbeddedDateOrFileDate(dummy));

				ObjectAttachmentIFace<DataModelObjBase> oaif = (ObjectAttachmentIFace<DataModelObjBase>) getAttachmentObject(rec
						.getClass());
				if (oaif == null)
				{
					//this should never happen
					log.error("couldn't create attachment interface object for " + rec.getClass().getName());
					return;
				}
				oaif.setAttachment(attachment);
				oaif.setObject((DataModelObjBase) rec);
				oaif.setOrdinal(ordinal);
				attachees.add(oaif);
				currentAttachees.add(oaif);
			}
			BusinessRulesIFace busRule = DBTableIdMgr.getInstance()
					.getBusinessRule(rec.getClass());
			if (busRule != null)
			{
				busRule.beforeSave(rec, session);
			}
			session.saveOrUpdate(rec);
			if (busRule != null)
			{
				if (!busRule.beforeSaveCommit(rec, session))
				{
					session.rollback();
					throw new Exception("Business rules processing failed");
				}
			}
			for (ObjectAttachmentIFace<?> att : currentAttachees)
			{
					AttachmentUtils.getAttachmentManager()
							.setStorageLocationIntoAttachment(att.getAttachment(), true);
					
					att.getAttachment().storeFile(false); // false means do not display an error dialog
			}
			session.commit();
			tblTransactionOpen = false;
			if (busRule != null)
			{
				busRule.afterSaveCommit(rec, session);
			}
			for (ObjectAttachmentIFace<?> att : currentAttachees)
			{
				newAttachments.add(new UploadedRecordInfo(att.getAttachment().getId(), -1, 0, null, false, null,
							t.getWriteTable().getName()));
			}
		} catch (HibernateException he)
		{
			if (tblTransactionOpen)
			{
				session.rollback();
			}
			// XXX To avoid hibernate errors, it may be necessary to perform
			// a merge below but that REALLY slows down uploads.
			// (refresh is bad enough)
			{
				throw new UploaderException(he, UploaderException.ABORT_IMPORT);
			}
		} catch (Exception ex)
		{
			if (tblTransactionOpen)
			{
				session.rollback();
			}
			if (!(ex instanceof UploaderException))
			{
				throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
			} else
			{
				throw (UploaderException )ex;
			}
		} finally
		{
			session.close();
		}
	}
    
    /**
     * remove uploaded attachments
     */
    protected void undoAttachments()
    {
    	for (UploadedRecordInfo uri : newAttachments)
    	{
    		BasicSQLUtils.update("delete from " + uri.getTblName().toLowerCase() + "attachment where attachmentid = " + uri.getKey());
    		BasicSQLUtils.update("delete from attachment where attachmentid = " + uri.getKey()); //assuming attachments aren't being shared
    	}
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

        UploadTable root = getRootTable();
        for (UploadTable ut : uploadTables)
        {
            RecordSet rs = ut.getRecordSet(ut == root);
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
            UploadTable root = getRootTable();
        	for (RecordSet rs : recordSets)
			{
				BusinessRulesIFace busRule = DBTableIdMgr.getInstance()
						.getBusinessRule(RecordSet.class);
				if (busRule != null)
				{
					busRule.beforeSave(rs, session);
				}
				session.beginTransaction();
				try
				{
					session.save(rs);
					if (busRule != null)
					{
						if (!busRule.beforeSaveCommit(rs, session))
						{
							session.rollback();
							throw new Exception(
									"Business rules processing failed");
						}
					}
					session.commit();
					if (busRule != null)
					{
						busRule.afterSaveCommit(rs, session);
					}
					if (rs.getType() == RecordSet.GLOBAL && rs.getDbTableId() == root.getTable().getTableInfo().getTableId())
					{
						final RecordSet mergedRs = session.merge(rs);
		        		SwingUtilities.invokeLater(new Runnable() {

							/* (non-Javadoc)
							 * @see java.lang.Runnable#run()
							 */
							@Override
							public void run() {
								CommandAction cmd = new CommandAction(RecordSetTask.RECORD_SET, RecordSetTask.ADD_TO_NAV_BOX);
								cmd.setData(mergedRs);
								CommandDispatcher.dispatch(cmd);
							}
		        			
		        		});
					}
				} catch (Exception ex)
				{
					edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
					edu.ku.brc.exceptions.ExceptionTracker.getInstance()
							.capture(Uploader.class, ex);
					session.rollback();
				}
			}
        }
        catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(Uploader.class, ex);
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
        logDebug("keyPressed");
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
    
    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    //@Override
    public void keyReleased(KeyEvent e)
    {
        // nuthin
        logDebug("keyReleased");
   }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    //@Override
    public void keyTyped(KeyEvent e)
    {
        logDebug("KeyTyped");
        //see note in goToMsgWBCell() re addKeyListener()
//        int key = e.getKeyCode();
//        if (key == KeyEvent.VK_ENTER || 
//                key == KeyEvent.VK_TAB || 
//                key == KeyEvent.VK_DOWN ||
//                key == KeyEvent.VK_UP || (key == KeyEvent.VK_TAB && e.isShiftDown()) ||
//                key == KeyEvent.VK_HOME ||
//                key == KeyEvent.VK_END)
//        {
//            editInvalidCell(e);
//            e.consume();
//        }
    }

    /**
     * @param e
     * 
     * Moves to WB cell for appropriate InvalidValue and starts editing it.
     */
    protected void editInvalidCell(KeyEvent e)
    {
        logDebug("editing invalid cell");
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_TAB || key == KeyEvent.VK_DOWN)
            goToNextInvalidCell();
        else if (key == KeyEvent.VK_UP || (key == KeyEvent.VK_TAB && e.isShiftDown()))
            goToPrevInvalidCell();
        else if (key == KeyEvent.VK_HOME)
            goToFirstInvalidCell();
        else if (key == KeyEvent.VK_END)
            goToLastInvalidCell();
    }

    
 /*
  * Stuff to handling with preventing logins and disabling tasks during uploads.
  * 
  */
    
    /**
     * True if a lock has been overridden. Prevents multiple complaints about uploader lock.
     */
    //XXX Need to test to be sure lockout of other logins is maintaind if user who overrides opens her own upload session.
    protected static boolean isLockIgnored = false;
    
    /**
     * @return true if it is possible (i.e. user has permission, ??) to override an upload lock.
     * 
     * In this case, override means that the lock will remain in effect, and other users will be locked out,
     * but the current user will be not be locked out.
     */
    protected static boolean canOverrideLock()
    {
        return SpecifyUser.isCurrentUserType(SpecifyUserTypes.UserType.Manager); 
    }
    
    /**
     * @return true if the lock can be removed - i.e. unlocked.
     */
    protected static boolean canRemoveLock()
    {
        return canOverrideLock();
    }
    
    /**
     * Checks to see if a lock has been set for the upload task. 
     * 
     * @return true if no lock is present or lock is overridden. Else return false.
     *  
     */
    public static int checkUploadLock(final Taskable caller)
    {
        if (isLockIgnored)
        {
            return NO_LOCK;
        }
        if (!TaskSemaphoreMgr.isLocked(getLockTitle(), "WORKBENCHUPLOAD", TaskSemaphoreMgr.SCOPE.Discipline))
        {
            return NO_LOCK;
        }
        int result = lockUpload(caller, false);
        isLockIgnored = result == Uploader.LOCK_IGNORED;
        return result;
    }
    
    public static String getLockTitle()
    {
        return UIRegistry.getResourceString("Uploader.UploaderTask");
    }
    /**
     * Sets a lock for the upload task.
     * 
     * @return true is successful.
     */
    public static int lockUpload(final Taskable caller, boolean doLock)
	{
		boolean override = false;
		boolean unlocked = false;
		boolean isLocked = TaskSemaphoreMgr.isLocked(getLockTitle(),
				"WORKBENCHUPLOAD", TaskSemaphoreMgr.SCOPE.Discipline);
		USER_ACTION result = TaskSemaphoreMgr.lock(getLockTitle(),
				"WORKBENCHUPLOAD", null, SCOPE.Discipline, false,
				new UploadLocker(canOverrideLock(), canRemoveLock(), caller,
						doLock), false);
		if (result == USER_ACTION.Override)
		{
			override = true;
			unlocked = TaskSemaphoreMgr.unlock(getLockTitle(),
					"WORKBENCHUPLOAD", SCOPE.Discipline);
			if (unlocked && doLock)
			{
				result = TaskSemaphoreMgr.lock(getLockTitle(),
						"WORKBENCHUPLOAD", null, SCOPE.Discipline, false);
			}
		}
		if (doLock)
		{
			if (result == USER_ACTION.OK)
			{
				return LOCKED;
			}
			return LOCK_FAILED;
		}
		if (!doLock)
		{
			if (isLocked)
			{
				if (!override)
				{
					if (result == USER_ACTION.OK)
					{
						return LOCK_IGNORED;
					}
					return LOCKED;
				} else
				{
					if (unlocked)
					{
						return Uploader.LOCK_REMOVED;
					}
					return LOCKED;
				}
			} else
			{
				return NO_LOCK;
			}
		}
		return NO_LOCK;
	}
    
    /**
     * Unlocks the upload task.
     * 
     * @return true if successful.
     */
    public static boolean unlockUpload()
    {
        return TaskSemaphoreMgr.unlock(getLockTitle(), "WORKBENCHUPLOAD",  TaskSemaphoreMgr.SCOPE.Discipline);
    }

    protected static void setAppLock(final boolean lock)
    {
        SwingUtilities.invokeLater(new Runnable(){
            public void run()
            {
                JMenuBar menuBar  = (JMenuBar)UIRegistry.get(UIRegistry.MENUBAR);
                for (int m = 0; m < menuBar.getMenuCount(); m++)
                {
                    if (isSystemMenu(menuBar, m))
                    {
                        menuBar.getMenu(m).setEnabled(!lock);
                    }
                    else if (isTabsMenu(menuBar, m))
                    {
                        menuBar.getMenu(m).setEnabled(!lock);
                    }
                }
            }
        });
    }
    public static void lockApp()
    {
        setAppLock(true);
    }
    
    /**
     * @param menuBar
     * @param menu
     * @return true if menuBar.getMenu(menu) is the Specify "System" menu.
     */
    protected static boolean isSystemMenu(JMenuBar menuBar, int menu)
    {
        //very cheap and dirty
        return menu == 3;
    }
    
    /**
     * @param menuBar
     * @param menu
     * @return true if menuBar.getMenu(menu) is the Specify "Tabs" menu.
     */
    protected static boolean isTabsMenu(JMenuBar menuBar, int menu)
    {
        //cheap. dirty. trash.
        return menu == 4;
    }
    
    public static void unlockApp()
    {
        setAppLock(false);
    }

    /**
     * @return the wbSS
     */
    public WorkbenchPaneSS getWbSS()
    {
        return wbSS;
    }

    
    /**
     * @return list of tree classes involved in the upload.
     */
    protected List<Pair<UploadTableTree, Boolean>> getTreesToLock(final boolean defaultStatus)
    {
    	List<Pair<UploadTableTree, Boolean>> trees = new LinkedList<Pair<UploadTableTree, Boolean>>();
    	for (UploadTable t : uploadTables)
    	{
    		//does t represents the root of a tree?
    		if (Treeable.class.isAssignableFrom(t.getTblClass()) && ((UploadTableTree )t).getParent() == null)
    		{
    			trees.add(new Pair<UploadTableTree, Boolean>((UploadTableTree )t, defaultStatus));
    		}
    	}
    	return trees;
    }
    
    /**
     * @return true if all necessary locks were set.
     * 
     * Sets locks on Trees involved in the upload.
     */
    /**
     * @return
     */
    public boolean setAdditionalLocks()
    {
    	//It is actually not necessary to set these locks. 
    	return true;
    	
//    	List<Pair<UploadTableTree, Boolean>> trees = getTreesToLock(false);
//    	
//    	if (trees.size() == 0)
//    	{
//    		//nothing to lock
//    		return true;
//    	}
//    	    	
//    	boolean result = true;
//    	for (Pair<UploadTableTree, Boolean> ttp : trees)
//    	{
//    		UploadTableTree utt = ttp.getFirst();
//    		final String title = utt.getTable().getTableInfo().getTitle();
//        	TaskSemaphoreMgrCallerIFace lockCallback = new TaskSemaphoreMgrCallerIFace(){
//        	    @Override
//        		public TaskSemaphoreMgr.USER_ACTION resolveConflict(SpTaskSemaphore semaphore, 
//                        boolean previouslyLocked,
//                        String prevLockBy)
//        	    {
//        	    	UIRegistry.showLocalizedMsg("Uploader.AdditionalLockFailTitle", "Uploader.AdditionalLockFail", 
//        	    			title, prevLockBy, title);
//        	    	return TaskSemaphoreMgr.USER_ACTION.Error;
//        	    }
//        		
//        	};
//    		
//    		TaskSemaphoreMgr.USER_ACTION action = TaskSemaphoreMgr.lock(title, 
//    				utt.getTblClass().getSimpleName() + "TreeDef", null, TaskSemaphoreMgr.SCOPE.Discipline, false, lockCallback);
//    		if (action == TaskSemaphoreMgr.USER_ACTION.OK)
//    		{
//    			ttp.setSecond(true);
//    		}
//    		else
//    		{
//    			result = false;
//    			break;
//    		}
//    	}
//    	
//    	if (!result)
//    	{
//    		unlockTrees(trees);
//    	}
//    	
//    	additionalLocksSet = true;
//    	return result;
    }
    
    /**
     * @param trees the trees to unlock.
     */
    protected void unlockTrees(final List<Pair<UploadTableTree, Boolean>> trees)
    {
    	//No longer needed
    	
//    	for (Pair<UploadTableTree, Boolean> ttp : trees)
//    	{
//    		if (ttp.getSecond())
//    		{
//    			UploadTableTree utt = ttp.getFirst();
//    			//XXX do something if unlock fails.
//    			TaskSemaphoreMgr.unlock(utt.getTable().getTableInfo().getTitle(), utt.getTblClass().getSimpleName() + "TreeDef", 
//    				TaskSemaphoreMgr.SCOPE.Discipline);
//    		}
//    	}
    }
    
    /**
     * Frees additional locks set on trees involved in the upload.
     */
    public void freeAdditionalLocks()
    {
    	if (additionalLocksSet)
    	{
    		List<Pair<UploadTableTree, Boolean>> trees = getTreesToLock(true);
    		unlockTrees(trees);
    	}
    }
    
    /**
     * @param umsbp
     * 
     * Apply settings in umsbp to all tables.
     */
    public void applyMatchSettingsToAllTables(final UploadMatchSettingsBasicPanel umsbp)
    {
    	umsbp.applySettingToAll(uploadTables);
    }
    
    public void loadRecordToWb(final DataModelObjBase rec, final Workbench wb) throws Exception
    {
    	UploadTable t = null;
    	for (UploadTable ut : uploadTables)
    	{
    		if (ut.getTblClass().equals(rec.getClass()))
    		{
    			t = ut;
    		}
    	}
    	
    	t.loadRecord(rec, 0);
    	WorkbenchRow row = wb.addRow();
    	row.setRecordId(rec.getId());
    	for (UploadTable ut : uploadTables)
    	{
    		int seq = 0;
    		boolean isOneToMany = ut.getUploadFields().size() > 1 || ut.getTable().getName().equalsIgnoreCase("address");
    		for (Vector<UploadField> flds : ut.getUploadFields())
    		{
				if (ut.getCurrentRecord(seq) != null)
				{
					if (isOneToMany)
					{
						WorkbenchRowExportedRelationship wber = new WorkbenchRowExportedRelationship();
						wber.initialize();
						wber.setWorkbenchRow(row);
						row.getWorkbenchRowExportedRelationships().add(wber);
						wber.setTableName(ut.getTblClass().getSimpleName());
						wber.setSequence(seq);
						//wber.setIsDeleted(false);
						wber.setRecordId(ut.getCurrentRecord(seq).getId());
					}
					//wber.setRelationshipName() ??? - skipping it: assuming that for uploader a table is only one-to-many'ed once from a parent.
					for (UploadField fld : flds)
					{
						if (fld.getIndex() != -1)
						{
    						Object value = fld.getGetter().invoke(ut.getCurrentRecord(seq));
    						if (value != null)
    						{
    							WorkbenchTemplateMappingItem mi = wb.getMappingFromColumn((short )fld.getIndex());
    							WorkbenchDataItem di = new WorkbenchDataItem();
    							di.initialize();
    							di.setWorkbenchTemplateMappingItem(mi);
    							di.setWorkbenchRow(row);
    							di.setRowNumber(row.getRowNumber());
    							di.setRequired(mi.getIsRequired());
    							//XXX need to deal with formatting and stuff using fld.DBFieldInfo ...
    							di.setCellData(ut.getTextForFieldValue(fld, value, 0));
    							row.getWorkbenchDataItems().add(di);
    						}
    					}
    				}
    			}
    			seq++;
    		}
    	}
    }


	/**
	 * @return the currentOp
	 */
	public String getCurrentOp() 
	{
		return currentOp;
	}
	
     /* @param structureErrors
     * 
     * Display a dialog listing the 'structural' problems with the dataset
     * that prevent uploading.
     */
    public static void showStructureErrors(Vector<UploadMessage> structureErrors)
    {
        JPanel pane = new JPanel(new BorderLayout());
        JLabel lbl = createLabel(getResourceString("WB_UPLOAD_BAD_STRUCTURE_MSG") + ":");
        lbl.setBorder(new EmptyBorder(3, 1, 2, 0));
        pane.add(lbl, BorderLayout.NORTH);
        JPanel lstPane = new JPanel(new BorderLayout());
        JList lst = UIHelper.createList(structureErrors);
        lst.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
        lstPane.setBorder(new EmptyBorder(1, 1, 10, 1));
        lstPane.add(lst, BorderLayout.CENTER);
        pane.add(lstPane, BorderLayout.CENTER);
        CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(),
                getResourceString("WB_UPLOAD_BAD_STRUCTURE_DLG"),
                true,
                CustomDialog.OKHELP,
                pane);
        UIHelper.centerAndShow(dlg);
        dlg.dispose();
    }

    
}
