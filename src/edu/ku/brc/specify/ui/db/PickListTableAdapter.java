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

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.ui.db.PickListItemIFace;
import edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr;

/**
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Nov 14, 2006
 *
 */
public class PickListTableAdapter extends PickListDBAdapter
{
    // Static Data Members
    protected static final Logger logger = Logger.getLogger(PickListTableAdapter.class);

    /**
     * Creates an adapter from a PickList.
     * @param pickList the PickList
     */
    public PickListTableAdapter(final PickList pickList)
    {
        this.pickList = pickList;
        
        if (pickList != null)
        {
            fill(PickListDBAdapterIFace.Type.valueOf(pickList.getType()));
        }
    }
    
    /**
     * Builds a HQL statement for gathering all all the items in a table. Using the 
     * QueryAdjusterForDomain to make sure it gets properly filtered per collection or
     * what ever.
     * @param tableInfo the table
     * @param fieldName the field to be retruned
     * @return the HQL statement
     */
    protected String buildHQL(final DBTableInfo tableInfo, final String fieldName)
    {
        // This could be moved to DBTableIdMgr but a new method would be needed
        StringBuffer strBuf = new StringBuffer();
        if (fieldName != null)
        {
            strBuf.append("SELECT "); 
            strBuf.append(fieldName); 
        }
        strBuf.append("FROM ");
        strBuf.append(tableInfo.getName());
        strBuf.append(" in class ");
        strBuf.append(tableInfo.getShortClassName());
        
        String specialWhereClause = QueryAdjusterForDomain.getInstance().getSpecialColumns(tableInfo, true);
        if (StringUtils.isNotEmpty(specialWhereClause))
        {
            strBuf.append(" WHERE ");
            strBuf.append(specialWhereClause);
        }
        return strBuf.toString();
    }
    
    /**
     * Fills non-Item type PickLists from a Table.
     * @param type the type to be filled
     */
    protected void fill(final PickListDBAdapterIFace.Type type)
    {
        if (type != PickListDBAdapterIFace.Type.Item)
        {
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(DBTableIdMgr.getInstance().getIdByShortName(pickList.getTableName()));
            if (tableInfo != null)
            {
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                try
                {
                    String sqlStr = buildHQL(tableInfo, type == PickListDBAdapterIFace.Type.TableField ? pickList.getFieldName() : null);
                    log.debug(sqlStr);
                    if (StringUtils.isNotEmpty(sqlStr))
                    {
                        List<?> dataList = session.getDataList(sqlStr);
                        if (dataList != null && !dataList.isEmpty())
                        {
                            if (type == PickListDBAdapterIFace.Type.TableField)
                            {
                                String   formatterStr = pickList.getFormatter();
                                boolean  hasFormatter = StringUtils.isNotEmpty(formatterStr);
                                for (Object dataObj : dataList)
                                {
                                    String valStr = hasFormatter ? String.format(formatterStr, dataObj) : dataObj.toString();
                                    items.add(pickList.addItem(valStr, valStr));
                                }
                                
                            } else
                            {
                                for (Object dataObj : dataList)
                                {
                                    String title = DataObjFieldFormatMgr.format(dataObj, pickList.getFormatter());
                                    items.add(pickList.addItem(title, dataObj));
                                }
                            }
                            
                        } else
                        {
                            // No Data Error
                        }

                    } else
                    {
                        log.error("Query String is empty for tableId["+tableInfo.getTableId()+"]");
                    }
                } catch (Exception ex)
                {
                    log.error(ex);
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
                throw new RuntimeException("Error looking up PickLIst's Table Name ["+pickList.getTableName()+"]");
            }
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
