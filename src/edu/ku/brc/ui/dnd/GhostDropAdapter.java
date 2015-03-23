/* Copyright (C) 2015, University of Kansas Center for Research
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

import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * (Adpated from Romain Guy's Glass Pane Drag Photo Demo)
 *
 * @code_status Beta
 * 
 * @author rods
 * @author Romain Guy
 * @author Sebastien Petrucci <sebastien_petrucci@yahoo.fr>*
 *
 */
public class GhostDropAdapter extends MouseAdapter 
{
    protected GhostGlassPane glassPane;

    protected String action;

    private List<GhostDropListener> listeners;

    public GhostDropAdapter(GhostGlassPane glassPane, String action) 
    {
        this.glassPane = glassPane;
        this.action = action;
        this.listeners = new ArrayList<GhostDropListener>();
    }

    public void addGhostDropListener(GhostDropListener listener) 
    {
        if (listener != null)
            listeners.add(listener);
    }

    public void removeGhostDropListener(GhostDropListener listener) 
    {
        if (listener != null)
            listeners.remove(listener);
    }

    protected void fireGhostDropEvent(GhostDropEvent evt) 
    {
        Iterator<GhostDropListener> it = listeners.iterator();
        while (it.hasNext()) 
        {
            it.next().ghostDropped(evt);
        }
    }
    
    protected boolean hasListeners()
    {
        return listeners.size() > 0;
    }
}
