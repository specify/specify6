/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.plugins.sgr;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import edu.ku.brc.sgr.datamodel.BatchMatchResultSet;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.tasks.SGRTask;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandActionWrapper;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.Trash;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.PermissionIFace;

/**
 * @author ben
 *
 * @code_status Alpha
 *
 * Created Date: Jun 17, 2011
 *
 */
@SuppressWarnings("serial")
public class BatchResultsMgr extends NavBoxButton
{
    public static final long    PROGRESS_UPDATE_INTERVAL = 2000;
    private final String        sgrBatchItemsLabel;
    private SGRBatchScenario    scenario;
    private JMenuItem           stopProcessing;
    private JMenuItem           resumeProcessing;
    private ProgressUpdater     progressUpdater;
    private BatchMatchResultSet resultSet;

    public BatchResultsMgr(BatchMatchResultSet resultSet, PermissionIFace permissions)
    {
        this(resultSet, null, permissions);
    }

    public BatchResultsMgr(final BatchMatchResultSet resultSet, final SGRBatchScenario initialScenario, PermissionIFace permissions)
    {
        super(resultSet.name(), IconManager.getIcon("SGR", IconManager.STD_ICON_SIZE));
        
        UIRegistry.loadAndPushResourceBundle("specify_plugins");
        this.scenario = initialScenario;
        this.resultSet = resultSet;
        this.sgrBatchItemsLabel = UIRegistry.getResourceString("SGR_BATCH_ITEMS");
        
        setData(resultSet);
        doToolTip();
                
        //addDragDataFlavor(SGRTask.BATCH_RESULTS_FLAVOR);
        JPopupMenu popupMenu = new JPopupMenu();

        UIHelper.createLocalizedMenuItem(popupMenu, "SGR_EDIT_BATCH_RESULTS", "", null, true, 
                new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        final BatchResultPropertyEditor editor = 
                            new BatchResultPropertyEditor(BatchResultsMgr.this);
                        
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                editor.setVisible(true);
                            }
                        });
                    }
        });
        
        stopProcessing = 
            UIHelper.createLocalizedMenuItem(popupMenu, "SGR_STOP_PROCESSING", "", null, 
                scenario != null && scenario.isRunning(), 
                new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent arg0)
                    {
                        if (scenario != null && scenario.isRunning())
                            scenario.abort();
                        else
                        {
                            stopProcessing.setEnabled(false);
                            resumeProcessing.setEnabled(true);
                            setIcon(IconManager.getIcon("SGR", IconManager.STD_ICON_SIZE));
                        }
                    }
                }
            );

        resumeProcessing = 
            UIHelper.createLocalizedMenuItem(popupMenu, "SGR_RESUME_PROCESSING", "", null, 
                scenario == null, new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent arg0)
                    {
                        if (resultSet.getdbTableId().equals(Workbench.getClassTableId()))
                        {
                            scenario = WorkBenchBatchMatch.resumeScenario(resultSet);
                            scenario.start();
                            stopProcessing.setEnabled(true);
                            resumeProcessing.setEnabled(false);
                            setUpScenario();
                        }
                    }
                }
            );
        
        UIHelper.createLocalizedMenuItem(popupMenu, "SGR_SHOW_HISTOGRAM", "", null, true, 
            new ActionListener() 
            {
                @Override 
                public void actionPerformed(ActionEvent e)
                {
                    final SGRTask sgr = (SGRTask) ContextMgr.getTaskByClass(SGRTask.class);
                    sgr.addHistogram(resultSet, scenario, 0.5f);
                }
            });

        setUpScenario();
        
        if (true) //permissions == null || permissions.canDelete())
        {
            CommandAction delCmdAction = new CommandAction(SGRTask.SGR, 
                    SGRTask.DELETE_CMD_ACT, this);
            setDeleteCommandAction(delCmdAction);
            addDragDataFlavor(Trash.TRASH_FLAVOR);

            UIHelper.createLocalizedMenuItem(popupMenu, "DELETE", "", null, true, 
                new CommandActionWrapper(delCmdAction));
        }
        
        setPopupMenu(popupMenu);
        
        addActionListener(new CommandActionWrapper(
                new CommandAction(SGRTask.SGR, "selected_resultset")
                ));

        
        createMouseInputAdapter();
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        UIRegistry.popResourceBundle();
    }
    
    public void delete()
    {
        if (scenario != null)
            scenario.abort();
        resultSet.delete();
    }
    
    private void setUpScenario()
    {
        if (scenario != null)
        {
            scenario.addOnFinished(new Runnable()
            {
                @Override
                public void run()
                {
                    // scenario executes this code in the EventDispatch thread.
                    stopProcessing.setEnabled(false);
                    resumeProcessing.setEnabled(true);
                    BatchResultsMgr.this.scenario = null;
                    setIcon(IconManager.getIcon("SGR", IconManager.STD_ICON_SIZE));
                }
            });
            
            if (scenario.isRunning())
            {
                setIcon(IconManager.getIcon("SGRGEAR", IconManager.STD_ICON_SIZE));
                progressUpdater = new ProgressUpdater();
                progressUpdater.execute();
            }
            else
            {
                setIcon(IconManager.getIcon("SGR", IconManager.STD_ICON_SIZE));
            }
        }
    }
    
    private void doToolTip()
    {
        String toolTip = "" + resultSet.nItems() + " " + sgrBatchItemsLabel;
        setToolTip(toolTip);
    }
    
    private static class ScenarioNotRunning extends Exception {};
    
    private class ProgressUpdater extends SwingWorker<Void, Double>
    {
        
        @Override
        protected Void doInBackground() throws Exception
        {
            while (true)
            {
                // Because this thread is running concurrently with the EventDispatch
                // thread, scenario could get set to null at any moment if the scenario
                // finishes.  Using if(scenario != null) would only introduce a race
                // condition, so try / catch is used instead.
                try {
                    try { if (!scenario.isRunning()) throw new ScenarioNotRunning(); }
                    catch (NullPointerException e) { throw new ScenarioNotRunning(); }
                }
                catch (ScenarioNotRunning e)
                {
                    doToolTip();
                    return null;
                }

                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // Here no race condition exists since invokeLater executes this code
                        // on the EventDispatch thread so scenario cannot be nullified while this
                        // function is executing.
                        if (scenario == null) return;
                        String toolTip = "" + scenario.finishedRecords() + " / " + scenario.totalRecords();
                        BatchResultsMgr.this.setToolTip(toolTip);
                    }
                });
                
                Thread.sleep(PROGRESS_UPDATE_INTERVAL);
            }
        }
    }
}
