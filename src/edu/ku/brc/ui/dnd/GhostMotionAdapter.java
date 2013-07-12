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
package edu.ku.brc.ui.dnd;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import edu.ku.brc.ui.UIRegistry;

/**
 * Class for making sure the image gets drawn on the glass pane while the image gets dragged on the glass pane
 * (Adpated from Romain Guy's Glass Pane Drag Photo Demo)
 *
 * @code_status Beta
 * 
 * @author rods
 * @author Romain Guy
 * @author S�bastien Petrucci <sebastien_petrucci@yahoo.fr>*
 *
 */
public class GhostMotionAdapter extends MouseMotionAdapter 
{
    private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
    private static final Cursor DEF_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
    
    private GhostGlassPane glassPane;
    private Cursor         currCursor = null; 

    /**
     * Contrsucts with the glass pane needed for drawing during the drag
     * @param glassPane
     */
    public GhostMotionAdapter(GhostGlassPane glassPane) 
    {
        this.glassPane = glassPane;
    }
    
    /**
     * Returns whether the flavors match
     * @param srcGA the source 
     * @param dstGA the destination
     * @return Returns whether the flavors match
     */
    protected boolean isFlavorOK(final GhostActionable  srcGA, final GhostActionable  dstGA)
    {
        for (DataFlavor srcDF : srcGA.getDragDataFlavors())
        {
            for (DataFlavor dstDF : dstGA.getDropDataFlavors())
            {
                if (srcDF.equals(dstDF)) 
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Updates (draws) the glass pane as the image is dragged
     */
    @Override
    public void mouseDragged(MouseEvent e) 
    {
        if (!DragAndDropLock.isDragAndDropStarted()) 
        {
            return;
        }
        
        Component c = e.getComponent();
        
        JComponent rootPane = (JComponent)UIRegistry.get(UIRegistry.MAINPANE);
        Point pp = (Point) e.getPoint().clone();
        SwingUtilities.convertPointToScreen(pp, c);
        SwingUtilities.convertPointFromScreen(pp, rootPane);

        boolean flavorOK = false;
        // find the component that under this point
        Component dropComponent = SwingUtilities.getDeepestComponentAt(rootPane, pp.x, pp.y);
        if (dropComponent instanceof GhostActionable && c != dropComponent)
        {
            flavorOK = isFlavorOK((GhostActionable)c, (GhostActionable)dropComponent);
        }

        if (flavorOK)
        {
            if (currCursor != HAND_CURSOR) // a little optimiztion
            {
                //glassPane.setAlpha(1.0f);
                glassPane.setCursor(HAND_CURSOR);
                currCursor = HAND_CURSOR; 
            }
        } else
        {
            if (currCursor != DEF_CURSOR) // a little optimiztion
            {
                //glassPane.resetAlpha(); 
                glassPane.setCursor(DEF_CURSOR);
                currCursor = DEF_CURSOR; 
            }
        }
        
        Point p = (Point) e.getPoint().clone();
        SwingUtilities.convertPointToScreen(p, c);
        SwingUtilities.convertPointFromScreen(p, glassPane);
        glassPane.setPoint(p);

        glassPane.repaint(glassPane.getRepaintRect());
    }
}
