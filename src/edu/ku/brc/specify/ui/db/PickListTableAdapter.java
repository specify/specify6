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
package edu.ku.brc.specify.ui.db;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.ui.db.PickListItemIFace;
import edu.ku.brc.ui.forms.DataObjFieldFormatMgr;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 14, 2006
 *
 */
public class PickListTableAdapter extends PickListDBAdapter
{
    // Static Data Memebers
    protected static final Logger logger = Logger.getLogger(PickListTableAdapter.class);

    /**
     * Creates an adapter from a PickList.
     * @param pickList the picklist
     */
    public PickListTableAdapter(final PickList pickList)
    {
        this.pickList = pickList;
        init();
    }
    
    /**
     * Initializes list from Database. 
     */
    protected void init()
    {
        PickListDBAdapterIFace.Type type = PickListDBAdapterIFace.Type.valueOf(pickList.getType());
        switch (type)
        {
            case Item : throw new RuntimeException("This adapter is not intended for PickList's of type '0'");
            
            case Table : 
                fillFromFullTable();
                break;
                
            case TableField : 
                fillFromTableField();
                break;
                
        } // switch 
    }
    
    /**
     * 
     */
    protected void fillFromFullTable()
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        
        //DBTableIdMgr.getQueryForTable(tableId, recordId)
        DBTableIdMgr.TableInfo tableInfo = DBTableIdMgr.lookupInfoById(DBTableIdMgr.lookupIdByShortName(pickList.getTableName()));
        if (tableInfo != null)
        {
            // This could be moved to DBTableIdMgr but a new method would be needed
            StringBuffer strBuf = new StringBuffer("from ");
            strBuf.append(tableInfo.getTableName());
            strBuf.append(" in class ");
            strBuf.append(tableInfo.getShortClassName());
            
            String sqlStr = strBuf.toString();//DBTableIdMgr.getQueryForTable(tableId, Integer.parseInt(idStr));
            if (StringUtils.isNotEmpty(sqlStr))
            {
                try
                {
                    List dataList = session.getDataList(sqlStr);
                    if (dataList != null && dataList.size() > 0)
                    {
                        for (Object dataObj : dataList)
                        {
                            String title = DataObjFieldFormatMgr.format(dataObj, pickList.getFormatter());
                            items.add(pickList.addPickListItem(title, dataObj));
                        }
                        
                    } else
                    {
                        // No Data Error
                    }
        
                } catch (Exception ex)
                {
                    log.error(ex);
                    ex.printStackTrace();
                }
            } else
            {
                log.error("Query String is empty for tableId["+tableInfo.getTableId()+"]");
            }
            session.close();
        } else
        {
            throw new RuntimeException("Error looking up PickLIst's Table Name ["+pickList.getTableName()+"]");
        }
    }

    /**
     * 
     */
    protected void fillFromTableField()
    {
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        
        //DBTableIdMgr.getQueryForTable(tableId, recordId)
        DBTableIdMgr.TableInfo tableInfo = DBTableIdMgr.lookupInfoById(DBTableIdMgr.lookupIdByShortName(pickList.getTableName()));
        if (tableInfo != null)
        {
            try
            {
                List dataList = session.getDataList(Class.forName(tableInfo.getClassName()), pickList.getFieldName(), true);
                if (dataList != null && dataList.size() > 0)
                {
                    Object[] array = new Object[1];
                    String   formatterStr   = pickList.getFormatter();
                    boolean  hasFormatter = StringUtils.isNotEmpty(formatterStr);
                    for (Object dataObj : dataList)
                    {
                        array[0] = dataObj;
                        String valStr = hasFormatter ? String.format(formatterStr, array) : dataObj.toString();
                        items.add(pickList.addPickListItem(valStr, valStr));
                    }
                    
                } else
                {
                    // No Data Error
                }
    
            } catch (Exception ex)
            {
                log.error(ex);
                ex.printStackTrace();
            }

            session.close();
        } else
        {
            throw new RuntimeException("Error looking up PickLIst's Table Name ["+pickList.getTableName()+"]");
        }
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#addItem(java.lang.String, java.lang.String)
     */
    @Override
    public PickListItemIFace addItem(String title, String value)
    {
        throw new RuntimeException("This type of PickList cannot be added to");
    }
}
