/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.tasks.subpane.wb;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceBase;
import edu.ku.brc.specify.tasks.subpane.qb.RowRepeater;
import edu.ku.brc.specify.tasks.subpane.qb.RowRepeaterColumn;
import edu.ku.brc.specify.tasks.subpane.qb.RowRepeaterConst;

/**
 * JaperReports Custom DataSource for WorkbenchRows.
 * 
 * @author rod
 * 
 * @code_status Complete
 * 
 * Apr 2, 2007
 * 
 */
public class WorkbenchJRDataSource implements JRDataSource
{
    private static final Logger log = Logger.getLogger(WorkbenchJRDataSource.class);

    private Hashtable<String, Short> map      = new Hashtable<String, Short>();
    private Workbench                workbench;
    private int                      rowIndex = -1;
    private List<WorkbenchRow>       workbenchRows;
    /**
     * Sends repeats of rows to consumer of this source.
     */
    protected final RowRepeater repeater;
    /**
     * Number of repeats of the currently row.
     */
    protected int currentRowRepeats = 0;

    protected Object[] rowArray;
    
    protected RowRepeater buildRepeater(final Object repeats) {
        if (repeats == null) {
            return null;
        }
        else if (repeats instanceof String) {
            //assuming repeatColumnName does not refer to a formatted or aggregated column-
            //also assuming valid columnName-
            return new RowRepeaterColumn(map.get((String )repeats));
        }
        else if (repeats instanceof Integer) {
            return new RowRepeaterConst((Integer )repeats);
        }
        else {
            log.error("invalid repeats parameter: " + repeats);
            return null;
        }

    }
    /**
     * Constructor with Workbench.
     * @param workbench the workbench
     */
    public WorkbenchJRDataSource(final Workbench workbench, final boolean useLongFieldNames, final Object repeats) {
        this.workbench = workbench;
        workbenchRows = workbench.getWorkbenchRowsAsList();
        createMap(useLongFieldNames);
        this.repeater = buildRepeater(repeats);
        rowArray = new Object[map.size()];
    }

    /**
     * Constructor with Workbench and a the rows to use.
     * @param workbench the workbench
     * @param workbenchRows the rows to use
     */
    public WorkbenchJRDataSource(final Workbench workbench, final List<WorkbenchRow> workbenchRows,
    		final boolean useLongFieldNames, final Object repeats) {
        this.workbench = workbench;
        this.workbenchRows = workbenchRows;
        Collections.sort(workbenchRows);
        createMap(useLongFieldNames);
        this.repeater = buildRepeater(repeats);
        rowArray = new Object[map.size()];
    }

    /**
     * Maps the name to the column index.
     */
    protected void createMap(final boolean useLongFieldNames)
    {
        for (WorkbenchTemplateMappingItem wbtmi : workbench.getWorkbenchTemplate()
                .getWorkbenchTemplateMappingItems())
        {
            String fldName = wbtmi.getFieldName();
            if (useLongFieldNames) 
            {
            	fldName = wbtmi.getTableName() + "." + fldName;
            }
        	map.put(fldName, wbtmi.getViewOrder());
            //System.out.println(wbtmi.getFieldName() + ", " + wbtmi.getViewOrder());
        }
    }

    /*
     * (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     */
    public Object getFieldValue(final JRField field) throws JRException
    {
        WorkbenchRow row = workbench.getRow(rowIndex);
        Short inx = map.get(field.getName());
        return inx == null ? "" : row.getData(inx);
    }
    
    /*
     * (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     */
    public Object getFieldValue(String field)
    {
        WorkbenchRow row = workbench.getRow(rowIndex);
        Short inx = map.get(field);
        return inx == null ? "" : row.getData(inx);
    }

    /*
     * (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#next()
     */
    public boolean next() throws JRException {
       if (currentRowRepeats > 0) {
            currentRowRepeats--;
            return true;
        }
        if (rowIndex >= workbenchRows.size() - 1) {
            return false;
        }
        rowIndex++;
        if (repeater != null) {
            currentRowRepeats = repeater.repeats(getRepeaterRowVals()) - 1;
        }
        else {
            currentRowRepeats = 0;
        }
        return true;
    }

    /**
     * @return
     */
    protected Object[] getRepeaterRowVals() {
    	WorkbenchRow row = workbench.getRow(rowIndex);
    	for (int i = 0; i < rowArray.length; i++) {
    		rowArray[i] = row.getData(i);
    	}
    	return rowArray;
    }

    
}
