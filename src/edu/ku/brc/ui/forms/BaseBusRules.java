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
package edu.ku.brc.ui.forms;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Feb 14, 2008
 *
 */
public abstract class BaseBusRules implements BusinessRulesIFace
{
    protected Viewable     viewable    = null;
    protected FormViewObj  formViewObj = null;
    protected List<String> reasonList  = new Vector<String>();
    protected Class<?>[]   dataClasses;
    
    /**
     * The data class that is used within the busniess rules.
     * @param dataClass the data class
     */
    public BaseBusRules(final Class<?> ... dataClasses)
    {
        this.dataClasses = dataClasses;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#initialize(edu.ku.brc.ui.forms.Viewable)
     */
    public void initialize(final Viewable viewableArg)
    {
        viewable = viewableArg;
        if (viewable instanceof FormViewObj)
        {
            formViewObj = (FormViewObj)viewable;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#beforeFormFill(edu.ku.brc.ui.forms.Viewable)
     */
    //@Overrided
    public void beforeFormFill(final Viewable viewable)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#fillForm(java.lang.Object, edu.ku.brc.ui.forms.Viewable)
     */
    public void afterFillForm(final Object dataObj, final Viewable viewable)
    {
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#addChildrenToNewDataObjects(java.lang.Object)
     */
    public void addChildrenToNewDataObjects(final Object newDataObj)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#shouldCreateDataForField(java.lang.String)
     */
    public boolean shouldCreateSubViewData(String fieldName)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#getDeleteMsg(java.lang.Object)
     */
    public String getDeleteMsg(final Object dataObj)
    {
        String title = "Object";
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
        return reasonList;
    }

    /**
     * Checks to see if it can be deleted.
     * @param tableName the table name to check
     * @param columnName the column name name to check
     * @param ids the Record IDs to check
     * @return true means it can be deleted, false means it found something
     */
    protected boolean okToDelete(final String tableName, final String columnName, final Integer...ids)
    {
        if (ids != null)
        {
            Connection conn = null;
            Statement  stmt = null;
            try
            {
                conn = DBConnection.getInstance().createConnection();
                stmt = conn.createStatement();
    
                return okToDelete(conn, stmt, tableName, columnName, ids);
                
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
        // else
        return true;
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
    protected boolean okToDelete(@SuppressWarnings("unused") final Connection connection, 
                                                             final Statement  stmt,
                                                             final String tableName, 
                                                             final String columnName,
                                                             final Integer...ids)
    {
        try
        {
            StringBuilder idString = new StringBuilder();
            for (Integer i: ids)
            {
                idString.append(i);
                idString.append(", ");
            }
            idString.deleteCharAt(idString.length()-2);
            String queryString = "select count(*) from " + tableName + " where " + tableName + "." + columnName + " in (" + idString.toString() + ")";
            ResultSet rs = stmt.executeQuery(queryString);
            boolean isOK = rs.next() && rs.getInt(1) == 0;
            rs.close();
            return isOK;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        }
        return false; // err on the side of not enabling the delete btn
    }
    
    /**
     * Helper to check a list of tables at one time.
     * @param nameCombos a list of names combinations "table name/Foreign Key name"
     * @param id the id to be checked
     * @return true if ok to delete
     */
    protected boolean okToDelete(final String[] nameCombos, final Integer...ids)
    {
        Connection conn = null;
        Statement  stmt = null;
        try
        {
            conn = DBConnection.getInstance().createConnection();
            stmt = conn.createStatement();

            for (int i=0;i<nameCombos.length;i++)
            {
                if (!okToDelete(conn, stmt, nameCombos[i], nameCombos[i+1], ids))
                {
                    //log.info("Found["+ nameCombos[i]+"]["+nameCombos[i+1]+"]");
                    DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoByTableName(nameCombos[i]);
                    if (tableInfo != null)
                    {
                        reasonList.add(tableInfo.getTitle());
                    }
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
    
    /**
     * Adds a standard found table message to the reason list.
     * @param tableId the table if where it was found
     */
    protected void addDeleteReason(final int tableId)
    {
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(tableId);
        if (tableInfo != null)
        {
            reasonList.add(tableInfo.getTitle());
        }
    }
    
    /**
     * @param skipTableNames
     * @param idColName
     * @param dataClassObj
     * @return
     */
    protected String[] gatherTableFieldsForDelete(final String[] skipTableNames, 
                                                  final String idColName,
                                                  final Class<?> dataClassObj)
    
    {
        boolean debug = false;
        
        int fieldCnt = 0;
        Hashtable<String, Vector<String>> fieldHash = new Hashtable<String, Vector<String>>();
        
        for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
        {
            Hashtable<String, Boolean> skipHash = new Hashtable<String, Boolean>();
            if (skipTableNames != null)
            {
                for (String name : skipTableNames)
                {
                    skipHash.put(name, true);
                }
            }
            
            if (dataClassObj != null)
            {
                for (DBRelationshipInfo ri : ti.getRelationships())
                {
                    
                    if (ri.getDataClass() == dataClassObj)
                    {
                        String colName = ri.getColName();
                        if (StringUtils.isNotEmpty(colName) && !(skipHash.get(ti.getName()) != null && colName.equals(idColName)))
                        {
                            Vector<String> fieldList = fieldHash.get(ti.getName());
                            if (fieldList == null)
                            {
                                fieldList = new Vector<String>();
                                fieldHash.put(ti.getName(), fieldList);
                            }
                            fieldList.add(ri.getColName());
                            fieldCnt++;
                        }
                    }
                }
            }
        }
        
        if (debug)
        {
            System.out.println("Fields to be checked:");
            for (String tableName : fieldHash.keySet())
            {
                System.out.println(" Table:" + tableName + " ");
                for (String fName : fieldHash.get(tableName))
                {
                    System.out.println("   Field:" + fName); 
                }
            }
        }
        
        int inx = 0;
        String[] tableFieldNamePairs = new String[fieldCnt * 2];
        for (String tableName : fieldHash.keySet())
        {
            for (String fName : fieldHash.get(tableName))
            {
                ///System.out.println("["+tableName+"]["+fName+"]");
                tableFieldNamePairs[inx++] = tableName;
                tableFieldNamePairs[inx++] = fName;
            }
        }
        return tableFieldNamePairs;
    }
    
    /**
     * @param skipTableNames
     * @param tableInfo
     * @return
     */
    protected String[] gatherTableFieldsForDelete(final String[]    skipTableNames, 
                                                  final DBTableInfo tableInfo)
    {
        return gatherTableFieldsForDelete(skipTableNames, tableInfo.getIdColumnName(), tableInfo.getClassObj());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace)
     */
    public void okToDelete(final Object dataObj,
                           final DataProviderSessionIFace session,
                           final BusinessRulesOkDeleteIFace deletable)
    {
        if (deletable != null)
        {
            deletable.doDeleteDataObj(dataObj, session, true);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#okToDelete(java.lang.Object)
     */
    public abstract boolean okToEnableDelete(final Object dataObj);
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#afterSave(java.lang.Object)
     */
    public boolean afterSaveCommit(final Object dataObj)
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#beforeMerge(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    public void beforeMerge(final Object dataObj, DataProviderSessionIFace session)
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#beforeSave(java.lang.Object)
     */
    public void beforeSave(final Object dataObj, DataProviderSessionIFace session)
    {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#beforeSave(java.lang.Object)
     */
    public boolean beforeSaveCommit(final Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        // do nothing
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#afterDelete(java.lang.Object)
     */
    public void afterDeleteCommit(final Object dataObj)
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#beforeDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    public void beforeDelete(final Object dataObj, final DataProviderSessionIFace session)
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#beforeDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    public boolean beforeDeleteCommit(final Object dataObj, final DataProviderSessionIFace session) throws Exception
    {
        // do nothing
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#processBusinessRules(java.lang.Object)
     */
    public STATUS processBusinessRules(final Object dataObj)
    {
        reasonList.clear();
        
        if (dataObj == null)
        {
            return STATUS.Error;
        }
        
        Class<?> dataObjClass = dataObj.getClass();
        for (Class<?> clazz: dataClasses)
        {
            // if dataObjClass is an extension of one of the handled classes...
            if (clazz.isAssignableFrom(dataObjClass))
            {
                return STATUS.OK;
            }
        }
        // if we get this far, this class of object isn't handled by these business rules
        return STATUS.OK;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#processBusinessRules(java.lang.Object, java.lang.Object, boolean)
     */
    public STATUS processBusinessRules(Object parentDataObj, Object dataObj, boolean isEdit)
    {
        return processBusinessRules(dataObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#doesSearchObjectRequireNewParent()
     */
    public boolean doesSearchObjectRequireNewParent()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#setObjectIdentity(java.lang.Object, edu.ku.brc.ui.forms.DraggableRecordIdentifier)
     */
    public void setObjectIdentity(final Object dataObj, final DraggableRecordIdentifier draggableIcon)
    {
        // no op
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#processSearchObject(java.lang.Object, java.lang.Object)
     */
    public Object processSearchObject(final Object parentdataObj, final Object dataObjectFromSearch)
    {
        return dataObjectFromSearch;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#shouldCloneField(java.lang.String)
     */
    public boolean shouldCloneField(final String fieldName)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#formShutdown()
     */
    public void formShutdown()
    {
        viewable    = null;
        formViewObj = null;
    }

}
