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
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBInfoBase;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jun 18, 2010
 *
 */
public class DataObjTableModel extends DefaultTableModel
{
    protected ArrayList<DBInfoBase> items;
    protected DBTableInfo           tableInfo;
    protected Vector<Object[]>      values     = null;
    protected boolean               isEditable = false;
    protected int[]                 mapInx     = null;
    
    protected ArrayList<Boolean>    sameValues  = null;
    protected ArrayList<Boolean>    hasDataList = null;
    
    protected HashMap<Integer, Integer> indexHash = new HashMap<Integer, Integer>();

    
    /**
     * @param tableId
     * @param colName
     * @param value
     * @param isEditable
     */
    public DataObjTableModel(final int     tableId, 
                             final String  colName, 
                             final String  value,
                             final boolean isEditable)
    {
        super();
        
        this.isEditable = isEditable;
        
        tableInfo = DBTableIdMgr.getInstance().getInfoById(tableId);
        
        ArrayList<DBInfoBase> itemsList = new ArrayList<DBInfoBase>();
        
        StringBuffer sql = new StringBuffer("SELECT ");
        for (DBFieldInfo fi : tableInfo.getFields())
        {
            if (fi.getColumn().equals("Version")) continue;
            
            itemsList.add(fi);
            if (itemsList.size() > 1) sql.append(',');
            sql.append(fi.getColumn());
        }
        int numCols = itemsList.size();
        
        sql.append(" FROM %s WHERE %s LIKE \"%s\"");
        String sqlStr = String.format(sql.toString(), tableInfo.getName(), colName, value + '%');

        values = BasicSQLUtils.query(sqlStr);
        
        sameValues  = new ArrayList<Boolean>(numCols);
        hasDataList = new ArrayList<Boolean>(numCols);
        for (int i=0;i<numCols;i++)
        {
            sameValues.add(true);
            hasDataList.add(false);
        }
        
        int hasDataCols = 0;
        for (Object[] col : values)
        {
            for (int i=0;i<numCols;i++)
            {
                Object  data    = col[i];
                boolean hasData = data != null;
                
                if (hasData && !hasDataList.get(i))
                {
                    hasDataList.set(i, true);
                    hasDataCols ++;
                }
            }
        }
        
        mapInx = new int[hasDataCols];
        int colInx = 0;
        System.out.println("-------------Has Data----------------------");
        for (int i=0;i<numCols;i++)
        {
            if (hasDataList.get(i))
            {
                System.out.println(itemsList.get(i).getTitle());
                mapInx[colInx] = i;
                indexHash.put(i, colInx);
                System.out.print("indexHash: "+i +" -> "+colInx);
                System.out.println("  mapInx:    "+colInx +" -> "+i);
                colInx++;
            }
        }        
        
        
        for (int i=0;i<mapInx.length;i++)
        {
            colInx = mapInx[i];
            
            if (hasDataList.get(colInx))
            {
                Object data = null;
                for (Object[] col : values)
                {
                    Object newData = col[colInx];
                    
                    if (data == null)
                    {
                        if (newData != null)
                        {
                            data = newData;
                        }
                        continue;
                    }
                   
                    if (newData != null && !data.equals(newData))
                    {
                        sameValues.set(colInx, false);
                        break;
                    }
                }
            }
        }
        
        
        System.out.println("-----------Same------------------------");
        for (int i=0;i<mapInx.length;i++)
        {
            colInx = mapInx[i];
            if (sameValues.get(colInx))
            {
                System.out.println(colInx + " " + itemsList.get(colInx).getTitle());
            }
        }
        
        items = new ArrayList<DBInfoBase>(itemsList);
    }
    
    /**
     * 
     */
    public DataObjTableModel(final int tableId, 
                             final ArrayList<DBInfoBase> items,
                             final ArrayList<Boolean> hasDataList, 
                             final ArrayList<Boolean> sameValues, 
                             final int[]              mapInx, 
                             final HashMap<Integer, Integer> indexHash)
    {
        super();
        
        this.items       = items;
        this.hasDataList = hasDataList;
        this.sameValues  = sameValues;
        this.mapInx      = mapInx;
        this.indexHash   = indexHash;
        this.isEditable  = true;
        
        tableInfo = DBTableIdMgr.getInstance().getInfoById(tableId);
        
        values = new Vector<Object[]>();
        values.add(new Object[items.size()]);
    }
    
    /**
     * @param column
     * @return
     */
    public boolean isSame(final int column)
    {
        return sameValues.get(mapInx[column]);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#getColumnCount()
     */
    @Override
    public int getColumnCount()
    {
        return mapInx != null ? mapInx.length : 0;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int column)
    {
        return mapInx != null ? items.get(mapInx[column]).getTitle() : "";
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#getRowCount()
     */
    @Override
    public int getRowCount()
    {
        return values != null ? values.size() : 0;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int row, int column)
    {
        System.out.println("----------------");
        Object[] col = values != null ? values.get(row) : null;
        if (col != null)
        {
            System.out.println("column "+column);
            System.out.println("mapInx[column] "+mapInx[column]);
            System.out.println("col len "+col.length);
            return col[mapInx[column]];
        }
        return null;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int column)
    {
        DBInfoBase base = items.get(mapInx[column]);
        if (base instanceof DBFieldInfo)
        {
            return ((DBFieldInfo)base).getDataClass();
        }
        return String.class;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int row, int column)
    {
        return isEditable;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
     */
    @Override
    public void setValueAt(Object aValue, int row, int column)
    {
        Object[] col = values != null ? values.get(row) : null;
        if (col != null)
        {
            col[mapInx[column]] = aValue;
            fireTableCellUpdated(row, column);
        }
    }

    /**
     * @return the items
     */
    public ArrayList<DBInfoBase> getItems()
    {
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(ArrayList<DBInfoBase> items)
    {
        this.items = items;
    }

    /**
     * @return the mapInx
     */
    public int[] getMapInx()
    {
        return mapInx;
    }

    /**
     * @param mapInx the mapInx to set
     */
    public void setMapInx(int[] mapInx)
    {
        this.mapInx = mapInx;
    }

    /**
     * @return the sameValues
     */
    public ArrayList<Boolean> getSameValues()
    {
        return sameValues;
    }

    /**
     * @param sameValues the sameValues to set
     */
    public void setSameValues(ArrayList<Boolean> sameValues)
    {
        this.sameValues = sameValues;
    }

    /**
     * @return the hasDataList
     */
    public ArrayList<Boolean> getHasDataList()
    {
        return hasDataList;
    }

    /**
     * @param hasDataList the hasDataList to set
     */
    public void setHasDataList(ArrayList<Boolean> hasDataList)
    {
        this.hasDataList = hasDataList;
    }

    /**
     * @return the indexHash
     */
    public HashMap<Integer, Integer> getIndexHash()
    {
        return indexHash;
    }

    /**
     * @param indexHash the indexHash to set
     */
    public void setIndexHash(HashMap<Integer, Integer> indexHash)
    {
        this.indexHash = indexHash;
    }
    
    
}
