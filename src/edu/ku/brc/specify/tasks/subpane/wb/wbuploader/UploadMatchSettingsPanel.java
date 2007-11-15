/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

public class UploadMatchSettingsPanel extends JPanel implements ActionListener
{
    protected static final Logger log = Logger.getLogger(UploadMatchSettingsPanel.class);

    protected DefaultComboBoxModel modeTexts;
    protected DefaultComboBoxModel boolTexts;
    protected JLabel tblLbl;
    protected TableModel tblModel;
    protected JTable tblTbl;
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
            log.debug("NOT setting fields to match!");
        }
    }
    
    public UploadMatchSettingsPanel(final Vector<UploadTable> tables, boolean readOnly)
    {
        this.tables = tables;
        
        modeTexts = new DefaultComboBoxModel(UploadMatchSetting.getModeTexts());
        
        boolTexts = new DefaultComboBoxModel();
        boolTexts.addElement(Boolean.toString(Boolean.TRUE)); //i18n ?
        boolTexts.addElement(Boolean.toString(Boolean.FALSE));  //i18n ?
        
        Vector<String> headers = new Vector<String>();
        headers.add(getResourceString("SL_TABLES"));
        headers.add(getResourceString("WB_MATCH_MODE_CAPTION"));
        headers.add(getResourceString("WB_UPLOAD_MATCH_REMEMBER_CAPTION"));
        headers.add(getResourceString("WB_UPLOAD_MATCH_FLDS_CAPTION"));
        
//        SortedSet<String> tblNames = new TreeSet<String>();
//        for (UploadTable tbl : tables)
//        {
//            tblNames.add(tbl.toString());
//        }
        Vector<Vector<String>> rows = new Vector<Vector<String>>();
        for (UploadTable tbl : tables)
        {
            Vector<String> row = new Vector<String>();
            row.add(tbl.toString());
            row.add(modeTexts.getElementAt(tbl.getMatchSetting().getMode()).toString());
            row.add(Boolean.toString(tbl.getMatchSetting().isRemember()));
            row.add(getResourceString("WB_UPLOAD_MATCH_ALL"));
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
                    if (col == 1 || col == 2)
                    {
                        JComboBox jBox;
                        if (col == 1)
                        {
                            jBox = new JComboBox(modeTexts);
                        }
                        else
                        {
                            jBox = new JComboBox(boolTexts);
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
        //tblLbl = new JLabel(getResourceString("WB_UPLOAD_MATCH_SETTINGS"));
        //tblLbl.setFont(tblLbl.getFont().deriveFont(Font.BOLD));        
        setLayout(new BorderLayout());
        //add(tblLbl, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(tblTbl, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(sp, BorderLayout.CENTER);
    }    
}
