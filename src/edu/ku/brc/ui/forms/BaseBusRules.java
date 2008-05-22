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
import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableChildIFace;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Feb 14, 2008
 *
 */
public class BaseBusRules implements BusinessRulesIFace
{
    private static final Logger  log   = Logger.getLogger(BaseBusRules.class);
    
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
    public void beforeFormFill()
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#fillForm(java.lang.Object, edu.ku.brc.ui.forms.Viewable)
     */
    public void afterFillForm(final Object dataObj)
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
    public boolean shouldCreateSubViewData(final String fieldName)
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#getMessagesAsString()
     */
    public String getMessagesAsString()
    {
        StringBuilder strBuf = new StringBuilder();
        for (String s : getWarningsAndErrors())
        {
            strBuf.append(s);
            strBuf.append("\n");
        }
        return strBuf.toString();
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
     * @param stmt
     * @param tableName
     * @param columnName
     * @param ids
     * @return
     */
    public Integer getCount(final Statement  stmt,
                            final String     tableName, 
                            final String     columnName,
                            final Integer... ids)
    {
        Integer count = null;
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
            if (rs.next())
            {
                count = rs.getInt(1);
            }
            rs.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        }
        return count;
    }
    
    /**
     * Helper to check a list of tables at one time.
     * @param nameCombos a list of names combinations "table name/Foreign Key name"
     * @param id the id to be checked
     * @return true if ok to delete
     */
    protected Integer getTotalCount(final String[] nameCombos, final Integer...ids)
    {
        Connection conn = null;
        Statement  stmt = null;
        try
        {
            conn = DBConnection.getInstance().createConnection();
            stmt = conn.createStatement();

            int total = 0;
            for (int i=0;i<nameCombos.length;i++)
            {
                Integer count = getCount(stmt, nameCombos[i], nameCombos[i+1], ids);
                if (count != null)
                {
                    total += count;
                }
                i++;
            }
            return total;
            
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
        return null;
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
        Integer count = getCount(stmt, tableName, columnName, ids);
        return count != null && count == 0;
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
    public boolean okToEnableDelete(final Object dataObj)
    {
        return false;
    }
    
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
    
    /**
     * Uses a generic strin from the resource bundle to create an error message using the localized name of the field.
     * @param msgKey the key of the message
     * @param fieldName the field name
     * @param dataClass the class for which the field name belongs too.
     * @return
     */
    protected String getErrorMsg(final String msgKey, final String fieldName, final Class<?> dataClass)
    {
        String      title = "Number"; // this should never happen so I am not localizing it
        DBTableInfo ti    = DBTableIdMgr.getInstance().getByClassName(dataClass.getName());
        if (ti != null)
        {
            DBTableChildIFace ci = ti.getItemByName(fieldName);
            if (ci != null)
            {
                title = ci.getTitle();
            }
        }
        return String.format(UIRegistry.getResourceString(msgKey), title);
    }

    /**
     * Helper method for checking for a duplicate number in a field that is unique.
     * @param numFieldName the name of the field to be checked
     * @param dataObj the data object containing the number
     * @param dataClass the class of the object beng checked
     * @param primaryFieldName the primary key field
     * @param numberMissingKey the localization key for the error message
     * @param numberInUseKey  the localization key for the error message
     * @return whether it is ok or in error
     */
    protected STATUS isCheckDuplicateNumberOK(final String           numFieldName, 
                                              final FormDataObjIFace dataObj,
                                              final Class<?>         dataClass,
                                              final String           primaryFieldName)
    {
        String number = (String)FormHelper.getValue(dataObj, numFieldName);
        
        // Let's check Appraisal for duplicates 
        if (StringUtils.isNotEmpty(number))
        {
            // Start by checking to see if the Appraisal Number has changed
            boolean checkNumberForDuplicates = true;
            Integer id = dataObj.getId();
            if (id != null)
            {
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    List<?> dataObjs = session.getDataList(dataClass, primaryFieldName, id);
                    if (dataObjs.size() == 1)
                    {
                        FormDataObjIFace existingDataObj = (FormDataObjIFace)dataObjs.get(0);
                        String           oldNumber       = (String)FormHelper.getValue(existingDataObj, numFieldName);
                        
                        if (oldNumber.equals(number))
                        {
                            checkNumberForDuplicates = false;
                        }
                    }
                } catch (Exception ex)
                {
                    log.error(ex);
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
            }
            
            // If the Id is null then it is a new permit, if not then we are editing the appraisal
            //
            // If the appraisal has not changed then we shouldn't check for duplicates
            if (checkNumberForDuplicates)
            {
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    List <?> appraisalNumbers = session.getDataList(dataClass, numFieldName, number);
                    if (appraisalNumbers.size() > 0)
                    {
                        reasonList.add(getErrorMsg("GENERIC_NUMBER_IN_USE", numFieldName, dataClass));
                        
                    } else
                    {
                        return STATUS.OK;
                    }
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
            } else
            {
                return STATUS.OK;
            }
        } else
        {
            reasonList.add(getErrorMsg("GENERIC_NUMBER_MISSING", numFieldName, dataClass));
        }

        return STATUS.Error;
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
