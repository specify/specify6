/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.treeutils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.CustomQueryListener;
import edu.ku.brc.dbsupport.JPAQuery;
import edu.ku.brc.dbsupport.SQLExecutionListener;
import edu.ku.brc.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.specify.ui.treetables.TreeNode;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;

/**
 * Can use one or two query string to calculate the Collection objects for a single node in the tree or for all of the children.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Jan 23, 2008
 *
 */
public class ChildNodeCounter implements SQLExecutionListener, CustomQueryListener
{
    protected TreeNode node;
    protected int      step    = 0;
    protected int      valStep = 2;
    protected boolean  isHQL   = false;
    
    protected String nodeNumQuery;
    protected String countQuery;
    protected int    slotIndex;
    protected TreeTableViewer<?, ?, ?> viewer;
    
    /**
     * Constructor.
     * @param topList the top window JLIst
     * @param bottomList the bottom Window JLIst
     * @param slotIndex which slot in the node to put the number
     * @param node the node to to found out the ocunt for
     * @param nodeNumQuery the first query
     * @param countQuery the second query
     */
    public ChildNodeCounter(final TreeTableViewer<?, ?, ?> viewer,
                            final int      slotIndex,
                            final TreeNode node, 
                            final String   nodeNumQuery, 
                            final String   countQuery,
                            final boolean  isHQL)
    {
        this.viewer       = viewer;
        this.slotIndex    = slotIndex;
        this.node         = node;
        this.countQuery   = countQuery;
        this.nodeNumQuery = nodeNumQuery;
        this.isHQL        = isHQL;
        
        setCalcCount(true);
        
        doQuery(getQuery(node.getId()));
         
        valStep = countQuery == null ? 1 : 2;
    }
    
    protected void setCalcCount(final boolean calc)
    {
        if (slotIndex == 1)
        {
            node.setCalcCount(calc);
        } else
        {
            node.setCalcCount2(calc);
        }
    }
    
    protected void setHasCalcCount(final boolean hasCalc)
    {
        if (slotIndex == 1)
        {
            node.setHasCalcCount(hasCalc);
        } else
        {
            node.setHasCalcCount2(hasCalc);
        }
    }
    /**
     * Create the query string from the data.
     * @param data the data from the previous query.
     * @return
     */
    @SuppressWarnings("unchecked")
    protected String getQuery(final Object data)
    {
        switch (step)
        {
            case 0:
                return String.format(nodeNumQuery, (Integer)data);
                
            case 1:
            {
                List<Object> row = ((List<List<Object>>)data).get(0);
                
                //int treeDefId  = (Integer)row.get(0);
                if (row.size() == 1)
                {
                    int x = 0;
                    x++;
                }
                int topNodeNum = (Integer)row.get(1);
                int botNodenum = (Integer)row.get(2);
                
                if (topNodeNum < botNodenum)
                {
                    return String.format(countQuery, topNodeNum, botNodenum);
                }
            }
        }
        return null;
    }
    
    /**
     * Execute query.
     * @param sql the SQL query
     */
    protected void doQuery(final String sql)
    {
        if (sql != null)
        {
            step++;
            
            if (isHQL)
            {
                JPAQuery jpaQuery = new JPAQuery(sql, this);
                jpaQuery.setUnique(true);
                jpaQuery.start();
                
            } else
            {
                SQLExecutionProcessor sqlExec = new SQLExecutionProcessor(this, sql);
                sqlExec.start();
            }
            
        } else
        {
            cleanup();
        }
    }

    /**
     * Sets the count value into the appropriate slot in the node.
     * @param count the count
     */
    protected void setCount(final int count)
    {
        if (slotIndex == 1)
        {
            node.setAssociatedRecordCount(count);
        } else
        {
            node.setAssociatedRecordCount2(count);
        }
        
        if (viewer != null)
        {
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    viewer.repaintLists();
                }
            });
        }
    }
    
    /**
     * Cleanup references
     */
    protected void cleanup()
    {
        //this.topList      = null;
        //this.bottomList   = null;
        this.node         = null;
        this.countQuery   = null;
        this.nodeNumQuery = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
     */
    public void exectionDone(final SQLExecutionProcessor process, final ResultSet resultSet)
    {
        setCalcCount(false);
        setHasCalcCount(true);

        if (step == valStep)
        {
            try
            {
                if (resultSet.next())
                {
                    int count = resultSet.getInt(1);
                    setCount(count);
                    cleanup();
                    return;
                }
                
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
            setCount(0);
            cleanup();
            
        } else
        {
            List<List<Object>> dataRows = new ArrayList<List<Object>>();
            try
            {
                ResultSetMetaData colInfo = resultSet.getMetaData();
                while (resultSet.next())
                {
                    List<Object> row = new ArrayList<Object>();
                    for (int i=1;i<=colInfo.getColumnCount();i++)
                    {
                        row.add(resultSet.getObject(i));
                    }
                    dataRows.add(row);
                }
                doQuery(getQuery(dataRows));
                
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SQLExecutionListener#executionError(edu.ku.brc.dbsupport.SQLExecutionProcessor, java.lang.Exception)
     */
    public void executionError(final SQLExecutionProcessor process, final Exception ex)
    {
        setCalcCount(false);
        setHasCalcCount(true); // XXX maybe set this to false
        
        setCount(0);
        cleanup();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#exectionDone(edu.ku.brc.dbsupport.CustomQueryIFace)
     */
    public void exectionDone(CustomQueryIFace customQuery)
    {
       List<?> list = customQuery.getDataObjects();
       if (step == valStep)
       {
           int count = (Integer)list.get(0);
           setCount(count);
           cleanup();
           return;
       }
       
       List<List<Object>> dataRows = new ArrayList<List<Object>>();
       for (Object row : list)
       {
           List<Object> rowArray = new ArrayList<Object>();
           dataRows.add(rowArray);
           
           if (row instanceof Collection<?>)
           {
               for (Object obj : (Collection<?>)row)
               {
                   rowArray.add(obj);
               }
           } else if (row instanceof Object[])
           {
               for (Object obj : (Object[])row)
               {
                   rowArray.add(obj);
               }
           } else
           {
               rowArray.add(row);
           }
       }
       doQuery(getQuery(dataRows));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#executionError(edu.ku.brc.dbsupport.CustomQueryIFace)
     */
    public void executionError(CustomQueryIFace customQuery)
    {
        // TODO Auto-generated method stub
        
    }
    
    
}
