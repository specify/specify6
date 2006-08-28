package edu.ku.brc.af.prefs;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class AppPrefsEditor extends JPanel implements TableModelListener
{
    protected boolean doRemote = false;
    
    protected JTable             table;
    protected PropertyTableModel model; 
    protected Vector<String>     items = new Vector<String>();
    protected AppPreferences     appPrefs;
    
    public AppPrefsEditor(final boolean isRemote)
    {
        appPrefs = isRemote ? AppPreferences.getInstance(): AppPreferences.getLocalPrefs();         
        for (Enumeration enumerator = appPrefs.getProperties().keys();enumerator.hasMoreElements();)
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
       
        add(new JScrollPane(table), BorderLayout.NORTH);
        
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
    }
    
    public void tableChanged(TableModelEvent e)
    {
        if (table.getSelectedRow() != -1)
        {
            
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
