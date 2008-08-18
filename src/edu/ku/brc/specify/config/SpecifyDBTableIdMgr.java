/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.config;

import java.sql.ResultSet;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.SQLExecutionListener;
import edu.ku.brc.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.Storage;
import edu.ku.brc.specify.datamodel.Taxon;

/**
 * This class implements the getTreeFieldNames method for the DBTableIdMgr class.
 * The getTreeFieldNames method returns all the names of the TreeDefs as if it were flatten into a single row
 * of columns. It skips the Root node's name.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Jul 19, 2008
 *
 */
public class SpecifyDBTableIdMgr extends DBTableIdMgr implements SQLExecutionListener
{
    private Vector<String> names = null;
    
    public SpecifyDBTableIdMgr()
    {
        super(true);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBTableIdMgr#getTreeFieldNames(edu.ku.brc.dbsupport.DBTableInfo)
     */
    @Override
    public List<String> getTreeFieldNames(DBTableInfo tableInfo)
    {
        Discipline discipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
        
        Integer id = null;
        if (tableInfo.getClassObj() == Taxon.class)
        {
            id = discipline.getTaxonTreeDef().getId();
            
        } else if (tableInfo.getClassObj() == Geography.class)
        {
            id = discipline.getGeographyTreeDef().getId();
            
        } else if (tableInfo.getClassObj() == GeologicTimePeriod.class)
        {
            id = discipline.getGeologicTimePeriodTreeDef().getId();
            
        } else if (tableInfo.getClassObj() == LithoStrat.class)
        {
            id = discipline.getLithoStratTreeDef().getId();
            
        } else if (tableInfo.getClassObj() == Storage.class)
        {
            id = discipline.getStorageTreeDef().getId();
        } else
        {
            return null;
        }
        
        String sql = String.format("select Name, RankId from TaxonTreeDefItem WHERE TaxonTreeDefID = %d ORDER BY RankID asc", id);
        SQLExecutionProcessor sqlProc = new SQLExecutionProcessor(this, sql);
        sqlProc.execute(); // do synchronously
        
        return names;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
     */
    public void exectionDone(SQLExecutionProcessor process, ResultSet resultSet)
    {
        names = new Vector<String>();
        try
        {
            // The first item should always be the root
            // but for extra insurance I am returning the RankId and checking.
            while (resultSet.next())
            {
                if (resultSet.getInt(2) > 0)
                {
                    names.add(resultSet.getString(1));
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SQLExecutionListener#executionError(edu.ku.brc.dbsupport.SQLExecutionProcessor, java.lang.Exception)
     */
    public void executionError(SQLExecutionProcessor process, Exception ex)
    {
        // no op
    }
}
