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
/**
 * 
 */
package edu.ku.brc.specify.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBInfoBase;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpLocaleContainerItem;
import edu.ku.brc.specify.datamodel.SpLocaleItemStr;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 3, 2007
 *
 */
public class SpecifySchemaI18NService extends SchemaI18NService
{
    private static final Logger      log      = Logger.getLogger(SpecifySchemaI18NService.class);
    
    protected Stack<Triple<String, String, String>>  recycler = new Stack<Triple<String, String, String>>();
    protected Vector<Triple<String, String, String>> results  = new Vector<Triple<String, String, String>>();
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SchemaI18NService#loadWithLocale(java.util.Locale)
     */
    @Override
    public void loadWithLocale(Locale locale)
    {
        String sql = "SELECT splocalecontainer.Name,Text FROM splocalecontainer INNER JOIN splocaleitemstr ON " +
        "splocalecontainer.SpLocaleContainerID = splocaleitemstr.SpLocaleContainerNameID where Language = '"+locale.getLanguage()+"'";
        
        retrieveString(sql);
        
        DBTableIdMgr mgr = DBTableIdMgr.getInstance();
        for (Triple<String, String, String> p : results)
        {
            DBTableInfo ti = mgr.getInfoByTableName(p.first);
            if (ti != null)
            {
                ti.setTitle(p.second);
            } else
            {
                log.error("Couldn't find table ["+p.first+"]");
            }
        }
        
        sql = "SELECT splocalecontainer.Name,Text FROM splocalecontainer INNER JOIN splocaleitemstr ON " +
        "splocalecontainer.SpLocaleContainerID = splocaleitemstr.SpLocaleContainerDescID where Language = '"+locale.getLanguage()+"'";
        
        retrieveString(sql);
        for (Triple<String, String, String> p : results)
        {
            DBTableInfo ti = mgr.getInfoByTableName(p.first);
            if (ti != null)
            {
                ti.setDescription(p.second);
            } else
            {
                log.error("Couldn't find table ["+p.first+"]");
            }
        }
        
        sql = "SELECT splocalecontainer.Name,splocalecontaineritem.Name, splocaleitemstr.Text "+
              "FROM splocalecontainer INNER JOIN splocalecontaineritem ON splocalecontainer.SpLocaleContainerID = splocalecontaineritem.SpLocaleContainerID "+
              "INNER JOIN splocaleitemstr ON splocalecontaineritem.SpLocaleContainerItemID = splocaleitemstr.SpLocaleContainerItemNameID order by splocalecontainer.Name";
        
        retrieveString(sql);
        
        String      name = "";
        DBTableInfo ti   = null;
        for (Triple<String, String, String> p : results)
        {
            if (!name.equals(p.first))
            {
                ti = mgr.getInfoByTableName(p.first);
                name = p.first;
            }
            
            if (ti != null)
            {
                DBInfoBase fi = ti.getItemByName(p.second);
                if (fi != null)
                {
                    ti.setTitle(p.third);
                    
                } else
                {
                    log.error("Couldn't find field["+p.second+"] for table ["+p.first+"]");
                }
            } else
            {
                log.error("Couldn't find table ["+p.first+"]");
            }
        }

        
        sql = "SELECT splocalecontainer.Name,splocalecontaineritem.Name, splocaleitemstr.Text "+
              "FROM splocalecontainer INNER JOIN splocalecontaineritem ON splocalecontainer.SpLocaleContainerID = splocalecontaineritem.SpLocaleContainerID "+
              "INNER JOIN splocaleitemstr ON splocalecontaineritem.SpLocaleContainerItemID = splocaleitemstr.SpLocaleContainerItemDescID order by splocalecontainer.Name";
        
        retrieveString(sql);
        
        name = "";
        ti   = null;
        for (Triple<String, String, String> p : results)
        {
            if (!name.equals(p.first))
            {
                ti = mgr.getInfoByTableName(p.first);
                name = p.first;
            }
            
            if (ti != null)
            {
                DBInfoBase fi = ti.getItemByName(p.second);
                if (fi != null)
                {
                    ti.setDescription(p.third);
                    
                } else
                {
                    log.error("Couldn't find field["+p.second+"] for table ["+p.first+"]");
                }
            } else
            {
                log.error("Couldn't find table ["+p.first+"]");
            }
        }
        
        results.clear();
        recycler.clear();
    }
    
    /**
     * @param locale
     * @param sql
     * @param results
     */
    protected void retrieveString(final String sql)
    {
        if (results.size() > 0)
        {
            recycler.addAll(results);
            results.clear();
        }
        
        Connection connection = null;
        Statement stmt        = null;
        ResultSet rs          = null;
        try
        {
            connection = DBConnection.getInstance().createConnection();
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery(sql);
            
            boolean isTriple = rs.getMetaData().getColumnCount() == 3;
            
            if (rs.first())
            {
                do
                {
                    Triple<String, String, String> p;
                    if (recycler.size() > 0)
                    {
                        p        = recycler.pop();
                        p.first  = rs.getString(1);
                        p.second = rs.getString(2);
                        
                        if (isTriple)
                        {
                            p.third = rs.getString(3);
                        }
                        
                    } else
                    {
                        p = new Triple<String, String, String>(rs.getString(1), rs.getString(2), isTriple ? rs.getString(3) : null);
                    }
                    results.add(p);
                    
                } while (rs.next());
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (stmt != null)
                {
                    stmt.close();
                }
                if (connection != null)
                {
                    connection.close();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * @param locale
     */
    protected void loadNames(final Locale locale)
    {


    }
    
    //---------------------------------------------------------------
    //-- Inner Class
    //---------------------------------------------------------------
    public class Triple<F, S, T>
    {
        /** The first value in the <code>Triple</code>. */
        public F first = null;
        
        /** The second value in the <code>Triple</code>. */
        public S second = null;
        
        /** The second value in the <code>Triple</code>. */
        public T third = null;
        
        /**
         * Construct a new <code>Pair</code> with the given values.
         * 
         * @param first the value of <code>first</code>
         * @param second the value of <code>second</code>
         */
        public Triple(F first, S second, T third)
        {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }
}
