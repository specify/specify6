/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;

/**
 * JaperReports Custom DataSource for WorkbenchRows.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 2, 2007
 *
 */
public class WorkbenchJRDataSource implements JRDataSource
{
    private Hashtable<String, Short> map = new Hashtable<String, Short>();
    private Workbench                workbench;
    private int                      rowIndex = -1;        
    private List<WorkbenchRow>       workbenchRows;
    
    public WorkbenchJRDataSource(final Workbench workbench)
    {
        this.workbench = workbench;
        workbenchRows  = workbench.getWorkbenchRowsAsList();
        createMap();
    }

    public WorkbenchJRDataSource(final Workbench workbench, final List<WorkbenchRow> workbenchRows)
    {
        this.workbench     = workbench;
        this.workbenchRows = workbenchRows;
        Collections.sort(workbenchRows);
        
        createMap();
    }
    
    /**
     * Maps the name to the column index.
     */
    protected void createMap()
    {
        for (WorkbenchTemplateMappingItem wbtmi : workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems())
        {
            map.put(wbtmi.getFieldName(), wbtmi.getViewOrder());
        }
    }

    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     */
    public Object getFieldValue(final JRField field) throws JRException
    {
        WorkbenchRow row = workbench.getRow(rowIndex);
        Short inx = map.get(field.getName());
        return inx == null ? "XX" :  row.getData(inx);
    }

    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#next()
     */
    public boolean next() throws JRException
    {
        System.out.println("["+rowIndex+"]["+workbenchRows.size()+"]");
        if (rowIndex >= workbenchRows.size()-1)
        {
            return false;
        }
        rowIndex++;
        return true;
    }
}

