/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.config;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
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

    /**
     * @param appContextMgr
     */
    public SpecifyDataObjFieldFormatMgr(AppContextMgr appContextMgr)
    {
        super(appContextMgr);
        CommandDispatcher.register(DISCIPLINE, this); //$NON-NLS-1$
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr#reset()
     */
    @Override
    public void reset()
    {
        instance = null;
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

        SpecifyUIFieldFormatterMgr spUIFFMgr = (SpecifyUIFieldFormatterMgr)UIFieldFormatterMgr.getInstance();
        return spUIFFMgr.getDisciplineDOMFromResource(getAppContextMgr(), DATAOBJFORMATTERS, localFileName);
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
            SpecifyUIFieldFormatterMgr.saveDisciplineResource(getAppContextMgr(), DATAOBJFORMATTERS, xml);
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
