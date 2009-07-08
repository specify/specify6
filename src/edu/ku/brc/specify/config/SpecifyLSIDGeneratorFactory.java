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
package edu.ku.brc.specify.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.GenericLSIDGeneratorFactory;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.ui.UIRegistry;


/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 9, 2009
 *
 */
public class SpecifyLSIDGeneratorFactory extends GenericLSIDGeneratorFactory
{
    protected static String PREF_NAME_PREFIX = "Prefs.LSID.";
    protected static int[]  TABLE_IDS = {1, 4, 3, 100, 2, 5, 69, 51};

    protected StringBuilder errMsg   = new StringBuilder();
    protected Boolean       isReady  = null;
    
    protected String        lsidAuthority = null;
    protected String        instCode = null;
    protected String        colCode  = null;

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.CollectionObjLSIDGenFactory#isReady()
     */
    @Override
    public boolean isReady()
    {
        if (isReady == null)
        {
            errMsg.setLength(0);
            Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
            if (inst != null)
            {
                lsidAuthority = inst.getLsidAuthority();
                if (StringUtils.isEmpty(lsidAuthority))
                {
                    errMsg.append("LSID Authority is empty.\n");  // I18N
                }
                instCode = inst.getCode();
                if (StringUtils.isEmpty(instCode))
                {
                    errMsg.append("Institution Code is empty.\n");  // I18N
                }
            } else
            {
                errMsg.append("Institution cannot be null to generate the LSID.\n");  // I18N
                return isReady = false;
            }
            
            Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
            if (collection != null)
            {
                colCode = collection.getCode();
                if (StringUtils.isEmpty(colCode))
                {
                    errMsg.append("Collection Code is empty.\n");  // I18N
                }
            } else
            {
                errMsg.append("Collection cannot be null to generate the LSID.\n");  // I18N
                return isReady = false;
            }
            isReady = errMsg.length() == 0;
        }
        return isReady;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.CollectionObjLSIDGenFactory#getErrorMsg()
     */
    @Override
    public String getErrorMsg()
    {
        return super.getErrorMsg();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.CollectionObjLSIDGenFactory#reset()
     */
    @Override
    public void reset()
    {
        isReady = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.CollectionObjLSIDGenFactory#getLSID(java.lang.String)
     */
    @Override
    public String createLSID(final CATEGORY_TYPE category, final String id)
    {
        if (isReady() && category != null && StringUtils.isNotEmpty(id))
        {
            return String.format("urn:lsid:%s:%s-%s-%s:%s", lsidAuthority, instCode, colCode, category.toString(), id);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.GenericLSIDGeneratorFactory#getLSID(edu.ku.brc.af.core.GenericLSIDGeneratorFactory.CATEGORY_TYPE, java.lang.String, int)
     */
    @Override
    public String createLSID(final CATEGORY_TYPE category, final String id, final int version)
    {
        if (isReady() && category != null && StringUtils.isNotEmpty(id))
        {
            return String.format("urn:lsid:%s:%s-%s-%s:%s:%d", lsidAuthority, instCode, colCode, category.toString(), id, version);
        }
        return super.createLSID(category, id, version);
    }
    
    /**
     * @param tableId the table id
     * @return the Category for a table id or null
     */
    protected CATEGORY_TYPE getCategoryFromTableId(final int tableId)
    {
        for (CATEGORY_TYPE cat : CATEGORY_TYPE.values())
        {
            if (tableId == TABLE_IDS[cat.ordinal()])
            {
                return cat;
            }
        } 
        return null;
    }
    
    /**
     * @param tableId
     * @return
     */
    public boolean isPrefOn(final int tableId)
    {
        CATEGORY_TYPE cat = getCategoryFromTableId(tableId);
        if (cat != null)
        {
            return AppPreferences.getRemote().getBoolean(PREF_NAME_PREFIX + cat.toString(), false);
        }
        return false;
    }
    
    /**
     * 
     */
    public void buildLSIDs(final PropertyChangeListener pcl)
    {
        UIFieldFormatterIFace formatter = null;
        DBFieldInfo fi = DBTableIdMgr.getInstance().getInfoById(1).getFieldByColumnName("CatalogNumber");
        formatter = fi.getFormatter();
        if (!formatter.isInBoundFormatter())
        {
            formatter = null;
        }
        
        reset();
        
        if (isReady())
        {
            boolean doVersioning = AppPreferences.getRemote().getBoolean(PREF_NAME_PREFIX + "UseVersioning", false);
            
            int count = 1;
            for (CATEGORY_TYPE cat : CATEGORY_TYPE.values())
            {
                String pName = PREF_NAME_PREFIX + cat.toString();
                
                if (pcl != null)
                {
                    pcl.propertyChange(new PropertyChangeEvent(this, "COUNT", 0, count++));
                }
                
                
                if (AppPreferences.getRemote().getBoolean(pName, false))
                {
                    Statement stmt    = null;
                    Statement updStmt = null;

                    try
                    {
                        stmt    = DBConnection.getInstance().getConnection().createStatement();
                        updStmt = DBConnection.getInstance().getConnection().createStatement();

                        buildLSIDs(cat, doVersioning, formatter, stmt, updStmt);
                        
                    } catch (SQLException ex)
                    {
                         ex.printStackTrace();
                         
                    } finally
                    {
                        if (pcl != null)
                        {
                            pcl.propertyChange(new PropertyChangeEvent(this, "COUNT", 0, count));
                        }
                        
                        try
                        {
                            if (stmt != null)
                            {
                                stmt.close();
                            }
                            if (updStmt != null)
                            {
                                updStmt.close();
                            }
                        } catch (SQLException e) {}
                    }
                }
                count++;
            }
        } else
        {
            UIRegistry.showError(errMsg.toString());
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.GenericLSIDGeneratorFactory#setLSIDOnId(edu.ku.brc.af.ui.forms.FormDataObjIFace, boolean, edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace)
     */
    @Override
    public String setLSIDOnId(final FormDataObjIFace      data,
                              final boolean               doVersioning,
                              final UIFieldFormatterIFace formatter)
    {
        CATEGORY_TYPE category = getCategoryFromTableId(data.getTableId());
        
        boolean isColObj = category == CATEGORY_TYPE.Specimen;
        
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(data.getTableId());
        if (tableInfo != null)
        {
            String primaryColumn = StringUtils.capitalize(tableInfo.getIdFieldName());
            
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM ");
            sb.append(tableInfo.getName());
            sb.append(" WHERE ");
            sb.append(primaryColumn);
            sb.append(" = ");
            sb.append(data.getId());
            
            Integer count = BasicSQLUtils.getCount(sb.toString());
            if (count != null && count > 0)
            {
                sb.setLength(0);
                sb.append("SELECT ");
                sb.append("Version");
                if (isColObj)
                {
                    sb.append(", CatalogNumber");
                }
                sb.append(" FROM ");
                sb.append(tableInfo.getName());
                sb.append(" WHERE ");
                sb.append(primaryColumn);
                sb.append(" = ");
                sb.append(data.getId());
                
                Statement stmt    = null;
                Statement updStmt = null;
                ResultSet rs      = null;
                try
                {
                    stmt    = DBConnection.getInstance().getConnection().createStatement();
                    updStmt = DBConnection.getInstance().getConnection().createStatement();
                    rs      = stmt.executeQuery(sb.toString());
                    while (rs.next())
                    {
                        int     version = rs.getInt(1);
                        String  catNum  = isColObj ? rs.getString(2) : null;
                        
                        if (!isColObj || StringUtils.isNotEmpty(catNum))
                        {
                            String idStr = data.getId().toString();
                            if (isColObj)
                            {
                                idStr = (String)formatter.formatToUI(catNum);
                            }
                            
                            String lsid;
                            if (doVersioning)
                            {
                                lsid = createLSID(category, idStr, version);
                            } else
                            {
                                lsid = createLSID(category, idStr);
                            }
                            sb.setLength(0);
                            sb.append("UPDATE ");
                            sb.append(tableInfo.getName());
                            sb.append(" SET GUID='");
                            sb.append(lsid);
                            sb.append("' WHERE ");
                            sb.append(primaryColumn);
                            sb.append(" = ");
                            sb.append(data.getId());
                            
                            //System.err.println(sb.toString());
                            
                            int rv = updStmt.executeUpdate(sb.toString());
                            return rv == 1 ? lsid : null;
                        }
                    }
                    rs.close();
                    
                } catch (SQLException ex)
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
                        if (updStmt != null)
                        {
                            updStmt.close();
                        }
                    } catch (SQLException e) {}
                }
            }
        }
        return null;
    }
    
    /**
     * @param category
     * @param doVersioning
     * @param formatter
     * @param stmt
     * @param updStmt
     * @return
     */
    private int buildLSIDs(final CATEGORY_TYPE category, 
                           final boolean       doVersioning,
                           final UIFieldFormatterIFace formatter,
                           final Statement     stmt,
                           final Statement     updStmt)
    {
        boolean isColObj = category == CATEGORY_TYPE.Specimen;
        
        Integer count = 0;
        
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(TABLE_IDS[category.ordinal()]);
        if (tableInfo != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM ");
            sb.append(tableInfo.getName());
            sb.append(" WHERE GUID IS NULL");
            
            count = BasicSQLUtils.getCount(sb.toString());
            if (count != null && count > 0)
            {
                sb.setLength(0);
                sb.append("SELECT ");
                sb.append(tableInfo.getIdFieldName());
                sb.append(", Version");
                if (isColObj)
                {
                    sb.append(", CatalogNumber");
                }
                sb.append(" FROM ");
                sb.append(tableInfo.getName());
                sb.append(" WHERE GUID IS NULL");
                
                //System.err.println(sb.toString());
                
                try
                {
                    ResultSet rs = stmt.executeQuery(sb.toString());
                    while (rs.next())
                    {
                        Integer id      = rs.getInt(1);
                        int     version = rs.getInt(2);
                        String  catNum  = isColObj ? rs.getString(3) : null;
                        
                        if (!isColObj || StringUtils.isNotEmpty(catNum))
                        {
                            String idStr = id.toString();
                            if (isColObj)
                            {
                                idStr = (String)formatter.formatToUI(catNum);
                            }
                            
                            String lsid;
                            if (doVersioning)
                            {
                                lsid = createLSID(category, idStr, version);
                            } else
                            {
                                lsid = createLSID(category, idStr);
                            }
                            sb.setLength(0);
                            sb.append("UPDATE ");
                            sb.append(tableInfo.getName());
                            sb.append(" SET GUID='");
                            sb.append(lsid);
                            sb.append("' WHERE ");
                            sb.append(tableInfo.getIdFieldName());
                            sb.append(" = ");
                            sb.append(id);
                            
                            //System.err.println(sb.toString());
                            
                            updStmt.executeUpdate(sb.toString());
                        }
                    }
                    rs.close();
                    
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return count;
    }
}
