/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.utilapps;

import java.util.Vector;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 27, 2007
 *
 */
public class NodeInfo
{
    protected String   className     = "";
    protected boolean  skip          = false;
    protected boolean  processKids   = true;
    protected boolean  alwaysAKid    = true;
    protected boolean  processAnyRel = false;
    protected ERDTable okWhenParent  = null;
    protected boolean  okToDuplicate = false;
    
    protected Vector<ERDTable> kids = new Vector<ERDTable>();
    
    public NodeInfo()
    {
        super();
    }
    
    public NodeInfo(final boolean skip, 
                    final boolean processKids, 
                    final boolean alwaysAKid, 
                    final boolean processAnyRel, 
                    final ERDTable okWhenParent)
    {
        super();
        this.skip = skip;
        this.processKids = processKids;
        this.alwaysAKid = alwaysAKid;
        this.processAnyRel = processAnyRel;
        this.okWhenParent = okWhenParent;
    }
    
    
    /**
     * @param className the className to set
     */
    public void setClassName(String className)
    {
        this.className = className;
    }

    /**
     * @return the className
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * @return the skip
     */
    public boolean isSkip()
    {
        return skip;
    }
    /**
     * @return the processKids
     */
    public boolean isProcessKids()
    {
        return processKids;
    }
    /**
     * @return the alwaysAKid
     */
    public boolean isAlwaysAKid()
    {
        return alwaysAKid;
    }
    /**
     * @return the okWhenParent
     */
    public ERDTable getOkWhenParent()
    {
        return okWhenParent;
    }

    /**
     * @return the processAnyRel
     */
    public boolean isProcessAnyRel()
    {
        return processAnyRel;
    }
    
    public void addKid(ERDTable kid)
    {
        kids.add(kid);
    }

    /**
     * @return the kids
     */
    public Vector<ERDTable> getKids()
    {
        return kids;
    }

    /**
     * @return the okToDuplicate
     */
    public boolean isOkToDuplicate()
    {
        return okToDuplicate;
    }

    /**
     * @param okToDuplicate the okToDuplicate to set
     */
    public void setOkToDuplicate(boolean okToDuplicate)
    {
        this.okToDuplicate = okToDuplicate;
    }
    
}