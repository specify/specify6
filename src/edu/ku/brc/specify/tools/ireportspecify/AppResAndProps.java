/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.ireportspecify;

import edu.ku.brc.af.core.AppResourceIFace;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Cheap class for tracking AppResource and "other stuff" associated with SpReports
 */
public class AppResAndProps
{
    private final AppResourceIFace appRes;
    private final Object repeats;
    /**
     * @param appRes
     * @param repeats
     */
    public AppResAndProps(AppResourceIFace appRes, Object repeats)
    {
        super();
        this.appRes = appRes;
        this.repeats = repeats;
    }
    /**
     * @return the appRes
     */
    public AppResourceIFace getAppRes()
    {
        return appRes;
    }
    /**
     * @return the repeats
     */
    public Object getRepeats()
    {
        return repeats;
    }
}
