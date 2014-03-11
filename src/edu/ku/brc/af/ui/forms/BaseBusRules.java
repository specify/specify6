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
package edu.ku.brc.af.ui.forms;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableChildIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.AttachmentOwnerIFace;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
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
    
    
    protected HashSet<AttachmentOwnerIFace<?>>   attachOwners  = new HashSet<AttachmentOwnerIFace<?>>();
    
    /**
     * The data class that is used within the business rules.
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
    @Override
    public void beforeFormFill()
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#fillForm(java.lang.Object, edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
    }
    
    

	/* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#addChildrenToNewDataObjects(java.lang.Object)
     */
    @Override
    public void addChildrenToNewDataObjects(final Object newDataObj)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#shouldCreateDataForField(java.lang.String)
     */
    @Override
    public boolean shouldCreateSubViewData(final String fieldName)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#getDeleteMsg(java.lang.Object)
     */
    @Override
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
    @Override
    public List<String> getWarningsAndErrors()
    {
        return reasonList;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#getMessagesAsString()
     */
    @Override
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
     * @param dataObj
     * @return true if current user has permission to edit.
     */
    protected boolean checkEditPermission(Object dataObj) {
        if (dataObj instanceof DataModelObjBase) {
        	DBTableInfo info = DBTableIdMgr.getInstance().getInfoById(((DataModelObjBase)dataObj).getTableId());
        	return info != null && (info.getPermissions().canModify() || (info.getPermissions().canAdd() && ((DataModelObjBase)dataObj).getId() == null)); 
        }
    	return true;
    }
    
    /**
     * @param dataObj
     * @return
     */
    protected String getDataObjDesc(Object dataObj) {
        if (dataObj instanceof DataModelObjBase) {
        	DBTableInfo info = DBTableIdMgr.getInstance().getInfoById(((DataModelObjBase)dataObj).getTableId());
        	if (info != null) {
        		return info.getTitle();
        	}
        }
    	return dataObj.getClass().getSimpleName();
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BusinessRulesIFace#isOkToSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean isOkToSave(Object dataObj, DataProviderSessionIFace session)
    {
        if (AppContextMgr.isSecurityOn() && !checkEditPermission(dataObj)) {
        	String msg = String.format(UIRegistry.getResourceString("DET_NO_MOD_PERM"), getDataObjDesc(dataObj));
        	if (!reasonList.contains(msg)) {
        		reasonList.add(String.format(UIRegistry.getResourceString("DET_NO_MOD_PERM"), getDataObjDesc(dataObj)));
        	}
        	return false;
        }
    	return true;
    }

    /* XXX bug #9497. A method similar to this is being performed in formViewObj
    protected void processControlsForSecurity(Object dataObj) {
    	List<String> ids = new ArrayList<String>();
    	viewable.getFieldIds(ids);
    	boolean editable = checkEditPermission(dataObj);
    	//if (!editable) {
    		for (String id : ids) {
    			formViewObj.getControlById(id).setEnabled(editable);
    		}
    	//}
    }*/
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BusinessRulesIFace#canCreateNewDataObject()
     */
    @Override
    public boolean canCreateNewDataObject()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BusinessRulesIFace#afterCreateNewObj(java.lang.Object)
     */
    @Override
    public void afterCreateNewObj(Object newDataObj)
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BusinessRulesIFace#createNewObj(boolean, java.lang.Object)
     */
    @Override
    public void createNewObj(boolean doSetIntoAndValidateArg, Object oldDataObj)
    {
    }

    /**
     * @param attOwner
     */
    protected void addExtraObjectForProcessing(final Object dObj)
    {
        if (dObj instanceof AttachmentOwnerIFace<?>)
        {
            attachOwners.add((AttachmentOwnerIFace<?>)dObj);
        }
    }
    
    /**
     * Checks to see if it can be deleted.
     * @param tableName the table name to check
     * @param columnName the column name name to check
     * @param ids the Record IDs to check
     * @return true means it can be deleted, false means it found something
     */
    protected boolean okToDelete(final String tableName,
                                 final String columnName, 
                                 final Integer...ids)
    {
        return okToDelete(0, tableName, columnName, ids);
    }

    /**
     * Checks to see if it can be deleted.
     * @param tableName the table name to check
     * @param columnName the column name name to check
     * @param count the count to check against
     * @param ids the Record IDs to check
     * @return true means it can be deleted, false means it found something
     */
    protected boolean okToDelete(final int count, 
                                 final String tableName,
                                 final String columnName, 
                                 final Integer...ids)
    {
        if (ids != null)
        {
            Connection conn = null;
            Statement  stmt = null;
            try
            {
                conn = DBConnection.getInstance().createConnection();
                stmt = conn.createStatement();
    
                return okToDelete(stmt, count, tableName, columnName, ids);
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseBusRules.class, ex);
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
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseBusRules.class, ex);
                    ex.printStackTrace();
                }
            }
            return false;
        }
        // else
        return true;
    }

    /**
     * Calls QueryAdjusterForDomain.getSpecialColumns to get any extra Where Columns
     * @param tableInfo the table information
     * @return String or null
     */
    protected String getExtraWhereColumns(final DBTableInfo tableInfo)
    {
        System.out.println(tableInfo != null ? tableInfo.getTableId():"  is null");
        
        return QueryAdjusterForDomain.getInstance().getSpecialColumns(tableInfo, false, false, tableInfo.getAbbrev());
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
            DBTableInfo tableInfo    = DBTableIdMgr.getInstance().getInfoByTableName(tableName);
            String      extraColumns = getExtraWhereColumns(tableInfo);
            String      join         = QueryAdjusterForDomain.getInstance().getJoinClause(tableInfo, false, tableInfo.getAbbrev(), false);
            String      queryString  = "select count(*) from " + tableName + " "+ tableInfo.getAbbrev() +" " + (join != null ? join : "") + "  where " + tableInfo.getAbbrev() + "." + columnName + " in (" + idString.toString() + ") ";
            if (StringUtils.isNotEmpty(extraColumns))
            {
                queryString += " AND " + extraColumns; 
            }
            
            //log.debug(queryString);
            ResultSet rs = stmt.executeQuery(queryString);
            if (rs.next())
            {
                count = rs.getInt(1);
            }
            rs.close();
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseBusRules.class, ex);
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseBusRules.class, ex);
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
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseBusRules.class, ex);
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
    protected boolean okToDelete(final Statement  stmt,
                                 final int        count,
                                 final String     tableName, 
                                 final String     columnName,
                                 final Integer...ids)
    {
        Integer recCount = getCount(stmt, tableName, columnName, ids);
        return recCount != null && recCount <= count;
    }
    
    /**
     * Helper to check a list of tables at one time.
     * @param nameCombos a list of names combinations "table name/Foreign Key name"
     * @param id the id to be checked
     * @return true if ok to delete
     */
    protected boolean okToDelete(final String[] nameCombos, final Integer...ids)
    {
        return okToDelete(0, nameCombos, ids);
    }
    
    /**
     * Helper to check a list of tables at one time.
     * @param nameCombos a list of names combinations "table name/Foreign Key name"
     * @param id the id to be checked
     * @return true if ok to delete
     */
    protected boolean okToDelete(final int count, final String[] nameCombos, final Integer...ids)
    {
        Connection conn = null;
        Statement  stmt = null;
        try
        {
            conn = DBConnection.getInstance().createConnection();
            if (conn == null)
            {
                log.debug("Couldn't create connection! Reason: "+DBConnection.getInstance().getErrorMsg());
                return false;
            }
            
            stmt = conn.createStatement();

            for (int i=0;i<nameCombos.length;i++)
            {
                if (!okToDelete(stmt, count, nameCombos[i], nameCombos[i+1], ids))
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseBusRules.class, ex);
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
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseBusRules.class, ex);
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
        HashSet<String> skipHash = new HashSet<String>();
        if (skipTableNames != null)
        {
            for (String name : skipTableNames)
            {
                skipHash.add(name);
            }
        }
        
        for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
        {
            String tblName = ti.getName();
            if (!skipHash.contains(tblName))
            {
            	if (dataClassObj != null)
            	{
            		for (DBRelationshipInfo ri : ti.getRelationships())
            		{
            			if (ri.getDataClass() == dataClassObj)
            			{
            				String colName = ri.getColName();
            				if (StringUtils.isNotEmpty(colName) /*&& !colName.equals(idColName)*/
                        		//I am pretty sure the following condition reproduces the logic in revision prior to 11305,
                        		//if skipHash.contains test is removed above.
                        		//&& (!skipHash.contains(tblName)  || (skipHash.contains(tblName) && !colName.equals(idColName)))
                        		)
            				{
            					Vector<String> fieldList = fieldHash.get(tblName);
            					if (fieldList == null)
            					{
            						fieldList = new Vector<String>();
            						fieldHash.put(tblName, fieldList);
            					}
            					fieldList.add(ri.getColName());
            					fieldCnt++;
            				}
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
     * @return whether the form is in edit mode
     */
    protected boolean isEditMode()
    {
        if (formViewObj != null)
        {
            MultiView mvParent = formViewObj.getMVParent();
            if (mvParent != null)
            {
                return mvParent.isEditable();
            }
        }
        return false;
    }
    
    /**
     * @return whether the data object is new
     */
    protected boolean isNewObject()
    {
        if (formViewObj != null)
        {
            MultiView mvParent = formViewObj.getMVParent();
            if (mvParent != null)
            {
                return MultiView.isOptionOn(mvParent.getOptions(), MultiView.IS_NEW_OBJECT);
            }
        }
        return false;
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
    @Override
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
    @Override
    public boolean okToEnableDelete(final Object dataObj)
    {
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#afterSave(java.lang.Object)
     */
    @Override
    public boolean afterSaveCommit(final Object dataObj, final DataProviderSessionIFace session)
    {
        return true;
    }

    
    /* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.BusinessRulesIFace#afterSaveFailure(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
	 */
	@Override
	public void afterSaveFailure(Object dataObj,
			DataProviderSessionIFace session)
	{
		// do nothing
	}

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.BusinessRulesIFace#saveFinalization(java.lang.Object)
	 */
	@Override
	public void saveFinalization(Object dataObj) 
	{
		// do nothing
	}

	/* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#beforeMerge(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeMerge(final Object dataObj, DataProviderSessionIFace session)
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#beforeSave(java.lang.Object)
     */
    @Override
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
    @Override
    public void afterDeleteCommit(final Object dataObj)
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#beforeDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public Object beforeDelete(final Object dataObj, final DataProviderSessionIFace session)
    {
        // do nothing
        return dataObj;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#beforeDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean beforeDeleteCommit(final Object dataObj, final DataProviderSessionIFace session) throws Exception
    {
        // do nothing
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BusinessRulesIFace#isOkToAddSibling(java.lang.Object)
     */
    @Override
    public boolean isOkToAddSibling(Object parentObj)
    {
        return true;
    }

    /**
     * Uses a generic strin from the resource bundle to create an error message using the localized name of the field.
     * @param msgKey the key of the message
     * @param fieldName the field name
     * @param dataClass the class for which the field name belongs too.
     * @return
     */
    protected String getErrorMsg(final String msgKey, final Class<?> dataClass, final String fieldName, final String value)
    {
        String      title = "Unknown Field"; // this should never happen so I am not localizing it
        DBTableInfo ti    = DBTableIdMgr.getInstance().getByClassName(dataClass.getName());
        if (ti != null)
        {
            DBTableChildIFace ci = ti.getItemByName(fieldName);
            if (ci != null)
            {
                title = ci.getTitle();
            }
        }
        return String.format(UIRegistry.getResourceString(msgKey), title, value);
    }

    /**
     * Helper method for checking for a duplicate number in a field that is unique.
     * @param fieldName the name of the field to be checked
     * @param dataObj the data object containing the number
     * @param dataClass the class of the object beng checked
     * @param primaryFieldName the primary key field
     * @param numberMissingKey the localization key for the error message
     * @param numberInUseKey  the localization key for the error message
     * @return whether it is ok or in error
     */
    protected STATUS isCheckDuplicateNumberOK(final String           fieldName, 
                                              final FormDataObjIFace dataObj,
                                              final Class<?>         dataClass,
                                              final String           primaryFieldName)
    {
        return isCheckDuplicateNumberOK(fieldName, dataObj, dataClass, primaryFieldName, true);
    }
    
    /**
     * Helper method for checking for a duplicate number in a field that is unique.
     * @param fieldName the name of the field to be checked
     * @param dataObj the data object containing the number
     * @param dataClass the class of the object beng checked
     * @param primaryFieldName the primary key field
     * @param numberMissingKey the localization key for the error message
     * @param numberInUseKey  the localization key for the error message
     * @param useSpecial use Discipline or CollectionMemberID etc to constrain the search
     * @return whether it is ok or in error
     */
    protected STATUS isCheckDuplicateNumberOK(final String           fieldName, 
                                              final FormDataObjIFace dataObj,
                                              final Class<?>         dataClass,
                                              final String           primaryFieldName,
                                              final boolean          useSpecial)
    {
        return isCheckDuplicateNumberOK(fieldName, dataObj, dataClass, primaryFieldName, false, useSpecial);
    }

    /**
     * Helper method for checking for a duplicate number in a field that is unique.
     * @param fieldName the name of the field to be checked
     * @param dataObj the data object containing the number
     * @param dataClass the class of the object beng checked
     * @param primaryFieldName the primary key field
     * @param isEmptyOK is it ok for the field to be empty
     * @param useSpecial use Discipline or CollectionMemberID etc to constrain the search
     * @return whether it is ok or in error
     */
    protected STATUS isCheckDuplicateNumberOK(final String           fieldName, 
                                              final FormDataObjIFace dataObj,
                                              final Class<?>         dataClass,
                                              final String           primaryFieldName,
                                              final boolean          isEmptyOK,
                                              final boolean          useSpecial)
    {
        String fieldValue = (String)FormHelper.getValue(dataObj, fieldName);
        
        // Let's check for duplicates 
        if (StringUtils.isNotEmpty(fieldValue))
        {
            Integer     id      = dataObj.getId();
            String      colName = null;
            DBTableInfo ti      = DBTableIdMgr.getInstance().getByClassName(dataClass.getName());
            DBFieldInfo fi      = ti.getFieldByName(primaryFieldName);
            if (fi != null)
            {
               colName = fi.getColumn(); 
            } else
            {
                if (ti.getIdFieldName().equals(primaryFieldName))
                {
                    colName = ti.getIdColumnName();
                }
            }
            
            fi = ti.getFieldByName(fieldName);
            
            String special = QueryAdjusterForDomain.getInstance().getSpecialColumns(ti, false);
            String quote   = fi.getDataClass() == String.class || fi.getDataClass() == Date.class ? "'" : "";
            String sql = String.format("SELECT COUNT(%s) FROM %s WHERE %s = %s%s%s", colName, ti.getName(), fi.getColumn(), quote, fieldValue, quote);
            if (id != null)
            {
                sql += " AND " + colName + " <> " + id;
            }
            sql += StringUtils.isNotEmpty(special) && useSpecial ? (" AND "+special) : "";
            
            //log.debug(sql);
            
            Integer cnt = BasicSQLUtils.getCount(sql);
            
            if (cnt == null || cnt == 0)
            {
                return STATUS.OK;
            }
            
            if (fi != null && fi.getFormatter() != null)
            {
                Object fmtObj = fi.getFormatter().formatToUI(fieldValue);
                if (fmtObj != null)
                {
                    fieldValue = fmtObj.toString();
                }
            }
            reasonList.add(getErrorMsg("GENERIC_FIELD_IN_USE", dataClass, fieldName, fieldValue));
            return STATUS.Error;
            
        } else if (isEmptyOK)
        {
            return STATUS.OK;
            
        } 
        reasonList.add(getErrorMsg("GENERIC_FIELD_MISSING", dataClass, fieldName, ""));

        return STATUS.Error;
    }
    
    /**
     * @return the 'new' btn from the FormViewObj or the TableViewObj
     */
    protected JButton getNewBtn()
    {
        if (formViewObj != null)
        {
            JButton newBtn = formViewObj.getNewRecBtn();
            if (newBtn == null && formViewObj.getRsController() != null)
            {
                return formViewObj.getRsController().getNewRecBtn();    
            }
            return newBtn;
            
        } else if (viewable instanceof TableViewObj)
        {
            TableViewObj tvo = (TableViewObj)viewable;
            return tvo.getNewButton();
        }
        return null;
    }

    /**
     * @return the 'delete' btn from the FormViewObj or the TableViewObj
     */
    protected JButton getDelBtn()
    {
        if (formViewObj != null)
        {
            JButton delBtn = formViewObj.getDelRecBtn();
            if (delBtn == null && formViewObj.getRsController() != null)
            {
                return formViewObj.getRsController().getDelRecBtn();    
            }
            return delBtn;
            
        } else if (viewable instanceof TableViewObj)
        {
            TableViewObj tvo = (TableViewObj)viewable;
            return tvo.getDeleteButton();
        }
        return null;
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
    @Override
    public STATUS processBusinessRules(Object parentDataObj, Object dataObj, boolean isExistingObject)
    {
        return processBusinessRules(dataObj);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BusinessRulesIFace#beginSecondaryRuleProcessing()
     */
    @Override
    public void startProcessingBeforeAfterRules()
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BusinessRulesIFace#endSecondaryRuleProcessing()
     */
    @Override
    public void endProcessingBeforeAfterRules()
    {
        
    }

    /**
     * Removed an Object from a Collection by Id.
     * @param collection the Java Collection
     * @param dataObj the data object to be removed
     */
    public static void removeById(final Collection<?> collection, final FormDataObjIFace dataObj)
    {
        for (Object obj : collection.toArray())
        {
            if (obj instanceof FormDataObjIFace)
            {
                FormDataObjIFace colObj = (FormDataObjIFace)obj;
                if (obj == colObj || (colObj.getId() != null && dataObj.getId() != null && dataObj.getId().equals(colObj.getId())))
                {
                    collection.remove(obj);
                    break;
                }
            }
        }
    }

    /**
     * Removed an Object from a Collection by Id.
     * @param collection the Java Collection
     * @param dataObj the data object to be removed
     */
    public static int countDataObjectById(final Collection<?> collection, final FormDataObjIFace dataObj)
    {
        int cnt = 0;
        for (Object obj : collection.toArray())
        {
            if (obj instanceof FormDataObjIFace)
            {
                FormDataObjIFace colObj = (FormDataObjIFace)obj;
                if (dataObj == colObj || (colObj.getId() != null && dataObj.getId() != null && dataObj.getId().equals(colObj.getId())))
                {
                    cnt++;
                }
            }
        }
        return cnt;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#doesSearchObjectRequireNewParent()
     */
    @Override
    public boolean doesSearchObjectRequireNewParent()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#setObjectIdentity(java.lang.Object, edu.ku.brc.ui.forms.DraggableRecordIdentifier)
     */
    @Override
    public void setObjectIdentity(final Object dataObj, final DraggableRecordIdentifier draggableIcon)
    {
        // no op
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BusinessRulesIFace#isOkToAssociateSearchObject(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean isOkToAssociateSearchObject(Object newParentDataObj, Object dataObjectFromSearch)
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#processSearchObject(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object processSearchObject(final Object parentdataObj, final Object dataObjectFromSearch)
    {
        return dataObjectFromSearch;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#shouldCloneField(java.lang.String)
     */
    @Override
    public boolean shouldCloneField(final String fieldName)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        viewable    = null;
        formViewObj = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BusinessRulesIFace#aboutToShutdown()
     */
    @Override
    public void aboutToShutdown()
    {
        // no op
    }
}
