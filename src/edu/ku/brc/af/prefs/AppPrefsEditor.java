/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.af.prefs;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import java.util.prefs.BackingStoreException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * Allows user to edit a properties file
 * 
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class AppPrefsEditor extends JPanel implements TableModelListener, ListSelectionListener
{
    protected boolean doRemote = false;
    
    protected JTable             table;
    protected PropertyTableModel model; 
    protected Vector<String>     items = new Vector<String>();
    protected AppPreferences     appPrefs;
    protected JButton            removeBtn;
    protected JButton            addBtn;
    
    /**
     * @param isRemote
     */
    public AppPrefsEditor(final boolean isRemote)
    {
        appPrefs = isRemote ? AppPreferences.getRemote(): AppPreferences.getLocalPrefs();         
        for (Enumeration<Object> enumerator = appPrefs.getProperties().keys();enumerator.hasMoreElements();)
        {
            items.add((String)enumerator.nextElement());
        }
        Collections.sort(items);
        
        setLayout(new BorderLayout());
        createUI();
        
    }
    
    /**
     * 
     */
    protected void createUI()
    {
        model = new PropertyTableModel(items);
        table = new JTable(model);
        table.getSelectionModel().addListSelectionListener(this);
       
        add(new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        
        addBtn    = UIHelper.createButton(UIRegistry.getResourceString("AppPrefsEditor.ADD_PROPERTY")); //$NON-NLS-1$
        removeBtn = UIHelper.createButton(UIRegistry.getResourceString("AppPrefsEditor.REMOVE_PROPERTY")); //$NON-NLS-1$
        removeBtn.setEnabled(false);
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g,p,f:p:g", "p,10px")); //$NON-NLS-1$ //$NON-NLS-2$
        CellConstraints cc = new CellConstraints();
        pb.add(addBtn, cc.xy(2, 1));
        pb.add(removeBtn, cc.xy(4, 1));
        
        add(pb.getPanel(), BorderLayout.SOUTH);
        
        removeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                removeItem();
            }
         });
         
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                addItem();
            }
         });
         
    }
    
    public void removeItem()
    {
        int inx = table.getSelectedRow();
        if (inx != -1)
        {
            appPrefs.remove((String)model.getValueAt(inx, 0));
            items.remove(table.getSelectedRow());
            removeBtn.setEnabled(false);
            table.repaint();
            appPrefs.setChanged(true);
            table.getSelectionModel().clearSelection();
            
            try
            {
                appPrefs.flush();
            } catch (BackingStoreException ex)
            {
                
            }
        }
    }
    
    protected void addItem()
    {
        String newKey = "New Item" + items.size(); //$NON-NLS-1$
        items.add(newKey);
        model.fireChange();
        
        // Scroll To New Row
        int row = items.size()-1;
        Rectangle rect = table.getCellRect(row, 0, true);
        table.scrollRectToVisible(rect);
        table.clearSelection();
        table.setRowSelectionInterval(row, row);
        appPrefs.setChanged(true);
        
        try
        {
            appPrefs.flush();
        } catch (BackingStoreException ex)
        {
            
        }
    }
    
    public void tableChanged(TableModelEvent e)
    {
        // do nothing
    }
    
    //--------------------------------
    // ListSelectionListener
    //--------------------------------
    public void valueChanged(ListSelectionEvent e)
    {
        if (!e.getValueIsAdjusting())
        {
            removeBtn.setEnabled(table.getSelectedRow() != -1);
        }
    }
    
    //--------------------------------
    // PropertyTableModel
    //--------------------------------
    public class PropertyTableModel extends DefaultTableModel
    {
        protected Vector<String> rowData = null;
        protected String[]       header  = {"Property", "Value"};

        /**
         * @param rowData
         */
        public PropertyTableModel(final Vector<String> rowData)
        {
            this.rowData = rowData;
        }

        public int getColumnCount()
        {
            return header.length;
        }

        public String getColumnName(int column)
        {
            return header[column];
        }

        public int getRowCount()
        {
            return rowData == null ? 0 : rowData.size();
        }

        public Object getValueAt(int row, int column)
        {
            String key = rowData.get(row);
            return column == 0 ? key : appPrefs.get(key, ""); //$NON-NLS-1$
        }

        public boolean isCellEditable(int row, int column)
        {
            if (column == 0)
            {
                return rowData.get(row).startsWith("New Item"); //$NON-NLS-1$
            }
            return true;
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            return String.class;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            String key = rowData.get(rowIndex);
            if (columnIndex == 0)
            {
                rowData.remove(rowIndex);
                rowData.insertElementAt((String)aValue, rowIndex);
            } else
            {
                appPrefs.put(key, aValue.toString());
            }
        }

        public void fireChange()
        {
            this.fireTableDataChanged();
        }
    }
    
}
