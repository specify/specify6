/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.ui.weblink;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.security.AccessController;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableInfo;

/**
 * This is a singleton factory for managing WebLinks.
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Apr 13, 2008
 *
 */
public class WebLinkMgr
{
    public static final String factoryName = "edu.ku.brc.ui.weblink.WebLinkMgr";
    
    //private static final Logger log = Logger.getLogger(WebLinkMgr.class);
    
    protected static WebLinkMgr instance = null;
    
    protected Vector<WebLinkDef> webLinkDefs = new Vector<WebLinkDef>();
    protected boolean            hasChanged  = false;
    
    /**
     * Protected Constructor
     */
    protected WebLinkMgr()
    {
        // no-op
    }

    /**
     * Returns the instance to the singleton
     * @return  the instance to the singleton
     */
    public static WebLinkMgr getInstance()
    {
        if (instance != null)
        {
            return instance;
        }
        
        // else
        String factoryNameStr = AccessController.doPrivileged(new java.security.PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(factoryName);
                    }
                });
            
        if (isNotEmpty(factoryNameStr)) 
        {
            try 
            {
                return instance = (WebLinkMgr)Class.forName(factoryNameStr).newInstance();
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate WebLink factory " + factoryNameStr);
                error.initCause(e);
                throw error;
            }
        }
        // if not factory than pass an instance of this in
        // and this does nothing to the SQL.
        return instance = new WebLinkMgr();
    }
    
    /**
     * Resets the cache.
     */
    protected void reset()
    {
        webLinkDefs.clear();
    }
    
    /**
     * @param name
     * @return
     */
    public WebLinkDef get(final String name)
    {
        for (WebLinkDef wld : webLinkDefs)
        {
            if (wld.getName().equals(name))
            {
                return wld;
            }
        }
        return null;
    }
    
    /**
     * @param xstream
     */
    protected void config(final XStream xstream)
    {
        WebLinkDef.configXStream(xstream);
        WebLinkDefArg.configXStream(xstream);
        WebLinkUsedBy.configXStream(xstream);
    }
    
    /**
     * @param xml
     */
    @SuppressWarnings("unchecked")
    protected void loadFromXML(final String xml)
    {
        if (StringUtils.isNotEmpty(xml))
        {
            XStream xstream = new XStream();
            config(xstream);
            webLinkDefs = (Vector<WebLinkDef>)xstream.fromXML(xml);
        }
    }
    
    /**
     * @return
     */
    protected String convertToXML()
    {
        XStream xstream = new XStream();
        config(xstream);
        return xstream.toXML(webLinkDefs);
    }
    
    /**
     * @param wld
     */
    public void add(final WebLinkDef wld)
    {
        webLinkDefs.add(wld);
    }
    
    /**
     * @param wld
     */
    public void remove(final WebLinkDef wld)
    {
        webLinkDefs.removeElement(wld);
    }
    
    /**
     * 
     */
    public void read()
    {
        throw new RuntimeException("Read is not implemented");
    }

    public void write()
    {
        throw new RuntimeException("Write is not implemented");
    }

    /**
     * @return the webLinkDefs
     */
    public Vector<WebLinkDef> getWebLinkDefs()
    {
        return webLinkDefs;
    }

    /**
     * @param tableInfo
     * @param fieldInfo
     */
    public WebLinkConfigDlg editWebLinks(final DBTableInfo tableInfo,
                                         final DBFieldInfo fieldInfo)
    {
        WebLinkConfigDlg dlg = new WebLinkConfigDlg(tableInfo, fieldInfo);
        dlg.setVisible(true);
        
        if (dlg.hasChanged())
        {
            hasChanged = true;
        }
        return dlg;
    }
}

