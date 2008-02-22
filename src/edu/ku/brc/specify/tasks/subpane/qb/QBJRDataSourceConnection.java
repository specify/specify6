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

import it.businesslogic.ireport.connection.JRCSVDataSourceConnection;

import java.util.HashMap;

import net.sf.jasperreports.engine.JRDataSource;
import edu.ku.brc.af.core.expresssearch.ERTICaptionInfo;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * Quick attempt at providing access to fields in a query while using iReport.
 * Extending JRCSVDataSourceConnection is necessary to get iReport to add the fields automatically and to allow use of new report wizard.
 *
 */
@SuppressWarnings("unchecked") //iReport's code has no generic parameters.
public class QBJRDataSourceConnection extends JRCSVDataSourceConnection
{

    public QBJRDataSourceConnection()
    {
        //emptiness
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
        for (ERTICaptionInfo col : QueryBldrPane.getColumnInfo(this.getName(), true))
        {
            getColumnNames().add(col.getColLabel());
        }
    }

    
    /* (non-Javadoc)
     * @see it.businesslogic.ireport.IReportConnection#getProperties()
     */
    @Override
    public HashMap getProperties()
    {
        HashMap map = new HashMap();
        for (int i=0; i< getColumnNames().size(); ++i)
        {
            map.put("COLUMN_" + i, getColumnNames().get(i) );
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

}
