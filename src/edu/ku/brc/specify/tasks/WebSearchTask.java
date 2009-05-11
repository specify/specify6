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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DateWrapper;

/**
 * Takes care of offering up record sets, updating, deleteing and creating them.
 *
 * @code_status Alpha
 * 
 * @author rods
 *
 */
public class WebSearchTask extends BaseTask
{
    // Static Data Members
    private static final Logger log  = Logger.getLogger(WebSearchTask.class);
    
    public static final String     WEBSEARCH        = "InfoRequest";
    public static final String     WEBSRCH_MSG      = "WEBSRCH_MSG";
    public static final String     CREATE_MAILMSG   = "CreateMailMsg";
    
    protected static final String infoReqIconName   = "inforequest";
    

    protected static DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");


    // Data Members
    protected NavBox navBox = null;
    
    /**
     * Default Constructor
     *
     */
    public WebSearchTask()
    {
        super(WEBSEARCH, getResourceString(WEBSEARCH));
        CommandDispatcher.register(WEBSEARCH, this);
        CommandDispatcher.register(DB_CMD_TYPE, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
        }
    }
    
     /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        recentFormPane = new FormPane(name, this, "");
        starterPane    = recentFormPane;
        return recentFormPane;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
    
    /**
     * 
     */
    protected void createAndSendBackup()
    {
        
    }
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    /**
     * Processes all Commands of type WEBSEARCH.
     * @param cmdAction the command to be processed
     */
    protected void processInfoRequestCommands(final CommandAction cmdAction)
    {
        UsageTracker.incrUsageCount("WS."+cmdAction.getType());
        
        if (cmdAction.isAction(CREATE_MAILMSG))
        {
            //createAndSendEMail();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.CommandListener#doCommand(edu.ku.brc.specify.ui.CommandAction)
     */
    public void doCommand(CommandAction cmdAction)
    {
        super.doCommand(cmdAction);
        
        if (cmdAction.isType(WEBSEARCH))
        {
            processInfoRequestCommands(cmdAction);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPermsArray()
     */
    @Override
    protected boolean[][] getPermsArray()
    {
        return new boolean[][] {{true, true, true, true},
                                {true, true, true, true},
                                {true, true, false, true},
                                {false, false, false, false}};
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#isPermissionsSettable()
     */
    @Override
    public boolean isPermissionsSettable()
    {
        return false;
    }
}
