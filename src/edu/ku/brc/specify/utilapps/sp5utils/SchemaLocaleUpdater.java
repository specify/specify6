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
package edu.ku.brc.specify.utilapps.sp5utils;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 16, 2009
 *
 */
public class SchemaLocaleUpdater extends CustomDialog
{
    protected JTable                table;
    protected DefaultTableModel     tblModel;
    protected Vector<FormFieldInfo> fields = new Vector<FormFieldInfo>();
    protected FormInfo              formInfo;
    
    protected JButton               hideAll;
    protected JButton               showAll;
    protected JButton               showSp5Items;

    
    /**
     * @param dialog
     * @param title
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @throws HeadlessException
     */
    public SchemaLocaleUpdater(final FormInfo formInfo) throws HeadlessException
    {
        super((Frame)null, "Update Schema Localizer", true, OKCANCEL, null);
        
        HashMap<String, FormFieldInfo> ffiHash = new HashMap<String, FormFieldInfo>();
        for (FormFieldInfo ffi : formInfo.getFields())
        {
            this.fields.add(ffi);
            ffiHash.get(ffi.getSp6FieldName());
        }
        
        DBTableInfo tblInfo = formInfo.getTblInfo();
        if (tblInfo != null)
        {
            for (DBFieldInfo fldInfo : tblInfo.getFields())
            {
                if (StringUtils.isNotEmpty(fldInfo.getColumn()))
                {
                    if (ffiHash.get(fldInfo.getColumn()) == null)
                    {
                        FormFieldInfo ffi = new FormFieldInfo(fldInfo.getColumn(), fldInfo.getTitle(), fldInfo.isHidden());
                        this.fields.add(ffi);
                        ffiHash.put(fldInfo.getColumn(), ffi);
                    }
                }
            }
            for (DBRelationshipInfo relInfo : tblInfo.getRelationships())
            {
                if (StringUtils.isNotEmpty(relInfo.getColName()))
                {
                    FormFieldInfo ffi = new FormFieldInfo(relInfo.getColName(), relInfo.getTitle(), relInfo.isHidden());
                    this.fields.add(ffi);
                    ffiHash.put(relInfo.getColName(), ffi);
                }
            }
        }
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        CellConstraints cc = new CellConstraints();

        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "p,2px,f:p:g,4px,p"));
        
        table = new JTable(tblModel = new FieldsModel());
        
        pb.add(UIHelper.createScrollPane(table), cc.xy(1, 3));
        
        PanelBuilder pbBtn = new PanelBuilder(new FormLayout("f:p:g,p,4px,p,4px,p", "p"));
        pbBtn.add(hideAll = UIHelper.createButton("Hide All"), cc.xy(2, 1));
        pbBtn.add(showAll = UIHelper.createButton("Show All"), cc.xy(4, 1));
        pbBtn.add(showSp5Items = UIHelper.createButton("Show Sp5 Fields"), cc.xy(6, 1));
        pb.add(pbBtn.getPanel(), cc.xy(1, 5));

        pb.setDefaultDialogBorder();

        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        hideAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (FormFieldInfo ffi : fields)
                {
                    ffi.setIsHidden(true);
                }
                tblModel.fireTableDataChanged();
            }
        });
        
        showAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
               for (FormFieldInfo ffi : fields)
               {
                   ffi.setIsHidden(false);
               }
               tblModel.fireTableDataChanged();
            }
        });
        
        showSp5Items.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (FormFieldInfo ffi : fields)
                {
                    if (ffi.getSp5FieldName() != null)
                    {
                        ffi.setIsHidden(false);
                    }
                }
                tblModel.fireTableDataChanged();
            }
        });
    }
    
    /**
     * 
     */
    protected void updateSchema()
    {
        DBConnection newConn = Sp5Forms.getNewDBConnection();
        
        if (newConn != null)
        {
            newConn.close();
        }
    }
    
    //---------------------------------------------------------------------
    //
    //---------------------------------------------------------------------
    class FieldsModel extends DefaultTableModel
    {
        protected String[] header = {"Sp6 Caption", "Sp6 Name", "Hidden", };
        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            return header != null ? header.length : 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            return header != null ? header[column] : "";
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return fields != null ? fields.size() : 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int row, int column)
        {
            FormFieldInfo ffi = fields.get(row);
            switch (column)
            {
                case 0: return ffi.getCaption();
                case 1: return ffi.getSp6FieldName();
                case 2: return ffi.getIsHidden();
            }
            return "";
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return column == 0 || column == 2;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
         */
        @Override
        public void setValueAt(Object value, int row, int column)
        {
            FormFieldInfo ffi = fields.get(row);
            switch (column)
            {
                case 0: ffi.setCaption((String)value);
                    break;
                    
                case 1: ffi.setSp6FieldName((String)value);
                    break;
                    
                case 2: ffi.setIsHidden((Boolean)value);
                    break;
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int column)
        {
            switch (column)
            {
                case 0: return String.class;
                case 1: return String.class;
                case 2: return Boolean.class;
            }
            return String.class;
        }
    }
}
