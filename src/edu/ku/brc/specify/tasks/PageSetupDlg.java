/*
 * Copyright (C) 2009  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.HeadlessException;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Complete
 *
 * Feb 26, 2009
 *
 */
public class PageSetupDlg extends CustomDialog
{
    protected JTextField   titleTxt;
    protected JComboBox    pageSizeCBX;
    protected JRadioButton portaitRB;
    protected JRadioButton landscapeRB;
    
    /**
     * @throws HeadlessException
     */
    public PageSetupDlg() throws HeadlessException
    {
        super((JFrame)UIRegistry.getMostRecentWindow(), getResourceString("PAGESETUP_TITLE"), true, null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        titleTxt    = UIHelper.createTextField(20);
        pageSizeCBX = UIHelper.createComboBox(new String[] {getResourceString("PS_LTR"), getResourceString("PS_LGL"), getResourceString("PS_A4")});
        
        ButtonGroup grp = new ButtonGroup();
        
        portaitRB   = new JRadioButton("Portrait");
        landscapeRB = new JRadioButton("Landscape");
        grp.add(portaitRB);
        grp.add(landscapeRB);
        
        CellConstraints cc   = new CellConstraints();
        PanelBuilder    rbpb = new PanelBuilder(new FormLayout("p,4px,p", "p"));
        rbpb.add(portaitRB,   cc.xy(1,1));
        rbpb.add(landscapeRB, cc.xy(3,1));
        
        portaitRB.setSelected(true);
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,4px,p,f:p:g", "p,6px,p,6px,p,6px,p"));
        int y = 1;
        
        pb.add(UIHelper.createI18NFormLabel("PS_TITLE"), cc.xy(1,y));
        pb.add(titleTxt,                                 cc.xyw(3,y,2)); y += 2;

        pb.add(UIHelper.createI18NFormLabel("PS_PAGESIZE"), cc.xy(1,y));
        pb.add(pageSizeCBX,                                 cc.xy(3,y)); y += 2;

        pb.add(UIHelper.createI18NFormLabel("PS_ORIENT"), cc.xy(1,y));
        pb.add(rbpb.getPanel(),                           cc.xy(3,y)); y += 2;

        pb.setDefaultDialogBorder();
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        pack();
        
        pageSizeCBX.setSelectedIndex(0);
    }
    
    /**
     * @param pageTitle
     */
    public void setPageTitle(final String pageTitle)
    {
        titleTxt.setText(pageTitle);
    }

    /**
     * @return
     */
    public String getPageTitle()
    {
        return titleTxt.getText();
    }
    
    public boolean isPortrait()
    {
        return portaitRB.isSelected();
    }
    
    public int getPageSize()
    {
        return pageSizeCBX.getSelectedIndex();
    }
}
