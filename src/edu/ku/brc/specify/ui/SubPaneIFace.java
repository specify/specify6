/* Filename:    $RCSfile: SubPaneIFace.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.ui;

import java.util.List;

import javax.swing.*;
import edu.ku.brc.specify.core.*;

/**
 * 
 * @author rods
 *
 * An interface for all pnaes that want to participate in the "main" panel of the UI.
 * 
 */
public interface SubPaneIFace
{
    /**
     * Returns the name of the sub pane
     * @return Returns the name of the sub pane
     */
    public String getName();
    
    /**
     * Returns the title 
     * @return Returns the title 
     */
    public String getTitle();
    
    /**
     * Returns the small icon used in the tab
     * @return Returns the small icon used in the tab
     */
    public Icon getIcon();
    
    /**
     * Returns the UI component of the pane
     * @return Returns the UI component of the pane
     */
    public JComponent getUIComponent();
    
    /**
     * Returns the task who owns this pane (needed for context)
     * @return Returns the task who owns this pane (needed for context)
     */
    public Taskable getTask();
}
