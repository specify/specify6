package edu.ku.brc.specify.tasks;

import com.google.common.io.PatternFilenameFilter;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpReport;
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

    public BatchEditTask() {
        this(BATCHEDIT, getResourceString(BATCHEDIT));
    }

    public BatchEditTask(final String name, final String title) {
        super(name, title);
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

    /**
     * register services at initialization.
     */
    @Override
    protected void registerServices()  {
        ContextMgr.registerService(new QueryBatchEditServiceInfo());
    }

   protected String getBatchEditType()
    {
        return BATCHEDIT;
    }

    @Override
    public void doCommand(CommandAction cmdAction) {
        super.doCommand(cmdAction);

        if (cmdAction.isType(getBatchEditType())) {
            processBatchEditCommands(cmdAction);

        } else if (cmdAction.isType(RecordSetTask.RECORD_SET) && cmdAction.isAction("Clicked")) {
            processRecordSetCommand(cmdAction);

        } else if (cmdAction.isType(TreeDefinitionEditor.TREE_DEF_EDITOR)) {
            //all we care to know is that a treeDefintion got changed somehow
            this.configurationHasChanged.set(true);
        } else if (cmdAction.isType(SchemaLocalizerDlg.SCHEMA_LOCALIZER)) {
            //XXX should check whether changed schema actually is the schema in use?
            // e.g. If German schema was saved when English is in use then ignore??
            this.configurationHasChanged.set(true);
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    if (SubPaneMgr.getInstance().getCurrentSubPane() == queryBldrPane) {
                        if (queryBldrPane != null) {
                            queryBldrPane.showingPane(true);
                        }
                    }
                }
            });
        }
    }

    protected void processBatchEditCommands(final CommandAction cmdAction) {
        if (cmdAction.isAction(QUERY_RESULTS_BATCH_EDIT)) {
            JTable dataTbl = (JTable) cmdAction.getProperties().get("jtable");
            if (dataTbl != null) {
                ResultSetTableModel rsm = (ResultSetTableModel) dataTbl.getModel();
                if (rsm.isLoadingCells()) {
                    UIRegistry.writeTimedSimpleGlassPaneMsg(UIRegistry.getResourceString("QB_NO_BATCH_EDIT_WHILE_LOADING_RESULTS"),
                            5000, null, null, true);
                    return;
                }
            }
            WorkbenchTask wbTask = (WorkbenchTask) ContextMgr.getTaskByClass(WorkbenchTask.class);
            wbTask.batchEditQueryResults(queryBldrPane.getQueryForBatchEdit(), (RecordSetIFace) cmdAction.getData(), queryBldrPane.getResultsCache());
            return;
        }

    }

    protected final class UpWBFilenameFilter implements FilenameFilter {

        public UpWBFilenameFilter() {}

        public boolean accept(File dir, String fileName) {
            return fileName.endsWith("_update_wb_datamodel.xml");
        }
    }

}
