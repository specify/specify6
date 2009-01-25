/*
 * Copyright (C) 2008 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
/**
 * 
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
 * @code_status Alpha
 * 
 * Base class for background tree structure tasks.
 * 
 */
public abstract class TreeTraversalWorker<T extends Treeable<T, D, I>, D extends TreeDefIface<T, D, I>, I extends TreeDefItemIface<T, D, I>>
        extends SwingWorker<Boolean, Object>
{
    
    protected QueryIFace               childrenQuery     = null;
    protected QueryIFace               ancestorQuery     = null;

    long                               progressChunk;
    int                                progressIncr;
    int                                progressPerCent;
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
                edu.ku.brc.af.core.UsageTracker.incrHQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TreeTraversalWorker.class, ex);
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
