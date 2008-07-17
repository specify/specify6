/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.util.LinkedList;
import java.util.List;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public abstract class BaseTreeDef<N extends Treeable<N,D,I>,
                                  D extends TreeDefIface<N,D,I>,
                                  I extends TreeDefItemIface<N,D,I>> extends DataModelObjBase 
                                  implements TreeDefIface<N,D,I>
{
    protected static transient boolean nodeNumbersAreUpToDate = true;
    protected static transient boolean doNodeNumberUpdates = true;
    protected static transient boolean uploadInProgress = false;
    protected transient DataProviderSessionIFace nodeUpdateSession = null;
    protected transient QueryIFace nodeQ = null;
    protected transient QueryIFace highestNodeQ = null;
    protected transient QueryIFace childrenQ = null;
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        nodeNumbersAreUpToDate = true;
        doNodeNumberUpdates = true;
        uploadInProgress = false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#nodeNumbersAreCurrent()
     */
    public boolean getNodeNumbersAreUpToDate()
    {
        return nodeNumbersAreUpToDate;
    }
    
    /**
     * @param rootObj
     * 
     * Builds the queries used during the node number update process.
     */
    protected void buildQueries(final DataModelObjBase rootObj)
    {
        String hql = "select n." + UploadTable.deCapitalize(rootObj.getDataClass().getSimpleName()) + "Id from " 
            + rootObj.getDataClass().getSimpleName() + " n where parentID=:parent";
        childrenQ = this.nodeUpdateSession.createQuery(hql);

        hql = "update " + rootObj.getDataClass().getSimpleName() 
            + " set nodeNumber=:node where " + UploadTable.deCapitalize(rootObj.getDataClass().getSimpleName()) + "Id=:id";
        nodeQ = this.nodeUpdateSession.createQuery(hql);
        
        hql = "update " + rootObj.getDataClass().getSimpleName() 
            + " set highestChildNodeNumber=:node where " 
            + UploadTable.deCapitalize(rootObj.getDataClass().getSimpleName()) + "ID=:id";
        highestNodeQ = this.nodeUpdateSession.createQuery(hql);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#updateAllNodes()
     */
    @SuppressWarnings("unchecked")
    public void updateAllNodes(final DataModelObjBase rootObj) throws Exception
    {
        N root = (N)rootObj;
        nodeUpdateSession = DataProviderFactory.getInstance().createSession();
        try
        {
            buildQueries(rootObj);
            //But there could be thousands and thousands of records affected within the transaction.
            //SQLServer accessed via ADO would blow up for large transactions  ???
            nodeUpdateSession.beginTransaction();
            
            writeNodeNumber(root.getTreeId(), 1);
            int highestChild = updateAllNodes2(root.getTreeId(), 1);
            writeHighestChildNodeNumber(root.getTreeId(), highestChild);
            
            nodeUpdateSession.commit();
            nodeNumbersAreUpToDate = true;
        }
        catch (Exception ex)
        {
            nodeUpdateSession.rollback();
        }
        finally
        {
            nodeUpdateSession.close();
            childrenQ = null;
            nodeQ = null;
            highestNodeQ = null;
            nodeUpdateSession = null;
        }
    }

    /**
     * @param rootId
     * @param rootNodeNumber
     * @return the highestChildNodeNumber for rootId
     * @throws Exception
     * 
     * Recursively walks the tree and numbers nodes.
     */
    protected Integer updateAllNodes2(final Integer rootId, final int rootNodeNumber) throws Exception
    {
        List<?> children = getChildren(rootId);
        int nodeNumber = rootNodeNumber;
        for (Object childId : children)
        {
            writeNodeNumber(childId, nodeNumber+1);
            nodeNumber = updateAllNodes2((Integer)childId, nodeNumber+1);
            writeHighestChildNodeNumber(childId, nodeNumber);
        }
        return nodeNumber;
    }

    /**
     * @param nodeId
     * @return children of parent with nodeId
     */
    protected List<?> getChildren(final int nodeId)
    {
        childrenQ.setParameter("parent", nodeId);
        return childrenQ.list();
    }
    
    /**
     * @param childId
     * @param nodeNumber
     * 
     * Sets the nodeNumber for the item with key childId.
     */
    protected void writeNodeNumber(final Object childId, final Integer nodeNumber)
    {
        nodeQ.setParameter("node", nodeNumber);
        nodeQ.setParameter("id", childId);
        nodeQ.executeUpdate();
    }

    /**
     * @param childId
     * @param nodeNumber
     * 
     * Sets the highestChildNodeNumber for the item with key childId.
     */
    protected void writeHighestChildNodeNumber(final Object childId, final Integer nodeNumber)
    {
        highestNodeQ.setParameter("node", nodeNumber);
        highestNodeQ.setParameter("id", childId);
        highestNodeQ.executeUpdate();
    }

        
    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#getDoNodeUpdates()
     */
    public boolean getDoNodeNumberUpdates()
    {
        return doNodeNumberUpdates;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#setDoNodeUpdates(boolean)
     */
    public void setDoNodeNumberUpdates(final boolean arg)
    {
        doNodeNumberUpdates = arg;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#isUploadInProgress()
     */
    public boolean isUploadInProgress()
    {
        return uploadInProgress;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#setUploadInProgress(boolean)
     */
    public void setUploadInProgress(final boolean arg)
    {
        uploadInProgress = arg;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#setNodeNumbersAreUpToDate(boolean)
     */
    //@Override
    public void setNodeNumbersAreUpToDate(final boolean arg)
    {
        nodeNumbersAreUpToDate = arg;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.Nameable#getName()
     */
    @Override
    public String getName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.Nameable#setName(java.lang.String)
     */
    @Override
    public void setName(String name)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    public Class<?> getDataClass()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    public Integer getId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTableId()
     */
    @Override
    public int getTableId()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#getStandardLevels()
     */
    @Override
    public List<TreeDefItemStandardEntry> getStandardLevels()
    {
        return new LinkedList<TreeDefItemStandardEntry>();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#getRankIncrement()
     */
    @Override
    public int getRankIncrement()
    {
        return 1000; //plenty of space for inserts?
    }

    
}
