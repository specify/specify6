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
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr;

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

    public SpecifyDataObjFieldFormatMgr()
    {
        CommandDispatcher.register("Collection", this);  
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

        AppResourceIFace appRes = AppContextMgr.getInstance().getResourceFromDir("Collection", "DataObjFormatters");
        if (appRes != null)
        {
            return AppContextMgr.getInstance().getResourceAsDOM(appRes);
        } 
        
        // Get the default resource by name and copy it to a new User Area Resource
        AppResourceIFace newAppRes = AppContextMgr.getInstance().copyToDirAppRes("Collection", "DataObjFormatters");
        
        // Save it in the User Area
        AppContextMgr.getInstance().saveResource(newAppRes);
        return AppContextMgr.getInstance().getResourceAsDOM(newAppRes);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr#saveXML(java.lang.String)
     */
    protected void saveXML(final String xml)
    {
        // save resource back to database
        if (AppContextMgr.getInstance() != null)
        {
            AppResourceIFace escAppRes = AppContextMgr.getInstance().getResourceFromDir("Collection", "DataObjFormatters");
            if (escAppRes != null)
            {
                escAppRes.setDataAsString(xml);
                AppContextMgr.getInstance().saveResource(escAppRes);
               
            } else
            {
                AppContextMgr.getInstance().putResourceAsXML("DataObjFormatters", xml);    
            }
        } else
        {
            File outFile = XMLHelper.getConfigDir(localFileName);
            try
            {
                FileUtils.writeStringToFile(outFile, xml);
                
            } catch (Exception ex)
            {
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
        if (cmdAction.isType("Collection") && cmdAction.isAction("Changed"))
        {
            load();
        }
    }
    
}
