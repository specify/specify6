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
package edu.ku.brc.ui.dnd;

import java.awt.Component;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Watches for the ghost image to be dropped and then negoiates between the source and the destination calls
 * doAction on the setination GhostActionale
 * 
 * (Adpated from Romain Guy's Glass Pane Drag Photo Demo)
 *
 * @code_status Beta
 * 
 * @author rods
 * @author Romain Guy
 * @author S�bastien Petrucci <sebastien_petrucci@yahoo.fr>*
 * @author rods
 *
 */
public class GhostActionableDropManager extends AbstractGhostDropManager 
{
    private GhostActionable ghostActionable;
    private GhostGlassPane  glassPane;

    /**
     * Constructs a drop zone
     * @param glassPane the glass pane to animate on drop
     * @param target the target JComponent 
     * @param ghostActionable the destination actionable
     */
    public GhostActionableDropManager(GhostGlassPane glassPane, JComponent target, GhostActionable ghostActionable) 
    {
        super(target);
        this.glassPane       = glassPane;
        this.ghostActionable = ghostActionable;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostDropListener#ghostDropped(edu.ku.brc.ui.dnd.GhostDropEvent)
     */
    @Override
    public void ghostDropped(GhostDropEvent e) 
    {
        Point p = getTranslatedPoint(e.getDropLocation());

        if (isInTarget(p)) 
        {
            BufferedImage bi = ghostActionable.getBufferedImage();
            glassPane.setImage(bi, bi.getWidth());
            glassPane.startAnimation(SwingUtilities.convertRectangle(component,
                                                                    component.getVisibleRect(),
                                                                    glassPane));
            Component source = e.getComponent();
           
            if (component instanceof GhostActionable && source instanceof GhostActionable)
            {
                ((GhostActionable)component).doAction((GhostActionable)source);
            }
            e.setConsumed(true);
           
        }  else if (component.isShowing()) 
        {
            // xxx ?? Not sure what to do with this now ??
            
           //glassPane.setImage(null);
           //glassPane.setVisible(false);
           //DragAndDropLock.setLocked(false);
        }
    }
}
