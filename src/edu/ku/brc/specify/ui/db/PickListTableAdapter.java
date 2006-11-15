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
     * Constructor with a unique name.
     * @param name the name of the picklist
     * @param createWhenNotFound indicates whether to automatically create the picklist when the name is not found,
     */
    /*public PickListTableAdapter(final String name, final boolean createWhenNotFound)
    {
        pickList = PickListDBAdapterFactory.getPickList(name);
        
        if (createWhenNotFound || pickList == null)
        {
            throw new RuntimeException("This type of picklist cannot be automatically created!["+name+"]");
        }
        init();
    }*/
    
    /**
     * Initializes list from Database. 
     */
    protected void init()
    {
        int type = pickList.getType();
        switch (type)
        {
            case 0 : throw new RuntimeException("This adapter is not intended for PickList's of type '0'");
            
            case 1 : 
                fillFromFullTable();
                break;
                
            case 2 : 
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
    }

    /**
     * 
     */
    protected void fillFromTableField()
    {
        
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
