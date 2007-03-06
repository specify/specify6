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
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UICacheManager.getLocalizedMessage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.ui.forms.BusinessRulesIFace;
import edu.ku.brc.ui.forms.DraggableRecordIdentifier;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.forms.Viewable;

public abstract class BaseBusRules implements BusinessRulesIFace
{
    protected List<String> errorList = new Vector<String>();
    protected Class<?>     dataClass;
    
    /**
     * The data class that is used within the busniess rules.
     * @param dataClass the data class
     */
    public BaseBusRules(final Class<?> dataClass)
    {
        this.dataClass = dataClass;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#fillForm(java.lang.Object, edu.ku.brc.ui.forms.Viewable)
     */
    public void fillForm(Object dataObj, Viewable viewable)
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#getDeleteMsg(java.lang.Object)
     */
    public String getDeleteMsg(Object dataObj)
    {
        String title     = "Object";
        if (dataObj instanceof FormDataObjIFace)
        {
            FormDataObjIFace dObj = (FormDataObjIFace)dataObj;
            title = dObj.getIdentityTitle();
        }
        // else
        return getLocalizedMessage("GENERIC_OBJ_DELETED", title);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#getWarningsAndErrors()
     */
    public List<String> getWarningsAndErrors()
    {
        return errorList;
    }

    /**
     * Checks to see if it can be deleted.
     * @param tableName the table name to check
     * @param columnName the column name name to check
     * @param id the Record ID to check
     * @return true means it can be deleted, false means it found something
     */
    protected boolean okToDelete(final String tableName, final String columnName, final long id)
    {
        Connection conn = null;
        Statement  stmt = null;
        try
        {
            conn = DBConnection.getInstance().createConnection();
            stmt = conn.createStatement();

            return okToDelete(conn, stmt, tableName, columnName, id);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try 
            {
                if (stmt != null)
                {
                    stmt.close();
                }
                if (conn != null)
                {
                    conn.close();
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Checks to see if it can be deleted.
     * @param connection db connection
     * @param stmt db statement
     * @param tableName the table name to check
     * @param columnName the column name name to check
     * @param id the Record ID to check
     * @return true means it can be deleted, false means it found something
     */
    protected boolean okToDelete(final Connection connection, 
                                 final Statement  stmt,
                                 final String tableName, 
                                 final String columnName, final long id)
    {
        try
        {
            ResultSet rs = stmt.executeQuery("select count(*) from " + tableName + " where " + tableName + "." + columnName + " = " + id);
            boolean isOK = rs.first() && rs.getInt(1) == 0;
            rs.close();
            return isOK;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        }
        return false; // error on the side of not enabling the delete btn
    }
    
    /**
     * Helper to check a list of tables at one time.
     * @param nameCombos a list of names combinations "table name/Foreign Key name"
     * @param id the id to be checked
     * @return true if ok to delete
     */
    protected boolean okToDelete(final String[] nameCombos, final long id)
    {
        Connection conn = null;
        Statement  stmt = null;
        try
        {
            conn = DBConnection.getInstance().createConnection();
            stmt = conn.createStatement();

            for (int i=0;i<nameCombos.length;i++)
            {
                if (!okToDelete(conn, stmt, nameCombos[i], nameCombos[i+1], id))
                {
                    return false;
                }
                i++;
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try 
            {
                if (stmt != null)
                {
                    stmt.close();
                }
                if (conn != null)
                {
                    conn.close();
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#okToDelete(java.lang.Object)
     */
    public abstract boolean okToDelete(Object dataObj);
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#afterSave(java.lang.Object)
     */
    public void afterSave(Object dataObj)
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#beforeSave(java.lang.Object)
     */
    public void beforeSave(Object dataObj)
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#processBusinessRules(java.lang.Object)
     */
    public STATUS processBusinessRules(Object dataObj)
    {
        errorList.clear();
        
        if (dataObj == null || !(dataObj.getClass() == dataClass))
        {
            return STATUS.Error;
        }       
        return STATUS.OK;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#setObjectIdentity(java.lang.Object, edu.ku.brc.ui.forms.DraggableRecordIdentifier)
     */
    public void setObjectIdentity(Object dataObj, DraggableRecordIdentifier draggableIcon)
    {
        // no op
    }

}
