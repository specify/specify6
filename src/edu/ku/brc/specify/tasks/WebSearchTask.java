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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.BackupServiceFactory;
import edu.ku.brc.specify.tasks.subpane.WebServiceSubPane;
import edu.ku.brc.specify.web.HttpLargeFileTransfer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.UIHelper;

/**
 * Takes care of offering up record sets, updating, deleting and creating them.
 *
 * @code_status Alpha
 * 
 * @author rods
 *
 */
public class WebSearchTask extends BaseTask
{
    // Static Data Members
    //private static final Logger log  = Logger.getLogger(WebSearchTask.class);
    
    public static final String     WEBSEARCH        = "WebSearch";
    public static final String     WS_BK_SEND       = "WS_BK_SEND";
    public static final String     WEBSRCH_MSG      = "WEBSRCH_MSG";
    
    protected static final String WEBSEARCH_ICON    = "Plugins";
    
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
    }

    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            /*navBox = new NavBox(getResourceString("Commands"));
            CommandAction cmdAction = new CommandAction(WEBSEARCH, WS_BK_SEND, null);
            makeDnDNavBtn(navBox, "Send Backup", "MySQL", cmdAction, null, false, false);
            
            navBoxes.add(navBox);*/
        }
    }
    
     /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        if (starterPane != null)
        {
            return starterPane;
        }
        return starterPane = new WebServiceSubPane(WEBSEARCH, this, false);
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.SubPaneMgrListener#subPaneRemoved(edu.ku.brc.af.ui.SubPaneIFace)
     */
    @Override
    public void subPaneRemoved(final SubPaneIFace subPane)
    {
        super.subPaneRemoved(subPane);
        
        if (starterPane != null && (starterPane == subPane || subPanes.size() == 0))
        {
            starterPane.shutdown();
            starterPane = null;
            TaskMgr.reenableAllDisabledTasks();
            TaskMgr.getTask("Startup").requestContext();
        }
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
    public void createAndSendBackup()
    {
        BackupServiceFactory.getInstance().doBackUp(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (evt.getPropertyName().equals("Done"))
                {
                    startCompression((String)evt.getNewValue());
                }
            }
        });
    }

    /**
     * @param fileName
     */
    private void startCompression(final String fileName)
    {
        if (StringUtils.isNotEmpty(fileName))
        {
            try
            {
                final File file = File.createTempFile("Bkup", ".gz");
                final HttpLargeFileTransfer httpFileTransfer = new HttpLargeFileTransfer();
                
                httpFileTransfer.compressFile(fileName, file.getAbsolutePath(), new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        if (evt.getPropertyName().equals("Done"))
                        {
                            startUpload(httpFileTransfer, false, file);
                        }
                    }
                });
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
            }
        }
    }
    
    /**
     * @param compressedFile
     */
    private void startUpload(final HttpLargeFileTransfer httpFileTransfer,
                             final boolean isSiteFile, 
                             final File compressedFile)
    {
        httpFileTransfer.transferFile(compressedFile.getAbsolutePath(), "http://localhost:8080/uploader/UploaderServlet", isSiteFile, null);
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        String menuDesc = "Specify.SYSTEM_MENU";
        
        menuItems = new Vector<MenuItemDesc>();
        
        if (permissions == null || permissions.canModify())
        {
            String    menuTitle = "Web Search";//"PluginsTask.PLUGIN_MENU"; //$NON-NLS-1$
            String    mneu      = "PluginsTask.PLUGIN_MNEU"; //$NON-NLS-1$
            String    desc      = "PluginsTask.PLUGIN_DESC"; //$NON-NLS-1$
            JMenuItem mi        = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null);
            mi.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    WebSearchTask.this.requestContext();
                }
            });
            MenuItemDesc rsMI = new MenuItemDesc(mi, menuDesc);
            rsMI.setPosition(MenuItemDesc.Position.Bottom);
            menuItems.add(rsMI);
        }
        
        return menuItems;

    }
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    /**
     * Processes all Commands of type WEBSEARCH.
     * @param cmdAction the command to be processed
     */
    protected void processWebSearchCommands(final CommandAction cmdAction)
    {
        UsageTracker.incrUsageCount("WS."+cmdAction.getType());
        
        if (cmdAction.isAction(WS_BK_SEND))
        {
            createAndSendBackup();
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
            processWebSearchCommands(cmdAction);
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
