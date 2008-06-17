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
import java.util.Collections;
import java.util.Comparator;
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
    protected final List<Object[]> dataObjects; 
    protected int maxFldIdx;
    
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
     * @param uploadTable
     * @param uploadData
     * 
     * Populates dataObjects with data directly from the dataSet.
     */
    public UploadResultsQuery(final UploadTable uploadTable, final UploadData uploadData)
    {
        dataObjects = new ArrayList<Object[]>(uploadTable.getUploadedRecs().size());
        Vector<Vector<edu.ku.brc.util.Pair<Integer, Integer>>> indexes = setupFldIdxInfo(uploadTable);
        if (indexes != null)
        {
            for (UploadedRecordInfo rec : uploadTable.getUploadedRecs())
            {
                Object[] rowData = new Object[maxFldIdx + 2];
                rowData[0] = rec.getKey();
                Vector<edu.ku.brc.util.Pair<Integer, Integer>> index = indexes.get(rec.getSeq());
                for (int i = 0; i < index.size(); i++)
                {
                    rowData[getTblCol(index.get(i)) + 1] = uploadData.getWbRow(rec.getWbRow())
                            .getData(getWbCol(index.get(i)));
                }
                dataObjects.add(rowData);
            }
        }
    }
    
    /**
     * @param uploadTable
     * 
     * @return a structure providing the dataSet column index for each column displayed and for each
     * sequence/occurrence (lastName1, lastName2)
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

        //this puts them order for processing in next step 
        Collections.sort(sortedFlds, new Comparator<UploadField>()
        {

            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(UploadField o1, UploadField o2)
            {
                int result = o1.getSequence() < o2.getSequence() ? -1 : (o1.getSequence() == o2.getSequence() ? 0 : 1);
                if (result != 0)
                {
                    return result;
                }
                
                //else
                result = o1.getIndex() < o2.getIndex() ? -1 : (o1.getIndex() == o2.getIndex() ? 0 : 1); 
                if (result != 0)
                {
                    return result;
                }
                
                //else
                return o1.getField().compareTo(o2.getField());
            }
        });
        
        //get the maximum 'sequence' for the field. ie: the maximum number of occurences of records per row (lastName1, lastName2...)
        int maxSequence = sortedFlds.get(sortedFlds.size()-1).getSequence() + 1;
        //else
        
        Vector<Vector<edu.ku.brc.util.Pair<Integer, Integer>>> result = new Vector<Vector<edu.ku.brc.util.Pair<Integer, Integer>>>(maxSequence);
        for (int s = 0; s < maxSequence; s++)
        {
            result.add(new Vector<edu.ku.brc.util.Pair<Integer, Integer>>());
        }
        int tblCol = -1;
        int seq = -1;
        for (UploadField fld : sortedFlds)
        {
            if (fld.getSequence() != seq)
            {
                seq = fld.getSequence();
                tblCol = 0;
            }
            (result.get(fld.getSequence())).add(new edu.ku.brc.util.Pair<Integer, Integer>(tblCol, fld.getIndex()));
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
        if (dataObjects.size() > 0)
        {
            cql.exectionDone(this);
        }
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

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#isInError()
     */
    //@Override
    public boolean isInError()
    {
        return false;
    }

    
}
