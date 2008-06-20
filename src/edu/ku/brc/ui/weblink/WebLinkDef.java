/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.ui.weblink;

import java.util.Vector;

import com.thoughtworks.xstream.XStream;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 13, 2008
 *
 */
public class WebLinkDef
{
    protected String name;
    protected String desc;
    protected String baseURLStr;
    protected Vector<WebLinkDefArg> args;
    protected Vector<WebLinkUsedBy> usedByList;
    
    /**
     * 
     */
    public WebLinkDef()
    {
        super();
        
        args       = new Vector<WebLinkDefArg>();
        usedByList = new Vector<WebLinkUsedBy>();
    }
    
    /**
     * 
     */
    public WebLinkDef(final String name)
    {
        super();
        this.name = name;
        args       = new Vector<WebLinkDefArg>();
        usedByList = new Vector<WebLinkUsedBy>();
    }
    
    
    /**
     * @param name
     * @param desc
     * @param baseURLStr
     */
    public WebLinkDef(String name, String desc, String baseURLStr)
    {
        this.name = name;
        this.desc = desc;
        this.baseURLStr = baseURLStr;
    }
    
    /**
     * @return whether there is at least one arg with a prompt.
     */
    public int getPromptCount()
    {
        int cnt = 0;
        for (WebLinkDefArg arg : args)
        {
            if (arg.isPrompt())
            {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }


    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }


    /**
     * @return the desc
     */
    public String getDesc()
    {
        return desc;
    }


    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc)
    {
        this.desc = desc;
    }


    /**
     * @return the baseURLStr
     */
    public String getBaseURLStr()
    {
        return baseURLStr;
    }


    /**
     * @param baseURLStr the baseURLStr to set
     */
    public void setBaseURLStr(String baseURLStr)
    {
        this.baseURLStr = baseURLStr;
    }


    /**
     * @return the args
     */
    public Vector<WebLinkDefArg> getArgs()
    {
        return args;
    }


    /**
     * @param args the args to set
     */
    public void setArgs(Vector<WebLinkDefArg> args)
    {
        this.args = args;
    }

    /**
     * @return the usedByList
     */
    public Vector<WebLinkUsedBy> getUsedByList()
    {
        return usedByList;
    }

    /**
     * @param usedByList the usedByList to set
     */
    public void setUsedByList(Vector<WebLinkUsedBy> usedByList)
    {
        this.usedByList = usedByList;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return name;
    }

    /**
     * @param xstream
     */
    public static void configXStream(final XStream xstream)
    {
        xstream.alias("weblinkdef", WebLinkDef.class); //$NON-NLS-1$
    }
}
