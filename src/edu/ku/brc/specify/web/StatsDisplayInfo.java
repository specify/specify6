/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.web;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 24, 2007
 *
 */
public class StatsDisplayInfo
{
    protected String url;
    protected String title;
    
    /**
     * @param url
     * @param title
     */
    public StatsDisplayInfo(String url, String title)
    {
        super();
        this.url = url;
        this.title = title;
    }
    /**
     * @return the url
     */
    public String getUrl()
    {
        return url;
    }
    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }
}
