/* Copyright (C) 2012, University of Kansas Center for Research
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
package edu.ku.brc.specify.dbsupport.cleanuptools;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.DataModelObjBase;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 6, 2012
 *
 */
public class MultipleRecordComparer
{
    protected boolean                     isParent;
    protected DBTableInfo                 tblInfo;
    protected DBTableInfo                 parentTI;
    protected FindItemInfo                fii;
    
    protected int                         numColsWithData  = 0;
    protected boolean                     hasColmnsOfData  = false;
    protected boolean                     hasKidsData      = false;
    
    protected boolean                     isSingleRowIncluded = false;
    
    protected Vector<DBFieldInfo>         columns       = new Vector<DBFieldInfo>();
    protected boolean[]                   colHasData    = null;
    protected boolean[]                   colIsSame     = null;
    protected Vector<Object[]>            dataItems     = new Vector<Object[]>();
    
    protected Vector<MergeInfoItem>            mergeItems   = new Vector<MergeInfoItem>();
    
    // for formatting
    private HashMap<DBFieldInfo, Integer> colToIndexMap = new HashMap<DBFieldInfo, Integer>();
    private Vector<DisplayColInfo>        displayCols   = new Vector<DisplayColInfo>();
    private boolean                       isSingleCol   = true;
    private int                           indexForTitle = -1;
    private DataModelObjBase              displayObj    = null;

    
    // Children Tables
    protected ArrayList<MultipleRecordComparer> kids   = new ArrayList<MultipleRecordComparer>();
    
    /**
     * @param fii
     * @param parentTableId
     * @param tableIds
     */
    public MultipleRecordComparer(final FindItemInfo fii, 
                                  final int parentTableId, 
                                  final int...tableIds)
    {
        super();
        
        this.fii     = fii;
        this.tblInfo = DBTableIdMgr.getInstance().getInfoById(parentTableId); 
        
        for (int tblId : tableIds)
        {
            MultipleRecordComparer mrc = new MultipleRecordComparer(fii, tblInfo, tblId);
            kids.add(mrc);
        }
        this.isParent = true;
    }
    
    /**
     * @param fii
     * @param parentTI
     * @param tableId
     */
    public MultipleRecordComparer(final FindItemInfo fii, 
                                  final DBTableInfo parentTI, 
                                  final int tableId)
    {
        super();
        this.fii      = fii;
        this.parentTI = parentTI; 
        this.tblInfo  = DBTableIdMgr.getInstance().getInfoById(tableId); 
        this.isParent = false;
    }
    
    /**
     * @param isSingleIncl one value for each sub-panel indicating whether only one row should be used. 
     */
    public void setSingleRowIncluded(final boolean...isSingleIncl)
    {
        if (isParent)
        {
            if (kids.size() == isSingleIncl.length)
            {
                for (int i=0;i<isSingleIncl.length;i++)
                {
                    kids.get(i).setSingleRowIncluded(isSingleIncl[i]);
                }
            }
        } else if (isSingleIncl.length == 1)
        {
           this.isSingleRowIncluded = isSingleIncl[0];
        }
    }
    
    /**
     * @return the isSingleRowIncluded
     */
    public boolean isSingleRowIncluded()
    {
        return isSingleRowIncluded;
    }

    /**
     * @param fii
     * @return
     */
    private boolean containsFieldInfo(final DBFieldInfo fii)
    {
        for (DisplayColInfo dci : displayCols)
        {
            if (dci.getFi() == fii)
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 
     */
    public boolean loadData()
    {
        int numSQLCols = 0;
        for (DisplayColInfo dci : displayCols)
        {
            if (dci.isIncludeSQLOnly())
            {
                columns.add(dci.getFi());
                numSQLCols++;
            }
        }
        
        for (DBFieldInfo fi : tblInfo.getFields())
        {
            if (containsFieldInfo(fi))
            {
                columns.insertElementAt(fi, numSQLCols);
                
            } else if (!fi.isHidden())
            {
                columns.add(fi);
            }
        }
        
        HashSet<Integer> inclColsIndexSet = new HashSet<Integer>();
        int index = 0;
        for (DBFieldInfo fi : columns)
        {
            if (containsFieldInfo(fi))
            {
                inclColsIndexSet.add(index);
            }
            index++;
        }
        
        // Build SELECT
        StringBuilder cols = new StringBuilder();
        for (DBFieldInfo fi : columns)
        {
            if (cols.length() > 0) cols.append(",");
            cols.append(fi.getColumn());
        }
        
        // Load Data
        DBTableInfo ti = parentTI != null ? parentTI : tblInfo; 
        final String sql = String.format("SELECT %s, %s FROM %s WHERE %s in %s", 
                                         cols.toString(), tblInfo.getIdColumnName(), 
                                         tblInfo.getName(), ti.getIdColumnName(), fii.getInClause(true));

        System.out.println("Data ------------\n"+sql);
        dataItems = BasicSQLUtils.query(sql);
        
        if (dataItems.size() > 0)
        {
            // First check to see which columns have data
            for (Object[] row : dataItems)
            {
                if (colHasData == null)
                {
                    colHasData = new boolean[row.length]; // all columns (including id)
                    for (int i=0;i<row.length;i++) colHasData[i] = false;
                }
                for (int i=0;i<row.length;i++)
                {
                    if (inclColsIndexSet.contains(i))
                    {
                        colHasData[i] = true;
                        
                    } else if (row[i] != null) 
                    {
                        if (row[i] instanceof String)
                        {
                            colHasData[i] = StringUtils.isNotEmpty((String)row[i]);
                        } else
                        {
                            colHasData[i] = true;
                        }
                    }
                    System.out.print(row[i]+", ");
                }
                System.out.println();
            }
            System.out.println("------------"+sql);
            //for (int j=0;j<hasData.length;j++) System.out.print(j+" "+hasData[j]+", ");
            //System.out.println();
            
            // Now check to see if all value in the each column are the same.
            colIsSame = new boolean[colHasData.length];
            //colIsSame[0] = true;
            
            // Don't check last column
            for (int i=0;i<colHasData.length-1;i++)
            {
                if (colHasData[i])
                {
                    colIsSame[i] = false;

                    if (!inclColsIndexSet.contains(i))
                    {
                        colIsSame[i] = true;
                        
                        Object value = null;
                        for (Object[] row : dataItems)
                        {
                            Object otherVal = row[i];
                            if (value == null)
                            {
                                if (otherVal != null)
                                {
                                    value = otherVal;
                                }
                            } else if (value != null)
                            {
                                if (!value.equals(otherVal))
                                {
                                    colIsSame[i] = false;
                                    break;
                                }
                            }
                        }
                    } else
                    {
                        System.out.println("Skipping "+i);
                    }
                } else
                {
                    colIsSame[i] = true;
                }
            }
            
            if (displayCols.size() > 0)
            {
                int inx = 0;
                for (DBFieldInfo fldCol : columns)
                {
                    if (containsFieldInfo(fldCol))
                    {
                        colToIndexMap.put(fldCol, inx);
                    }
                    inx++;
                }
                indexForTitle = colToIndexMap.size() > 0 ? colToIndexMap.values().iterator().next() : -1; // for singles only
            }

            
//            for (Integer index : colToIndexMap.values())
//            {
//                colIsSame[index] = false;
//            }
            
            //System.out.println(String.format("Cols %d  hd: %d", columns.size(), hasData.length));
            
            Vector<DBFieldInfo> oldColumns = new Vector<DBFieldInfo>(columns); // does not include ID column
            columns.clear();
            
            // Add 'Is Included to data model
            if (isParent)
            {
                DBFieldInfo fldInfo = new DBFieldInfo(tblInfo, "FALSE", "MergedInto", "boolean", 1, true, true, false, false, false, null);
                fldInfo.setTitle(getResourceString("CLNUP_MERGE_INTO"));
                columns.add(fldInfo);
                
                fldInfo = new DBFieldInfo(tblInfo, "FALSE", "MergedFrom", "boolean", 1, true, true, false, false, false, null);
                fldInfo.setTitle(getResourceString("CLNUP_MERGE_FROM"));
                columns.add(fldInfo);
                
                if (indexForTitle > -1) indexForTitle += 2;
                
            } else
            {
                DBFieldInfo isInclFld = new DBFieldInfo(tblInfo, "FALSE", "IsIncluded", "boolean", 1, true, true, false, false, false, null);
                isInclFld.setTitle(getResourceString("CLNUP_MERGE_ISINCL"));
                columns.add(isInclFld);
                if (indexForTitle > -1) indexForTitle++;
            }
            
            
            for (int j=0;j<colIsSame.length;j++) System.out.print(String.format("%3d", j));
            System.out.println();
            for (int j=0;j<colHasData.length;j++) System.out.print(String.format("  %s", colHasData[j] ? "Y" : "N"));
            System.out.println("  (Has Data)");
            for (int j=0;j<colIsSame.length;j++) System.out.print(String.format("  %s", colIsSame[j] ? "Y" : "N"));
            System.out.println("  (Is Same)");

            numColsWithData = 0;
            for (int i=0;i<colHasData.length-1;i++)
            {
                System.out.println(i+" -> "+(colHasData[i] && !colIsSame[i])+" Has: "+colHasData[i]+"  !SM: "+!colIsSame[i]+"  "+oldColumns.get(i).getTitle());
                if (colHasData[i] && !colIsSame[i]) 
                {
                    numColsWithData++;
                    columns.add(oldColumns.get(i));
                    System.out.println(i+" Added: "+oldColumns.get(i).getTitle());
                }
            }
            
            hasColmnsOfData = numColsWithData > 0;
            if (hasColmnsOfData)
            {
                numColsWithData += 2; // For IsIncluded and IdColumn
                if (isParent) numColsWithData++;
                
                Vector<Object[]> oldDataItems = new Vector<Object[]>(dataItems);
                dataItems.clear();
                
                for (Object[] row : oldDataItems)
                {
                    int inx = 0;
                    Object[] newRow = new Object[numColsWithData];
                    newRow[inx++] = false; // isIncluded or Merged Into
                    
                    if (isParent) newRow[inx++] = false; // Merged From
                    
                    for (int i=0;i<row.length;i++)
                    {
                        //System.out.println(i+" -> "+(colHasData[i] && !colIsSame[i])+" "+colHasData[i]+" "+!colIsSame[i]);
                        if (colHasData[i] && !colIsSame[i])
                        {
                            newRow[inx++] = row[i];
                        }
                    }
                    dataItems.add(newRow);
                }
                
                for (int j=0;j<columns.size();j++) System.out.print(j+" "+columns.get(j).getTitle()+", ");
                System.out.println();
                System.out.println(String.format("Cols %d  hd: %d", columns.size(), colHasData.length));
            }
        }
        
        /*for (MultipleRecordComparer mrc : kids)
        {
            if (mrc.loadData())
            {
                hasKidsData = true;
            }
        }*/
        
        return hasColmnsOfData;
    }
    
    /**
     * @param cols
     */
    /*public void addDisplayColumns(final DBFieldInfo...cols)
    {
        for (DBFieldInfo col : cols)
        {
            displayCols.add(col);
        }
        isSingleCol = false;
    }*/
    
    /**
     * @param col
     */
    public void addDisplayColumn(final String colName)
    {
        DBFieldInfo fi = tblInfo.getFieldByColumnName(colName);
        if (fi != null)
        {
            displayCols.add(new DisplayColInfo(fi, false));
        }
    }
    
    /**
     * @param colName
     * @param colTitle
     * @param sql 
     */
    public void addDisplayColumn(final String colName, 
                                 final String colTitle, 
                                 final String sql)
    {
        DBFieldInfo fi = new DBFieldInfo(tblInfo, sql, colName, "text", 256, true, true, false, false, false, null);
        if (fi != null)
        {
            fi.setTitle(colTitle);
            displayCols.add(new DisplayColInfo(fi, true));
        }
    }

    /**
     * @return
     */
    public String getTitle()
    {
        if (dataItems.size() > 0 && isParent)
        {
            Object[] firstRow = dataItems.get(0);
            return getFormattedTitle(firstRow);
        }
        return "N / A";
    }

    /**
     * @return
     */
    public String getFormattedTitle(final Object[] rowData)
    {
        if ((isSingleCol || isParent) && indexForTitle > -1 && rowData[indexForTitle] != null)
        {
            return rowData[indexForTitle].toString();
        }
        
        if (displayCols.size() > 0)
        {
            if (displayObj == null)
            {
                try
                {
                    displayObj = (DataModelObjBase) tblInfo.getClassObj().newInstance();
                } catch (InstantiationException e)
                {
                    e.printStackTrace();
                } catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
            
            if (displayObj != null)
            {
                for (DisplayColInfo dci : displayCols)
                {
                    if (!dci.isIncludeSQLOnly())
                    {
                        int inx = colToIndexMap.get(dci.getFi());
                        FormHelper.setValue(displayObj, dci.getFi().getColumn(), rowData[inx]);
                    }
                }
                UIFieldFormatterIFace formatter = UIFieldFormatterMgr.getInstance().getFormatter(tblInfo.getDataObjFormatter());
                       
                Object fmtObj = formatter != null ? formatter.formatToUI(displayObj) : null;
                return  fmtObj != null ? fmtObj.toString() : displayObj.getIdentityTitle();
            }
        }
        return null;
    }

    /**
     * @return the numColsWithData
     */
    public int getNumColsWithData()
    {
        return numColsWithData;
    }

    /**
     * @return the hasColmnsOfData
     */
    public boolean hasColmnsOfData()
    {
        return hasColmnsOfData;
    }

    /**
     * @return the hasKidsData
     */
    public boolean hasKidsData()
    {
        return hasKidsData;
    }

    /**
     * @return the kids
     */
    public List<MultipleRecordComparer> getKids()
    {
        return kids;
    }

    /**
     * @return the columns
     */
    public List<DBFieldInfo> getColumns()
    {
        return columns;
    }

    /**
     * @return the fii
     */
    public FindItemInfo getFindItemInfo()
    {
        return fii;
    }

    /**
     * @return the dataItems
     */
    public Vector<Object[]> getDataItems()
    {
        return dataItems;
    }

    /**
     * @return the tblInfo
     */
    public DBTableInfo getTblInfo()
    {
        return tblInfo;
    }

    /**
     * @return the isParent
     */
    public boolean isParent()
    {
        return isParent;
    }
    
    class DisplayColInfo
    {
        private DBFieldInfo fi;
        private boolean     includeSQLOnly;
        
        /**
         * @param fi
         * @param includeSQLOnly
         */
        public DisplayColInfo(DBFieldInfo fi, boolean includeSQLOnly)
        {
            super();
            this.fi = fi;
            this.includeSQLOnly = includeSQLOnly;
        }
        /**
         * @return the fi
         */
        public DBFieldInfo getFi()
        {
            return fi;
        }
        /**
         * @return the includeSQLOnly
         */
        public boolean isIncludeSQLOnly()
        {
            return includeSQLOnly;
        }
        
    }
}
