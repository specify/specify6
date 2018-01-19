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

    protected final boolean isPermitted;

    public BatchEditTask() {
        this(BATCHEDIT, getResourceString(BATCHEDIT));
    }

    public BatchEditTask(final String name, final String title) {
        super(name, title);
        this.isPermitted = !AppContextMgr.isSecurityOn() || SpecifyUser.isCurrentUserType(SpecifyUserTypes.UserType.Manager);
        if (isPermitted) {
            CommandDispatcher.register(QueryTask.QUERY, this);
        } else {
            isVisible = false;
        }
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
        if (isPermitted) {
            ContextMgr.registerService(new QueryBatchEditServiceInfo());
        }
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
        htmlDesc.append("<h3>Welcome to Batch Editing</h3>"); //I18N
        htmlDesc.append("<b>Warning:</b> Batch Editing is a powerful method for improving data quality--it can change potentially " +
                "thousands of data records with just a few mouse clicks. Once edits are made and confirmed, chang" +
                "es will be permanent and they cannot be rolled back. We strongly recommend that you have a fresh " +
                "backup of your database, in case you need to restore your data.");
        htmlDesc.append("<p>Follow these instructions to edit your data:</p>");
        //htmlDesc.append("<ol>");
        htmlDesc.append("<p>1. To use the Batch Editor you must first choose the records you wish to edit. "
                + "Choose from the following options in the Side Bar:</p>");
        htmlDesc.append("<ul>");
        htmlDesc.append("<li>Create a new Query</li>");
        htmlDesc.append("<li>Open a saved Query</li>");
        htmlDesc.append("<li>Drop a Record Set onto a saved Query</li>");
        htmlDesc.append("</ul>");
        htmlDesc.append("<p>2. In the Query Builder, specify parameters and then click the <i>Search button</i> to execute the query.</p>");
        htmlDesc.append("<p>3. In Query Results, then click on the small Batch Editing (Pencils) icon in the upper right Query Re" +
                "sults title bar to begin your editing session. If you want to Batch Edit a subset of the Query Results, " +
                "first highlight those records, then click in the small Batch Editing icon at the top of the Query Results " +
                "screen.</p>");
        //htmlDesc.append("</ol>");
        htmlDesc.append("</body>");
        starterPane = new HtmlDescPane(name, this, htmlDesc.toString());
        //((HtmlDescPane)starterPane).setBackground(new Color(0xfd5875));
        return starterPane;
    }

}
