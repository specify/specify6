/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.ui.forms.formatters;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.AddRemoveEditPanel;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.forms.validation.ValSpinner;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 27, 2008
 *
 */
public class UIFormatterFieldDlg extends CustomDialog
{
    protected JList list;
    
    protected ValSpinner sizeSpnr;
    protected JTextField value;
    protected JComboBox  typeCBX;
    protected JCheckBox  autoNumChk;
    protected JCheckBox  useOtherChk;
    protected JTextField classNameTF;
    
    
    /**
     * @param dialog
     * @param title
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @throws HeadlessException
     */
    public UIFormatterFieldDlg(Frame frame) throws HeadlessException
    {
        super(frame, "Editor", true, OKCANCELHELP, null); //I18N
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,10px,p:g", "f:p:g"));
        
        ActionListener addAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                addFmt();
            }
        };
        
        ActionListener delAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                delFmt();
            }
        };
        
        ActionListener editAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                editFmt();
            }
        };
        
        AddRemoveEditPanel addRemovePanel = new AddRemoveEditPanel(addAL, delAL, editAL,
                                                                   "UIF_ADD_FORMATTER",
                                                                   "UIF_DEL_FORMATTER",
                                                                   "UIF_EDT_FORMATTER");
        
        PanelBuilder leftPB  = new PanelBuilder(new FormLayout("p","p,2px,f:p:g,2px,p"));
        list = new JList(new DefaultListModel());
        JScrollPane sb = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        leftPB.add(new JLabel("UIFormatters", SwingConstants.CENTER), cc.xy(1,1));
        leftPB.add(sb,             cc.xy(1, 3));
        leftPB.add(addRemovePanel, cc.xy(1, 5));
        addRemovePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        PanelBuilder    rightPB = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", UIHelper.createDuplicateJGoodiesDef("p", "2px", 6)));
        
        sizeSpnr    = new ValSpinner(1, 20, true, false);
        value       = new JTextField();
        typeCBX     = new JComboBox(UIFieldFormatterField.FieldType.values());
        autoNumChk  = new JCheckBox("Is Auto-Number"); // I18N
        useOtherChk = new JCheckBox("Use External Class"); 
        classNameTF = new JTextField();
        
        int y = 1;
        rightPB.add(new JLabel("Type:", SwingConstants.RIGHT), cc.xy(1,y));
        rightPB.add(typeCBX, cc.xy(3,y)); y += 2;
        
        rightPB.add(new JLabel("Size:", SwingConstants.RIGHT), cc.xy(1,y));
        rightPB.add(sizeSpnr, cc.xy(3,y)); y += 2;
        
        rightPB.add(new JLabel("Value:", SwingConstants.RIGHT), cc.xy(1,y));
        rightPB.add(value, cc.xy(3,y)); y += 2;
        
        //rightPB.add(new JLabel("Type:", SwingConstants.RIGHT), cc.xy(1,y));
        rightPB.add(autoNumChk, cc.xy(3,y)); y += 2;
        rightPB.add(useOtherChk, cc.xy(3,y)); y += 2;
        
        rightPB.add(new JLabel("External Class Name:", SwingConstants.RIGHT), cc.xy(1,y));
        rightPB.add(classNameTF, cc.xywh(3,y, 2, 1)); y += 2;
        
        pb.add(leftPB.getPanel(), cc.xy(1, 1));
        pb.add(rightPB.getPanel(), cc.xy(3, 1));
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        pack();
    }
    
    /**
     * 
     */
    protected void addFmt()
    {
        
    }
    
    
    protected void delFmt()
    {
        
    }
    
    
    protected void editFmt()
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // TODO Auto-generated method stub
        super.cleanUp();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#getOkBtn()
     */
    @Override
    public JButton getOkBtn()
    {
        // TODO Auto-generated method stub
        return super.getOkBtn();
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String args[]) throws Exception
    {
        UIFormatterFieldDlg dlg = new UIFormatterFieldDlg(null);
        dlg.pack();
        dlg.setVisible(true);
    }
}
