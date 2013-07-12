/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.toycode;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Stack;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 26, 2009
 *
 */
public class COLTaxaMaker
{
    protected String[] levelNames = {"Kingdom","Phylum","Class","Superfamily","Order","Family","Genus","Infraspecies","Species"};
    protected Connection   connection = null;
    protected PrintWriter pw;
    
    protected Stack<String> tree = new Stack<String>();
    protected int           total = 0;
    
    /**
     * 
     */
    public COLTaxaMaker()
    {
        super();
    }

    
    protected void build()
    {
        String dbName           = "CoL2008AC"; 
        String itUsername       = "root";
        String itPassword       = "root";
        
       
        
        DBConnection colDBConn  = null;
        Statement    stmt       = null;
        try
        {
            pw = new PrintWriter(new File("taxa.csv"));
            
            DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver("MySQL");
            String             connStr    = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, "localhost", dbName, itUsername, itPassword, driverInfo.getName());
            
            colDBConn  = DBConnection.createInstance(driverInfo.getDriverClassName(), driverInfo.getDialectClassName(), dbName, connStr, itUsername, itPassword);
            connection = colDBConn.createConnection();
            stmt       = connection.createStatement();
            
            doLevel(0, 0);
            
            stmt.close();
            colDBConn.close();
            
            pw.close();
            
            System.out.println("Done "+total);
            
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param level
     */
    protected void printSpaces(final int level)
    {
        for (int i=0;i<level;i++)
        {
            System.out.print("  ");
        }
    }
    
    protected void doLevel(final int parentId, final int level) throws SQLException
    {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT record_id, taxon, name FROM taxa WHERE parent_id = "+parentId);
        int cnt = 0;
        while (rs.next())
        {
            printSpaces(level);
            tree.push(rs.getString(3));
            doLevel(rs.getInt(1), level+1);
            tree.pop();
            cnt++;
        }
        
        if (cnt == 0)
        {
            for (int i=0;i<tree.size();i++)
            {
                pw.append(tree.get(i));
                if (i < tree.size()-1)
                {
                    pw.append(',');
                }
            }
            //pw.append(rs.getString(3));
            pw.append('\n');
            total++;
            if (total % 10000 == 0)
            {
                System.out.println(total);
                pw.flush();
            }
            if (total == -1)
            {
                pw.close();
                System.exit(0);
            }
        }
        rs.close();
        stmt.close();
    }
    
    public static void main(String[] args)
    {
        COLTaxaMaker maker = new COLTaxaMaker();
        maker.build();
    }
}
