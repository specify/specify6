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

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.weblink.WebLinkMgr;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 13, 2008
 *
 */
public class SpecifyWebLinkMgr extends WebLinkMgr
{
    private static final String WEBLINKS      = "WebLinks";
    private static final String DISCPLINEDIR  = "Discipline";
    private static final String COMMONDIR     = "Common";
    
    protected static boolean    doingLocal = false;

    /**
     * 
     */
    public SpecifyWebLinkMgr()
    {
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
        if (doingLocal)
        {
            File file = XMLHelper.getConfigDir("common/weblinks.xml");
            try
            {
                loadFromXML(FileUtils.readFileToString(file));
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        } else
        {
            AppResourceIFace escAppRes = AppContextMgr.getInstance().getResourceFromDir(COMMONDIR, WEBLINKS);
            if (escAppRes != null)
            {
                loadFromXML(AppContextMgr.getInstance().getResourceAsXML(escAppRes));
               
            } else
            {
                // Get the default resource by name and copy it to a new User Area Resource
                AppResourceIFace newAppRes = AppContextMgr.getInstance().copyToDirAppRes(DISCPLINEDIR, WEBLINKS);
                if (newAppRes != null)
                {
                    // Save it in the User Area
                    AppContextMgr.getInstance().saveResource(newAppRes);
                    loadFromXML(AppContextMgr.getInstance().getResourceAsXML(newAppRes));
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.weblink.WebLinkMgr#write()
     */
    @Override
    public void write()
    {
        if (doingLocal)
        {
            File outputFile = XMLHelper.getConfigDir("common/weblinks.xml");
            try
            {
                FileUtils.writeStringToFile(outputFile, convertToXML());
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
            
        } else
        {
            AppResourceIFace escAppRes = AppContextMgr.getInstance().getResourceFromDir(DISCPLINEDIR, WEBLINKS);
            if (escAppRes != null)
            {
                escAppRes.setDataAsString(convertToXML());
                AppContextMgr.getInstance().saveResource(escAppRes);
               
            } else
            {
                String xml = convertToXML();
                System.err.println(xml);
                AppResourceIFace appRes = AppContextMgr.getInstance().createAppResourceForDir(DISCPLINEDIR);
                appRes.setLevel((short)0);
                appRes.setName(WEBLINKS);
                appRes.setMimeType("text/xml");
                appRes.setDataAsString(xml);
                
                AppContextMgr.getInstance().saveResource(appRes);
            }
        }
    }

}
