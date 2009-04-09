/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.config;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr;
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
    private static final String DISCIPLINE        = "Discipline";
    private static final String DATAOBJFORMATTERS = "DataObjFormatters";
    
    /**
     * 
     */
    public SpecifyDataObjFieldFormatMgr()
    {
        CommandDispatcher.register(DISCIPLINE, this); //$NON-NLS-1$
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

        return SpecifyUIFieldFormatterMgr.getDisciplineDOMFromResource(DATAOBJFORMATTERS, localFileName);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr#saveXML(java.lang.String)
     */
    protected void saveXML(final String xml)
    {
        // save resource back to database
        if (doingLocal)
        {
            File outFile = XMLHelper.getConfigDir(localFileName);
            try
            {
                FileUtils.writeStringToFile(outFile, xml);
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDataObjFieldFormatMgr.class, ex);
            }
            
        } else if (AppContextMgr.getInstance() != null)
        {
            SpecifyUIFieldFormatterMgr.saveDisciplineResource(DATAOBJFORMATTERS, xml);
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
            load();
        }
    }
    
}
