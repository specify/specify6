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
package edu.ku.brc.specify.ui.db;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PickListItem;
import edu.ku.brc.ui.UIHelper;

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
     * @param fieldName the field to be returned
     * @return the HQL statement
     */
    protected String buildHQL(final DBTableInfo tableInfo, final String fieldName)
    {
        // This could be moved to DBTableIdMgr but a new method would be needed
        StringBuffer strBuf = new StringBuffer();
        if (fieldName != null)
        {
            strBuf.append("SELECT "); 
            strBuf.append(tableInfo.getAbbrev()); 
            strBuf.append('.'); 
            strBuf.append(fieldName); 
        }
        strBuf.append(" FROM ");
        strBuf.append(tableInfo.getShortClassName());
        
        strBuf.append(" ");
        strBuf.append(tableInfo.getAbbrev());
        
        String joinSnipet = QueryAdjusterForDomain.getInstance().getJoinClause(tableInfo, true, null, false); // first false means SQL
        if (joinSnipet != null)
        {
            strBuf.append(' ');
            strBuf.append(joinSnipet);
            strBuf.append(' ');
        }
        
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
                    boolean isBasicType = false;
                    boolean isFromField = type == PickListDBAdapterIFace.Type.TableField;
                    if (isFromField)
                    {
                        DBFieldInfo fi = tableInfo.getFieldByColumnName(pickList.getFieldName());
                        isBasicType = fi != null ? UIHelper.isPrimitiveObjectType(fi.getDataClass()) : false;
                    }
                    
                    String sqlStr = buildHQL(tableInfo, type == PickListDBAdapterIFace.Type.TableField ? pickList.getFieldName() : null);
                    //log.debug(sqlStr);
                    if (StringUtils.isNotEmpty(sqlStr))
                    {
                        List<?> dataList = session.getDataList(sqlStr);
                        if (dataList != null && !dataList.isEmpty())
                        {
                            if (type == PickListDBAdapterIFace.Type.TableField)
                            {
                                TreeSet<String> values = isBasicType ? new TreeSet<String>(String.CASE_INSENSITIVE_ORDER) : null;
                                String   formatterStr = isBasicType ? null : pickList.getFormatter();
                                boolean  hasFormatter = StringUtils.isNotEmpty(formatterStr);
                                for (Object dataObj : dataList)
                                {
                                    if (dataObj instanceof Object[])
                                    {
                                        dataObj = ((Object[])dataObj)[0];
                                    }
                                    
                                    if (dataObj != null)
                                    {
                                        String valStr = hasFormatter ? String.format(formatterStr, dataObj) : dataObj.toString();
                                        if (isBasicType)
                                        {
                                            values.add(valStr);
                                        } else
                                        {
                                            items.add(pickList.addItem(valStr, valStr));
                                        }
                                    }
                                }
                                
                                if (isBasicType)
                                {
                                    for (String v : values)
                                    {
                                        items.add(pickList.addItem(v, v));
                                    }
                                }
                            } else
                            {
                                for (Object dataObj : dataList)
                                {
                                    if (dataObj instanceof Object[])
                                    {
                                        dataObj = ((Object[])dataObj)[0];
                                    }
                                    String title;
                                    if (pickList.getFormatter() != null)
                                    {
                                        title = DataObjFieldFormatMgr.getInstance().format(dataObj, pickList.getFormatter());
                                    } else
                                    {
                                        title = ((FormDataObjIFace)dataObj).getIdentityTitle();
                                    }
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
                    ex.printStackTrace();
                    log.error(ex);
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PickListTableAdapter.class, ex);
                    
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
    public PickListItemIFace addItem(final String title, final String value)
    {
        // this should never happen!
        if (pickList.getType() != PickListDBAdapterIFace.Type.TableField.value())
        {
            throw new RuntimeException("This type of PickList cannot be added to");
        }
        
        int     sizeLimit = 50; // arbitrary size could be a pref (XXX PREF)
        Integer sizeLimitInt = pickList.getSizeLimit();
        if (sizeLimitInt != null)
        {
            sizeLimit = sizeLimitInt.intValue();
        }
        
        searchablePLI.setTitle(title);
        int index = Collections.binarySearch(items, searchablePLI);
        if (index < 0)
        {
            // find oldest item and remove it
            if (items.size() >= sizeLimit && sizeLimit > 0) 
            {
                PickListItemIFace oldest = null;
                for (PickListItemIFace pli : items)
                {
                    if (oldest == null || pli.getTimestampCreated().getTime() < oldest.getTimestampCreated().getTime())
                    {
                        oldest = pli;
                    }
                }
                items.remove(oldest);
                pickList.removeItem(oldest);
            }
            
            PickListItem item = new PickListItem(title, value, new Timestamp(System.currentTimeMillis()));
            items.add(item);
            
            if (pickList != null)
            {
                pickList.addItem(item);
                item.setPickList(pickList);
                pickList.reorder();
            }
            
            Collections.sort(items);
            
            super.fireContentsChanged(this, 0, items.size()-1);
            
            for (ChangeListener cl : changeListeners)
            {
                cl.stateChanged(new ChangeEvent(this));
            }

            return item;
            
        }
        // else
        return items.elementAt(index);

    }
}
