/* Copyright (C) 2015, University of Kansas Center for Research
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

import java.util.List;

import javax.swing.SwingWorker;

import org.hibernate.HibernateException;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 * @author timbo
 * 
 * @code_status Alphae
 * 
 * Base class for background tree structure tasks.
 * 
 */
public abstract class TreeTraversalWorker<T extends Treeable<T, D, I>, D extends TreeDefIface<T, D, I>, I extends TreeDefItemIface<T, D, I>>
        extends SwingWorker<Boolean, Object>
{
	
	/**
	 * The number of db operations that can occur before the session should be flushed.
	 */
	protected static int               writesPerFlush = 2000;
	
    protected QueryIFace               childrenQuery     = null;
    protected QueryIFace               ancestorQuery     = null;

    long                               progressChunk;
    int                                progressIncr;
    int                                progressPerCent;
    int								   writeCount;
    protected final D                  treeDef;

    protected DataProviderSessionIFace traversalSession = null;

    /**
     * @param treeDef
     */
    public TreeTraversalWorker(final D treeDef)
    {
        super();
        this.treeDef = treeDef;
    }

    /**
     * intialize members dealing with progress reporting.
     */
    protected void initProgress()
    {
        int total = BasicSQLUtils.getNumRecords("select count(" + getNodeKeyFldName() + ") from "
                + getNodeTblName() + " where " + getNodeTreeFldName() + "="
                + treeDef.getTreeDefId());
        progressChunk = Math.round(Math.floor(total / 100.0));
        if (progressChunk < 1)
        {
            // probably good enough
            progressChunk = 1;
        }
        progressIncr = 0;
        progressPerCent = 0;
    }

    /**
     * Initializes members required for session flushing within transactions.
     * Workers that use transactions should call this method.
     */
    protected void initCacheInfo()
    {
    	writeCount = 0;
    }
    
    /**
     *  increments  progress.
     */
    protected void incrementProgress()
    {
        if (++progressIncr == progressChunk)
        {
            updateProgress();
            progressIncr = 0;
        }
    }

    /**
     * Updates members required for session flushing within transactions.
     * Workers that use transactions should call this method after each write - or periodically.
     */
    protected void checkCache() throws Exception
    {
        if (++writeCount == writesPerFlush)
        {
            clearCache();
            writeCount = 0;
        }
    }
    
    /**
      * clear the session cache.
      * 
      * flush too, just in case.
     */
    protected void clearCache() throws Exception
    {
        traversalSession.flush();
    	traversalSession.clear();
		/* Every time updateNodeQuery.executeUpdate() is executed, an entry
		 * is added to the hibernate session.actionQueue.executions data structure.
		 * For large trees, out of memory errors occur.
		 * 
		 * Attempts to combine multiple node updates into one updateNodeQuery.executeUpdate() failed.
		 *
		 * Even when a transaction was not opened, the executions structure was filled (besides, with hibernate,
		 * session updates MUST be in a transaction or they do not actually get written to the db).
		 * 
		 * So, periodic commits are required. This means that an entire tree update cannot be rolled back, but
		 * in theory, the tree was not in correct shape before the rebuild began, so this is not so serious an issue.
		 */
		traversalSession.commit();
		traversalSession.beginTransaction();
    }
    
    /**
     * updates progress and notifies progress listeners.  
     */
    protected void updateProgress()
    {
        progressPerCent++;
        if (progressPerCent > 100)
        {
            progressPerCent = 90; // heh heh
        }
        setProgress(progressPerCent);
    }

    /**
     * @return name of key field for node table.
     */
    protected String getNodeKeyFldName()
    {
        return treeDef.getNodeClass().getSimpleName() + "Id";
    }

    /**
     * @return node table name.
     */
    protected String getNodeTblName()
    {
        return treeDef.getNodeClass().getSimpleName().toLowerCase();
    }

    /**
     * @return TreeDef field name for nodes.
     */
    protected String getNodeTreeFldName()
    {
        return treeDef.getNodeClass().getSimpleName() + "TreeDefId";
    }

    /**
     * @return node parent field name.
     */
    protected String getNodeParentFldName()
    {
        return "parentId";
    }

    /**
     * @return root node of tree. (not used)
     */
    protected T getTreeRoot()
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        boolean attached = false;
        try
        {
            I rootDefItem = treeDef.getDefItemByRank(0);
            try
            {
                session.attach(rootDefItem);
                attached = true;
            }
            catch (HibernateException ex)
            {
                //continue
            }
            T result = rootDefItem.getTreeEntries().iterator().next();
            if (attached)
            {
                session.evict(rootDefItem);
            }
            return result;
        }
        finally
        {
            session.close();
        }
    }

    /**
     * @param parentId
     * @return list of keys of children of parentId.
     */
    protected List<?> getChildIds(int parentId)
    {
        childrenQuery.setParameter("parentArg", parentId);
        return childrenQuery.list();
    }
    
    /**
     * @return name for progress bar.
     */
    public String getProgressName()
    {
        return treeDef.getName();
    }
    
    /**
     * Creates query used to retrieve children.
     */
    protected void buildChildrenQuery()
    {
        String childrenSQL = "select " + getNodeKeyFldName() + " from " + getNodeTblName()
                + " where " + getNodeParentFldName() + " =:parentArg  order by name";
        childrenQuery = traversalSession.createQuery(childrenSQL, true);
    }

}
