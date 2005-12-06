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

import java.awt.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.ui.*;
import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.dbsupport.SQLExecutionListener;
import edu.ku.brc.specify.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.specify.ui.db.ResultSetTableModel;

/**
 * A pane with a text field for entring in a query and then the results are displayed in a table.
 * 
 * @author rods
 *
 */
public class SQLQueryPane extends BaseSubPane implements SQLExecutionListener
{
    //private static Log log = LogFactory.getLog(SQLQueryPane.class);

    private JTextField            textField;
    private JTextArea             textArea;
    private JScrollPane           taScrollPane;
    private JTextComponent        text;
    private JTable                table;
    private JButton               exeBtn;
    
    private JButton               toggleBtn;
    private ImageIcon             upIcon;
    private ImageIcon             dwnIcon;
    
    private JPanel                txtPanel;
    private JPanel                btnPanel;
    private boolean               hideSQLField;
    private SQLExecutionProcessor sqlExecutor;
    private String                sqlStr;
    
    /**
     * Default Constructor
     *
     */
    public SQLQueryPane(final String name, 
                        final Taskable task, 
                        final boolean hideSQLField)
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
            btnPanel = new JPanel(new BorderLayout());
            txtPanel = new JPanel(new BorderLayout());
            
            textField = new JTextField();
            textField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    if (textField.getText().length() > 0)
                    {
                        doQuery();
                    }
                    
                }
              });
            
            textArea = new JTextArea(80,6);
            taScrollPane = new JScrollPane(textArea);

            taScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            taScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            taScrollPane.setPreferredSize(new Dimension(400,100));
            
            text = textField;
            
            FormLayout      formLayout = new FormLayout("p,2dlu,100dlu:g,2dlu,p", "center:p:g");
            PanelBuilder    builder    = new PanelBuilder(formLayout);
            CellConstraints cc         = new CellConstraints();
           
            txtPanel.add(textField, BorderLayout.CENTER);
            
            JPanel sp = new JPanel(new BorderLayout());
            
            exeBtn = new JButton(getResourceString("Execute"));
            exeBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) 
                {
                    doQuery();
                }
            });
            
            upIcon    = IconManager.getInstance().getIcon("Green Arrow Down", IconManager.IconSize.Std16);
            dwnIcon   = IconManager.getInstance().getIcon("Green Arrow Up", IconManager.IconSize.Std16);
            toggleBtn = new JButton(dwnIcon);
            toggleBtn.setBorder(new EmptyBorder(2,2,2,2));
            toggleBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) 
                {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() 
                        {
                   toggleTextControls();

                        }
                    });  
                }
            });
            
            sp.add(toggleBtn, BorderLayout.WEST);
            sp.add(exeBtn, BorderLayout.EAST);
            btnPanel.add(sp, BorderLayout.EAST);

            builder.add(new JLabel("SQL:"), cc.xy(1,1));
            builder.add(txtPanel, cc.xy(3,1));
            builder.add(btnPanel, cc.xy(5,1));
            
            add(builder.getPanel(), BorderLayout.NORTH);
            
            add(new JScrollPane(table), BorderLayout.CENTER);            
        }
        
    }
    
    /**
     * Enables the Text Field and the Execute Button
     *
     */
    public void enableUI(boolean enabled)
    {
        if (!hideSQLField)
        {
            exeBtn.setEnabled(enabled);
            toggleBtn.setEnabled(enabled);
            text.setEnabled(enabled);
        }
    }
    
    /**
     * Enables the Text Field and the Execute Button
     *
     */
    public void toggleTextControls()
    {
        if (text == textField)
        {
            text = textArea;
            toggleBtn.setIcon(upIcon);
            txtPanel.remove(textField);
            txtPanel.add(taScrollPane, BorderLayout.CENTER);
            
        } else
        {
            text = textField;
            toggleBtn.setIcon(dwnIcon);
            txtPanel.remove(taScrollPane);
            txtPanel.add(text, BorderLayout.CENTER);
        }


        text.invalidate();
        doLayout();
        
        /*txtPanel.invalidate();
        btnPanel.invalidate();
        txtPanel.getParent().invalidate();
        txtPanel.getParent().getParent().invalidate();
        taScrollPane.invalidate();
        invalidate();
        
        text.doLayout();
        txtPanel.doLayout();
        btnPanel.doLayout();
        txtPanel.getParent().doLayout();
        txtPanel.getParent().getParent().doLayout();
        taScrollPane.doLayout();
        doLayout();
        
        Rectangle rect = getBounds();
        RepaintManager mgr = RepaintManager.currentManager(this);
        mgr.addDirtyRegion((JComponent)this, rect.x, rect.y, rect.width, rect.height);

        rect = taScrollPane.getBounds();
        mgr = RepaintManager.currentManager(taScrollPane);
        mgr.addDirtyRegion((JComponent)taScrollPane, rect.x, rect.y, rect.width, rect.height);*/

        // do the following on the gui thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() 
            {
                
                /*txtPanel.invalidate();
                txtPanel.doLayout();
                txtPanel.repaint();
                btnPanel.doLayout();
                btnPanel.repaint();
                txtPanel.getParent().doLayout();
                txtPanel.getParent().repaint();
                */
                //doLayout();
                
                text.repaint();
                txtPanel.repaint();
                btnPanel.repaint();
                txtPanel.getParent().repaint();
                txtPanel.getParent().getParent().repaint();
                taScrollPane.repaint();
                
                
                repaint();

            }
        });    

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
        
        if(text != null)
        {
            text.setText(sqlStr);
        }
    }
    
    /**
     * Returns the string represetning the SQL
     * @return Returns the string represetning the SQL
     */
    public String getSQLStr()
    {
        return text == null ? sqlStr : text.getText();
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
        if (this.hideSQLField)
        {
            removeAll();
            add(new JScrollPane(table), BorderLayout.CENTER);            
        }
        table.setModel(new ResultSetTableModel(resultSet));
        sqlExecutor = null;
        enableUI(true);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() 
            {
                invalidate();
                doLayout();
                repaint();
            }
        });    
        
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
