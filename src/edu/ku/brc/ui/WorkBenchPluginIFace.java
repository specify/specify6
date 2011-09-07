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

import java.util.List;

import javax.swing.JButton;

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
     * @param parent
     */
    public abstract void setSubPanel(SubPaneIFace parent);
    
    /**
     * Does the WorkBench Template definition have the necessary columns for the plugin.
     * @return list of missing fields required by plugin (might return null)
     */
    public abstract List<String> getMissingFieldsForPlugin();
    
    /**
     * @param ss
     */
    /**
     * @param ss
     */
    public abstract void setWorkbenchPaneSS(WorkbenchPaneSS wbpss);
    
    public abstract void setSpreadSheet(SpreadSheet ss);
    
    /**
     * @param workbench
     */
    public abstract void setWorkbench(Workbench workbench);
    
    /**
     * @return
     */
    public abstract boolean process(List<WorkbenchRow> rows);
    
    /**
     * 
     */
    public abstract void shutdown();

    public abstract void setButton(JButton btn);
    
}
