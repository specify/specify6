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
package edu.ku.brc.services.biogeomancer;

import java.util.List;

/**
 * Listener interface for knowing when the GeoRef processing has completed.
 * 
 * @author rod
 *
 * @code_status Complete
 *
 * Jan 14, 2008
 *
 */
public interface GeoCoordProviderListenerIFace
{
    
    /**
     * Tells the listener that it is about to display some UI.
     */
    public abstract void aboutToDisplayResults();
    
    
    /**
     * Tells the user it has completed and returns the items.
     * @param items the list of items to be GeoRef'ed (the complete list).
     * @param itemsUpdated the number of items updated
     */
    public abstract void complete(final List<GeoCoordDataIFace> items, int itemsUpdated);
}
