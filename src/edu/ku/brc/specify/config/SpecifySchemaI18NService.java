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
import java.util.Locale;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBInfoBase;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;

/**
 * This class gets all the L10N string from the database for a locale and populates the DBTableInfo etc structures.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Oct 3, 2007
 *
 */
public class SpecifySchemaI18NService extends SchemaI18NService
{
    private static final Logger      log      = Logger.getLogger(SpecifySchemaI18NService.class);
    
    protected Stack<Vector<String>>  recycler = new Stack<Vector<String>>();
    protected Vector<Vector<String>> results  = new Vector<Vector<String>>();
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SchemaI18NService#loadWithLocale(java.lang.Byte, int, edu.ku.brc.dbsupport.DBTableIdMgr, java.util.Locale)
     */
    @Override
    public void loadWithLocale(final Byte         schemaType, 
                               final int          collectionTypeId,
                               final DBTableIdMgr mgr, 
                               final Locale       locale)
    {
        String sql = "SELECT splocalecontainer.Name, Text, IsHidden FROM splocalecontainer INNER JOIN splocaleitemstr ON " +
                     "splocalecontainer.SpLocaleContainerID = splocaleitemstr.SpLocaleContainerNameID where Language = '"+locale.getLanguage()+"' AND " +
                     "splocalecontainer.SchemaType = " + schemaType +" AND splocalecontainer.CollectionTypeID = " + collectionTypeId;
        
        retrieveString(sql);
        
        for (Vector<String> p : results)
        {
            DBTableInfo ti = mgr.getInfoByTableName(p.get(0));
            if (ti != null)
            {
                ti.setTitle(p.get(1));
                ti.setHidden(!p.get(2).equals("0"));
                
            } else
            {
                log.error("Couldn't find table ["+p.get(0)+"]");
            }
        }
        
        sql = "SELECT splocalecontainer.Name,Text FROM splocalecontainer INNER JOIN splocaleitemstr ON " +
              "splocalecontainer.SpLocaleContainerID = splocaleitemstr.SpLocaleContainerDescID where Language = '"+locale.getLanguage()+"' AND " +
              "splocalecontainer.SchemaType = " + schemaType +" AND splocalecontainer.CollectionTypeID = " + collectionTypeId;
        
        retrieveString(sql);
        for (Vector<String> p : results)
        {
            DBTableInfo ti = mgr.getInfoByTableName(p.get(0));
            if (ti != null)
            {
                ti.setDescription(p.get(1));
            } else
            {
                log.error("Couldn't find table ["+p.get(0)+"]");
            }
        }
        
        sql = "SELECT splocalecontainer.Name,splocalecontaineritem.Name,splocalecontaineritem.Format, splocalecontaineritem.IsUIFormatter,splocaleitemstr.Text "+
              "FROM splocalecontainer INNER JOIN splocalecontaineritem ON splocalecontainer.SpLocaleContainerID = splocalecontaineritem.SpLocaleContainerID "+
              "INNER JOIN splocaleitemstr ON splocalecontaineritem.SpLocaleContainerItemID = splocaleitemstr.SpLocaleContainerItemNameID "+
              " where splocaleitemstr.Language = '"+locale.getLanguage()+"' AND " +
              "splocalecontainer.SchemaType = " + schemaType +" AND splocalecontainer.CollectionTypeID = " + collectionTypeId + " order by splocalecontainer.Name";
        
        retrieveString(sql);
        
        String      name = "";
        DBTableInfo ti   = null;
        for (Vector<String> p : results)
        {
            if (!name.equals(p.get(0)))
            {
                ti = mgr.getInfoByTableName(p.get(0));
                name = p.get(0);
            }
            
            if (ti != null)
            {
                DBInfoBase fi = ti.getItemByName(p.get(1));
                if (fi != null)
                {
                    fi.setTitle(p.get(4));
                    
                } else
                {
                    log.error("Couldn't find field["+p.get(1)+"] for table ["+p.get(0)+"]");
                }
                
                if (fi instanceof DBFieldInfo)
                {
                    String format   = p.get(2);
                    boolean isUIFmt = p.get(3) == null ? false : !p.get(3).equals("0");
                    
                    DBFieldInfo fieldInfo = (DBFieldInfo)fi;
                    if (isUIFmt)
                    {
                        UIFieldFormatterIFace formatter = UIFieldFormatterMgr.getFormatter(format);
                        if (formatter != null)
                        {
                            fieldInfo.setFormatter(formatter);
                        } else
                        {
                            log.error("Couldn't find UIFieldFormatter with name ["+format+"]");
                        }
                        
                    } else if (StringUtils.isNotEmpty(format))
                    {
                        fieldInfo.setFormatStr(format);
                    }
                }
                
            } else
            {
                log.error("Couldn't find table ["+p.get(0)+"]");
            }
        }

        
        sql = "SELECT splocalecontainer.Name, splocalecontaineritem.Name, splocaleitemstr.Text, splocalecontaineritem.IsHidden "+
              "FROM splocalecontainer INNER JOIN splocalecontaineritem ON splocalecontainer.SpLocaleContainerID = splocalecontaineritem.SpLocaleContainerID "+
              "INNER JOIN splocaleitemstr ON splocalecontaineritem.SpLocaleContainerItemID = splocaleitemstr.SpLocaleContainerItemDescID "+
              " where splocaleitemstr.Language = '"+locale.getLanguage()+"' AND " +
              "splocalecontainer.SchemaType = " + schemaType +" AND splocalecontainer.CollectionTypeID = " + collectionTypeId + " order by splocalecontainer.Name";
        
        retrieveString(sql);
        
        name = "";
        ti   = null;
        for (Vector<String> p : results)
        {
            if (!name.equals(p.get(0)))
            {
                ti = mgr.getInfoByTableName(p.get(0));
                name = p.get(0);
            }
            
            if (ti != null)
            {
                DBInfoBase fi = ti.getItemByName(p.get(1));
                if (fi != null)
                {
                    fi.setDescription(p.get(2));
                    fi.setHidden(!p.get(3).equals("0"));
                    
                } else
                {
                    log.error("Couldn't find field["+p.get(1)+"] for table ["+p.get(0)+"]");
                }
            } else
            {
                log.error("Couldn't find table ["+p.get(0)+"]");
            }
        }
        
        results.clear();
        recycler.clear();
    }
    
    /**
     * Retrieves the results of the query and puts them into a Vector. The Vector for each row is either created new or grabbed from a recycler.
     * @param sql the SQL to execute
     */
    protected void retrieveString(final String sql)
    {
        if (results.size() > 0)
        {
            recycler.addAll(results);
            results.clear();
        }
        
        log.debug(sql);
        
        Connection connection = null;
        Statement stmt        = null;
        ResultSet rs          = null;
        try
        {
            connection = DBConnection.getInstance().createConnection();
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery(sql);
            
            int cnt = rs.getMetaData().getColumnCount();
            
            if (rs.next())
            {
                do
                {
                    Vector<String> p;
                    if (recycler.size() > 0)
                    {
                        p = recycler.pop();
                        p.clear();
                        
                    } else
                    {
                        p = new Vector<String>();
                    }
                    for (int i=0;i<cnt;i++)
                    {
                        p.add(rs.getString(i+1));
                    }
                    results.add(p);
                    
                    /* DEBUG
                    if (p.get(0).toLowerCase().startsWith("coll"))
                    {
                        for (String s : p)
                        {
                            System.out.print(s+", ");
                        }
                        System.out.println("");
                    }*/
                    
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
}
