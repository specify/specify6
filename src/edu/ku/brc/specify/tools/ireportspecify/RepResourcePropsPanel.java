/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
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
    protected JComboBox typeCombo;
    protected JTextField resDirTxt;
    protected JComboBox tblCombo;
    protected ReportRepeatPanel repeatPanel;
    protected JButton canceller = null;
    
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
        String rowDefStr = showTableIds ? "p,p,p,p,p,10dlu" : "p,p,p,p,10dlu"; 

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
        levelTxt = UIHelper.createTextField("3");
        levelTxt.setEnabled(false);
//        builder.add(levelTxt, cc.xy(3, 3));
        
        builder.add(UIHelper.createLabel(UIRegistry.getResourceString("REP_REPTYPE_LBL")), cc.xy(1,3));
        typeCombo = UIHelper.createComboBox();
        typeCombo.addItem(UIRegistry.getResourceString("REP_REPORT"));
        typeCombo.addItem(UIRegistry.getResourceString("REP_LABEL"));
        if (reportType != null && reportType.equals("Label"))
        {
            typeCombo.setSelectedIndex(1);
        }
        builder.add(typeCombo, cc.xy(3, 3));
        
//        builder.add(UIHelper.createLabel(UIRegistry.getResourceString("REP_RESDIR_LBL")), cc.xy(1,5));
        resDirTxt = UIHelper.createTextField(MainFrameSpecify.DEFAULT_REPORT_RESOURCE_DIR);
        resDirTxt.setEnabled(false);
//        builder.add(resDirTxt, cc.xy(3, 5));

        if (showTableIds)
        {
            builder.add(UIHelper.createLabel(UIRegistry.getResourceString("REP_TBL_LBL")), cc.xy(1,4));
            tblCombo = UIHelper.createComboBox();
            fillTblCombo();
            tblCombo.setSelectedIndex(0);
            builder.add(tblCombo, cc.xy(3, 4));
        }
        
        if (rep != null)
        {
            builder.add(UIHelper.createLabel(UIRegistry.getResourceString("REP_REPEAT_LBL")), cc.xy(1, showTableIds ? 5 : 4));
            repeatPanel = new ReportRepeatPanel(rep.getConnection(), canceller);
            repeatPanel.createUI(rep.getSpReport() == null ? null : rep.getSpReport().getRepeats());
            builder.add(repeatPanel, cc.xy(3, showTableIds ? 5 : 4));
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
    public JTextField getResDirTxt()
    {
        return resDirTxt;
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
