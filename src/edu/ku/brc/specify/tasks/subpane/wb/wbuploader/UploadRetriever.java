/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.awt.Frame;
import java.sql.ResultSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.CustomQueryListener;
import edu.ku.brc.dbsupport.JPAQuery;
import edu.ku.brc.dbsupport.SQLExecutionListener;
import edu.ku.brc.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.specify.tasks.subpane.ESResultsSubPane;
import edu.ku.brc.specify.tasks.subpane.ESResultsTablePanel;
import edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.QueryForIdResultsIFace;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class UploadRetriever implements CommandListener, SQLExecutionListener, CustomQueryListener
{
    protected static final Logger log = Logger.getLogger(UploadRetriever.class);
    
    public void viewUploads(final List<UploadTable> uploadTables, final Taskable task, final String title)
    {
        viewUploads2(uploadTables, new ESResultsSubPane(title, task, true) 
        {

            /* (non-Javadoc)
             * @see edu.ku.brc.specify.tasks.subpane.ESResultsSubPane#createResultsTable(edu.ku.brc.ui.db.QueryForIdResultsIFace)
             */
            @Override
            protected ESResultsTablePanel createResultsTable(QueryForIdResultsIFace results)
            {
                return new UploadResultsTablePanel(this, results, results.shouldInstallServices(), results.isExpanded()); 
            }
            
        });
    }
    
    protected void viewUploads1(final List<UploadTable> uploadTables, final ExpressSearchResultsPaneIFace esrPane)
    {
        //Starts a bunch of jpa query threads. 
        //Could also skip query launching step and just do a select for each uploaded key and add the result to esrPane???
        for (UploadTable ut : uploadTables)
        {
            startUploadViewJPA(esrPane, ut);
        }
    }

    protected void viewUploads2(final List<UploadTable> uploadTables, final ExpressSearchResultsPaneIFace esrPane)
    {
        for (UploadTable ut : uploadTables)
        {
            esrPane.addSearchResults(new UploadResults(ut, Uploader.getCurrentUpload().uploadData));
        }
        CustomDialog cd = new CustomDialog((Frame )UIRegistry.getTopWindow(), 
                        "Lookee", 
                        true,
                        (ESResultsSubPane )esrPane);
        UIHelper.centerAndShow(cd);
    }

    
    protected JPAQuery startUploadViewJPA(final ExpressSearchResultsPaneIFace esrPane,
                                          final UploadTable ut)
    {
        JPAQuery jpaQuery = null;
        String sqlStr = getSQL(ut);
        log.debug(sqlStr);
        if (sqlStr != null)
        {
            jpaQuery = new JPAQuery(sqlStr, this);
            jpaQuery.setData(new Object[] { ut, esrPane });
            jpaQuery.start();
        }
        return jpaQuery;
    }

    protected String getSQL(final UploadTable ut)
    {
        //select fields present in the uploaded dataSet
        String fldsClause = "";
        for (Vector<UploadField> flds : ut.getUploadFields())
        {
            for (UploadField fld : flds)
            {
                if (fld.getIndex() != -1 && !(fld.getSequence() > 0))
                {
                    fldsClause += (StringUtils.isNotBlank(fldsClause) ? ", " : "") + "tbl." + fld.getField().getName();
                }
            }
        }

        if (StringUtils.isBlank(fldsClause))
        {
            return null;
        }
        
        //Make (potentially giant) IN expression for uploaded record keys
        //XXX Will this work???
        String whereClause = "";
        for (UploadedRecordInfo info : ut.getUploadedRecs())
        {
            whereClause += (StringUtils.isNotBlank(whereClause) ? ", " : "") + info.getKey().toString();
        }
        if (StringUtils.isNotBlank(whereClause))
        {
            whereClause = " where " + ut.getTable().getKey().getName() + " in(" + whereClause + ")";
        }
        
        if (StringUtils.isBlank(whereClause))
        {
            return null;
        }
        
        return "select " + fldsClause + " from " + ut.getWriteTable().getName() + " tbl " + whereClause;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#exectionDone(edu.ku.brc.dbsupport.CustomQueryIFace)
     */
    //@Override
    public void exectionDone(CustomQueryIFace customQuery)
    {
        // TODO Auto-generated method stub
        Object data = ((JPAQuery)customQuery).getData();
        UploadTable ut = (UploadTable )((Object[])data)[0];
        String name = ut.toString();
        log.debug("Upload results done for " + name + ": " + customQuery.getDataObjects().size());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#executionError(edu.ku.brc.dbsupport.CustomQueryIFace)
     */
    //@Override
    public void executionError(CustomQueryIFace customQuery)
    {
        // TODO Auto-generated method stub
        log.debug("Upload results error: " + customQuery.getName());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    //@Override
    public void doCommand(CommandAction cmdAction)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
     */
    //@Override
    public void exectionDone(SQLExecutionProcessor process, ResultSet resultSet)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SQLExecutionListener#executionError(edu.ku.brc.dbsupport.SQLExecutionProcessor, java.lang.Exception)
     */
    //@Override
    public void executionError(SQLExecutionProcessor process, Exception ex)
    {
        // TODO Auto-generated method stub
        
    }

}
