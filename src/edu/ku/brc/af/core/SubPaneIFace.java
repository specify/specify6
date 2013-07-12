/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.af.core;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JComponent;

import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.RecordSetIFace;


/**
 *
 * An interface for all pane that want to participate in the "main" panel of the UI. SubPaneIFace are managed by the SubPaneMgr.
 * It is common for panes implementing the SubPaneIFace to be created by Taskables.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public interface SubPaneIFace
{
    /**
     * Returns the name of the sub pane.
     * @return Returns the name of the sub pane
     */
    public abstract String getPaneName();

    /**
     * Sets a name.
     * @param name the new name
     */
    public abstract void setPaneName(String name);

    /**
     * Returns the title.
     * @return the title
     */
    public abstract String getTitle();

    /**
     * Returns the small icon used in the tab.
     * @return the small icon used in the tab
     */
    public abstract Icon getIcon();

    /**
    public abstract abstract rns the UI component of the pane.
     * @return the UI component of the pane
     */
    public abstract JComponent getUIComponent();
    
    /**
     * Returns the UI component of the pane.
     * @return the UI component of the pane
     */
    public abstract Component getFirstFocusable();
    
    /**
     * Returns the MultiView for the SubPane (may return null).
     * @return the MultiView for the SubPane (may return null).
     */
    public abstract MultiView getMultiView();

    /**
     * Returns the task who owns this pane (needed for context).
     * @return the task who owns this pane (needed for context)
     */
    public abstract Taskable getTask();

    /**
     * Returns the RecordSet contained in this panel, it may return null.
     * @return the RecordSet contained in this panel, it may return null.
     */
    public abstract RecordSetIFace getRecordSet();

    /**
     * Tells the SubPane that it is about to be shown or hidden.
     * @param show true = show, false hide
     */
    public abstract void showingPane(boolean show);
    
    /**
     * @return the time in millisecond when the pane was created
     */
    public abstract Long getCreateTime();
    
    /**
     * Returns the string of the Java Help target that should be displayed.
     * @return the string of the Java Help target that should be displayed.
     */
    public abstract String getHelpTarget();

    /**
     * Tells the panel it is about to be closed and destroyed and it can return "false" if it wants the processed stopped.
     * @return true means it was shutdown correctly, false means the shutdown process stop if it can
     */
    public abstract boolean aboutToShutdown();

    /**
     * Tells the panel it is being closed and destroyed
     */
    public abstract void shutdown();

 }
