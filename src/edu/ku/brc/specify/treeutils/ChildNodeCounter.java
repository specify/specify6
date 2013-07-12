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
package edu.ku.brc.specify.treeutils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

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
    protected static final Logger		log		= Logger.getLogger(ChildNodeCounter.class);

	protected TreeNode					node;
	protected int						step	= 0;
	protected int						valStep	= 2;
	protected boolean					isHQL	= false;

	protected String					nodeNumQuery;
	protected String					countQuery;
	protected int						slotIndex;
	protected TreeTableViewer<?, ?, ?>	viewer;
    
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
                if (((List<List<Object>>)data).size() > 0)
                {
                	List<Object> row = ((List<List<Object>>)data).get(0);
                	if (row != null && row.size() > 2)
                	{
                		if (row.get(1) == null || row.get(2) == null)
                		{
                			//This should never happen if trees have been built correctly
                			//... unless we are forced to allow incremental node updates to be turned off
                			//for performance reasons.
                			log.warn("null node number: skipping count");
                			return null;
                		}
                    
                		//int treeDefId  = (Integer)row.get(0);
                		int topNodeNum = (Integer)row.get(1);
                		int botNodenum = (Integer)row.get(2);
                    
                		if (topNodeNum < botNodenum)
                		{
                			return String.format(countQuery, topNodeNum, botNodenum);
                		}
                	}
                }
                else
                {
                	log.warn("object list is empty: skipping count");
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
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ChildNodeCounter.class, ex);
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
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ChildNodeCounter.class, ex);
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
