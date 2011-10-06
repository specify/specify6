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
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableList;

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
import edu.ku.brc.ui.DropDownButtonStateless;
import edu.ku.brc.ui.DropDownButtonStateless.MenuInfo;
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
    private SpreadSheet             spreadSheet;
    private Workbench               workbench;

    private SGRMatcher              matcher;
    private MatchConfiguration      matcherConfiguration;
    private WorkbenchColorizer      colorizer;
    private WorkbenchPaneSS         workbenchPaneSS;

    private Workbench2SGR           workbench2SGR;
    private List<JComponent>        ssButtons;
    private List<JComponent>        formButtons;

    protected Integer               nResults;

    private DropDownButtonStateless matcherButton;
    private DropDownButtonStateless batchButton;
        
    private void selectMatcher()
    {
        List<MatchConfiguration> mcs = DataModel.getMatcherConfigurations();
        
        ChooseFromListDlg<MatchConfiguration> dlg = 
            new ChooseFromListDlg<MatchConfiguration>((Frame)UIRegistry.get(UIRegistry.FRAME), 
                    "Choose Matcher", mcs);
        
        UIHelper.centerAndShow(dlg);
        if (!dlg.isCancelled())
        {
            setMatcherConfiguration(dlg.getSelectedObject());
            refreshFormPane();
        }
    }    
    
    public void setBatchMatchResults(BatchMatchResultSet rs)
    {
        for (MenuInfo mi : batchButton.getMenuInfos())
        {
            BatchMenuInfo bmi = (BatchMenuInfo) mi;
            if (bmi.resultSet.id() == rs.id())
            {
                batchButton.setSelected(bmi);
                setMatcherConfiguration(rs.getMatchConfiguration());
                return;
            }
        }
    }
    
    public void setMatcherConfiguration(MatchConfiguration mc)
    {
        for (MenuInfo mi : matcherButton.getMenuInfos())
        {
            MatcherMenuInfo mmi = (MatcherMenuInfo) mi;
            if (mmi.matchConfiguration.id() == mc.id())
            {
                matcherButton.setSelected(mmi);
                return;
            }
        }
    }

    public boolean isReady()
    {
        return matcherConfiguration != null;
    }
    
    public MatchResults doQuery(WorkbenchRow row)
    {
        assert isReady();
        SGRRecord doc = workbench2SGR.row2SgrRecord(row);
        return matcher.match(doc.asMatchable(), nResults);
    }

    @Override
    public Collection<JComponent> getSSButtons()
    {
        if (ssButtons != null)
            return ssButtons;
        
        if (workbenchPaneSS.getTask() instanceof SGRTask)
        {
            UIRegistry.loadAndPushResourceBundle("specify_plugins");
            String tooltip = UIRegistry.getResourceString("SGR_SHOW_RESULTS");
            String label = UIRegistry.getResourceString("SGR_BATCH_RESULTS");
            UIRegistry.popResourceBundle();
            ImageIcon icon = IconManager.getIcon("SGR", IconManager.IconSize.Std20);

            batchButton = new DropDownButtonStateless(label, icon, tooltip,
                    new DropDownButtonStateless.MenuGenerator()
            {
                @Override
                public List<MenuInfo> getItems()
                {
                    List<MenuInfo> batchList = new LinkedList<DropDownButtonStateless.MenuInfo>();
                    for (BatchMatchResultSet rs : 
                        DataModel.getBatchMatchResultSetsFor(
                                (long)workbench.getId(), workbench.getDbTableId())) 
                    {
                        batchList.add(new BatchMenuInfo(rs));
                    }
                    return batchList;
                }
            });
            
            batchButton.addActionListener(new ActionListener()
            {             
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    workbenchPaneSS.showPanel(PanelType.Form);                    
                }
            });
            
            ssButtons = ImmutableList.of((JComponent)batchButton);
        }
        else
        {
            ssButtons = ImmutableList.of();
        }
        
        return ssButtons;
    }

    @Override
    public Collection<JComponent> getFormButtons()
    {
        if (formButtons != null)
            return formButtons;

        if (workbenchPaneSS.getTask() instanceof SGRTask)
        {
            UIRegistry.loadAndPushResourceBundle("specify_plugins");
            String tooltip = UIRegistry.getResourceString("SGR_REFRESH_RESULTS");
            String label = UIRegistry.getResourceString("SGR_SELECT_MATCHER");
            UIRegistry.popResourceBundle();
            ImageIcon icon = IconManager.getIcon("SGRMatchers", IconManager.IconSize.Std20);
            
            matcherButton = new DropDownButtonStateless(label,  icon, tooltip,
                    new DropDownButtonStateless.MenuGenerator()
            {
                
                @Override
                public List<MenuInfo> getItems()
                {
                    List<MenuInfo> matchersList = new LinkedList<DropDownButtonStateless.MenuInfo>();
                    for (MatchConfiguration mc : DataModel.getMatcherConfigurations()) 
                    {
                        matchersList.add(new MatcherMenuInfo(mc));
                    }
                    return matchersList;
                }
            });
            
            matcherButton.addActionListener(new ActionListener()
            {             
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    refreshFormPane();                   
                }
            });
         
            formButtons = ImmutableList.of((JComponent)matcherButton);
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
    
    private class MatcherMenuInfo extends MenuInfo
    {
        public final MatchConfiguration matchConfiguration;

        public MatcherMenuInfo(MatchConfiguration mc)
        {
            super(StringUtils.abbreviate(mc.name(), 14), null, null);
            this.matchConfiguration = mc;
        }
        
        @Override
        public void selected()
        {
            Factory matcherFactory = matchConfiguration.createMatcherFactory();
            matcherFactory.docSupplied = true;
            matcherFactory.debugQuery = true;
            try
            {
                matcher = matcherFactory.build();
            } catch (MalformedURLException ex)
            {
                throw new RuntimeException(ex);
            }
            
            SGRPluginImpl.this.matcherConfiguration = matchConfiguration;
            refreshFormPane();
            
            BatchMatchResultSet rs = null;
            try
            {
                rs = colorizer.getResultSet();
            }
            catch (NullPointerException e) {}
            
            if (rs != null && matcherConfiguration.id() != rs.getMatchConfiguration().id())
            {
                colorizer.stopColoring();
                batchButton.reset();
            }
        }
    }

    private class BatchMenuInfo extends MenuInfo
    {
        public final BatchMatchResultSet resultSet;

        public BatchMenuInfo(BatchMatchResultSet rs)
        {
            super(StringUtils.abbreviate(rs.name(), 14), null, null);
            this.resultSet = rs;
        }
        
        @Override
        public void selected()
        {
            colorizer.setBatchResults(resultSet);
            workbenchPaneSS.showHideSgrCol(true);
            workbenchPaneSS.sgrSort();
            setMatcherConfiguration(resultSet.getMatchConfiguration());
        }
    }

    public MatchConfiguration getMatcher()
    {
        return matcherConfiguration;
    }

    @Override
    public boolean process(List<WorkbenchRow> rows)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#setSubPanel(edu.ku.brc.af.core.SubPaneIFace)
     */
    @Override
    public void setSubPanel(SubPaneIFace parent)
    {
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
