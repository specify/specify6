/*
 * Created on Feb 15, 2005
 * 
 * To change the template for this generated file go to Window&gt;Preferences&gt;Java&gt;Code
 * Generation&gt;Code and Comments
 */
package edu.ku.brc.specify.ui.forms;

import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.w3c.dom.Node;

/**
 * This classes is the abstract "base" class implementation of the model that is used in the
 * JTables. Classes are derived from this class so they can show more or less information for the
 * items that are being displayed.
 */
public class TableViewObjModel extends AbstractTableModel
{
    public static final int TABLE_FIELDS = 0;
    public static final int QUERY_FIELDS = 1;

    protected Vector        _colInfo     = new Vector();
    protected List          _data        = null;
    protected Class         _class       = null;
    protected boolean       _isAttr      = false;

    // protected DataGetter _dataGetter = null;

    /**
     * Constructor for Table Model
     */
    public TableViewObjModel()
    {
        // _data = aData;
        // _class = aDefClass;
        // _isAttr = aIsAttr;
        // _dataGetter = aDataGetter;

        // changeView(aNode);
    }

    /**
     * 
     * @param node
     *            node
     */
    public void changeView(Node node)
    {
        /*
         * if (aNode == null || _class == null) { return; } _colInfo.clear(); try { NodeList colList =
         * XPathAPI.selectNodeList(aNode, "columns/column"); for (int i=0;i<colList.getLength();i++) {
         * Node node = colList.item(i); String label = XMLHelper.findAttrValue(node, "label");
         *  // Get the Value for the TextField String fieldName = XMLHelper.findAttrValue(node,
         * "name"); Class typeClass = null;
         * 
         * if (!_isAttr) { if (fieldName.indexOf(".") == -1) { typeClass =
         * _class.getDeclaredField(fieldName).getType(); } else { typeClass = String.class; } } else {
         * typeClass = String.class; } ColumnInfo colInfo = new ColumnInfo(label, typeClass,
         * fieldName); _colInfo.addElement(colInfo);
         *  } } catch (Exception e) { System.err.println(e); } fireTableModelChanged();
         */
    }

    public void clear()
    {
        if (_data != null)
        {
            _data.clear();
        }
        fireTableModelChanged();
    }

    public void setData(List aData)
    {
        _data = aData;
        fireTableModelChanged();
    }

    /**
     * Returns the number of columns
     * 
     * @return Number of columns
     */
    public int getColumnCount()
    {
        return _colInfo.size();
    }

    /**
     * Returns the Class object for a column
     * 
     * @param aColumn
     *            the column in question
     * @return the Class of the column
     */
    @SuppressWarnings("unchecked")
    public Class getColumnClass(int aColumn)
    {
        return ((ColumnInfo) _colInfo.elementAt(aColumn))._class;
    }

    /**
     * Indicates if col and row is editable
     * 
     * @param aRow
     *            the row of the cell
     * @param aColumn
     *            the column of the cell
     */
    public boolean isCellEditable(int aRow, int aColumn)
    {
        return false;
    }

    /**
     * Get the column name
     * 
     * @param aColumn
     *            the column of the cell to be gotten
     */
    public String getColumnName(int aColumn)
    {
        return ((ColumnInfo) _colInfo.elementAt(aColumn))._label;
    }

    public Object getItem(int aIndex)
    {
        if (_data == null)
            return "";
        return _data.get(aIndex);
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
         * if (_data == null) return "";
         * 
         * Object obj = _data.get(aRow); ColumnInfo colInfo =
         * (ColumnInfo)_colInfo.elementAt(aColumn); Object value = ""; try { if (_isAttr) {
         * PrepAttrs pa = (PrepAttrs)_data.get(aRow); if (colInfo._fieldName.equals("fieldname")) {
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
    public void setValueAt(Object aValue, int aRow, int aColumn)
    {

    }

    /**
     * Returns the number of rows
     * 
     * @return Number of rows
     */
    public int getRowCount()
    {
        return _data == null ? 0 : _data.size();
    }

    /**
     * Notifies the table that it has been completely updated.
     * 
     */
    public void fireTableModelChanged()
    {
        if (_data != null)
        {
            fireTableRowsUpdated(0, _data.size() - 1);
        }
        fireTableDataChanged();
    }

    // ------------------------------------------
    protected class ColumnInfo
    {
        public String _label     = null;
        public Class  _class     = null;
        public String _fieldName = null;

        public ColumnInfo(String aLabel, Class aClass, String aFieldName)
        {
            _label = aLabel;
            _class = aClass;
            _fieldName = aFieldName;
            // System.out.println("["+_label+"]["+_class.getName()+"]["+_fieldName+"]");
        }
    }

}
