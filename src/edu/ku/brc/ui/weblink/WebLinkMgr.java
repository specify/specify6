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
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.db.DBTableInfo;

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
    public static final String factoryName = "edu.ku.brc.ui.weblink.WebLinkMgr"; //$NON-NLS-1$
    
    private static final Logger log = Logger.getLogger(WebLinkMgr.class);
    
    protected static WebLinkMgr instance = null;
    
    protected Vector<WebLinkDef> webLinkDefs;
    protected boolean            hasChanged  = false;
    
    /**
     * Protected Constructor
     */
    protected WebLinkMgr()
    {
        webLinkDefs = new Vector<WebLinkDef>();
    }

    /**
     * @param webLinkMgr
     */
    @SuppressWarnings("unchecked")
    public WebLinkMgr(final WebLinkMgr webLinkMgr)
    {
        webLinkDefs = (Vector<WebLinkDef>)webLinkMgr.webLinkDefs.clone();
    }
    
    /**
     * Copies internal data structures.
     * @param webLinkMgr source of the changes
     */
    protected void copyFrom(final WebLinkMgr webLinkMgr)
    {
        this.hasChanged  = webLinkMgr.hasChanged;
        this.webLinkDefs = webLinkMgr.webLinkDefs;
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
                InternalError error = new InternalError("Can't instantiate WebLink factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        // if not factory than pass an instance of this in
        // and this does nothing to the SQL.
        return instance = new WebLinkMgr();
    }
    
    /**
     * Reloads the data from peristent storage.
     */
    public void reload()
    {
        reset();
        read();
    }
    
    /**
     * @param source
     */
    public void applyChanges(final WebLinkMgr source)
    {
        if (source.hasChanged)
        {
            this.hasChanged = source.hasChanged;
            copyFrom(source);
            write();
            
        } else
        {
            log.debug("Not saved = No Changes");
        }
    }
    
    /**
     * @return the hasChanged
     */
    public boolean isHasChanged()
    {
        return hasChanged;
    }

    /**
     * @param hasChanged the hasChanged to set
     */
    public void setHasChanged(boolean hasChanged)
    {
        this.hasChanged = hasChanged;
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
    @SuppressWarnings("unchecked") //$NON-NLS-1$
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
        hasChanged = true;
    }
    
    /**
     * @param wld
     */
    public void remove(final WebLinkDef wld)
    {
        webLinkDefs.removeElement(wld);
        hasChanged = true;
    }
    
    /**
     * 
     */
    public void read()
    {
        throw new RuntimeException("Read is not implemented"); //$NON-NLS-1$
    }

    public void write()
    {
        throw new RuntimeException("Write is not implemented"); //$NON-NLS-1$
    }

    /**
     * @return the webLinkDefs
     */
    public Vector<WebLinkDef> getWebLinkDefs(final DBTableInfo tableInfo)
    {
        Vector<WebLinkDef> list  = new Vector<WebLinkDef>();
        for (WebLinkDef wld : webLinkDefs)
        {
            if ((tableInfo != null && wld.getTableName() != null && wld.getTableName().equals(tableInfo.getName())) ||
                (tableInfo == null && wld.getTableName() == null))
            {
                //System.out.println(wld.getName() + "  "+wld.getTableName());
                list.add(wld);
            }
        }
        return list;
    }

    /**
     * @param tableInfo
     * @param fieldInfo
     */
    public WebLinkConfigDlg editWebLinks(final DBTableInfo tableInfo,
                                         final boolean     isTableMode)
    {
        WebLinkConfigDlg dlg = new WebLinkConfigDlg(this, tableInfo, isTableMode);
        dlg.createUI();
        dlg.setSize(400,400);
        dlg.setVisible(true);
        
        if (dlg.hasChanged())
        {
            hasChanged = true;
        }
        return dlg;
    }
}

