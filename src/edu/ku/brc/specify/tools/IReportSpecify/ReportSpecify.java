/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
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
 *Extends iReport Report with association to a specify spAppResource.
 */
public class ReportSpecify extends Report
{
    //i am assuming the name of a resource is sufficient as a unique id for a single login??
    //Probably could just store ref to AppResourceIFace??
    private String spAppResourceId;

    public ReportSpecify(final AppResourceIFace res)
    {
        super();
        if (res != null)
        {
            setAppResourceId(res.getName());
        }
        else
        {
            setAppResourceId(null);
        }
    }

    public void setAppResourceId(final String id)
    {
        spAppResourceId = id;
    }

    public String getSpAppResourceId()
    {
        return spAppResourceId;
    }

    public boolean resourceMatch(final AppResourceIFace res)
    {
        return res.getName().equals(getSpAppResourceId());
    }
}
