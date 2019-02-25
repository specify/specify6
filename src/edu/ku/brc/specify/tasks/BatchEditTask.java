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
        //htmlDesc.append("<title>Untitled Document</title>");
        htmlDesc.append("</head>");
        htmlDesc.append("");
        htmlDesc.append("<body>");
        htmlDesc.append("<h2><strong>Batch Editing</strong></u><strong> &nbsp;&nbsp;&nbsp;</strong>");
        htmlDesc.append("<img src='" + imgsrc + "' alt=\"Batch Edit Pencils\" width=\"32\" height=\"32\"></h2>");
        htmlDesc.append("  <br>");
        htmlDesc.append("</p>");
        htmlDesc.append("<p> Batch Editing is a powerful data management tool for improving data  quality and consistency; it can modify thousands of data records with a few clicks  of the mouse. </p>");
        htmlDesc.append("<p><strong>Changes  made with Batch Editing are permanent and irreversible</strong>. </p>");
        htmlDesc.append("<p>It is critically important to have a recent backup  of your database in case massive, unintended changes are made to your database,  and reloading original data from a backup copy is needed to recover. <br>");
        htmlDesc.append("  Batch Editing is intended  for experienced Specify users who understand the logical relationships among  Specify's data tables.&nbsp; Batch Editing  works by starting with one of five Specify data tables: Collection Object,  Collecting Event, Locality, Preparation, and Agent.<br>");
        htmlDesc.append("</p>");
        htmlDesc.append("<p> Changes made to records  in one table can have cascading effects to records in linked tables. For  example, in databases that use shared Collecting Events, a change to a data  field in the Locality table will be applied to all Collecting Event records  linked to that Locality.&nbsp; That change, in  turn, will apply to all Collection Objects linked to those Collecting Events.   <br>");
        htmlDesc.append("</p>");
        htmlDesc.append("<p> In the above  example, it is important to understand before using the Batch Editor, whether  to make a change to the Locality for all Collection Objects in your database  associated with it, or whether to make a smaller change to a particular  Collecting Event—only for its subset of associated Collection Object  records.&nbsp; Or you might want to update the  Locality record linked to that particular Collecting Event, but not for other  Collecting Events associated with that Locality. Batch Editing can  change thousands of records permanently in seconds, be sure to understand the  potential impact of batch changes.&nbsp; For  guidance on how to use Batch Editing, see the demonstration video on the  Consortium's web site.<br></p>");
        htmlDesc.append("<p> Batch Editing  begins with formulating and running a query on one of the five base tables to  retrieve records for possible editing.&nbsp;  You can add fields from other tables by building out the query  specification in the Query Builder.</p>");
        htmlDesc.append("<ol start=\"1\" type=\"1\">");
        htmlDesc.append("  <li>Select       records for Batch Editing choosing one of these options from the Side Bar: </li>");
        htmlDesc.append("  <ol start=\"1\" type=\"1\">");
        htmlDesc.append("    <li>Create Query </li>");
        htmlDesc.append("    <li>Saved Queries,        or </li>");
        htmlDesc.append("    <li>Record Sets--drag        and drop a Record Set onto a Saved Query to retrieve its contents</li>");
        htmlDesc.append("  </ol>");
        htmlDesc.append("</ol>");
        htmlDesc.append("<ol start=\"2\" type=\"1\">");
        htmlDesc.append("  <li>In the Query       Builder, add any needed tables, fields, and parameters, then click <em>Search</em> to execute the query. If you would like to use those query customizations       again, save and name the Query template, otherwise just run it.</li>");
        htmlDesc.append("</ol>");
        htmlDesc.append("<ol start=\"3\" type=\"1\">");
        htmlDesc.append("  <li>In the Query       Results window, click the Batch Editing pencils icon in the upper right of       the title bar to begin an editing session. If you want to Batch Edit a       subset of the records resulting from your Query, first highlight those       records in the Query Results window and then click on the Batch Editing       pencils icon at the top right of that screen, the highlighted records will then be put into the       Batch Editing window.  If specific       records are not chosen, a maximum of 7,000 records returned from a query       will be brought into the Batch Editor. </li>");
        htmlDesc.append("</ol>");
        htmlDesc.append("<ol start=\"4\" type=\"1\">");
        htmlDesc.append("  <li>Once edits       are complete, click the <em>Apply</em> button at the lower right of the       workspace, check for errors reported in the Batch Edit process log window,       if no errors are reported, click <em>Save</em> to complete the edit. Or click <em>Cancel</em> to continue editing. </li>");
        htmlDesc.append("</ol>");
        htmlDesc.append("<p>Batch Editing Notes:</p>");
        htmlDesc.append("<ol>");
        htmlDesc.append("  <li>Closing  a Batch Edit tab or shutting down Specify with a Batch Edit session open before  changes are applied and saved will cancel the changes; edits will not be  applied to the records.  </li>");
        htmlDesc.append("  <li>Batch  Editing does not lock out other users from independently viewing and modifying  records that are included within a Batch. Changes made to records included  within an open Batch Edit session will overwrite any changes made independently  by other users.  For that reason, it is  best to complete a Batch Edit session before leaving Specify unattended for an  extended period, and not to bring a large number of unneeded records into a  Batch Edit session. </li>");
        htmlDesc.append("  <li>To  protect data integrity, Batch Edit sessions will cancel and changes will not be  applied, if the <em>Save</em> or <em>Cancel</em> button are not clicked three  minutes after pressing <em>Apply</em>. </li>");
        htmlDesc.append("  <li>Data  fields with text shown in gray are not editable in Batch Editing mode, and are  displayed for viewing only.  For example,  GUID field values are automatically permanently assigned to records and are not  changeable.</li>");
        htmlDesc.append("</ol>");
        htmlDesc.append("<p>&nbsp;</p>");
        htmlDesc.append("</body>");
        htmlDesc.append("</html>");

    /*    htmlDesc.append("<head>");
        htmlDesc.append("<meta charset=\"utf-8\">");
        htmlDesc.append("<title>Batch Editing</title>");
        htmlDesc.append("</head>");
        htmlDesc.append("<style ");
        htmlDesc.append("div {margin-top: 60px; margin-bottom: 60px; margin-right: 60px; margin-left: 60px;}");
        htmlDesc.append(">");
        htmlDesc.append("</head>");


        htmlDesc.append("<body font face=\"verdana\">");
        htmlDesc.append("<div>");
        htmlDesc.append("<h2><strong>Batch Editing with Specify &nbsp;&nbsp;&nbsp;&nbsp; </strong>");
		htmlDesc.append("<img src='" + imgsrc + "' alt=\"Batch Edit Pencils\" width=\"32\" height=\"32\"><img src='" + imgsrc + "' alt=\"Batch Edit Pencils\" width=\"32\" height=\"32\"><img src='" + imgsrc + "' alt=\"Batch Edit Pencils\" width=\"32\" height=\"32\"><img src='" + imgsrc + "' alt=\"Batch Edit Pencils\" width=\"32\" height=\"32\"></h2>");
        htmlDesc.append("<strong>Caution: Batch Editing is a powerful tool for enhancing data quality--it can permanently modify thousands of data records with a few mouse clicks. When editing is complete and changes are confirmed, data records will be permanently updated with your new values.");
        htmlDesc.append("<p><u>Changes made with Batch Editing are not reversible. Specify does not preserve copies of your original data values.</u>");
        htmlDesc.append("<p>We strongly recommend having a fresh backup of your database in case unintended changes are made and reloading the last backup file is needed to restore the original data. </strong>");
        htmlDesc.append("<p>Specify users should have a basic understanding of the relationships among the Collection Object, Collecting Event, Locality, Preparation and Agent data tables before Batch Editing. &nbsp;The Batch Edit process begins with a Query on one of those tables.</p>");
        //htmlDesc.append("<p><strong> </strong></p>");
        htmlDesc.append("<ol>");
        //htmlDesc.append("<p>");
        htmlDesc.append("<li> First select the records to be edited. Start by choosing from these options using the Side Bar: </li>");
        htmlDesc.append("<ol><li> Create a new Query</li>");
        htmlDesc.append("<li>Open a saved Query, or</li>");
        htmlDesc.append("<li>Drop a Record Set onto a saved Query</li></ol>");
        htmlDesc.append("<br>");
        htmlDesc.append("<li>Next in the Query Builder, add any needed tables, fields, or parameters and then click the <em>Search</em> button to execute the query. Save the Query if you made changes and would like to use the template again. </li>");
        htmlDesc.append("<br>");
        htmlDesc.append("<li>Then in Query Results, click on the other Batch Editing pencils icon in the upper right of the Query Results title bar to begin an editing session. If you want to Batch Edit only a subset of the Query Results, first highlight those records in the Query Results window and then click on the Batch Editing pencils icon at the top right of that screen--only those records will be brought into the Batch Editing window. </li>");
        htmlDesc.append("<br>");
        htmlDesc.append("<li>Once edits are complete, click on the <em>Apply</em> button at the lower right of the workspace, check for any errors reported in the Batch Edit process log window, then click <em>Save</em> to complete the edit, or <em>Cancel</em> to continue editing. </li>");
        htmlDesc.append("</ol>");
        htmlDesc.append("Closing the Batch Editing Tab or shutting down Specify while Batch Editing before changes are committed and saved will cancel a batch and edits will not be applied. It is important not to leave a Batch Edit session open and unfinished if other users might independently edit records contained within the batch. And it is a best practice to complete a batch before leaving Specify for an extended period. Lastly, Batch Edit sessions will be cancelled three minutes after pressing the <em>Apply</em> button, if <em>Save</em> or <em>Cancel</em> is not clicked.");
        htmlDesc.append("</p></div>");
        htmlDesc.append("</body>");
*/

        /*
        htmlDesc.append("<img src='" + imgsrc + "'/>");
        htmlDesc.append("<h2>Batch Editing with Specify</h2>"); //I18N
        htmlDesc.append("<p></p>");
        htmlDesc.append("<b>Caution:</b> Batch Editing is a powerful tool for improving data quality, but it can permanently change " +
                "thousands of data records with a few mouse clicks.");
        htmlDesc.append("<p> After editing is complete and desired changes are applied and confirmed, data " +
                "records will be permanently modified with new data values. Changes made with Batch Editing are not reversible. " +
                "They cannot be rolled back.</p>");
        htmlDesc.append("<p>Specify users should have a basic understanding of the relationships among the Collection Object, Collecting Event, and Locality data tables before querying on one of those tables as the focus of an editing session.</p>");
        htmlDesc.append("<p>Once changes are applied, Specify does not retain a copy of your original data. We strongly recommend making a fresh backup of your database in case a catastrophic error is made and you need to roll back to the original data by restoring the entire database.</p>");
        htmlDesc.append("<p>Use these steps to select and Batch Edit data:</p>");
        //htmlDesc.append("<ol>");
        htmlDesc.append("<p>1. First select the records to be edited. Choose from the following options using the Side Bar:</p>");
        htmlDesc.append("<ul>");
        htmlDesc.append("<li>Create a new Query</li>");
        htmlDesc.append("<li>Open a saved Query, or</li>");
        htmlDesc.append("<li>Drop a Record Set onto a saved Query</li>");
        htmlDesc.append("</ul>");
        htmlDesc.append("<p>2. Next, in the Query Builder, add any needed fields or parameters and then click the <i>Search</i> button to execute the query. Save the Query if you made changes and plan to use it again.</p>");
        htmlDesc.append("<p>3. Then, in Query Results, click on the other Batch Editing pencils icon in the upper right of the Query Results title bar to begin an editing session. If you want to Batch Edit only a subset of the Query Results, first highlight those records in the Query Results window and then click on the Batch Editing pencils icon at the top right of that screen--only those records will be brought into the Batch Editing window.</p>");
        htmlDesc.append("<p>4. To finish, once edits are made, click on the <i>Apply</i> button at the lower right of the workspace, check for any errors reported in the Batch Edit process log window, then click <i>Save</i> to complete the edit, or <i>Cancel</i> to return to editing.</p>");
        htmlDesc.append("<p>Closing the Batch Editing Tab or shutting down Specify anytime during the Batch Editing process before changes are committed and saved, will cancel the batch and no edits will be applied.  Also, Batch Edit sessions are cancelled 180 seconds after pressing the <i>Apply</i> button, if <i>Save</i> or <i>Cancel</i> is not clicked.</p>");
        //htmlDesc.append("</ol>");
        htmlDesc.append("</body>");
        */

        starterPane = new HtmlDescPane(name, this, htmlDesc.toString());
        //((HtmlDescPane)starterPane).setBackground(new Color(0xfd5875));
        return starterPane;
    }

}
