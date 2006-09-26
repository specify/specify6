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

import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * This classes is the abstract "base" class implementation of the model that is used in the
 * JTables. Classes are derived from this class so they can show more or less information for the
 * items that are being displayed.
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class TableViewObjModel extends AbstractTableModel
{
    public static final int TABLE_FIELDS = 0;
    public static final int QUERY_FIELDS = 1;

    protected Vector<Object>        colInfo     = new Vector<Object>();
    protected List<Object>          data        = null;
    protected Class<?>         classObj       = null;
    protected boolean       isAttr      = false;

    // protected DataGetter _dataGetter = null;

    /**
     * Constructor for Table Model.
     */
    public TableViewObjModel()
    {
        // changeView(aNode);
    }

//    /**
//     * Change the view.
//     * @param node node
//     */
//    public void changeView(Node node)
//    {
//        /*
//         * if (aNode == null || classObj == null) { return; } colInfo.clear(); try { NodeList colList =
//         * XPathAPI.selectNodeList(aNode, "columns/column"); for (int i=0;i<colList.getLength();i++) {
//         * Node node = colList.item(i); String label = XMLHelper.findAttrValue(node, "label");
//         *  // Get the Value for the TextField String fieldName = XMLHelper.findAttrValue(node,
//         * "name"); Class typeClass = null;
//         * 
//         * if (!isAttr) { if (fieldName.indexOf(".") == -1) { typeClass =
//         * classObj.getDeclaredField(fieldName).getType(); } else { typeClass = String.class; } } else {
//         * typeClass = String.class; } ColumnInfo colInfo = new ColumnInfo(label, typeClass,
//         * fieldName); colInfo.addElement(colInfo);
//         *  } } catch (Exception e) { System.err.println(e); } fireTableModelChanged();
//         */
//    }

    /**
     * Clears the data.
     */
    public void clear()
    {
        if (data != null)
        {
            data.clear();
        }
        fireTableModelChanged();
    }

    /**
     * Sets the data
     * @param aData
     */
    public void setData(final List<Object> aData)
    {
        this.data = aData;
        fireTableModelChanged();
    }

    /**
     * Returns the number of columns.
     * 
     * @return Number of columns
     */
    public int getColumnCount()
    {
        return colInfo.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int column)
    {
        return ((ColumnInfo) colInfo.elementAt(column))._classObj;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int aRow, int aColumn)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int aColumn)
    {
        return ((ColumnInfo) colInfo.elementAt(aColumn))._label;
    }

    public Object getItem(int aIndex)
    {
        if (data == null)
            return "";
        return data.get(aIndex);
    }

    /**
     * Gets the value of the row col
     * 
     * @param aRow
     *            the row of the cell to be gotten
     * @param aColumn
     *            the column of the cell to be gotten
     */
    public Object getValueAt(int aRow, int aColumn)
    {
        return null;
        /*
         * if (data == null) return "";
         * 
         * Object obj = data.get(aRow); ColumnInfo colInfo =
         * (ColumnInfo)colInfo.elementAt(aColumn); Object value = ""; try { if (isAttr) {
         * PrepAttrs pa = (PrepAttrs)data.get(aRow); if (colInfo._fieldName.equals("fieldname")) {
         * value = pa.getName(); } else if (colInfo._fieldName.equals("value")) { value =
         * pa.getValue(); } else { return ""; } } else { return _dataGetter.getFieldValue(obj,
         * colInfo._fieldName); } } catch (Exception e) {
         * System.err.println(e+"\n"+colInfo._fieldName); } return value;
         */
    }

    /**
     * Sets a new value into the Model
     * 
     * @param aValue
     *            the value to be set
     * @param aRow
     *            the row of the cell to be set
     * @param aColumn
     *            the column of the cell to be set
     */
    @Override
    public void setValueAt(Object aValue, int aRow, int aColumn)
    {
        // do nothing
    }

    /**
     * Returns the number of rows
     * 
     * @return Number of rows
     */
    public int getRowCount()
    {
        return data == null ? 0 : data.size();
    }

    /**
     * Notifies the table that it has been completely updated.
     * 
     */
    public void fireTableModelChanged()
    {
        if (data != null)
        {
            fireTableRowsUpdated(0, data.size() - 1);
        }
        fireTableDataChanged();
    }

    // ------------------------------------------
    protected class ColumnInfo
    {
        public String _label     = null;
        public Class<?>  _classObj     = null;
        public String _fieldName = null;

        public ColumnInfo(String aLabel, Class<?> aClass, String aFieldName)
        {
            _label = aLabel;
            _classObj = aClass;
            _fieldName = aFieldName;
            // System.out.println("["+_label+"]["+classObj.getName()+"]["+_fieldName+"]");
        }
    }

}
