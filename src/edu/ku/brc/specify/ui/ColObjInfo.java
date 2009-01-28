/*
 * Copyright (C) 2009  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.ui;

import java.util.Hashtable;

/**
 * Used to gather CollectionObject Information for Loans.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 28, 2009
 *
 */
public class ColObjInfo 
{
    protected String  taxonName;
    protected String  catNo;
    protected Integer colObjId;
    protected Hashtable<Integer, PrepInfo> preps = null;
    
    /**
     * @param colObjId
     * @param catNo
     * @param taxonName
     */
    public ColObjInfo(final Integer colObjId,
                      final String catNo,
                      final String taxonName)
    {
        super();
        this.colObjId  = colObjId;
        this.catNo     = catNo;
        this.taxonName = taxonName;
    }
    
    /**
     * @param pi
     */
    public void add(final PrepInfo pi)
    {
        if (preps == null)
        {
            preps = new Hashtable<Integer, PrepInfo>();
        }
        preps.put(pi.getPrepId(), pi);
    }
    
    /**
     * @param prepId
     * @return
     */
    public PrepInfo get(final Integer prepId)
    {
        return preps != null ? preps.get(prepId) : null;
    }
    
     
    /**
     * @return the catNo
     */
    public String getCatNo()
    {
        return catNo;
    }

    /**
     * @param catNo the catNo to set
     */
    public void setCatNo(String catNo)
    {
        this.catNo = catNo;
    }

    /**
     * @return the preps
     */
    public Hashtable<Integer, PrepInfo> getPreps()
    {
        return preps;
    }

    /**
     * @return the taxonName
     */
    public String getTaxonName()
    {
        return taxonName;
    }

    /**
     * @return the colObjId
     */
    public Integer getColObjId()
    {
        return colObjId;
    }
}
