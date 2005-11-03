package edu.ku.brc.specify.ui.forms.persist;

import java.util.Vector;

public class FormTableView extends FormView
{

    protected Vector<FormColumn> columns = new Vector<FormColumn>();
    
    /**
     * 
     *
     */
    public FormTableView()
    {
        super(ViewType.table, -1);
        
    }

    /**
     * 
     * @param aType
     * @param aId
     */
    public FormTableView(ViewType aType, int aId)
    {
        super(ViewType.table, aId);
        
    }
    
    /**
     * 
     * @param aColumn
     * @return
     */
    public FormColumn addColumn(FormColumn aColumn)
    {
        columns.add(aColumn);
        return aColumn;
    }
    
    public Vector<FormColumn> getColumns()
    {
        return columns;
    }

    public void setColumns(Vector<FormColumn> columns)
    {
        this.columns = columns;
    }

    //-------------------------------------------------------------------
    // Helpers
    //-------------------------------------------------------------------
    public FormColumn createColumn(String aName, String aLabel)
    {
        return addColumn(new FormColumn(aName, aLabel));
    }
    
   
    
}
