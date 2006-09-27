/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.plugins.MenuItemDesc;
import edu.ku.brc.af.plugins.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.ui.CommandAction;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class AttachmentsTask extends BaseTask
{
    protected static final Logger log = Logger.getLogger(AttachmentsTask.class);
            
    public static final String      ATTACHMENTS                = "Attachments_Support";
    public static final String      NEW_ATTACHMENT_ACTION      = "NewAttachment";
    public static final String      GET_ATTACHMENT_INFO_ACTION = "AttachmentInfoRequest";
    public static final String      OPEN_ATTACHMENT_ACTION     = "OpenAttachment";
    
    protected List<ToolBarItemDesc> toolBarItems;
    protected List<MenuItemDesc> menuItems;

    
    public AttachmentsTask()
    {
        super(ATTACHMENTS, getResourceString(ATTACHMENTS));
        toolBarItems = new Vector<ToolBarItemDesc>();
        menuItems = new Vector<MenuItemDesc>();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#initialize()
     */
    @Override
    public void initialize()
    {
        super.initialize();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        return menuItems;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        return toolBarItems;
    }
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.getType()!=ATTACHMENTS)
        {
            // not a format I recognize, maybe it's for somebody else
            return;
        }
        
        String action = cmdAction.getAction();
        if (action.equals(NEW_ATTACHMENT_ACTION))
        {
            // TODO: implement this
        }
        else if (action.equals(GET_ATTACHMENT_INFO_ACTION))
        {
            // TODO: implement this
        }
        else if (action.equals(OPEN_ATTACHMENT_ACTION))
        {
            // TODO: implement this
        }
    }
}
