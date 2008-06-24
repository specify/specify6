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

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
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
     * the resource to be associated with it (not necessarily it's current resource, i guess)
     */
    protected final AppResourceIFace resource;
    
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
    
    /**
     * @param report
     * @param resource
     */
    public RepResourcePropsPanel(final String reportName, final AppResourceIFace resource, final boolean showTableIds)
    {
        super();
        this.reportName = reportName;
        this.resource = resource;
        this.showTableIds = showTableIds;
        createUI();
    }
    
    protected void createUI()
    {
        String rowDefStr = showTableIds ? "p,p,p,p,p,p" : "p,p,p,p,p";
        PanelBuilder builder = new PanelBuilder(new FormLayout("right:p, 2dlu, fill:p:grow", rowDefStr), this);
        CellConstraints cc = new CellConstraints();
        
        builder.add(new JLabel(UIRegistry.getResourceString("REP_NAME_LBL")), cc.xy(1,1));
        nameTxt = new JTextField(reportName != null? reportName : "untitled");
        builder.add(nameTxt, cc.xy(3, 1));
        
        JLabel titleLbl = new JLabel(UIRegistry.getResourceString("REP_TITLE_DESC_LBL"));
        builder.add(titleLbl, cc.xy(1,2));
        titleTxt = new JTextField(resource != null ? resource.getDescription() : "none");
        builder.add(titleTxt, cc.xy(3, 2));
        titleLbl.setVisible(false);
        titleTxt.setVisible(false);
        
        builder.add(new JLabel(UIRegistry.getResourceString("REP_LEVEL_LBL")), cc.xy(1,3));
        levelTxt = new JTextField("3");
        levelTxt.setEnabled(false);
        builder.add(levelTxt, cc.xy(3, 3));
        
        builder.add(new JLabel(UIRegistry.getResourceString("REP_REPTYPE_LBL")), cc.xy(1,4));
        typeCombo = new JComboBox();
        typeCombo.addItem(UIRegistry.getResourceString("REP_REPORT"));
        typeCombo.addItem(UIRegistry.getResourceString("REP_LABEL"));
        String repType = resource != null ? resource.getMetaDataMap().getProperty("reporttype") : null;
        if (repType != null && repType.equals("Label"))
        {
            typeCombo.setSelectedIndex(1);
        }
        builder.add(typeCombo, cc.xy(3, 4));
        
        builder.add(new JLabel(UIRegistry.getResourceString("REP_RESDIR_LBL")), cc.xy(1,5));
        resDirTxt = new JTextField("Collection");
        resDirTxt.setEnabled(false);
        builder.add(resDirTxt, cc.xy(3, 5));

        if (showTableIds)
        {
            builder.add(new JLabel(UIRegistry.getResourceString("REP_TBL_LBL")), cc.xy(1,6));
            tblCombo = new JComboBox();
            fillTblCombo();
            tblCombo.setSelectedIndex(0);
            builder.add(tblCombo, cc.xy(3, 6));
        }
    }

    public int getTableId()
    {
        if (!showTableIds)
        {
            return -1;
        }
        return ((DBTableInfo)tblCombo.getSelectedItem()).getTableId();
    }
    
    protected void fillTblCombo()
    {
        //XXX need to get 'main' tbls...
        for (int id=1; id<10; id++)
        {
            tblCombo.addItem(DBTableIdMgr.getInstance().getInfoById(id));
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
                new RepResourcePropsPanel(null, null, true));
        UIHelper.centerAndShow(tcd);
    }
}
