/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import it.businesslogic.ireport.IReportConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.ERTICaptionInfo;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * Allows access to fields in Specify queries while designing reports with IReport.
 * NOTE: Currently, the field names derive from the getTitle() method of FieldInfo.
 * This means that if titles are changed, then reports will need to be modified.
 * I think it is not too hard to add code to update field names in jrxmls when a report is run, but this has not been done yet.
 * We could avoid this issue by using the column names from the hql but they would not be user friendly at all.
 *  
 */
@SuppressWarnings("unchecked") //iReport's code has no generic parameters.
public class QBJRDataSourceConnection extends IReportConnection
//public class QBJRDataSourceConnection extends JRDataSourceProviderConnection
{
    protected final String queryName; //apparently iReport has it's own uses for the
                                      //name prop(s) so need to declare a new var.
    protected final SpQuery query;
    
    protected final List<QBJRFieldDef> fields = new ArrayList<QBJRFieldDef>();
    
//    public QBJRDataSourceConnection()
//    {
//        //emptiness
//    }
    
    /**
     * @param query (query.forceLoad(false) should have already been executed)
     */
    public QBJRDataSourceConnection(final SpQuery query)
    {
        super();
        this.setName(query.getName());
        this.queryName = query.getName();
        this.query = query;
    }
    
    /**
     * @param queryName
     * 
     * probably should do without this constructor.
     */
    public QBJRDataSourceConnection(final String queryName)
    {
        super();
        this.setName(queryName);
        this.queryName = queryName;
        this.query = null;
    }
    
    /* (non-Javadoc)
     * @see it.businesslogic.ireport.IReportConnection#getJRDataSource()
     */
    @Override
    public JRDataSource getJRDataSource()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see it.businesslogic.ireport.IReportConnection#loadProperties(java.util.HashMap)
     */
    @Override
    public void loadProperties(HashMap map)
    {
        if (queryName != null)
        {
            fields.clear();
            List<ERTICaptionInfo> cols;
            if (query == null)
            {
                cols = QueryBldrPane.getColumnInfo(queryName, true);
            }
            else
            {
                cols = QueryBldrPane.getColumnInfoSp(query.getFields(), true);
            }
            for (ERTICaptionInfo col : cols)
            {
                fields.add(new QBJRFieldDef(col.getColLabel(), col.getColClass()));
            }
        }
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
            map.put("COLUMN_" + i, fields.get(i).getFldName());
        }
        return map;
    }

    /* (non-Javadoc)
     * @see it.businesslogic.ireport.IReportConnection#test()
     */
    @Override
    public void test() throws Exception
    {
        // TODO Auto-generated method stub
        super.test();
    }
    
    /**
     * @return number of fields.
     */
    public int getFields()
    {
        return fields.size();
    }
    
    /**
     * @param index
     * @return field at index.
     */
    public QBJRFieldDef getField(int index)
    {
        return fields.get(index);
    }
    
    public class QBJRFieldDef
    {
        protected final String fldName;
        protected final Class<?> fldClass;
        
        public QBJRFieldDef(final String fldName, final Class<?> fldClass)
        {
            this.fldName = fldName;
            this.fldClass = fldClass;
        }

        /**
         * @return the fldName
         */
        public String getFldName()
        {
            return fldName;
        }

        /**
         * @return the fldClass
         */
        public Class<?> getFldClass()
        {
            return fldClass;
        }
    }

    /* (non-Javadoc)
     * @see it.businesslogic.ireport.IReportConnection#getDescription()
     */
    @Override
    public String getDescription()
    {
        return UIRegistry.getResourceString("SPECIFY_REPORT_CONNECTION");
    }

    /**
     * @return the query
     */
    public SpQuery getQuery()
    {
        return query;
    }
    
    
}
