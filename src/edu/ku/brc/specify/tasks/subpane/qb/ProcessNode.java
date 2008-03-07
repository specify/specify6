/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.Vector;


/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * Used during qb sql/hql generation.
 *
 */
public class ProcessNode
{
    protected Vector<ProcessNode> kids = new Vector<ProcessNode>();
    protected BaseQRI             qri;

    public ProcessNode(BaseQRI qri)
    {
        this.qri = qri;
    }

    public Vector<ProcessNode> getKids()
    {
        return kids;
    }

    public BaseQRI getQri()
    {
        return qri;
    }

    public boolean contains(BaseQRI qriArg)
    {
        for (ProcessNode pn : kids)
        {
            if (pn.getQri().equals(qriArg)) { return true; }
        }
        return false;
    }
}
