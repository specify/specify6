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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.google.common.base.Function;

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
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPaneSS;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CustomDialog;
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
    private final JPopupMenu   popupMenu;
    private MatchConfiguration matcherConfiguration;
    private WorkbenchColorizer colorizer;
    private WorkbenchPaneSS    workbenchPaneSS;

    private SubPaneIFace       subPane;
    private Workbench2SGR      workbench2SGR;
   
    public SGRPluginImpl()
    {
        super();

        popupMenu = createPopupMenu();
    }

    private JPopupMenu createPopupMenu()
    {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem mi = new JMenuItem();
        mi.setText("Colorize WB...");
        mi.addActionListener(new ActionListener()
        {
            
            @Override
            public void actionPerformed(ActionEvent e)
            {
                List<BatchMatchResultSet> resultSets = 
                    DataModel.getBatchMatchResultSetsFor(
                            (long)workbench.getId(), workbench.getDbTableId());
                
                ChooseFromListDlg<BatchMatchResultSet> dlg = 
                    new ChooseFromListDlg<BatchMatchResultSet>(
                            (Frame)UIRegistry.get(UIRegistry.FRAME), 
                            "Choose Match Configuration", resultSets);
                
                UIHelper.centerAndShow(dlg);
                if (!dlg.isCancelled())
                {
                    BatchMatchResultSet resultSetForColors = dlg.getSelectedObject();
                    if (matcherConfiguration == null)
                    {
                        setMatcherConfiguration(resultSetForColors.getMatchConfiguration());
                        colorizer.setBatchResults(resultSetForColors);
                        workbenchPaneSS.showHideSgrCol(true);
                    }
                    else if (resultSetForColors.matchConfigurationId() != matcherConfiguration.id())
                    {
                        CustomDialog dlg2 = new CustomDialog((Frame)UIRegistry.get(UIRegistry.FRAME), 
                                "Confirm SGR Matcher", true, 
                                CustomDialog.OKCANCEL, null, 
                                CustomDialog.OK_BTN);
                        
                        MatchConfiguration rsMc = resultSetForColors.getMatchConfiguration();
                        
                        String msg = "<html><p>The selected Batch Result set used a different " +
                        " SGR Matcher than is currently selected. Do you wish to switch to the " +
                        " Matcher that was used for the Batch run and continue?</p> " +
                        "<table><tr><th>Batch Matcher:</th><td>" + rsMc.name() + "</td></tr>" +
                        "<tr><th>Current Matcher:</th><td>" + matcherConfiguration.name() +
                        "</td></tr></table></html>";
                        
                        dlg2.setContentPanel(new JLabel(msg));
                        
                        UIHelper.centerAndShow(dlg2);
                        if (!dlg2.isCancelled())
                        {
                            setMatcherConfiguration(resultSetForColors.getMatchConfiguration());
                            colorizer.setBatchResults(resultSetForColors);
                            workbenchPaneSS.showHideSgrCol(true);
                        }
                    }
                    else // matchers are consistent
                    {
                        colorizer.setBatchResults(resultSetForColors);
                        workbenchPaneSS.showHideSgrCol(true);
                    }
                }                
            }
        });
        menu.add(mi);
        
        mi = new JMenuItem();
        mi.setText("Select Matcher...");
        mi.addActionListener(new ActionListener()
        {
            
            @Override
            public void actionPerformed(ActionEvent e)
            {
                List<MatchConfiguration> mcs = DataModel.getMatcherConfigurations();
                
                ChooseFromListDlg<MatchConfiguration> dlg = 
                    new ChooseFromListDlg<MatchConfiguration>((Frame)UIRegistry.get(UIRegistry.FRAME), 
                            "Choose Match Configuration", mcs);
                
                UIHelper.centerAndShow(dlg);
                if (!dlg.isCancelled())
                {
                    colorizer.stopColoring();
                    setMatcherConfiguration(dlg.getSelectedObject());        
                }
            }
        });
        menu.add(mi);
        
        mi = new JMenuItem();
        mi.setText("Stop coloring.");
        mi.addActionListener(new ActionListener()
        {
            
            @Override
            public void actionPerformed(ActionEvent e)
            {
                colorizer.stopColoring();
                workbenchPaneSS.showHideSgrCol(false);
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
        return matcher.match(doc.asMatchable());
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
        btn.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)            
            {
                super.mousePressed(e);
                if (e.isPopupTrigger())
                    doPopup(e);
            }
            
            @Override
            public void mouseReleased(MouseEvent e)
            {
                super.mouseReleased(e);
                if (e.isPopupTrigger())
                    doPopup(e);
            }
        });
    }

    private void doPopup(MouseEvent e)
    {
        if (!popupMenu.isShowing()) 
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
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
}
