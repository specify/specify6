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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import edu.ku.brc.ui.UICacheManager;

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
    
    protected void createUI()
    {
        model = new PropertyTableModel(items);
        table = new JTable(model);
        table.getSelectionModel().addListSelectionListener(this);
       
        add(new JScrollPane(table), BorderLayout.NORTH);
        
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        
        removeBtn = UICacheManager.createButton("Remove Property");
        removeBtn.setEnabled(false);
        add(removeBtn, BorderLayout.SOUTH);
        
        removeBtn.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e)
           {
               removeItem();
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
    
    public class PropertyTableModel implements TableModel
    {
        protected Vector<TableModelListener> listeners = new Vector<TableModelListener>();
        protected Vector<String> rowData;
        protected String[]       header = {"Property", "Value"};

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
            return rowData.size();
        }

        public Object getValueAt(int row, int column)
        {
            String key = rowData.get(row);
            return column == 0 ? key : appPrefs.get(key, "");
        }

        public boolean isCellEditable(int row, int column)
        {
            return column == 1;
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            return String.class;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            String key = rowData.get(rowIndex);
            appPrefs.put(key, aValue.toString());
        }

        public void addTableModelListener(TableModelListener l)
        {
            listeners.add(l);
        }

        public void removeTableModelListener(TableModelListener l)
        {
            listeners.remove(l);
        }

    }
    
}
