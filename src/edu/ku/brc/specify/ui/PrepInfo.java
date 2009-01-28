/*
 * Copyright (C) 2009  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.ui;

/**
 * Used to gather Preparation information for Loans
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 28, 2009
 *
 */
public class PrepInfo
{
    protected Integer prepId;
    protected int     type;
    protected int     qtyPrep;
    protected int     qtyLoaned;
    protected int     qtyResolved;
    
    /**
     * @param prepId
     * @param qtyLoaned
     * @param qtyResolved
     * @param qtyAvail
     */
    public PrepInfo(Integer prepId, 
                    int type, 
                    int qtyPrep, 
                    int qtyLoaned, 
                    int qtyResolved)
    {
        super();
        this.prepId      = prepId;
        this.type        = type;
        this.qtyPrep     = qtyPrep;
        this.qtyLoaned   = qtyLoaned;
        this.qtyResolved = qtyResolved;
    }

    public void add(final int qtyLoanedArg, 
                    final int qtyResolvedArg)
    {
        this.qtyLoaned += qtyLoanedArg;
        this.qtyResolved += qtyResolvedArg;
    }
    
    /**
     * @return
     */
    public int getAvailable()
    {
        return qtyPrep - qtyLoaned + qtyResolved;
    }
    
    /**
     * @return the prepId
     */
    public Integer getPrepId()
    {
        return prepId;
    }

    /**
     * @return the type
     */
    public int getType()
    {
        return type;
    }

    /**
     * @return the qtyPrep
     */
    public int getQtyPrep()
    {
        return qtyPrep;
    }

    /**
     * @return the qtyLoaned
     */
    public int getQtyLoaned()
    {
        return qtyLoaned;
    }

    /**
     * @return the qtyResolved
     */
    public int getQtyResolved()
    {
        return qtyResolved;
    }

}

