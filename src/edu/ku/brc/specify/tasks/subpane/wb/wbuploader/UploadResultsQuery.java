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


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.CustomQueryListener;
import edu.ku.brc.dbsupport.QueryResultsContainerIFace;
import edu.ku.brc.dbsupport.QueryResultsDataObj;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class UploadResultsQuery implements CustomQueryIFace
{
    final List<Object[]> dataObjects; 
    
    public UploadResultsQuery(final UploadTable uploadTable, final UploadData uploadData)
    {
        dataObjects = new ArrayList<Object[]>(uploadTable.getUploadedRecs().size());
        List<Integer> indexes = buildFldIdxs(uploadTable);
        for (UploadedRecordInfo rec : uploadTable.getUploadedRecs())
        {
            Object[] rowData = new Object[indexes.size()+1];
            rowData[0] = rec.getKey();
            for (int i = 0; i < indexes.size(); i++)
            {
                rowData[i+1] = uploadData.getWbRow(rec.getWbRow()).getData(indexes.get(i));
            }
            dataObjects.add(rowData);
        }
    }
    
    protected List<Integer> buildFldIdxs(final UploadTable uploadTable)
    {
        List<Integer> result = new LinkedList<Integer>();
        for (Vector<UploadField> flds : uploadTable.getUploadFields())
        {
            for (UploadField fld : flds)
            {
                if (fld.getIndex() != -1 && !(fld.getSequence() > 0))
                {
                    result.add(fld.getIndex());
                }
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#execute()
     */
   //@Override
    public boolean execute()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#execute(edu.ku.brc.dbsupport.CustomQueryListener)
     */
    //@Override
    public void execute(CustomQueryListener cql)
    {
        cql.exectionDone(this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#getDataObjects()
     */
    //@Override
    public List<?> getDataObjects()
    {
        return dataObjects;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#getName()
     */
    //@Override
    public String getName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#getQueryDefinition()
     */
    //@Override
    public List<QueryResultsContainerIFace> getQueryDefinition()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#getResults()
     */
    //@Override
    public List<QueryResultsDataObj> getResults()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
