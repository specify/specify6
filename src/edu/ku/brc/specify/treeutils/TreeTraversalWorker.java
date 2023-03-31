/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import edu.ku.brc.specify.Specify;
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
	protected static int               writesPerFlush = Specify.HIBERNATE_BATCH_SIZE;
	
    protected QueryIFace               childrenQuery     = null;
    protected QueryIFace               ancestorQuery     = null;

    long                               progressChunk;
    int                                progressIncr;
    int                                progressPerCent;
    int								   writeCount;
    protected final D                  treeDef;

    protected DataProviderSessionIFace traversalSession = null;
    protected boolean externalSession = false;

    /**
     * @param treeDef
     */
    public TreeTraversalWorker(final D treeDef)
    {
        super();
        this.treeDef = treeDef;
    }

    public TreeTraversalWorker(final D treeDef, final DataProviderSessionIFace traversalSession)
    {
        super();
        this.treeDef = treeDef;
        this.traversalSession = traversalSession;
        this.externalSession = true;
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
    	
    	/*Apparently commits are NOT necessary after flush/clear.*/
    	/* but they require more than 1G of memory for trees with 95k+ nodes. */
    	if (!externalSession) {
    		traversalSession.commit();
    		traversalSession.beginTransaction(); 
    	}
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
     *
     * @return
     */
    protected String getNodeDefItemFldName() { return treeDef.getNodeClass().getSimpleName() + "TreeDefItemId"; }

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
    protected T getTreeRoot() {
        return getTreeRoot(null);
    }

    /**
     *
     * @param sessionArg
     * @return
     */
    protected T getTreeRoot(final DataProviderSessionIFace sessionArg) {
        DataProviderSessionIFace session = sessionArg == null
                ? DataProviderFactory.getInstance().createSession()
                : sessionArg;
        try {
            return session.getData(treeDef.getNodeClass(), "definitionItem", treeDef.getDefItemByRank(0), DataProviderSessionIFace.CompareType.Equals);
        } finally {
            if (sessionArg == null) {
                session.close();
            }
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
