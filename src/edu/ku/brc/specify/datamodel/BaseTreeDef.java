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
    protected boolean nodeNumbersAreCurrent;
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        nodeNumbersAreCurrent = true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#getTreeRoot()
     */
    public N getTreeRoot()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#nodeNumbersAreCurrent()
     */
    public boolean nodeNumbersAreCurrent()
    {
        return nodeNumbersAreCurrent;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#updateAllNodes()
     */
    public void updateAllNodes()
    {
        throw new RuntimeException(getClass().getName() + ".updateAllNodes() is not implemented.");
    }

}
