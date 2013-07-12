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
package edu.ku.brc.specify.tasks.subpane.qb;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.tasks.subpane.JRConnectionFieldDef;
import edu.ku.brc.specify.tasks.subpane.SpJRIReportConnection;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * Allows access to fields in Specify queries and workbenches while designing reports with IReport.
 * NOTE: Currently, the field names derive from the getTitle() method of FieldInfo.
 * This means that if titles are changed, then reports will need to be modified.
 * I think it is not too hard to add code to update field names in jrxmls when a report is run, but this has not been done yet.
 * We could avoid this issue by using the column names from the hql but they would not be user friendly at all.
 *  
 */
@SuppressWarnings("unchecked") //iReport's code has no generic parameters.
public class QBJRIReportConnection extends SpJRIReportConnection
{
    protected final SpQuery query;
    
    
    /**
     * @param query (query.forceLoad(false) should have already been executed)
     */
    public QBJRIReportConnection(final SpQuery query)
    {
        super(query.getName());
        this.query = query;
    }
    
    /**
     * @param queryName
     * 
     * probably should do without this constructor.
     */
    public QBJRIReportConnection(final String queryName)
    {
        super(queryName);
        this.query = null;
    }
    

    /* (non-Javadoc)
     * @see it.businesslogic.ireport.IReportConnection#loadProperties(java.util.HashMap)
     */
    @Override
    public void loadProperties(HashMap map)
    {
        if (objectName != null)
        {
            fields.clear();
            List<ERTICaptionInfo> cols;
            if (query == null)
            {
                cols = QueryBldrPane.getColumnInfo(objectName, true);
            }
            else
            {
                cols = QueryBldrPane.getColumnInfoSp(query.getFields(), true);
            }
            for (ERTICaptionInfo col : cols)
            {
                fields.add(new JRConnectionFieldDef(((ERTICaptionInfoQB )col).getColStringId(), 
                		col.getColLabel(), getColClass(col)));
            }
            //if query.getSelectDistinct() is true then the id will always be null.
            fields.add(new JRConnectionFieldDef("id", 
                		UIRegistry.getResourceString("QBJRIReportConnection.ID"), Integer.class));
            fields.add(new JRConnectionFieldDef("resultsetsize", 
            		UIRegistry.getResourceString("QBJRIReportConnection.ResultSetSize"), String.class));
        
        }
    }

    /**
     * @param col
     * @return the class of the data contained in the column, or the
     * the class returned by the formatter for the data in the column.
     * 
     * This code may be affected by changes to QBJRDataSourceBase.processValue, and vice-versa.
     */
    protected Class<?> getColClass(final ERTICaptionInfo col)
    {
        Class<?> cls = col.getColClass();
        if (cls.equals(Calendar.class)
                || cls.equals(Timestamp.class)
                || cls.equals(java.sql.Date.class) 
                || cls.equals(Date.class))
        {
            return String.class;
        }
    
        UIFieldFormatterIFace formatter = col.getUiFieldFormatter();
        if (formatter != null && formatter.isInBoundFormatter())
        {
            return Object.class;
        }
        
        return cls;
    }
    
    /* (non-Javadoc)
     * @see it.businesslogic.ireport.IReportConnection#getProperties()
     */
    @Override
    public HashMap getProperties()
    {
        HashMap map = new HashMap();
        for (int i=0; i< fields.size(); ++i)
        {
            //map.put("COLUMN_" + i, fields.get(i).getFldName());
            map.put("COLUMN_" + i, fields.get(i).getFldTitle());
        }
        return map;
    }

    
    /* (non-Javadoc)
     * @see it.businesslogic.ireport.IReportConnection#getDescription()
     */
    @Override
    public String getDescription()
    {
        return UIRegistry.getResourceString("REP_SPECIFY_REPORT_CONNECTION");
    }

    /**
     * @return the query
     */
    public SpQuery getQuery()
    {
        return query;
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.SpJRIReportConnection#getSpObject()
	 */
	@Override
	public DataModelObjBase getSpObject()
	{
		return query;
	}
    
    
}
