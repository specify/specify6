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
 * Populates dataObjects with uploaded record keys. Data is obtained directly from the dataSet, which is processor
 * and memory efficient. But not as simple as it should be. 
 */
public class UploadResultsQuery implements CustomQueryIFace
{
    protected final CustomQueryListener customQueryListener; 
    protected final UploadTable uploadTable;
    protected final UploadData uploadData;
    protected final List<Object[]> dataObjects; 
    protected int maxFldIdx;
    protected final static int MAX_DISPLAYED_ROWS = 2000;
    
    /**
     * @param fldDef
     * @return the column in which the field is displayed.
     */
    protected Integer getTblCol(final edu.ku.brc.util.Pair<Integer, Integer> fldDef)
    {
        return fldDef.getFirst();
    }
    
    /**
     * @param fldDef
     * @return the column holding the field in the dataSet.
     */
    protected Integer getWbCol(final edu.ku.brc.util.Pair<Integer, Integer> fldDef)
    {
        return fldDef.getSecond();
    }
    
    /**
     * Populates dataObjects with data directly from the dataSet.
     * @param uploadTable
     * @param uploadData
     */
    public UploadResultsQuery(final CustomQueryListener cql, final UploadTable uploadTable, final UploadData uploadData)
    {
        this.customQueryListener = cql;
        this.uploadTable = uploadTable;
        this.uploadData = uploadData;
        dataObjects = new ArrayList<Object[]>(uploadTable.getUploadedRecs().size());
    }
    
    /**
     * 
     */
    protected void fillRows()
    {
        Vector<Vector<edu.ku.brc.util.Pair<Integer, Integer>>> indexes = setupFldIdxInfo(uploadTable);
        if (indexes != null)
        {
            int row = 0;
            for (UploadedRecordInfo rec : uploadTable.getUploadedRecs())
            {
                Object[] rowData = new Object[maxFldIdx + 2];
                rowData[0] = rec.getKey();
                Vector<edu.ku.brc.util.Pair<Integer, Integer>> index = indexes.get(rec.getSeq());
                for (int i = 0; i < index.size(); i++)
                {
                    int wbCol = getWbCol(index.get(i));
                    Object val;
                    if (rec.getAutoAssignedVal() != null && uploadTable.getAutoAssignedField() != null && wbCol == uploadTable.getAutoAssignedField().getIndex())
                    {
                         val = rec.getAutoAssignedVal();
                    }
                    else
                    {
                        val = uploadData.getWbRow(rec.getWbRow())
                        .getData(wbCol);
                    }
                    rowData[getTblCol(index.get(i)) + 1] = val;
                }
                dataObjects.add(rowData);
                if (++row == MAX_DISPLAYED_ROWS)
                {
                    break;
                }
            }
        }
        
    }
    /**
     * @param uploadTable
     * 
     * @return a structure providing the dataSet column index for each column displayed and for each
     * sequence/occurrence (eg. lastName1, lastName2 ...)
     * 
     */
    protected Vector<Vector<edu.ku.brc.util.Pair<Integer, Integer>>> setupFldIdxInfo(final UploadTable uploadTable)
    {
        List<UploadField> sortedFlds = new LinkedList<UploadField>();
        for (Vector<UploadField> flds : uploadTable.getUploadFields())
        {
            for (UploadField fld : flds)
            {
                if (fld.getIndex() != -1)
                {
                    sortedFlds.add(fld);
                }
            }
        }

        if (sortedFlds.size() == 0)
        {
            //in this case the dataset contained no 'user' fields in the table.
            return null;
        }

        //else...

        //this puts them in order for processing in next step 
        UploadRetriever.columnOrder(sortedFlds);
        
        //get the maximum 'sequence' for the field. ie: the maximum number of occurences of records per row (lastName1, lastName2...)
        int maxSequence = sortedFlds.get(sortedFlds.size()-1).getSequenceInt() + 1;
        
        Vector<Vector<edu.ku.brc.util.Pair<Integer, Integer>>> result = new Vector<Vector<edu.ku.brc.util.Pair<Integer, Integer>>>(maxSequence);
        for (int s = 0; s < maxSequence; s++)
        {
            result.add(new Vector<edu.ku.brc.util.Pair<Integer, Integer>>());
        }
        int tblCol = -1;
        int seq = -1;
        for (UploadField fld : sortedFlds)
        {
            if (fld.getSequenceInt() != seq)
            {
                seq = fld.getSequenceInt();
                tblCol = 0;
            }
            (result.get(fld.getSequenceInt())).add(new edu.ku.brc.util.Pair<Integer, Integer>(tblCol, fld.getIndex()));
            if (tblCol > maxFldIdx)
            {
                maxFldIdx = tblCol;
            }
            tblCol++;
        }
        return result;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#execute()
     */
    @Override
    public boolean execute()
    {
        // JPQQuery uses this trick...
        execute(customQueryListener);
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#execute(edu.ku.brc.dbsupport.CustomQueryListener)
     */
    @Override
    public void execute(CustomQueryListener cql)
    {
        fillRows();
        if (dataObjects.size() > 0)
        {
            cql.exectionDone(this);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#getDataObjects()
     */
    @Override
    public List<?> getDataObjects()
    {
        return dataObjects;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#getName()
     */
    @Override
    public String getName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#getQueryDefinition()
     */
    @Override
    public List<QueryResultsContainerIFace> getQueryDefinition()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#getResults()
     */
    @Override
    public List<QueryResultsDataObj> getResults()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#isInError()
     */
    @Override
    public boolean isInError()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#isCancelled()
     */
    @Override
    public boolean isCancelled()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#cancel()
     */
    @Override
    public void cancel()
    {
        //ignore
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#getTableIds()
     */
    @Override
    public List<Integer> getTableIds()
    {
        return null;
    }

}
