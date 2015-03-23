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
package edu.ku.brc.specify.toycode.mexconabio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

public class AgentNames
{
    private static final String connStr = "jdbc:mysql://localhost/%s?characterEncoding=UTF-8&autoReconnect=true";
    
    private Connection oldDBConn = null;
    private Connection newDBConn = null;
    
    private String     oldDBName;
    private String     newDBName;
    
    
    /**
     * @param oldDBName
     * @param newDBName
     */
    public AgentNames(String oldDBName, String newDBName)
    {
        super();

        this.oldDBName = oldDBName;
        this.newDBName = newDBName;

        try
        {
            oldDBConn = DriverManager.getConnection(String.format(connStr, oldDBName), "root", "root");
            newDBConn = DriverManager.getConnection(String.format(connStr, newDBName), "root", "root");

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    public void shutdown()
    {
        try
        {
            oldDBConn.close();
            newDBConn.close();
        } catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void parseForFirstLastName(final String str)
    {
        if (StringUtils.contains(str, ','))
        {
            String[] toks     = StringUtils.split(str, ',');
            String   lastName = toks[0].trim();
            String   first    = "";
            if (toks.length > 1)
            {
                first = toks[1].trim();
                //if (StringUtils.contains(first, '.'))
            }
            System.out.println("    ["+first+"]["+lastName+"] " + (StringUtils.contains(first, '.') ? "*" : ""));
        } else
        {
            System.out.println("    ["+str.trim()+"]");
        }
    }
    
    private void parseForNames(final String nameStr)
    {
        String str = nameStr;
        if (StringUtils.contains(str, '\"'))
        {
            str = StringUtils.remove(str, '\"');
        }
        
        if (StringUtils.contains(str, ';'))
        {
            String[] toks = StringUtils.split(str, ';');
            for (String s : toks)
            {
                parseForFirstLastName(s);
            }
        } else
        {
            parseForFirstLastName(str);
            /*String[] toks = str.split("^([-\\w]+)(?:(?:\\s?[,|&]\\s)([-\\w]+)\\s?)*(.*)");   //"((?:[^, &]+\\s*[,&]+\\s*)*[^, &]+)\\s+([^,&]+)");
            for (String s : toks)
            {
                System.out.println("    ["+s+"]");
            }*/
        }
        System.out.println();
    }
    
    /**
     * 
     */
    public void process()
    {
        //String sql = "SELECT collector_name FROM raw WHERE collector_name IS NOT NULL AND collector_name LIKE '%;%' limit 4000,1000";
        String sql = "SELECT collector_name FROM raw WHERE collector_name IS NOT NULL limit 4000,1000";
        try
        {
            Statement stmt  = oldDBConn.createStatement(ResultSet.FETCH_FORWARD, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                String str = rs.getString(1);
                System.out.println("\n"+str);
                parseForNames(str);
            }
            rs.close();
            stmt.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @return
     */
    protected Vector<String> getDatabases()
    {
        Vector<String> names = new Vector<String>();

        String sql = "SELECT `TABLES`.TABLE_SCHEMA FROM `TABLES` WHERE `TABLES`.TABLE_NAME = 'agent'";
        for (Object dbName : BasicSQLUtils.querySingleCol(oldDBConn, sql))
        {
            sql = "SELECT COUNT(*) FROM `TABLES` WHERE `TABLES`.TABLE_NAME = 'spversion' AND `TABLES`.TABLE_SCHEMA = '" + dbName +"'";
            if (BasicSQLUtils.getCount(oldDBConn, sql) == 1)
            {
                if (!dbName.equals("information_schema"))
                {
                    names.add(dbName.toString());
                    System.out.println(dbName.toString());
                }
            }
        }
        
        return names;
    }
    
    /**
     * 
     */
    protected void findBadAgents()
    {
        Connection dbConn  = null;
        try
        {
            Vector<String> names = getDatabases();
            System.out.println("-------------- Bad Agent Databases ------------------");
            for (String dbName : names)
            {
                dbConn = DriverManager.getConnection(String.format(connStr, dbName), "root", "root");
                
                //System.out.println("-> "+dbName);
                String sql = "SELECT COUNT(*) FROM agent WHERE LastName IS NOT NULL AND (LastName LIKE '%;%' OR LastName LIKE '%,%')";
                int cnt = BasicSQLUtils.getCountAsInt(dbConn, sql);
                if (cnt > 1)
                {
                    System.out.println(dbName + " " + cnt);
                    if (cnt > 1)
                    {
                        sql = "SELECT LastName, FirstName, MiddleInitial FROM agent WHERE LastName IS NOT NULL AND (LastName LIKE '%;%' OR LastName LIKE '%,%') LIMIT 0,10";
                        for (Object[] row : BasicSQLUtils.query(dbConn, sql))
                        {
                            String lastName   = (String)row[0];
                            String firstName  = (String)row[1];
                            String middleInit = (String)row[1];
                            
                            lastName = StringUtils.replaceChars(lastName, '\n', ' ');
                            System.out.println("  ["+lastName+"]["+(firstName == null ? "" : firstName)+"]["+(middleInit == null ? "" : middleInit)+"]");
                            parseForNames(lastName);
                        }
                    }
                }
                dbConn.close();
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    public void test()
    {
        String[] lastNames = {"White, Siri & Lichtw.", "Valle, White and Cafaro", "Luttrell, Davis, and Abe", 
                              "J.E.T., T.A.P., & R.E.N.", "Fenwick, Mark & Stevens, Darren", "Fautin, D. & Cox, S.",
                              "Bruce, N.L. & Thiel, M.", "G. E. Crow, with Barry Hammel, Francisco Morales", "G. E. Crow, D. I. Rivera, C. Charpentier]",
                              "F. Smith, V. Grant & R. Rukavina", "C.A. Taylor, Jr.", "Brad G. Millen, Susan M. Woodward (ROM)",
                              "Continental Shelf Associates, Inc.", };
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        AgentNames an = new AgentNames("information_schema", "kui_fish_dbo_6");
        an.findBadAgents();
        an.shutdown();
        
        //AgentNames an = new AgentNames("plants", "kui_fish_dbo_6");
        //an.process();
        //an.shutdown();
    }

}
