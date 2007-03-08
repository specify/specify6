/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
/**
 * 
 */
package edu.ku.brc.specify.tools.IReportSpecify;

import it.businesslogic.ireport.Report;

import edu.ku.brc.af.core.AppResourceIFace;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Extends iReport Report with association to a specify appResource.
 */
public class ReportSpecify extends Report
{
    //i am assuming the name of a resource is sufficient as a unique id for a single login??
    //Probably could just store ref to AppResourceIFace??
    private String appResourceID;

    public ReportSpecify(final AppResourceIFace res)
    {
        super();
        setAppResourceID(res.getName());
    }

    public void setAppResourceID(final String ID)
    {
        appResourceID = ID;
    }

    public String getAppResourceID()
    {
        return appResourceID;
    }

    public boolean resourceMatch(final AppResourceIFace res)
    {
        return res.getName() == getAppResourceID();
    }
}
