package edu.ku.brc.specify.ui.forms.persist;

import java.util.Vector;


public class FormFormView extends FormView
{
    protected Vector<String>  columnDef = new Vector<String>();
    protected Vector<String>  rowDef    = new Vector<String>();
    protected Vector<FormRow> rows      = new Vector<FormRow>(); 

    /**
     * 
     *
     */
    public FormFormView()
    {
        super(ViewType.form, -1);
        
    }

    /**
     * 
     * @param aType
     * @param aId
     */
    public FormFormView(ViewType aType, int aId)
    {
        super(ViewType.form, aId);
        
    }
    
    public void addColDef(String aColDef)
    {
        columnDef.add(aColDef);
    }

    public void addRowDef(String aRowDef)
    {
        rowDef.add(aRowDef);
    }
    
    public FormRow addRow(FormRow aRow)
    {
        rows.add(aRow);
        return aRow;
    }

    public Vector<String> getColumnDef()
    {
        return columnDef;
    }

    public Vector<String> getRowDef()
    {
        return rowDef;
    }

    public Vector<FormRow> getRows()
    {
        return rows;
    }

    public void setColumnDef(Vector<String> columnDef)
    {
        this.columnDef = columnDef;
    }

    public void setRowDef(Vector<String> rowDef)
    {
        this.rowDef = rowDef;
    }

    public void setRows(Vector<FormRow> rows)
    {
        this.rows = rows;
    }
    
}
