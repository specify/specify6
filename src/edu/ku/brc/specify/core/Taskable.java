/* Filename:    $RCSfile: MainPanel.java,v $
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
package edu.ku.brc.specify.core;

import java.util.List;
import java.awt.*;
import javax.swing.*;


/**
 * Status: Work In Progress
 * 
 * @author rods
 *
 */
public interface Taskable
{

    /**
     * 
     * @return a collection of Nav Boxes
     */
    public List<NavBoxIFace> getNavBoxes();
    
    /**
     * Return the icon that represents the task
     * @return Return the icon that represents the task
     */
    public Icon getIcon();
    
    /**
     * Returns the name of the task
     * @return Returns the name of the task
     */
    public String getName();

}
