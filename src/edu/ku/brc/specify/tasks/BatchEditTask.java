package edu.ku.brc.specify.tasks;

import com.google.common.io.PatternFilenameFilter;
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
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.UIRegistry;
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
                    && (!AppContextMgr.isSecurityOn() || SpecifyUser.isCurrentUserType(SpecifyUserTypes.UserType.Manager));
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
    public PermissionEditorIFace getPermEditorPanel() {
        //return null to remove task from security ui
        return null;
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
        //StringBuilder htmlDesc = new StringBuilder("<body style=\"background-color:#fd5875\">");
        StringBuilder htmlDesc = new StringBuilder();
        //htmlDesc.append("<style>ol{margin: 0;}</style>");
        htmlDesc.append("<body font face=\"verdana\">");
        htmlDesc.append("<h2>Batch Editing with Specify</h2>"); //I18N
        htmlDesc.append("<p></p>");
        htmlDesc.append("<b>Caution:</b> Batch Editing is a powerful tool for improving data quality, but it can permanently change " +
                "thousands of data records with a few mouse clicks.");
        htmlDesc.append("<p> After editing is complete and desired changes are applied and confirmed, data " +
                "records will be permanently modified with new data values. Changes made with Batch Editing are not reversible. " +
                "They cannot be rolled back.</p>");
        htmlDesc.append("<p>Specify users should have a basic understanding of the relationships among the Collection Object, Collecting Event, and Locality data tables before querying on one of those tables as the focus of an editing session.</p>");
        htmlDesc.append("<p>Once changes are applied, Specify does not retain a copy of your original data. We strongly recommend making a fresh backup of your database, in case a catastrophic error is made and you need to roll back to the original data by restoring the entire database.</p>");
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
        htmlDesc.append("<p>Closing the Batch Editing Tab, or shutting down Specify anytime during the Batch Editing process before changes are committed and saved, will cancel the batch and no edits will be applied.  Also, Batch Edit sessions are cancelled 180 seconds after pressing the Apply button, if Save or Cancel is not clicked.</p>");
        //htmlDesc.append("</ol>");
        htmlDesc.append("</body>");
        starterPane = new HtmlDescPane(name, this, htmlDesc.toString());
        //((HtmlDescPane)starterPane).setBackground(new Color(0xfd5875));
        return starterPane;
    }

}
