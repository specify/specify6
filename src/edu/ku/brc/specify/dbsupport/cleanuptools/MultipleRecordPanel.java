/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.ResultSetController;
import edu.ku.brc.af.ui.forms.ResultSetControllerListener;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 6, 2012
 *
 */
public class MultipleRecordPanel extends JPanel implements ResultSetControllerListener
{
    protected MultipleRecordComparer mrc;
    
    protected boolean             isParent;
    protected DBTableInfo         tblInfo;
    protected DBTableInfo         parentTI;
    protected FindItemInfo        fii;
    protected int                 tableId;
    protected List<DBFieldInfo>   columns;
    protected Vector<JComponent>  fields               = new Vector<JComponent>();
    protected boolean[]           hasData              = null;
    private boolean               isSingleRowIncluded  = false; 
    
    protected Vector<Object[]>    dataItems            = new Vector<Object[]>();
    protected Vector<Boolean[]>   chosenItems          = new Vector<Boolean[]>();
    protected Vector<Boolean>     isSelected           = new Vector<Boolean>();
    protected JCheckBox[]         isSelChkBox          = new JCheckBox[] { null, null};
    protected boolean             ignoreSelections     = false;
    
    protected SubstDouble2DecimalRenderer dblRenderer  = new SubstDouble2DecimalRenderer(4);
    protected ChangeListener      changeListener       = null;
    
    protected JTable              table;
    protected LocKidsTableModel   model;
    
    protected int                 index     = -1;
    protected ResultSetController rs;
    
    
    /**
     * @param fii
     * @param parentTableId
     * @param tableId
     */
    public MultipleRecordPanel(final MultipleRecordComparer mrc)
    {
        super();
        this.mrc = mrc;
        
        this.columns   = mrc.getColumns();
        this.dataItems = mrc.getDataItems();
        this.tblInfo   = mrc.getTblInfo();
        this.isParent  = mrc.isParent();
        
        
        for (Object[] row : dataItems)
        {
            Boolean[] chosenRow = new Boolean[row.length];
            for (int i=0;i<chosenRow.length;i++) chosenRow[i] = false;
            chosenItems.add(chosenRow);
        }
    }
    
    /**
     * @param isSingleRowIncluded the isSingleRowIncluded to set
     */
    public void setSingleRowIncluded(boolean isSingleRowIncluded)
    {
        this.isSingleRowIncluded = isSingleRowIncluded;
    }

    /**
     * @return the isSingleRowIncluded
     */
    public boolean isSingleRowIncluded()
    {
        return isSingleRowIncluded;
    }

    /**
     * 
     */
    public void createUI()
    {
        rs = new ResultSetController(null, false, false, false, tblInfo.getTitle(), dataItems.size(), true);
        
        ChangeListener changeListener = new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                System.out.println("ignoreSelections "+ignoreSelections);
                if (!ignoreSelections)
                {
                    ignoreSelections = true;
                    
                    JCheckBox chkBx   = (JCheckBox)e.getSource();
                    if (isParent)
                    {
                        Object[]  rowData = dataItems.get(index);
                        if (chkBx == isSelChkBox[0])
                        {
                            rowData[0] = chkBx.isSelected(); 
                            if (isParent) rowData[1] = !chkBx.isSelected(); 
                        } else
                        {
                            rowData[1] = chkBx.isSelected(); 
                            rowData[0] = !chkBx.isSelected(); 
                        }
                    } else if (isSingleRowIncluded)
                    {
                        for (int i=0;i<dataItems.size();i++)
                        {
                            dataItems.get(i)[0] = false;
                        }
                        dataItems.get(index)[0] = chkBx.isSelected();
                    }
                    ignoreSelections = false;
                    
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            model.fireTableDataChanged();
                            fillForm();
                            notifyChangeListener();
                        }
                    });
                }
            }
        };
        
        CellConstraints   cc     = new CellConstraints();
        String            rowDef = UIHelper.createDuplicateJGoodiesDef("p", "4px", columns.size() + (mrc.isParent() ? 1 : 0));
        PanelBuilder      pbForm = new PanelBuilder(new FormLayout("p,2px,f:p:g", rowDef));
        
        int y = 1;
        
        /*if (mrc.isParent())
        {
            System.out.println("["+mrc.getTitle()+"]");
            JLabel lbl = UIHelper.createLabel(mrc.getTitle());
            pbForm.add(lbl,  cc.xyw(1, y, 3));
            y += 2;
        }*/
        
        final int CB_INX = 1;
        for (DBFieldInfo fi : columns)
        {
            JComponent comp    = null;
            String     lblText = " ";
            if (fi.getDataClass() != Boolean.class)
            {
                //System.out.println(fi.getDataClass()+"  "+fi.getTitle());
                lblText = fi.getTitle() + ":";
                JLabel fld = UIHelper.createLabel("  ");
                fld.setBackground(Color.WHITE);
                fld.setOpaque(true);
                comp = fld;
                
            } else if (y == CB_INX || (isParent && y == (CB_INX+2)))
            {
                JCheckBox chkBx = UIHelper.createCheckBox(fi.getTitle());
                isSelChkBox[y == CB_INX ? 0 : 1] = chkBx;
                chkBx.addChangeListener(changeListener);
                comp = chkBx;
            } else
            {
                JCheckBox chkbox = UIHelper.createCheckBox(fi.getTitle());
                comp = chkbox;
            }
            
            JLabel lbl = UIHelper.createLabel(lblText, SwingConstants.RIGHT);
            pbForm.add(lbl,  cc.xy(1, y));
            pbForm.add(comp, cc.xy(3, y));
            fields.add(comp);
            y += 2;
        }
        int margin = 8;
        pbForm.setBorder(BorderFactory.createEmptyBorder(margin, margin, margin, margin));
        
        PanelBuilder pbRS = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
        pbRS.add(rs.getPanel(), cc.xy(2, 1));
        
        JScrollPane sp = new JScrollPane(pbForm.getPanel(), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
                                         ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        model = new LocKidsTableModel();
        table = new JTable(model);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JCheckboxTableCellRenderer cellRenderer = new JCheckboxTableCellRenderer();
        
        DefaultCellEditor defRenderer = new DefaultCellEditor(new JCheckBox())
        {
            @Override
            public Component getTableCellEditorComponent(JTable table,
                                                         Object value,
                                                         boolean isSelected,
                                                         int row,
                                                         int column)
            {
                JCheckBox cbx =  (JCheckBox)super.getTableCellEditorComponent(table, value, isSelected, row, column);
                System.out.println("Edit: "+value+"  "+row+"  col "+column+"  "+cbx.isSelected());
                
                Boolean[] isSelRow = chosenItems.get(row);
                isSelRow[column] = !isSelRow[column];
                cbx.setSelected(isSelRow[column]);

                if (value instanceof Boolean || value == null)
                {
                    cbx.setSelected((Boolean)value);
                    cbx.setText("");
                } else if (value instanceof String)
                {
                    cbx.setText((String)value);
                }
                return cbx;
            }
            
        };
        
        for (int i=0;i<table.getColumnModel().getColumnCount();i++)
        {
            table.getColumnModel().getColumn(i).setCellEditor(defRenderer);
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
           
        }
        //UIHelper.calcColumnWidths(table, 5); // 5 - is the number visible rows
        //UIHelper.autoResizeColWidth(table, model);
        UIHelper.setVisibleRowCount(table, 5);
        UIHelper.makeTableHeadersCentered(table, false);
        JScrollPane tblSP = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting() && table.getSelectedRow() > -1)
                {
                    ignoreSelections = true;
                    rs.setIndex(table.getSelectedRow());
                    ignoreSelections = false;
                }
            }
        });
        
        SubstDouble2DecimalRenderer celRendr = new SubstDouble2DecimalRenderer(4);
        celRendr.setHorizontalTextPosition(SwingConstants.CENTER);
        //System.out.println(String.format("%d  %d", columns.size(), table.getColumnModel().getColumnCount()));
        for (int i=0;i<columns.size();i++) // no column for Id
        {
            DBFieldInfo fi = columns.get(i);
            if (fi.getDataClass() == Float.class || 
                fi.getDataClass() == Double.class || 
                fi.getDataClass() == BigDecimal.class)
            {
                table.getColumnModel().getColumn(i).setCellRenderer(celRendr);
            }
        }
        
        // Main Panel
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,8px,p,4px,p"), this);
        pb.add(tblSP, cc.xy(1, 1));
        pb.add(sp, cc.xy(1, 3));
        pb.add(pbRS.getPanel(), cc.xy(1, 5));
        
        for (JCheckBox chkBox : isSelChkBox)
        {
            if (chkBox != null)
            {
                chkBox.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        updateSelectedValues();
                    }
                });
            }
        }
        
        rs.addListener(this);
        
        for (int i=0;i<dataItems.size();i++)
        {
            isSelected.add(Boolean.FALSE);
        }
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                indexChanged(0);
            }
        });
    }
    
    /**
     * @return whether the user has selected the rows properly for processing.
     */
    public boolean isDataValid()
    {
        if (isParent)
        {
            int cntInto = 0;
            int cntFrom = 0;
            for (int i=0;i<dataItems.size();i++)
            {
                Object[] rd = dataItems.get(i);
                cntInto += (Boolean)rd[0] ? 1 : 0;
                cntFrom += (Boolean)rd[1] ? 1 : 0;
            }
            if (dataItems.size() == 2)
            {
                return cntInto == 1 && cntFrom == 1;
            } else
            {
                return cntInto == 1 && cntFrom > 0;
            }
        }
        return true;
    }
    
    /**
     * 
     */
    private void updateSelectedValues()
    {
        for (int i=0;i<isSelected.size();i++)
        {
            isSelected.setElementAt(false, i);
        }
        isSelected.setElementAt(true, index);
    }
    
    /**
     * 
     */
    protected void fillForm()
    {
        if (dataItems.size() > 0 && index > -1)
        {
            int fldNum = 0;
            Object[] values = dataItems.get(index);
            for (int i=0;i<fields.size();i++)
            {
                Object val = values[i];
                if (val == null) val = "";
                //System.out.println(fldNum+"  ["+val+"]");
                
                JComponent comp = fields.get(fldNum);
                if (val instanceof Float || 
                    val instanceof Double || 
                    val instanceof BigDecimal)
                {
                    ((JLabel)comp).setText(dblRenderer.formatValue(val).toString());
                } else if (comp instanceof JLabel)
                {
                    ((JLabel)comp).setText(val.toString());
                } else 
                {
                    
                    ignoreSelections = true;
                    ((JCheckBox)comp).setSelected(getBooleanFromObj(val));
                    ignoreSelections = false;
                }
                fldNum++;
            }
            isSelChkBox[0].setSelected(getBooleanFromObj(values[0]));//isSelected.get(index));
            if (isParent) isSelChkBox[1].setSelected(getBooleanFromObj(values[1]));
        }
    }
    
    /**
     * @param val
     * @return
     */
    private boolean getBooleanFromObj(final Object val)
    {
        if (val == null) return false;
        
        if (val instanceof Boolean) return (Boolean)val;
        
        if (val instanceof String)
        {
            String vStr = (String)val;
            return vStr.equalsIgnoreCase("true");
        }
        
        if (val instanceof Integer) return ((Integer)val) != 0;
        
        return false;
    }
    
    /**
     * 
     */
    private void notifyChangeListener()
    {
        if (changeListener != null)
        {
            changeListener.stateChanged(new ChangeEvent(this));
        }
    }
    
    /**
     * @param changeListener the changeListener to set
     */
    public void setChangeListener(ChangeListener changeListener)
    {
        this.changeListener = changeListener;
    }
    
    public List<MergeInfoItem> getMergeInfo()
    {
        Vector<MergeInfoItem> items = new Vector<MergeInfoItem>();
        
        for (Object[] rowData : dataItems)
        {
            boolean isMergedInto = isParent ? (Boolean)rowData[0] : false;
            boolean isMergedFrom = isParent ? (Boolean)rowData[1] : false;
            boolean isIncluded   = !isParent ? (Boolean)rowData[0] : false;
            Integer id           = (Integer)rowData[rowData.length-1];
            
            items.add(new MergeInfoItem(id, isMergedInto, isMergedFrom, isIncluded));
        }
        return items;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.ResultSetControllerListener#indexChanged(int)
     */
    @Override
    public void indexChanged(int newIndex)
    {
        index = newIndex;
        fillForm();
        
        if (!ignoreSelections)
        {
            table.getSelectionModel().setSelectionInterval(index, index);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.ResultSetControllerListener#indexAboutToChange(int, int)
     */
    @Override
    public boolean indexAboutToChange(int oldIndex, int newIndex)
    {
        //isSelected.setElementAt(isSelChkBox.isSelected(), oldIndex);
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.ResultSetControllerListener#newRecordAdded()
     */
    @Override
    public void newRecordAdded()
    {
    }
    
    class JCheckboxTableCellRenderer extends JCheckBox implements TableCellRenderer
    {
        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column)
        {
            Boolean[] isSelRow = chosenItems.get(row);
            //isSelRow[column] = !isSelRow[column];
            setSelected(isSelRow[column]);
            if (value instanceof Boolean)
            {
                setText("");
            } else if (value instanceof String)
            {
                setText((String)value);
            }
            return this;
        }
        
    };
    //--------------------------------------------------------------------------
    class LocKidsTableModel extends DefaultTableModel
    {
        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            return columns.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            return columns.get(column).getTitle();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return dataItems.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int row, int column)
        {
            Object[] rowValues = dataItems.get(row);
            return rowValues[column];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            //System.out.println(String.format("%d %d  %s", row, column, (column < (mrc.isParent() ? 2 : 1) ? "Y" : "N")));
            return true;//column < (mrc.isParent() ? 2 : 1);
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int col)
        {
            return columns.get(col).getDataClass();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
         */
        @Override
        public void setValueAt(Object obj, int row, int column)
        {
            System.out.println(obj+"  "+row+"  col "+column);
            
//            System.out.println("isSingleRowIncluded: "+isSingleRowIncluded+"  "+mrc.isParent());
//            Object[] rowData = dataItems.get(row);
//            if (mrc.isParent())
//            {
//                Boolean value = (Boolean)obj;
//                if (value)
//                {
//                    if (column == 0)
//                    {
//                        for (int i=0;i<dataItems.size();i++)
//                        {
//                            dataItems.get(i)[0] = false;
//                        }
//                        Object[] rd = dataItems.get(row);
//                        rd[0] = true;
//                        rd[1] = false;
//                        
//                    } else if (column == 1)
//                    {
//                        Object[] rd = dataItems.get(row);
//                        if ((Boolean)rd[0])
//                        {
//                            rd[0] = false;
//                        }
//                        rd[1] = true;
//                    }
//                } else
//                {
//                    Object[] rd = dataItems.get(row);
//                    if (column == 0)
//                    {
//                        rd[0] = false;
//                    } else
//                    {
//                        rd[1] = false;
//                    }
//                }
//                
//            } else if (isSingleRowIncluded)
//            {
//                for (int i=0;i<dataItems.size();i++)
//                {
//                    dataItems.get(i)[0] = false;
//                }
//                dataItems.get(row)[0] = (Boolean)obj;
//            } else
//            {
//                rowData[column] = obj;
//            }
//            
//            
//            SwingUtilities.invokeLater(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    fireTableDataChanged();
//                    fillForm();
//                    notifyChangeListener();
//                }
//            });

        }
    }
}
