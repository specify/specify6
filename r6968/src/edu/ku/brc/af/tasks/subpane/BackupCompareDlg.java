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
package edu.ku.brc.af.tasks.subpane;

import static edu.ku.brc.ui.UIRegistry.getMostRecentWindow;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import edu.ku.brc.ui.BiColorTableCellRenderer;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 23, 2008
 *
 */
public class BackupCompareDlg extends CustomDialog
{
    protected Vector<Object[]> rowData;
    
    /**
     * @param dialog
     * @param title
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @throws HeadlessException
     */
    public BackupCompareDlg(final Vector<Object[]> rowData) throws HeadlessException
    {
        super((Frame)getMostRecentWindow(), getResourceString("BKCMP_TABLE_TITLE"), true, null);
        
        this.rowData = rowData;
        
        okLabel     = getResourceString("BKCMP_TABLE_RESTORE");
        cancelLabel = getResourceString("Cancel");
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        JTable      table = new JTable(new CompareModel());
        JScrollPane sp    = UIHelper.createScrollPane(table);
        table.setDefaultRenderer(String.class, new BiColorTableCellRenderer(false)); // Not Centered
        table.setDefaultRenderer(Integer.class, new BiColorTableCellRenderer(false)); // Not Centered

        boolean allOK = true;
        for (Object[] rowVals : rowData)
        {
            if (((Integer)rowVals[3]) > 0)
            {
                allOK = false;
                break;
            }
        }
        
        JLabel desc  = UIHelper.createI18NLabel(allOK ? "BKCMP_TABLE_OK" : "BKCMP_TABLE_DIF");
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(desc, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel = panel;
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        pack();
    }
    

    class CompareModel extends DefaultTableModel
    {
        protected String[] headers = new String[4];
        
        public CompareModel()
        {
            String[] keys = {"BKCMP_TABLE","BKCMP_OLD","BKCMP_NEW","BKCMP_DIFF",};
            int i = 0;
            for (String key : keys)
            {
                headers[i++] = getResourceString(key); 
            }
        }

        @Override
        public int getColumnCount()
        {
            return 4;
        }

        @Override
        public String getColumnName(int column)
        {
            return headers[column];
        }

        @Override
        public int getRowCount()
        {
            return rowData == null ? 0 : rowData.size();
        }

        @Override
        public Object getValueAt(int row, int column)
        {
            Object[] rowVals = rowData.get(row);
            return rowVals[column];
        }

        @Override
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return columnIndex == 0 ? String.class : Integer.class;
        }
        
    }
}
