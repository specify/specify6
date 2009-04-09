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
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.specify.datamodel.SpAppResourceDir;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;

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
    //private static final Logger log = Logger.getLogger(SpecifyWebLinkMgr.class);
            
    private static final String DISCIPLINE    = "Discipline";
    private static final String WEBLINKS      = "WebLinks";
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
        CommandDispatcher.register(DISCIPLINE, this); //$NON-NLS-1$
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
        
        String xml = null;
        if (doingLocal)
        {
            File file = XMLHelper.getConfigDir(DISKLOC);
            try
            {
                xml = FileUtils.readFileToString(file);
                
            } catch (IOException ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyWebLinkMgr.class, ex);
                ex.printStackTrace();
            }
        } else
        {
            SpecifyAppContextMgr acMgr = (SpecifyAppContextMgr)AppContextMgr.getInstance();
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                SpecifyUser user       = acMgr.getClassObject(SpecifyUser.class);
                Discipline  discipline = acMgr.getClassObject(Discipline.class);
                
                SpAppResourceDir appResDir = acMgr.getAppResDir(session, user, discipline, null, null, false, WEBLINKS, false);
                if (appResDir != null)
                {
                    SpAppResource appRes = appResDir.getResourceByName(WEBLINKS);
                    if (appRes != null)
                    {
                        session.close();
                        session = null;
                        
                        xml = AppContextMgr.getInstance().getResourceAsXML(appRes);
                    }
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyUIFieldFormatterMgr.class, ex);
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
            
            if (xml != null)
            {
                loadFromXML(xml);
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
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyWebLinkMgr.class, ex);
                    ex.printStackTrace();
                }
                
            } else
            {
                SpecifyUIFieldFormatterMgr.saveDisciplineResource(WEBLINKS, convertToXML());
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
        if (cmdAction.isType(DISCIPLINE) && cmdAction.isAction("Changed"))
        {
            read();
        }
    }

}
