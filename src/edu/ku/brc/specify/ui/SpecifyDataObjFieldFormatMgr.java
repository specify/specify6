/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.ui;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.specify.datamodel.SpAppResourceDir;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 6, 2007
 *
 */
public class SpecifyDataObjFieldFormatMgr extends DataObjFieldFormatMgr implements CommandListener
{
    protected static String         COLLECTION        = "Collection";
    protected static String         BACKSTOPDIR       = "BackStop";
    protected static String         DATAOBJFORMATTERS = "DataObjFormatters";
    
    /**
     * 
     */
    public SpecifyDataObjFieldFormatMgr()
    {
        CommandDispatcher.register(COLLECTION, this);  
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr#getDOM()
     */
    protected Element getDOM() throws Exception
    {
        if (doingLocal)
        {
            return XMLHelper.readDOMFromConfigDir(localFileName);
        }

        AppResourceIFace appRes = AppContextMgr.getInstance().getResourceFromDir(COLLECTION, DATAOBJFORMATTERS);
        if (appRes != null)
        {
            return AppContextMgr.getInstance().getResourceAsDOM(appRes);
        }
        
        return XMLHelper.readDOMFromConfigDir(localFileName);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr#saveXML(java.lang.String)
     */
    protected void saveXML(final String xml)
    {
        // save resource back to database
        if (AppContextMgr.getInstance() != null && !doingLocal)
        {
            AppResourceIFace appRes = AppContextMgr.getInstance().getResourceFromDir(COLLECTION, DATAOBJFORMATTERS);
            if (appRes != null)
            {
                System.err.println(xml);
                appRes.setDataAsString(xml);
                AppContextMgr.getInstance().saveResource(appRes);
               
            } else
            {
                // Save the UIFieldFormatters into a new Resource in Collections.
                SpecifyAppContextMgr context = (SpecifyAppContextMgr)AppContextMgr.getInstance();
                SpAppResourceDir     collDir = context.getSpAppResourceDirByName(COLLECTION);
                
               if (collDir != null)
                {
                    SpAppResource uifAppRes = context.getSpAppResourceDirByName(BACKSTOPDIR).getResourceByName(DATAOBJFORMATTERS);
                    SpAppResource appResUF  = new SpAppResource();
                    appResUF.initialize();
                    if (uifAppRes != null)
                    {
                        appResUF.setMetaData(uifAppRes.getMetaData());
                        appResUF.setDescription(uifAppRes.getDescription());
                        appResUF.setFileName(uifAppRes.getFileName());
                        appResUF.setMimeType(uifAppRes.getMimeType());
                        appResUF.setName(uifAppRes.getName());

                        SpecifyUser user  = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
                        Agent       agent = AppContextMgr.getInstance().getClassObject(Agent.class);
                        appResUF.setCreatedByAgent(agent);
                        appResUF.setSpecifyUser(user);

                        appResUF.setLevel(uifAppRes.getLevel());
                    }
                    appResUF.setSpAppResourceDir(collDir);
                    collDir.getSpAppResources().add(appResUF);
                    System.err.println(xml);
                    appResUF.setDataAsString(xml);
                    ((SpecifyAppContextMgr) AppContextMgr.getInstance()).saveResource(appResUF);
                    
                } else
                {
                    AppContextMgr.getInstance().putResourceAsXML(DATAOBJFORMATTERS, xml); //$NON-NLS-1$
                }
            }
            
        } else
        {
            File outFile = XMLHelper.getConfigDir(localFileName);
            try
            {
                FileUtils.writeStringToFile(outFile, xml);
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDataObjFieldFormatMgr.class, ex);
                ex.printStackTrace();
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
        if (cmdAction.isType(COLLECTION) && cmdAction.isAction("Changed"))
        {
            load();
        }
    }
    
}
