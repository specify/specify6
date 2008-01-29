/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.ui.forms.formatters;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
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

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.AddRemoveEditPanel;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 27, 2008
 *
 */
public class UIFormatterDlg extends CustomDialog
{
    protected JList list;
    
    protected JTextField nameTF;
    protected JComboBox  tableCBX;
    protected JComboBox  fieldCBX;
    protected JCheckBox  defaultChk;
    protected JCheckBox  externalChk;
    protected JTextField classNameTF;
    
    
    /**
     * @param dialog
     * @param title
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @throws HeadlessException
     */
    public UIFormatterDlg(Frame frame) throws HeadlessException
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
        
        DefaultListModel listModel = new DefaultListModel();
        
        List<UIFieldFormatterIFace> fmtrs = new Vector<UIFieldFormatterIFace>(UIFieldFormatterMgr.getFormatters());
        Collections.sort(fmtrs, new Comparator<UIFieldFormatterIFace>() {
            public int compare(UIFieldFormatterIFace o1, UIFieldFormatterIFace o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
        
        for (UIFieldFormatterIFace f : fmtrs)
        {
            listModel.addElement(f);
        }
        PanelBuilder leftPB  = new PanelBuilder(new FormLayout("p","p,2px,f:p:g,2px,p"));
        list = new JList(listModel);
        JScrollPane sb = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        leftPB.add(new JLabel("UIFormatters", SwingConstants.CENTER), cc.xy(1,1));
        leftPB.add(sb,             cc.xy(1, 3));
        leftPB.add(addRemovePanel, cc.xy(1, 5));
        addRemovePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        PanelBuilder    rightPB = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", UIHelper.createDuplicateJGoodiesDef("p", "2px", 6)));
        
        Vector<DBTableInfo> tables = DBTableIdMgr.getInstance().getTables();
        Collections.sort(tables);
        
        nameTF      = new JTextField();
        tableCBX    = new JComboBox(tables);
        fieldCBX    = new JComboBox(new DefaultComboBoxModel());
        defaultChk  = new JCheckBox("Is Default"); // I18N
        externalChk = new JCheckBox("Use External Class"); 
        classNameTF = new JTextField();
        
        tableCBX.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e)
            {
                tableSelected();
            }
        });
        
        int y = 1;
        rightPB.add(new JLabel("Name:", SwingConstants.RIGHT), cc.xy(1,y));
        rightPB.add(nameTF, cc.xy(3,y)); y += 2;
        
        rightPB.add(new JLabel("Table:", SwingConstants.RIGHT), cc.xy(1,y));
        rightPB.add(tableCBX, cc.xy(3,y)); y += 2;
        
        rightPB.add(new JLabel("Field:", SwingConstants.RIGHT), cc.xy(1,y));
        rightPB.add(fieldCBX, cc.xy(3,y)); y += 2;
        
        rightPB.add(defaultChk, cc.xy(3,y)); y += 2;
        rightPB.add(externalChk, cc.xy(3,y)); y += 2;
        
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
    protected void updateUIEnabled()
    {
        boolean isOK = true;
        
        if (nameTF.getText().length() == 0)
        {
            isOK = false;
        }
        
        if (tableCBX.getSelectedIndex() == -1)
        {
            isOK = false;
        }
        
        if (fieldCBX.getSelectedIndex() == -1)
        {
            isOK = false;
        }
        
        okBtn.setEnabled(isOK);
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
    
    protected void tableSelected()
    {
        DefaultComboBoxModel model = (DefaultComboBoxModel)fieldCBX.getModel();
        model.removeAllElements();
        DBTableInfo ti = (DBTableInfo)tableCBX.getSelectedItem();
        if (ti != null)
        {
            for (DBFieldInfo fi : ti.getFields())
            {
                if (fi.getDataClass() == String.class)
                {
                    model.addElement(fi);
                }
            }
        }
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
        UIFormatterDlg dlg = new UIFormatterDlg(null);
        dlg.pack();
        dlg.setVisible(true);
    }
}
