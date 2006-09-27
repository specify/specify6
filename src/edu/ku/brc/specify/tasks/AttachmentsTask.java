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

import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.plugins.MenuItemDesc;
import edu.ku.brc.af.plugins.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class AttachmentsTask extends BaseTask
{
    public static final String ATTACHMENTS = "Attachments_Support";
    
    protected List<ToolBarItemDesc> toolBarItems;
    protected List<MenuItemDesc> menuItems;

    
    public AttachmentsTask()
    {
        super(ATTACHMENTS, getResourceString(ATTACHMENTS));
        toolBarItems = new Vector<ToolBarItemDesc>();
        menuItems = new Vector<MenuItemDesc>();
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
}
