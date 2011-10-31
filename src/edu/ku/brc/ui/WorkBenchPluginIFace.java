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
package edu.ku.brc.ui;

import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;

import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPaneSS;
import edu.ku.brc.ui.tmanfe.SpreadSheet;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 26, 2010
 *
 */
public interface WorkBenchPluginIFace
{
    
    /**
     * Provides the plugin with a reference to the workbench
     * it will be dealing with.
     * 
     * Will be called immediately after the plugin is instantiated.
     * 
     * @param workbench
     */
    public abstract void setWorkbench(Workbench workbench);

    
    /**
     * Provides the plugin with a reference to the pane containing
     * the workbench editor.
     * 
     * Will be called immediately after the plugin is instantiated.
     * 
     * @param ss
     */
    public abstract void setWorkbenchPaneSS(WorkbenchPaneSS wbpss);
    
    
    /**
     * Provides the plugin with a reference to the spreadsheet
     * object for the workbench.
     * 
     * Will be called immediately after the plugin is instantiated.
     * 
     * @param ss
     */
    public abstract void setSpreadSheet(SpreadSheet ss);
    

    /**
     * Called when the workbench is being closed to allow the
     * plugin to clean up after itself.
     */
    public abstract void shutdown();
    

    /**
     * Does the WorkBench Template definition have the necessary columns for the plugin.
     * 
     * Not currently used.
     * 
     * @return list of missing fields required by plugin (might return null)
     */
    public abstract List<String> getMissingFieldsForPlugin();

    /**
     * The plugin should return a collection of buttons it wants
     * added to the Spreadsheet view of the workbench. These can
     * be the same or different from those for the form view.
     * 
     * @return
     */
    public abstract Collection<JComponent> getSSButtons();

    
    /**
     * The plugin should return a collection of buttons it wants
     * added to the form view of the workbench. These can be the
     * same or different from those for the spreadsheet view.
     *  
     * @return
     */
    public abstract Collection<JComponent> getFormButtons();
}
