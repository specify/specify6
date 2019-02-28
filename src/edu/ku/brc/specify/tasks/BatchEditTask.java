package edu.ku.brc.specify.tasks;

import com.google.common.io.PatternFilenameFilter;
import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.core.*;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.tasks.subpane.HtmlDescPane;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.SpecifyUserTypes;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.rstools.RecordSetToolsIFace;
import edu.ku.brc.specify.tasks.subpane.qb.*;
import edu.ku.brc.specify.tools.schemalocale.SchemaLocalizerDlg;
import edu.ku.brc.specify.ui.db.ResultSetTableModel;
import edu.ku.brc.specify.ui.treetables.TreeDefinitionEditor;
import edu.ku.brc.ui.*;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

public class BatchEditTask extends QueryTask {
    private static final Logger log = Logger.getLogger(BatchEditTask.class);

    // Static Data Members
    public static final String BATCHEDIT                = "BatchEdit";

    protected List<String> batchEditables;

    protected Boolean isPermitted = null;

    public BatchEditTask() {
        this(BATCHEDIT, getResourceString(BATCHEDIT));
    }

    public BatchEditTask(final String name, final String title) {
        super(name, title);
        if (isPermitted()) {
            CommandDispatcher.register(QueryTask.QUERY, this);
        } else {
            isVisible = false;
        }
    }

    @Override
    public boolean isPermitted() {
        if (this.isPermitted == null) {
            //queryTask and workbenchTask will already be created because their defs precede BatchEdit's def in plugin_registry.xml
            Taskable queryTask = TaskMgr.getTask(QueryTask.QUERY);
            Taskable workbenchTask = TaskMgr.getTask(WorkbenchTask.WORKBENCH);
            boolean prereqTsksEnabled = queryTask != null && queryTask.isEnabled()
                    && workbenchTask != null && workbenchTask.isEnabled() && workbenchTask.getPermissions().canModify();
            this.isPermitted = prereqTsksEnabled
                    && (!AppContextMgr.isSecurityOn() ||
                    //SpecifyUser.isCurrentUserType(SpecifyUserTypes.UserType.Manager));
                    getPermissions().canView());
        }
        return this.isPermitted;
    }

    @Override
    protected void readOrgLists()  {
        File configDir = new File(UIRegistry.getDefaultWorkingPath() + File.separator + "config");
        String[] xmlFiles = configDir.list(new UpWBFilenameFilter());
        batchEditables  = new ArrayList<>();
        for (String f : xmlFiles) {
            batchEditables.add(f.replace("_update_wb_datamodel.xml", ""));
        }
        freqQueries = new ArrayList<>(batchEditables);
        extraQueries = new ArrayList<>();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPermEditorPanel()
     */
    @Override
//    public PermissionEditorIFace getPermEditorPanel() {
//        //return null to remove task from security ui
//        return null;
//    }
    public PermissionEditorIFace getPermEditorPanel()
    {
        return new BasicPermisionPanel("BatchEditTask.PermTitle", "BatchEditTask.PermEnable", null,
                null, null);
    }
    /**
     * @return the permissions array
     */
    @Override
    protected boolean[][] getPermsArray()
    {
        return new boolean[][] {{true, true, true, true},
                {false, false, false, false},
                {false, false, false, false},
                {false, false, false, false}};
    }


    @Override
    protected boolean isLoadableQuery(SpQuery query) {
        return super.isLoadableQuery(query) && isBatchEditableQuery(query);
    }

    protected boolean isBatchEditableQuery(SpQuery query) {
        return batchEditables.indexOf(DBTableIdMgr.getInstance().getInfoById(query.getContextTableId()).getShortClassName().toLowerCase()) >= 0;
    }

    @Override
    protected boolean showQueryCreator(final String tblName) {
        return batchEditables.indexOf(tblName.toLowerCase()) >= 0;
    }

    @Override
    protected void registerServices() {
        if (isPermitted()) {
            ContextMgr.registerService(new QueryBatchEditServiceInfo());
        }
    }

    @Override
    public boolean isViewable() {
        return isPermitted();
    }

    protected final class UpWBFilenameFilter implements FilenameFilter {

        public UpWBFilenameFilter() {}

        public boolean accept(File dir, String fileName) {
            return fileName.endsWith("_update_wb_datamodel.xml");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane() {
        String imgsrc = IconManager.getIcon("BatchEdit").toString();

        StringBuilder htmlDesc = new StringBuilder();


        htmlDesc.append("<!doctype html>");
        htmlDesc.append("<html>");
        htmlDesc.append("<head>");
        htmlDesc.append("<meta charset=\"utf-8\">");
        htmlDesc.append("<title>Untitled Document</title>");
        htmlDesc.append("</head>");
        htmlDesc.append("");
        htmlDesc.append("<body>");
        htmlDesc.append("<h3><strong>Batch Editing</strong>&nbsp;&nbsp;&nbsp;");
        htmlDesc.append("<img src='" + imgsrc + "' alt=\"Batch Edit Pencils\" width=\"32\" height=\"32\"></h3>");
        //htmlDesc.append("<h3>Batch  Editing</h3>");
        //htmlDesc.append("<img src='" + imgsrc + "' alt=\"Batch Edit Pencils\" width=\"32\" height=\"32\">");
        htmlDesc.append("<p>Batch Editing is a powerful data  management tool for improving data quality and consistency, it can modify  thousands of data records with a few clicks of the mouse.<br></strong></p>");
        htmlDesc.append("<p><strong>Changes made with Batch Editing are irreversible</strong><strong>; there is  no roll-back function.</strong> <br></p>");
        htmlDesc.append("<p> It is critically important to have a  recent backup of your database available in case massive, unintended changes  are made to it, and you need to restore your database from a backup copy. Batch  Editing is intended for experienced Specify users who understand the logical  relationships among Specify's data tables. Changes made to records in one table  can have cascading effects on records in linked tables. For example, in  databases that use shared Collecting Events, a change to a data field in the  Locality table will be applied to all Collecting Event records linked to that  Locality.&nbsp;That change, in turn, will apply to all Collection Object  records linked to those Collecting Events.&nbsp; </p>");
        htmlDesc.append("<p>");
        htmlDesc.append("  In the above example, it would be important  to understand whether you want to make a change to a Locality record for all Collecting  Events associated with it, and in turn, all of the Collection Object records  associated with those Collecting Events, or if you need to make a more limited  change to Locality information for a particular Collecting Event—affecting only  that Collecting Event and the Collection Object records associated with it.  Batch Editing can change thousands of records permanently, be sure to  understand the potential impact of batch changes based on the logical  relationships among Specify's tables.&nbsp;For guidance on how to use Batch  Editing, see the demonstration video on the Consortium's web site.</p>");
        htmlDesc.append("<p><strong>Using the Batch Editor:</strong></p>");
        htmlDesc.append("<br>");
        htmlDesc.append("Batch Editing begins with formulating  and running a query on one of the five data tables: Collection Object,  Collecting Event, Locality, Preparation, or Agent.&nbsp;Fields from other  tables can be added by building out the query specification in the Query  Builder.");
        htmlDesc.append("<ol start=\"1\" type=\"1\">");
        htmlDesc.append("  <li>Select records for Batch Editing by choosing from one       of these options from the Side Bar:");
        htmlDesc.append("  <ol start=\"1\" type=\"1\">");
        htmlDesc.append("    <li>Create Query </li>");
        htmlDesc.append("    <li>Saved Queries, or </li>");
        htmlDesc.append("    <li>Record Sets--drag and drop a Record Set onto a Saved        Query to retrieve its contents </li>");
        htmlDesc.append("  </ol> </li>");
        htmlDesc.append("  <li>In the Query Builder, add any needed tables, fields,       and parameters, then click <em>Search</em> to execute the query. If you       would like to use those query customizations again, save and name the       Query template--otherwise just run it. </li>");
        htmlDesc.append("  <li>In the Query Results window, click the Batch Editing       pencils icon in the upper right of the title bar to begin an editing       session. If you want to Batch Edit a subset of the records resulting from       your Query, first highlight those records in the Query Results window and <em>then</em> click on the Batch Editing       pencils icon at the top right of that screen, the highlighted records will       then be put into the Batch Editing window.&nbsp;A maximum of 7,000 records       returned from a query can be brought into the Batch Editor. </li>");
        htmlDesc.append("  <li>Once edits are complete, click the <em>Apply</em> button       at the lower right of the workspace, check for errors reported in the       Batch Editor log window. If no errors are reported, click <em>Save</em> to       complete the edit. Or click <em>Cancel</em> to stop the finalization process       and to continue editing. </li>");
        htmlDesc.append("</ol>");
        htmlDesc.append("");
        htmlDesc.append("	<p><strong>Batch Editing Notes:</strong> </p>");
        htmlDesc.append("<ol start=\"1\" type=\"1\">");
        htmlDesc.append("  <li>Closing a Batch Edit tab (window) or shutting down       Specify with a Batch Edit session open before changes are applied and       saved, will cancel the changes; edits will not be applied to records.       &nbsp; </li>");
        htmlDesc.append("  <li>Batch Editing does not lock out other users from       independently viewing and modifying records that are included within a       Batch. Changes made to records included within an open Batch Edit session,       when saved, will overwrite simultaneous changes made to those records independently       by other users. For that reason with multiple database users, it is best practice       to complete a Batch Edit session before leaving Specify unattended for an       extended period; also not to bring a large number of unneeded records into       a Batch Edit session. </li>");
        htmlDesc.append("  <li>To protect data integrity, Batch Edit sessions will       cancel and changes will not be applied, if the <em>Save</em> or the <em>Cancel</em> button is not clicked within three minutes after pressing <em>Apply</em>. </li>");
        htmlDesc.append("  <li>Data fields with text shown in gray are not editable in       Batch Editing mode and are displayed as read-only.&nbsp; For example, GUID       field values are automatically and permanently assigned to records and       cannot be edited. </li>");
        htmlDesc.append("</ol>");
        htmlDesc.append("</body>");
        htmlDesc.append("</html>");


        starterPane = new HtmlDescPane(name, this, htmlDesc.toString());
        //((HtmlDescPane)starterPane).setBackground(new Color(0xfd5875));
        return starterPane;
    }

}
