/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class UploadMatchSettingsPanel extends JPanel implements ActionListener
{
    protected static final Logger log = Logger.getLogger(UploadMatchSettingsPanel.class);

    protected DefaultComboBoxModel modeTexts;
    protected DefaultComboBoxModel boolTexts;
    protected JLabel tblLbl;
    protected TableModel tblModel;
    protected JTable tblTbl;
    protected JButton applyBtn;
    protected JLabel caption;
    
    protected final Vector<UploadTable> tables;
    
    public void actionPerformed(ActionEvent action)
    {
        if (action.getSource().getClass().equals(JComboBox.class))
        {
            if (tblTbl.getEditingRow() != -1 && tblTbl.getEditingColumn() != -1)
            {
                JComboBox jbox = (JComboBox)action.getSource();
                tblTbl.getModel().setValueAt(jbox.getSelectedItem(), tblTbl.getEditingRow(), tblTbl.getEditingColumn());
            }
        }
        else if (action.getActionCommand().equals("APPLY"))
        {
            apply();
        }
    }
    
    public void refresh()
    {
        for (int row = 0; row < tables.size(); row++)
        {
            UploadMatchSetting matchSet = tables.get(row).getMatchSetting();
            tblTbl.getModel().setValueAt(modeTexts.getElementAt(matchSet.getMode()).toString(), row, 1);
            tblTbl.getModel().setValueAt(Boolean.toString(matchSet.isRemember()), row, 2);
        }
    }
    
    public void apply()
    {
        for (int row = 0; row < tables.size(); row++)
        {
            UploadMatchSetting matchSet = tables.get(row).getMatchSetting();
            matchSet.setMode(UploadMatchSetting.getMode(tblTbl.getModel().getValueAt(row, 1).toString()));
            matchSet.setRemember(Boolean.valueOf(tblTbl.getModel().getValueAt(row, 2).toString()));
            matchSet.setMatchEmptyValues(Boolean.valueOf(tblTbl.getModel().getValueAt(row, 3).toString()));
            log.debug("NOT setting fields to match!");
        }
    }
    
    public UploadMatchSettingsPanel(final Vector<UploadTable> tables, boolean readOnly, boolean showApplyBtn)
    {
        super();
        
        setLayout(new BorderLayout());
        //display tables alphabetically
        this.tables = new Vector<UploadTable>(new TreeSet<UploadTable>(tables));
        
        modeTexts = new DefaultComboBoxModel(UploadMatchSetting.getModeTexts());
        
        boolTexts = new DefaultComboBoxModel();
        boolTexts.addElement(Boolean.toString(Boolean.TRUE)); //i18n ?
        boolTexts.addElement(Boolean.toString(Boolean.FALSE));  //i18n ?
        
        Vector<String> headers = new Vector<String>();
        headers.add(getResourceString("SL_TABLES"));
        headers.add(getResourceString("WB_MATCH_MODE_CAPTION"));
        headers.add(getResourceString("WB_UPLOAD_MATCH_REMEMBER_CAPTION"));
        headers.add(getResourceString("WB_UPLOAD_MATCH_BLANKS_CAPTION"));
        //headers.add(getResourceString("WB_UPLOAD_MATCH_FLDS_CAPTION"));
        
        Vector<Vector<String>> rows = new Vector<Vector<String>>();
        for (UploadTable tbl : this.tables)
        {
            Vector<String> row = new Vector<String>();
            row.add(tbl.toString());
            row.add(modeTexts.getElementAt(tbl.getMatchSetting().getMode()).toString());
            row.add(Boolean.toString(tbl.getMatchSetting().isRemember()));
            row.add(Boolean.toString(tbl.getMatchSetting().isMatchEmptyValues()));
            //row.add(getResourceString("WB_UPLOAD_MATCH_ALL"));
            rows.add(row);
        }
        
        if (readOnly)
        {
            tblModel = new DefaultTableModel(rows, headers)
            {
                @Override
                public boolean isCellEditable(int row, int col)
                {
                    return false;
                }
            };
        }
        else
        {
            final ActionListener myself = this;
            tblModel = new DefaultTableModel(rows, headers)
            {
                @Override
                public boolean isCellEditable(int row, int col)
                {
                    if (col >= 1 && col <= 3)
                    {
                        JComboBox jBox;
                        if (col == 1)
                        {
                            jBox = createComboBox(modeTexts);
                        }
                        else
                        {
                            jBox = createComboBox(boolTexts);
                        }
                        jBox.addActionListener(myself);
                        tblTbl.setDefaultEditor(tblTbl.getColumnClass(col), new DefaultCellEditor(
                                jBox));
                        return true;
                    }
                    return false;
                }
            };
        }
        tblTbl = new JTable(tblModel);
        Enumeration<TableColumn> cols = tblTbl.getColumnModel().getColumns();
        while (cols.hasMoreElements())
        {
            TableColumn col = cols.nextElement();
            col.setPreferredWidth(new JLabel((String )col.getHeaderValue()).getPreferredSize().width);
        }
        
        PanelBuilder pb; 
        if (showApplyBtn)
        {
            pb = new PanelBuilder(new FormLayout("2dlu, f:p:g, 2dlu", "2dlu, c:p, 2dlu, f:p:g, p, 2dlu"));
        }
        else
        {
            pb = new PanelBuilder(new FormLayout("2dlu, f:p:g, 2dlu", "2dlu, c:p, 2dlu, f:p:g, 2dlu"));
        }
        CellConstraints cc = new CellConstraints();        
        
        caption = createLabel(getResourceString("WB_UPLOAD_MATCH_SETTINGS") + ":");
        caption.setFont(caption.getFont().deriveFont(Font.BOLD));
        //add(caption, BorderLayout.NORTH);
        pb.add(caption, cc.xy(2, 2));
        
        JScrollPane sp = new JScrollPane(tblTbl, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pb.add(sp, cc.xy(2, 4));
                
        if (showApplyBtn)
        {
            applyBtn = createButton(getResourceString("APPLY"));
            applyBtn.setActionCommand("APPLY");
            applyBtn.addActionListener(this);
            pb.add(applyBtn, cc.xy(2, 6));
        }
        else
        {
            applyBtn = null;
        }
        
        add(pb.getPanel(), BorderLayout.CENTER);
    }    
}
