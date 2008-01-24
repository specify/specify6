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
    protected transient boolean nodeNumbersAreUpToDate = true;
    protected transient boolean doNodeNumberUpdates = true;
    protected transient boolean uploadInProgress = false;
    protected transient DataProviderSessionIFace nodeUpdateSession = null;
    protected transient DataModelObjBase nodeUpdateRoot = null;
    protected N treeRoot;
    
    
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#updateAllNodes()
     */
    public void updateAllNodes(final DataModelObjBase rootObj) throws Exception
    {
        nodeUpdateRoot = rootObj;
        N root = (N)rootObj;
        nodeUpdateSession = DataProviderFactory.getInstance().createSession();
        try
        {
            //But there could be thousands and thousands of records affected within the transaction.
            //SQLServer accessed via ADO would blow up for large transactions  ???
            nodeUpdateSession.beginTransaction();
            writeNodeNumber(root.getTreeId(), 1);
            int highestChild = updateAllNodes2(root.getTreeId(), 1);
            writeHighestChildNodeNumber(root.getTreeId(), highestChild);
            nodeUpdateSession.commit();
        }
        catch (Exception ex)
        {
            nodeUpdateSession.rollback();
        }
        finally
        {
            nodeUpdateSession.close();
            nodeUpdateSession = null;
        }
    }

//    protected Object getNodeNumber(final int nodeId)
//    {
//        String hql = "select nodeNumber from " + nodeUpdateRoot.getDataClass().getSimpleName()
//            + " where " + nodeUpdateRoot.getDataClass().getSimpleName() + "Id=" + String.valueOf(nodeId);
//        QueryIFace q = this.nodeUpdateSession.createQuery(hql);
//        return q.uniqueResult();
//    }
    
    protected List<?> getChildren(final int nodeId)
    {
        String hql = "select n." + UploadTable.deCapitalize(nodeUpdateRoot.getDataClass().getSimpleName()) + "Id from " 
            + nodeUpdateRoot.getDataClass().getSimpleName() + " n where parentID=" + String.valueOf(nodeId);
        QueryIFace q = this.nodeUpdateSession.createQuery(hql);
        return q.list();        
        
    }
    
    protected void writeNodeNumber(final Object childId, final Integer nodeNumber)
    {
        String hql = "update " + nodeUpdateRoot.getDataClass().getSimpleName() 
            + " set nodeNumber=" + nodeNumber.toString() + " where " + UploadTable.deCapitalize(nodeUpdateRoot.getDataClass().getSimpleName()) + "Id="
            + childId.toString();
        QueryIFace q = this.nodeUpdateSession.createQuery(hql);
        q.executeUpdate();
    }

    protected void writeHighestChildNodeNumber(final Object childId, final Integer nodeNumber)
    {
        String hql = "update " + nodeUpdateRoot.getDataClass().getSimpleName() 
            + " set highestChildNodeNumber=" + nodeNumber.toString() + " where " 
            + UploadTable.deCapitalize(nodeUpdateRoot.getDataClass().getSimpleName()) + "ID="
            + childId.toString();
        QueryIFace q = this.nodeUpdateSession.createQuery(hql);
        q.executeUpdate();
    }

    
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
//        
//        
//        
//        
//        
//        session.attach(root);
//        Set<N> children = root.getChildren();
//        session.evict(root);
//        for (N child : children)// guess its ok if children are not consistently ordered?
//        {
//            child.setNodeNumber(nodeNumber + 1);
//            child.setHighestChildNodeNumber(updateAllNodes2(child, session));
//            nodeNumber = child.getHighestChildNodeNumber();
//            session.saveOrUpdate(child);
//        }
        return nodeNumber;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#getDoNodeUpdates()
     */
    public boolean getDoNodeNumberUpdates()
    {
        return this.doNodeNumberUpdates;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#setDoNodeUpdates(boolean)
     */
    public void setDoNodeNumberUpdates(final boolean arg)
    {
        this.doNodeNumberUpdates = arg;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#isUploadInProgress()
     */
    public boolean isUploadInProgress()
    {
        return this.uploadInProgress;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#setUploadInProgress(boolean)
     */
    public void setUploadInProgress(final boolean arg)
    {
        this.uploadInProgress = arg;
    }

    
}
