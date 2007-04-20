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

package edu.ku.brc.specify.tasks.subpane;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIHelper.centerAndShow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.SQLExecutionListener;
import edu.ku.brc.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.specify.ui.db.ResultSetTableModel;
import edu.ku.brc.specify.ui.db.ResultSetTableModelDM;
import edu.ku.brc.specify.ui.db.SaveRecordSetDlg;
import edu.ku.brc.ui.UIRegistry;
/**
 * A pane with a text field for entring in a query and then the results are displayed in a table.
 * 
 * @code_status Alpha
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class SQLQueryPane extends BaseSubPane implements SQLExecutionListener
{
    //private static final Logger log = Logger.getLogger(SQLQueryPane.class);

    private JTextArea             textArea;
    private JScrollPane           taScrollPane;
    private JTable                table;
    private JButton               exeBtn;
    private JLabel                label;
    
    private JButton               selectAllBtn;
    private JButton               deselectAllBtn;
    private JButton               saveToRSBtn;


    private boolean               hideSQLField;
    private SQLExecutionProcessor sqlExecutor;
    private String                sqlStr;
    
    /**
     * Default Constructor
     * @param name the name of the pane
     * @param task the owning task
     * @param hideSQLField whether to hide the user editable text field that used to enter a query
     * @param hideBtnPanel whether to hide the button panel
     */
    public SQLQueryPane(final String name, 
                        final Taskable task, 
                        final boolean hideSQLField,
                        final boolean hideBtnPanel)
    {
        super(name, task);
        
        setPreferredSize(new Dimension(600,600));
        
        this.hideSQLField = hideSQLField;
        
        
        // builder.add(viewPanel, cc.xy(1, 1));
        
        /*String msg = "This message was sent to you from Specify 6.0. Note the attachment, an Excel Spreadsheet. It was created from a Query within Specify.\n" +
        "This means that after performing a query the user can simply click on a button to send the results 'as an MS-Excel speadsheet' to a user.\n\n" +
        "I think it is also possible that we will be able to read back in Excel spreadsheets also.\n\n" + 
        "This also means we will be able to easily email Accession objects from Lori to the Collection Managers, etc.\n\nRod";
        EMailHelper.sendMsg("imap.ku.edu", "rods", "xxxxx", "rods@ku.edu",  "rods@ku.edu, beach@ku.edu, megkumin@ku.edu, abentley@ku.edu",  "Catalog Items You Requested", msg, TableModel2Excel.convertToExcel("Collection Items", table.getModel()));
        */
        
        table = new JTable();

        if (!hideSQLField)
        {
            textArea     = new JTextArea(80,6);
            taScrollPane = new JScrollPane(textArea);

            taScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            taScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            taScrollPane.setPreferredSize(new Dimension(400,100));
            
            FormLayout      formLayout = new FormLayout("p,2dlu,100dlu:g,2dlu,p", "center:p:g");
            PanelBuilder    builder    = new PanelBuilder(formLayout);
            CellConstraints cc         = new CellConstraints();
           
            exeBtn = new JButton(getResourceString("Execute"));
            exeBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) 
                {
                    doQuery();
                }
            });

            builder.add(label = new JLabel("SQL:"), cc.xy(1,1));
            builder.add(taScrollPane, cc.xy(3,1));
            builder.add(exeBtn, cc.xy(5,1));
            
            add(builder.getPanel(), BorderLayout.NORTH);
            
            add(new JScrollPane(table), BorderLayout.CENTER);            
        }
        
        if (!hideBtnPanel)
        {
            FormLayout      formLayout = new FormLayout("p,2dlu,p,2dlu,p", "center:p:g");
            PanelBuilder    builder    = new PanelBuilder(formLayout);
            CellConstraints cc         = new CellConstraints();
            
            selectAllBtn   = new JButton(getResourceString("SelectAll"));
            deselectAllBtn = new JButton(getResourceString("DeselectAll"));
            saveToRSBtn    = new JButton(getResourceString("SaveToRecordSet"));
            
            builder.add(selectAllBtn,   cc.xy(1,1));
            builder.add(deselectAllBtn, cc.xy(3,1));
            builder.add(saveToRSBtn,    cc.xy(5,1));
            
            add(builder.getPanel(), BorderLayout.SOUTH);
            
            selectAllBtn.addActionListener(new ActionListener()
                    {  public void actionPerformed(ActionEvent ae) { table.selectAll(); } });

   
            deselectAllBtn.addActionListener(new ActionListener()
                    {  public void actionPerformed(ActionEvent ae) { table.clearSelection();} });
              
            saveToRSBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) 
                {
                    ResultSetTableModelDM resultSet = new ResultSetTableModelDM(((ResultSetTableModel)table.getModel()).getResultSet());
                    SaveRecordSetDlg dlg = new SaveRecordSetDlg(resultSet, table.getSelectedRows());
                   
                    centerAndShow(dlg);
                }
            });
            enableUI(true);
        }
    }
    
    /**
     * Enables the Text Field and the Execute Button
     * @param enabled enabled
     */
    public void enableUI(boolean enabled)
    {
        if (!hideSQLField)
        {
            exeBtn.setEnabled(enabled);
            textArea.setEnabled(enabled);
            label.setEnabled(enabled);
            
            if (selectAllBtn != null)
            {
                boolean en = enabled && table.getModel() != null && table.getModel().getRowCount() > 0;
    
                selectAllBtn.setEnabled(en);
                deselectAllBtn.setEnabled(en && table.getSelectedRowCount() > 0);
                saveToRSBtn.setEnabled(en);
            }
        }
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
        
        progressLabel.setText(getResourceString("QueryStarting"));
        
        String queryStr = getSQLStr();
        // clean up SQL from text control
        queryStr = queryStr.replace("\r\n", " ");
        queryStr = queryStr.replace("\r", " ");
        queryStr = queryStr.replace("\n", " ");
        sqlExecutor = new SQLExecutionProcessor(this, queryStr);
        sqlExecutor.setAutoCloseConnection(false);
        sqlExecutor.start();
    }
    
    /**
     * Sests a SQL string to be executed
     * @param sqlStr the sql string to be executed
     */
    public void setSQLStr(final String sqlStr)
    {
        this.sqlStr = sqlStr;
        if (textArea != null)
        {
            textArea.setText(sqlStr);
        }
    }
    
    /**
     * Returns the string represetning the SQL
     * @return Returns the string represetning the SQL
     */
    public String getSQLStr()
    {
        return textArea == null ? sqlStr : textArea.getText();
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
     * @see edu.ku.brc.af.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.af.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
     */
    public void exectionDone(final SQLExecutionProcessor process, final java.sql.ResultSet resultSet)
    {
        if (this.hideSQLField)
        {
            removeAll();
            add(new JScrollPane(table), BorderLayout.CENTER);            
        }
        table.setModel(new ResultSetTableModel(resultSet));
        
        table.setRowSelectionAllowed(true);
        //table.getSelectionModel().addListSelectionListener(this);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        TableModel model = table.getModel();
        for (int i=0;i<model.getColumnCount();i++) 
        {
            TableColumn column = table.getColumn(model.getColumnName(i));
            column.setCellRenderer(renderer);
        }

        sqlExecutor = null;
        enableUI(true);
        
        UIRegistry.forceTopFrameRepaint();    
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.dbsupport.SQLExecutionListener#executionError(edu.ku.brc.af.dbsupport.SQLExecutionProcessor, java.lang.Exception)
     */
    public void executionError(final SQLExecutionProcessor process, final Exception ex)
    {
        sqlExecutor = null;
        enableUI(true);
        
        JOptionPane.showMessageDialog(this, ex.toString(), "SQL Error", JOptionPane.ERROR_MESSAGE); // XXX LOCALIZE
    }

    
}
