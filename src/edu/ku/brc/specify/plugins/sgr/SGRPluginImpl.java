/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.plugins.sgr;

import static edu.ku.brc.ui.UIHelper.createIconBtn;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.sgr.MatchResults;
import edu.ku.brc.sgr.SGRMatcher;
import edu.ku.brc.sgr.SGRMatcher.Factory;
import edu.ku.brc.sgr.SGRRecord;
import edu.ku.brc.sgr.datamodel.BatchMatchResultSet;
import edu.ku.brc.sgr.datamodel.DataModel;
import edu.ku.brc.sgr.datamodel.MatchConfiguration;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.tasks.SGRTask;
import edu.ku.brc.specify.tasks.subpane.wb.SGRFormPane;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPaneSS;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPaneSS.PanelType;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.WorkBenchPluginIFace;
import edu.ku.brc.ui.tmanfe.SpreadSheet;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 26, 2010
 *
 */
public class SGRPluginImpl implements WorkBenchPluginIFace
{
    private SpreadSheet        spreadSheet;
    private Workbench          workbench;

    private SGRMatcher         matcher;
    private final JPopupMenu   popupMenuGrid;
    private final JPopupMenu   popupMenuForm;
    private MatchConfiguration matcherConfiguration;
    private WorkbenchColorizer colorizer;
    private WorkbenchPaneSS    workbenchPaneSS;

    private SubPaneIFace       subPane;
    private Workbench2SGR      workbench2SGR;
    private List<JButton>      ssButtons;
    private List<JButton>      formButtons;
    protected Integer nResults;
   
    public SGRPluginImpl()
    {
        super();

        popupMenuGrid = createPopupMenuForGrid();
        popupMenuForm = createPopupMenuForForm();
    }

    private JPopupMenu createPopupMenuForGrid()
    {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem mi = new JMenuItem();

        mi.setText("Select Matcher...");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) { selectMatcher(); }
        });
        menu.add(mi);
        
//        mi = new JMenuItem();
//        mi.setText("Stop coloring.");
//        mi.addActionListener(new ActionListener()
//        {
//            
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                colorizer.stopColoring();
//                workbenchPaneSS.showHideSgrCol(false);
//            }
//        });
//        menu.add(mi);
       
        return menu;
    }
    
    private void selectMatcher()
    {
        List<MatchConfiguration> mcs = DataModel.getMatcherConfigurations();
        
        ChooseFromListDlg<MatchConfiguration> dlg = 
            new ChooseFromListDlg<MatchConfiguration>((Frame)UIRegistry.get(UIRegistry.FRAME), 
                    "Choose Matcher", mcs);
        
        UIHelper.centerAndShow(dlg);
        if (!dlg.isCancelled())
        {
            SGRTask sgrTask = (SGRTask) ContextMgr.getTaskByClass(SGRTask.class);
            sgrTask.setMatcher(dlg.getSelectedObject());
            refreshFormPane();
        }
    }    
    
    private JPopupMenu createPopupMenuForForm()
    {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem mi = new JMenuItem();

        mi.setText("Select Matcher...");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) { selectMatcher(); }
        });
        menu.add(mi);
        
        mi = new JMenuItem();
        mi.setText("Set number of results...");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                List<Integer> choices = ImmutableList.of(1, 2, 5, 10, 20);
                
                ChooseFromListDlg<Integer> dlg = 
                    new ChooseFromListDlg<Integer>((Frame)UIRegistry.get(UIRegistry.FRAME), 
                            "Choose", choices);
                
                UIHelper.centerAndShow(dlg);
                if (!dlg.isCancelled())
                {
                    nResults = dlg.getSelectedObject();
                    refreshFormPane();
                }
            }
        });
        menu.add(mi);
       
        return menu;
    }
    
    public void setMatcherConfiguration(MatchConfiguration mc)
    {
        matcherConfiguration = mc;

        Factory matcherFactory = matcherConfiguration.createMatcherFactory();
        matcherFactory.docSupplied = true;
        matcherFactory.debugQuery = true;
        try
        {
            matcher = matcherFactory.build();
        } catch (MalformedURLException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#process(java.util.List)
     */
    @Override
    public boolean process(final List<WorkbenchRow> rows)
    {
        if (rows.size() < 1)
            return true;
        
        UIRegistry.loadAndPushResourceBundle("specify_plugins");
        
        if (!isReady())
        {
            String msg = "You must select a match configuration before using SGR in the Workbench.";
            
            CustomDialog dlg = new CustomDialog((Frame)UIRegistry.get(UIRegistry.FRAME), 
                    "No Matcher Selected", true, CustomDialog.OK_BTN, new JLabel(msg));
            
            UIHelper.centerAndShow(dlg);
            return true;
        }
        
        final WorkbenchRow row = rows.get(0);
        final MatchResults results =  doQuery(row);
   
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                SGRResultsChooser chooser = new SGRResultsChooser(
                        (Frame)UIRegistry.getTopWindow(), row,
                        results, spreadSheet, new Finished());
                
                //chooser.createUI();
                chooser.setVisible(true); // Centers 
            }
        });
        UIRegistry.popResourceBundle();        
        
        return true;
    }
    
    public boolean isReady()
    {
        return matcherConfiguration != null;
    }
  
    private class Finished implements Function<Void, Void>
    {
        @Override
        public Void apply(Void arg0)
        {
            return null;
        }
    }
    
    public MatchResults doQuery(WorkbenchRow row)
    {
        assert isReady();
        SGRRecord doc = workbench2SGR.row2SgrRecord(row);
        return matcher.match(doc.asMatchable(), nResults);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#setSubPanel(edu.ku.brc.af.core.SubPaneIFace)
     */
    @Override
    public void setSubPanel(SubPaneIFace parent)
    {
        this.subPane = parent;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#setSpreadSheet(edu.ku.brc.ui.tmanfe.SpreadSheet)
     */
    @Override
    public void setSpreadSheet(SpreadSheet ss)
    {
        this.spreadSheet = ss;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#setWorkbench(edu.ku.brc.specify.datamodel.Workbench)
     */
    @Override
    public void setWorkbench(final Workbench workbench)
    {
        this.workbench = workbench;
        if (workbench != null)
        {
            workbench2SGR = new Workbench2SGR(workbench);
        }
        
        if (colorizer != null)
        {
            colorizer.cleanup();
        }
        
        colorizer = new WorkbenchColorizer(workbench, spreadSheet);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#getMissingFieldsForPlugin()
     */
    @Override
    public List<String> getMissingFieldsForPlugin()
    {
//        ArrayList<String> list = new ArrayList<String>();
//        if (collNumIndex == null && fieldNumIndex == null)
//        {
//            return list;
//        }
//        
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#shutdown()
     */
    @Override
    public void shutdown()
    {
        if (colorizer != null)
        {
            colorizer.cleanup();
        }
        
        workbench = null;
        workbenchPaneSS = null;
    }

    @Override
    public void setButton(JButton btn)
    {

    }

    private void doPopup(MouseEvent e, JPopupMenu popup)
    {
        if (!popup.isShowing()) 
            popup.show(e.getComponent(), e.getX(), e.getY());
    }
    
    public WorkbenchColorizer getColorizer()
    {
    	return colorizer;
    }

	@Override
	public void setWorkbenchPaneSS(WorkbenchPaneSS wbpss)
	{
		workbenchPaneSS = wbpss;
	}

    @Override
    public Collection<JButton> getSSButtons()
    {
        if (ssButtons != null)
            return ssButtons;
        
        if (workbenchPaneSS.getTask() instanceof SGRTask)
        {
            JButton button = createIconBtn("SGR", IconManager.IconSize.Std20, null, false,
                    new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            workbenchPaneSS.showPanel(PanelType.Form);
                        }
                    });
            
            button.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)            
                {
                    super.mousePressed(e);
                    if (e.isPopupTrigger())
                        doPopup(e, popupMenuGrid);
                }
                
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    super.mouseReleased(e);
                    if (e.isPopupTrigger())
                        doPopup(e, popupMenuGrid);
                }
            });
            
            button.setEnabled(true);
            ssButtons = ImmutableList.of(button);
        }
        else
        {
            ssButtons = ImmutableList.of();
        }
        
        return ssButtons;
    }

    @Override
    public Collection<JButton> getFormButtons()
    {
        if (formButtons != null)
            return formButtons;

        if (workbenchPaneSS.getTask() instanceof SGRTask)
        {
            JButton button = createIconBtn("SGR", IconManager.IconSize.Std20, null, false,
                    new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            refreshFormPane();
                        }
                    });
            
            button.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)            
                {
                    super.mousePressed(e);
                    if (e.isPopupTrigger())
                        doPopup(e, popupMenuForm);
                }
                
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    super.mouseReleased(e);
                    if (e.isPopupTrigger())
                        doPopup(e, popupMenuForm);
                }
            });
            
            button.setEnabled(true);
            formButtons = ImmutableList.of(button);
        }
        else
        {
            formButtons = ImmutableList.of();
        }            
        return formButtons;
    }

    private void refreshFormPane()
    {
        SGRFormPane sgrFormPane;
        try
        {
            sgrFormPane = (SGRFormPane) workbenchPaneSS.getFormPane();
        }
        catch (ClassCastException e) { return; }
        sgrFormPane.copyDataFromForm();
        sgrFormPane.refreshResults();        
    }
}
