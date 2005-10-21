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

package edu.ku.brc.specify.ui.db;

import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.*;
import javax.swing.table.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.ui.*;
import edu.ku.brc.specify.dbsupport.*;
import edu.ku.brc.specify.helpers.*;


import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;

public class SQLQueryPane extends JPanel implements MainPaneIFace
{
    public static final String paneName = "SQL Query Pane";
            
    private static Log log = LogFactory.getLog(SQLQueryPane.class);

    private JTextField           queryField;
    private ResultSetTableModel  tableModel;
    private JTable               table;
    
    private Connection         dbConnection = null;
    private Statement          dbStatement  = null;
    
    /**
     * 
     *
     */
    public SQLQueryPane()
    {
        setLayout(new BorderLayout());
        setBackground(Color.GREEN);
        
        setPreferredSize(new Dimension(600,600));
        
        //FormLayout      formLayout = new FormLayout("p,5dlu,p", "p,2dlu,fill:p,2dlu,p,2dlu");
        //PanelBuilder    builder    = new PanelBuilder(formLayout);
        //CellConstraints cc         = new CellConstraints();
        
        // builder.add(viewPanel, cc.xy(1, 1));
        
        ResultSet rs = doSpecial();
        table = new JTable(new ResultSetTableModel(rs));
        
        String msg = "This message was sent to you from Specify 6.0. Note the attachment, an Excel Spreadsheet. It was created from a Query within Specify.\n" +
        "This means that after performing a query the user can simply click on a button to send the results 'as an MS-Excel speadsheet' to a user.\n\n" +
        "I think it is also possible that we will be able to read back in Excel spreadsheets also.\n\n" + 
        "This also means we will be able to easily email Accession objects from Lori to the Collection Managers, etc.\n\nRod";
        
        //EMailHelper.sendMsg("imap.ku.edu", "rods", "xxxxx", "rods@ku.edu",  "rods@ku.edu, beach@ku.edu, megkumin@ku.edu, abentley@ku.edu",  "Catalog Items You Requested", msg, TableModel2Excel.convertToExcel("Collection Items", table.getModel()));
        
        add(new JScrollPane(table), BorderLayout.CENTER);

    }
    
    public ResultSet doSpecial()
    {
        try
        {
            dbConnection = getConnection();    
            dbStatement = dbConnection.createStatement();
            
            String returnString = null;
            StringBuffer sql = new StringBuffer("");
    
            /*
            sql.append("Select ");
            sql.append("collectionobj.catalogNumber, ");
            sql.append("collectionobjectcatalog.CollectionObjectCatalogID, ");
            sql.append("collectionobjectcatalog.CatalogNumber ");
            sql.append("From ");
            sql.append("collectionobj ");
            sql.append("Where ");
            sql.append("collectionobjectcatalog.CatalogNumber = '27332' ");
            */
            sql.append("Select ");
            sql.append("collectionobjectcatalog.CatalogNumber,");
            sql.append("collectionobject.CollectionObjectID,");
            //sql.append("collectionobject.DerivedFromID,");
            sql.append("taxonname.TaxonName ");
            sql.append("From ");
            sql.append("collectionobject ");
            sql.append("Inner Join collectionobjectcatalog ON collectionobject.CollectionObjectID = collectionobjectcatalog.CollectionObjectCatalogID ");
            sql.append("Inner Join determination ON determination.BiologicalObjectID = collectionobjectcatalog.CollectionObjectCatalogID ");
            sql.append("Inner Join taxonname ON determination.TaxonNameID = taxonname.TaxonNameID ");
            sql.append("where taxonname.TaxonName = \"lutrensis\"");
                    
            String sqlStr = sql.toString();
            
            return dbStatement.executeQuery(sqlStr);
            
            /*while (rs.next()) {
                System.out.println("["+rs.getString("collectionobjectcatalog.CatalogNumber") + "]["+rs.getInt("collectionobject.CollectionObjectID") + "]["+rs.getInt("collectionobjectcatalog.CollectionObjectCatalogID")+"]");
                //String password = rs.getString("PASSWORD");
            }
            stmt.close();
            con.close();
            */
             
            
        } catch (Exception ex)
        {
            log.error("Error in special", ex);
        }
        return null;
    }
    
    private Connection getConnection()
    {
        // instead of using JDBC we should use Hibernates connection
        Connection con = null;
        try
        {
            String dbUserid = "rods"; // Your Database user id
            String dbPassword = "rods" ; // Your Database password
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost/demo_fish", dbUserid ,dbPassword );
            
        } catch (Exception ex)
        {
            log.error("Error in special", ex);
        }
        return con;
    }
    
    //----------------------------------
    // MainPaneIFace
    //----------------------------------
    
    public void aboutToShow()
    {
        
    }
    
    public void aboutToHide()
    {
        
    }

}
