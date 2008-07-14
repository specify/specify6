/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.config;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.weblink.WebLinkMgr;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 13, 2008
 *
 */
public class SpecifyWebLinkMgr extends WebLinkMgr implements CommandListener
{
    private static final Logger log = Logger.getLogger(SpecifyWebLinkMgr.class);
            
    private static final String WEBLINKS      = "WebLinks";
    private static final String DISCPLINEDIR  = "Discipline";
    private static final String COMMONDIR     = "Common";
    private static final String DISKLOC       = "common/weblinks.xml";
    
    protected static boolean    doingLocal = false;

    /**
     * 
     */
    public SpecifyWebLinkMgr()
    {
        read();
    }
    
    /**
     * @param specifyWebLinkMgr
     */
    public SpecifyWebLinkMgr(final SpecifyWebLinkMgr specifyWebLinkMgr)
    {
        super(specifyWebLinkMgr);
        read();
    }
    

    /**
     * @param doingLocal the doingLocal to set
     */
    public static void setDoingLocal(boolean doingLocal)
    {
        SpecifyWebLinkMgr.doingLocal = doingLocal;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.weblink.WebLinkMgr#read()
     */
    @Override
    public void read()
    {
        reset();
        
        if (doingLocal)
        {
            File file = XMLHelper.getConfigDir(DISKLOC);
            try
            {
                loadFromXML(FileUtils.readFileToString(file));
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        } else
        {
            AppResourceIFace appRes = AppContextMgr.getInstance().getResourceFromDir(DISCPLINEDIR, WEBLINKS);
            if (appRes != null)
            {
                loadFromXML(AppContextMgr.getInstance().getResourceAsXML(appRes));
                
            } else
            {
                appRes = AppContextMgr.getInstance().getResourceFromDir(COMMONDIR, WEBLINKS);
                if (appRes != null)
                {
                    loadFromXML(AppContextMgr.getInstance().getResourceAsXML(appRes));
                   
                } else
                {
                    log.error("Couldn't get WebLinks");
                }
                
                
                /* else
                {
                    // Get the default resource by name and copy it to a new User Area Resource
                    AppResourceIFace newAppRes = AppContextMgr.getInstance().copyToDirAppRes(DISCPLINEDIR, WEBLINKS);
                    if (newAppRes != null)
                    {
                        // Save it in the User Area
                        AppContextMgr.getInstance().saveResource(newAppRes);
                        loadFromXML(AppContextMgr.getInstance().getResourceAsXML(newAppRes));
                    }
                }*/
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.weblink.WebLinkMgr#write()
     */
    @Override
    public void write()
    {
        if (hasChanged)
        {
            if (doingLocal)
            {
                File outputFile = XMLHelper.getConfigDir(DISKLOC);
                try
                {
                    FileUtils.writeStringToFile(outputFile, convertToXML());
                    hasChanged = false;

                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                
            } else
            {
                AppResourceIFace appRes = AppContextMgr.getInstance().getResourceFromDir(DISCPLINEDIR, WEBLINKS);
                if (appRes != null)
                {
                    appRes.setDataAsString(convertToXML());
                    AppContextMgr.getInstance().saveResource(appRes);
                   
                } else
                {
                    String xml = convertToXML();
                    appRes = AppContextMgr.getInstance().createAppResourceForDir(DISCPLINEDIR);
                    appRes.setLevel((short)0);
                    appRes.setName(WEBLINKS);
                    appRes.setMimeType("text/xml");
                    appRes.setDataAsString(xml);
                    
                    AppContextMgr.getInstance().saveResource(appRes);
                }
                hasChanged = false;
            }
        }
    }
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.CommandListener#doCommand(edu.ku.brc.af.ui.CommandAction)
     */
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isType("Collection") && cmdAction.isAction("Changed"))
        {
            read();
        }
    }

}
