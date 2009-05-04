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
package edu.ku.brc.specify.tools.ireportspecify;

import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.specify.tasks.DataEntryTask;
import edu.ku.brc.specify.tasks.DataEntryView;
import edu.ku.brc.specify.tasks.GeographyTreeTask;
import edu.ku.brc.specify.tasks.GtpTreeTask;
import edu.ku.brc.specify.tasks.InteractionEntry;
import edu.ku.brc.specify.tasks.InteractionsTask;
import edu.ku.brc.specify.tasks.LithoStratTreeTask;
import edu.ku.brc.specify.tasks.StorageTreeTask;
import edu.ku.brc.specify.tasks.TaxonTreeTask;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *A tentative user interface for specifying properties Report resources.
 */
public class RepResourcePropsPanel extends JPanel
{
    /**
     * name of report resource
     */
    protected final String reportName;
    /**
     * type of report ("Label" or "Report")
     */
    protected final String reportType;
        
    /**
     * the id of the table the report is designed for. 
     */
    protected final boolean showTableIds;
    
    protected JTextField nameTxt;
    protected JTextField titleTxt;
    protected JTextField levelTxt;
    protected JTextField subReportsTxt;
    protected JComboBox typeCombo;
    protected JComboBox resDirCombo;
    protected JComboBox tblCombo;
    protected ReportRepeatPanel repeatPanel;
    protected JButton canceller = null;
    
    /**
     * @author timo
     *
     *Title/Name pairs for ResourceDir list.
     */
    private class ResDirItem extends edu.ku.brc.util.Pair<String, String>
    {
    	/**
    	 * @param title
    	 * @param name
    	 */
    	public ResDirItem(final String title, final String name)
    	{
    		super(title, name);
    	}
    	
    	/**
    	 * @return the name of the direectory.
    	 */
    	public String getTitle()
    	{
    		return getFirst();
    	}
    	
    	/**
    	 * @return localized title for the directory.
    	 */
    	public String getName()
    	{
    		return getSecond();
    	}

		/* (non-Javadoc)
		 * @see edu.ku.brc.util.Pair#toString()
		 */
		@Override
		public String toString() 
		{
			return getFirst();
		}
    }
    
    /**
     * @param report
     * @param resource
     */
    public RepResourcePropsPanel(final String reportName, final String reportType, final boolean showTableIds, final ReportSpecify rep)
    {
        super();
        this.reportName = reportName;
        this.reportType = reportType;
        this.showTableIds = showTableIds;
        createUI(rep);
    }
    
    /**
     * @param rep
     * 
     * sets up the UI
     */
    protected void createUI(final  ReportSpecify rep)
    {
//        String rowDefStr = showTableIds ? "p,p,p,p,p,p,p,10dlu" : "p,p,p,p,p,p,10dlu"; //adding 10dlu lower padding to try  
//                                                                                       // to try to prevent selection
//                                                                                       // problems in ReportRepeatPanel 
//                                                                                       // typeCombo.

        //Hiding Collection and Level
        String rowDefStr = showTableIds ? "p,p,p,p,p,p,p,10dlu" : "p,p,p,p,p,p,10dlu"; 

        PanelBuilder builder = new PanelBuilder(new FormLayout("right:p, 2dlu, fill:p:grow", rowDefStr), this);
        CellConstraints cc = new CellConstraints();
        
        builder.add(UIHelper.createLabel(UIRegistry.getResourceString("REP_NAME_LBL")), cc.xy(1,1));
        nameTxt = UIHelper.createTextField(reportName != null? reportName : "untitled");
        builder.add(nameTxt, cc.xy(3, 1));
        
        JLabel titleLbl = UIHelper.createLabel(UIRegistry.getResourceString("REP_TITLE_DESC_LBL"));
        builder.add(titleLbl, cc.xy(1,2));
        titleTxt = UIHelper.createTextField("none");
        builder.add(titleTxt, cc.xy(3, 2));
        titleLbl.setVisible(false);
        titleTxt.setVisible(false);
        
//        builder.add(UIHelper.createLabel(UIRegistry.getResourceString("REP_LEVEL_LBL")), cc.xy(1,3));
        levelTxt = UIHelper.createTextField("0");
        levelTxt.setEnabled(false);
//        builder.add(levelTxt, cc.xy(3, 3));
        
        builder.add(UIHelper.createLabel(UIRegistry.getResourceString("REP_REPTYPE_LBL")), cc.xy(1,3));
        typeCombo = UIHelper.createComboBox();
        typeCombo.addItem(UIRegistry.getResourceString("REP_REPORT"));
        typeCombo.addItem(UIRegistry.getResourceString("REP_LABEL"));
        typeCombo.addItem(UIRegistry.getResourceString("REP_INVOICE"));
        //typeCombo.addItem(UIRegistry.getResourceString("REP_SUBREPORT"));
        if (reportType != null && reportType.equals("Label"))
        {
            typeCombo.setSelectedIndex(1);
        }
        else if (reportType != null && reportType.equals("Invoice"))
        {
            typeCombo.setSelectedIndex(2);
        }
        else if (reportType != null && reportType.equals("Subreport"))
        {
            typeCombo.setSelectedIndex(2);
        }

        builder.add(typeCombo, cc.xy(3, 3));
        
        builder.add(UIHelper.createLabel(UIRegistry.getResourceString("REP_RESDIR_LBL")), cc.xy(1,4));
        resDirCombo = UIHelper.createComboBox();
        fillResDirCombo();
        if (rep != null && rep.getAppResource() != null)
        {
        	SpAppResource repRes = (SpAppResource )rep.getAppResource();
        	//XXX Assuming contents of combo are: Discipline, Personal.
        	if (repRes.getSpAppResourceDir().getIsPersonal())
        	{
        		resDirCombo.setSelectedIndex(1);
        	}
        	else
        	{
        		resDirCombo.setSelectedIndex(0);
        	}
        }
        
        builder.add(resDirCombo, cc.xy(3, 4));

        int row = 5;
        if (rep == null)
        {
        	builder.add(UIHelper.createLabel(UIRegistry.getResourceString("REP_SUBREPS_LBL")), cc.xy(1,row));
        	subReportsTxt = UIHelper.createTextField(null);
        	builder.add(subReportsTxt, cc.xy(3, row));
        	row++;
        }
        
        if (showTableIds)
        {
            builder.add(UIHelper.createLabel(UIRegistry.getResourceString("REP_TBL_LBL")), cc.xy(1,row));
            tblCombo = UIHelper.createComboBox();
            fillTblCombo();
            tblCombo.setSelectedIndex(0);
            builder.add(tblCombo, cc.xy(3, row));
            row++;
        }
        
        if (rep != null)
        {
            builder.add(UIHelper.createLabel(UIRegistry.getResourceString("REP_REPEAT_LBL")), cc.xy(1, row));
            repeatPanel = new ReportRepeatPanel(rep.getConnection(), canceller);
            repeatPanel.createUI(rep.getSpReport() == null ? null : rep.getSpReport().getRepeats());
            builder.add(repeatPanel, cc.xy(3, row));
        }
        else
        {
            repeatPanel = null;
        }
    }

    /**
     * @return tableid for resource
     */
    public int getTableId()
    {
        if (!showTableIds)
        {
            return -1;
        }
        return ((DBTableInfo)tblCombo.getSelectedItem()).getTableId();
    }
    
    /**
     * populate resource directory combo with supported directories.
     */
    protected void fillResDirCombo()
    {
    	resDirCombo.addItem(new ResDirItem(UIRegistry.getResourceString("SpecifyAppContextMgr.Discipline"), "Discipline"));
    	resDirCombo.addItem(new ResDirItem(UIRegistry.getResourceString("SpecifyAppContextMgr.Personal"), "Personal"));
    }
    
    /**
     * fills tableCombo with tables and treeable tables available/visible on DataEntryTask and InteractionsTask
     */
    protected void fillTblCombo()
    {
        
        Vector<DBTableInfo> tbls = new Vector<DBTableInfo>();
        DataEntryTask dataEntryTask = (DataEntryTask )TaskMgr.getTask(DataEntryTask.DATA_ENTRY);
        if (dataEntryTask != null)
        {
            for (DataEntryView dv : dataEntryTask.getStdViews())
            {
                if (dv.isVisible())
                {
                    tbls.add(dv.getTableInfo());
                }
            }
        }
        if (TaskMgr.getTask(TaxonTreeTask.TAXON) != null)
        {
            DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName("taxon");
            if (!tbls.contains(info))
            {
                tbls.add(info);
            }
        }
        if (TaskMgr.getTask(GeographyTreeTask.GEOGRAPHY) != null)
        {
            DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName("geography");
            if (!tbls.contains(info))
            {
                tbls.add(info);
            }
        }
        if (TaskMgr.getTask(LithoStratTreeTask.LITHO) != null)
        {
            DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName("lithostrat");
            if (!tbls.contains(info))
            {
                tbls.add(info);
            }
        }
        if (TaskMgr.getTask(GtpTreeTask.GTP) != null)
        {
            DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName("geologictimeperiod");
            if (!tbls.contains(info))
            {
                tbls.add(info);
            }
        }
        if (TaskMgr.getTask(StorageTreeTask.STORAGE) != null)
        {
            DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName("storage");
            if (!tbls.contains(info))
            {
                tbls.add(info);
            }
        }
        

        InteractionsTask interactionsTask = (InteractionsTask )TaskMgr.getTask(InteractionsTask.INTERACTIONS);
        if (interactionsTask != null)
        {
            for (InteractionEntry ie : interactionsTask.getEntries())
            {
                if (ie.isOnLeft() && ie.isVisible())
                {
                    DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(ie.getTableName());
                    if (!tbls.contains(info))
                    {
                        tbls.add(info);
                    }
                }
            }
        }
        if (dataEntryTask != null)
        {
            for (DataEntryView dv : dataEntryTask.getMiscViews())
            {
                if (dv.isVisible())
                {
                    DBTableInfo info = dv.getTableInfo();
                    if (!tbls.contains(info))
                    {
                        tbls.add(info);
                    }
                }
            }
        }
        if (interactionsTask != null)
        {
            for (InteractionEntry ie : interactionsTask.getEntries())
            {
                if (!ie.isOnLeft() && ie.isVisible())
                {
                    DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(ie.getTableName());
                    if (!tbls.contains(info))
                    {
                        tbls.add(info);
                    }
                }
            }
        }
        
        for (DBTableInfo tbl : tbls)
        {
            tblCombo.addItem(tbl);
        }
     }
    
    /**
     * @return the nameTxt
     */
    public JTextField getNameTxt()
    {
        return nameTxt;
    }

    /**
     * @return the titleTxt
     */
    public JTextField getTitleTxt()
    {
        return titleTxt;
    }

    /**
     * @return the levelTxt
     */
    public JTextField getLevelTxt()
    {
        return levelTxt;
    }

    /**
     * @return the typeCombo
     */
    public JComboBox getTypeCombo()
    {
        return typeCombo;
    }

    /**
     * @return the resDirTxt
     */
    public JComboBox getResDirCombo()
    {
        return resDirCombo;
    }
    
    public static void main(String args[])
    {
        CustomDialog tcd = new CustomDialog(null, "Testing", false, 
                new RepResourcePropsPanel(null, null, true, null));
        UIHelper.centerAndShow(tcd);
    }

    /**
     * @param canceller the canceller to set
     */
    public void setCanceller(JButton canceller)
    {
        this.canceller = canceller;
    }
    
    /**
     * @return the repeat property for the report.
     */
    public Object getRepeats()
    {
        if (repeatPanel != null)
        {
            return repeatPanel.getRepeats();
        }
        return null;
    }
    
    
    /**
	 * @return the subReportsTxt
	 */
	public JTextField getSubReportsTxt()
	{
		return subReportsTxt;
	}

	/**
     * @return true if all properties are valid.
     */
    public boolean validInputs()
    {
        if (repeatPanel != null)
        {
            return repeatPanel.validInputs();
        }
        return true;
    }
}
