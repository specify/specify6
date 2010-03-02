/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.conversion;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Feb 26, 2010
 *
 */
//----------------------------------------------------------------
class CollectionInfoModel extends DefaultTableModel
{
    protected static String[]        headers            = null;
    protected Vector<CollectionInfo> collectionInfoList;
    /**
     * 
     */
    public CollectionInfoModel(final Vector<CollectionInfo> modelList)
    {
        super();
        collectionInfoList = modelList;
        getHeaders();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#getColumnCount()
     */
    @Override
    public int getColumnCount()
    {
        return headers == null ? 0 : headers.length;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int column)
    {
        return headers[column];
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#getRowCount()
     */
    @Override
    public int getRowCount()
    {
        return collectionInfoList != null ? collectionInfoList.size() : 0;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        return getColumnClassForModel(columnIndex);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int row, int column)
    {
        CollectionInfo ci = collectionInfoList.get(row);
        return getValueAt(ci, column);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
     */
    @Override
    public void setValueAt(Object value, int row, int column)
    {
        
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int row, int column)
    {
        return canEdit(column);
    }
    
    
    
    /**
     * @param col
     * @return
     */
    public boolean canEdit(final int col)
    {
        switch (col)
        {
            case  0 : return true; // isIncluded;
            case  1 : return false; // colObjTypeName;
            case  2 : return true; // catSeriesName;
            case  3 : return true; // catSeriesPrefix;
            case  4 : return true; // catSeriesRemarks;
            case  5 : return false; // taxonNameId;
            case  6 : return false; // taxonomyTypeName;
            case  7 : return false; // disciplineTypeObj;
            case  8 : return false; // kingdomId;
            case  9 : return false; // taxonName;
            case 10 : return false; // taxonNameCnt;
            case 11 : return false; // colObjDetTaxCnt;
            case 12 : return false; // srcHostTaxonCnt;
            case 13 : return false; // colObjCnt;
         }
        return false; 
    }
    
    /**
     * @param col
     * @return
     */
    public Object getValueAt(final CollectionInfo ci, final int col)
    {
        
        switch (col)
        {
            case  0 : return ci.isIncluded;
            case  1 : return ci.colObjTypeName;
            case  2 : return ci.catSeriesName;
            case  3 : return ci.catSeriesPrefix;
            case  4 : return ci.taxonomicUnitTypeID != null ? ci.taxonomicUnitTypeID.toString() : "N/A";
            case  5 : return ci.taxonNameId;
            case  6 : return ci.taxonomyTypeName;
            case  7 : return ci.disciplineTypeObj != null ? ci.disciplineTypeObj.getTitle() : "N/A"; 
            case  8 : return ci.kingdomId;
            case  9 : return ci.taxonName;
            case 10 : return ci.taxonNameCnt;
            case 11 : return ci.colObjDetTaxCnt;
            case 12 : return ci.srcHostTaxonCnt;
            case 13 : return ci.colObjCnt;
        }
        return ""; 
    }
    
    /**
     * @return
     */
    public static String[] getHeaders()
    {
        if (headers == null)
        {
            headers = new String[] {
                    "Is Included",
                    "Coll Obj Type Name",
                    "Cat Series Name", 
                    "Cat Series Prefix", 
                    "Cat Series Remarks", 
                    "Taxon Root ID", 
                    "Taxonomy Type Name",
                    "Discipline",
                    "Kingdom Id", 
                    "Taxon Name (Root)", 
                    "# of Taxon", 
                    "# of Det Col Obj",
                    "Src Host Taxon",
                    "Col Obj Count"};
        }
        return headers;
    }
    
    /**
     * @param columnIndex
     * @return
     */
    private static Class<?> getColumnClassForModel(int columnIndex)
    {
        switch (columnIndex)
        {
            case  0 : return Boolean.class;
            case  1 : return String.class;
            case  2 : return String.class;
            case  3 : return String.class;
            case  4 : return String.class;
            case  5 : return Integer.class;
            case  6 : return String.class;
            case  7 : return String.class;
            case  8 : return Integer.class;
            case  9 : return String.class;
            case 10 : return Integer.class;
            case 11 : return Integer.class;
            case 12 : return Long.class;
            case 13 : return Integer.class;
        }
        return String.class;
    }
};

