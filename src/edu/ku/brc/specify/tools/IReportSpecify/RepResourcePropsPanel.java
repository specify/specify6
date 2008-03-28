/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.IReportSpecify;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppResourceIFace;
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
     * an iReport report
     */
    protected final ReportSpecify report;
    /**
     * the resource to be associated with it (not necessarily it's current resource, i guess)
     */
    protected final AppResourceIFace resource;
    
    protected JTextField nameTxt;
    protected JTextField titleTxt;
    protected JTextField levelTxt;
    protected JComboBox typeCombo;
    protected JTextField resDirTxt;
    
    /**
     * @param report
     * @param resource
     */
    public RepResourcePropsPanel(ReportSpecify report, AppResourceIFace resource)
    {
        super();
        this.report = report;
        this.resource = resource;
        createUI();
    }
    
    protected void createUI()
    {
        PanelBuilder builder = new PanelBuilder(new FormLayout("right:p, 2dlu, fill:p:grow", "p,p,p,p,p"), this);
        CellConstraints cc = new CellConstraints();
        
        builder.add(new JLabel(UIRegistry.getResourceString("REP_NAME_LBL")), cc.xy(1,1));
        nameTxt = new JTextField(report != null? report.getName() : "untitled");
        builder.add(nameTxt, cc.xy(3, 1));
        
        builder.add(new JLabel(UIRegistry.getResourceString("REP_TITLE_DESC_LBL")), cc.xy(1,2));
        titleTxt = new JTextField(resource != null ? resource.getDescription() : "none");
        builder.add(titleTxt, cc.xy(3, 2));
        
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
                new RepResourcePropsPanel(null, null));
        UIHelper.centerAndShow(tcd);
    }
}
