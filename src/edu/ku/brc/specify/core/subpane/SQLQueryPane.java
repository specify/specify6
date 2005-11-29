/* Filename:    $RCSfile: SQLQueryPane.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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

package edu.ku.brc.specify.core.subpane;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.dbsupport.SQLExecutionListener;
import edu.ku.brc.specify.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.specify.ui.db.ResultSetTableModel;

/**
 * 
 * @author rods
 *
 */
public class SQLQueryPane extends BaseSubPane implements SQLExecutionListener
{
    //private static Log log = LogFactory.getLog(SQLQueryPane.class);

    private JTextField           queryField;
    private JTable               table;
    private JButton              exeBtn;
    
    private SQLExecutionProcessor sqlExecutor;
    
    /**
     * Default Constructor
     *
     */
    public SQLQueryPane(final String name, 
                        final Taskable task, 
                        final boolean hideSQLField)
    {
        super(name, task);
        
        setLayout(new BorderLayout());
        
        setPreferredSize(new Dimension(600,600));
        
        
        // builder.add(viewPanel, cc.xy(1, 1));
        
        /*String msg = "This message was sent to you from Specify 6.0. Note the attachment, an Excel Spreadsheet. It was created from a Query within Specify.\n" +
        "This means that after performing a query the user can simply click on a button to send the results 'as an MS-Excel speadsheet' to a user.\n\n" +
        "I think it is also possible that we will be able to read back in Excel spreadsheets also.\n\n" + 
        "This also means we will be able to easily email Accession objects from Lori to the Collection Managers, etc.\n\nRod";
        EMailHelper.sendMsg("imap.ku.edu", "rods", "xxxxx", "rods@ku.edu",  "rods@ku.edu, beach@ku.edu, megkumin@ku.edu, abentley@ku.edu",  "Catalog Items You Requested", msg, TableModel2Excel.convertToExcel("Collection Items", table.getModel()));
        */
        
        table = new JTable();
        
        JPanel sqlPanel = new JPanel(new BorderLayout());
        queryField = new JTextField();
        queryField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (queryField.getText().length() > 0)
                {
                    doQuery();
                }
                
            }
          });
        
        sqlPanel.add(new JLabel("SQL:"), BorderLayout.WEST);
        sqlPanel.add(queryField, BorderLayout.CENTER);
        exeBtn = new JButton(getResourceString("Execute"));
        
        exeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                doQuery();
            }
        });
        
        sqlPanel.add(exeBtn, BorderLayout.EAST);

               
        if (!hideSQLField)
        {
            add(sqlPanel, BorderLayout.NORTH);
        }
        add(new JScrollPane(table), BorderLayout.CENTER);
        
    }
    
    /**
     * Enables the Text Field and the Execute Button
     *
     */
    public void enableUI(boolean enabled)
    {
        exeBtn.setEnabled(enabled);
        queryField.setEnabled(enabled);
    }
    
    /**
     * Creates an sql executor and has it execute the query on a separate thread
     *
     */
    public void doQuery()
    {
        enableUI(false);
        if (sqlExecutor != null)
        {
            sqlExecutor.close();
        }
        
        if (table.getModel() instanceof ResultSetTableModel)
        {
            ResultSetTableModel model = (ResultSetTableModel)table.getModel();
            if (model != null)
            {
                model.clear();
            }
        }
        sqlExecutor = new SQLExecutionProcessor(this, queryField.getText());
        sqlExecutor.setAutoCloseConnection(false);
        sqlExecutor.start();
    }
    
    /**
     * Sests a SQL string to be executed
     * @param sqlStr the sql string to be executed
     */
    public void setSQLStr(final String sqlStr)
    {
        queryField.setText(sqlStr);
    }
    
    /**
     * Cleans up any remaining data objects
     */
    public void finalize()
    {
        if (sqlExecutor != null)
        {
            sqlExecutor.close();
        }
        sqlExecutor = null;
    }
    
    //-----------------------------------------------------
    //-- SQLExecutionListener
    //-----------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.specify.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
     */
    public void exectionDone(final SQLExecutionProcessor process, final java.sql.ResultSet resultSet)
    {
        
        table.setModel(new ResultSetTableModel(resultSet));
        sqlExecutor = null;
        enableUI(true);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.SQLExecutionListener#executionError(edu.ku.brc.specify.dbsupport.SQLExecutionProcessor, java.lang.Exception)
     */
    public void executionError(final SQLExecutionProcessor process, final Exception ex)
    {
        sqlExecutor = null;
        enableUI(true);
        
        JOptionPane.showMessageDialog(this, ex.toString(), "SQL Error", JOptionPane.ERROR_MESSAGE); // XXX LOCALIZE
    }

    
}
