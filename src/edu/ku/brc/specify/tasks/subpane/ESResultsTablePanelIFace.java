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
package edu.ku.brc.specify.tasks.subpane;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.util.List;

import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;

/**
 * The important 
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 18, 2007
 *
 */
public interface ESResultsTablePanelIFace extends PropertyChangeListener
{
    /**
     * Initializes 
     * @param esrPane
     * @param results
     */
    public abstract void initialize(final ExpressSearchResultsPaneIFace esrPane,
                                    final QueryForIdResultsIFace        results);
    /**
     * @return the UI component for the results panel
     */
    public abstract Component getUIComponent();
    
    /**
     * Cleans up references to other objects.
     */
    public abstract void cleanUp();

    /**
     * Returns a list of recordIds.
     * @param returnAll indicates whether all the records should be returned if nothing was selected
     * @return a list of recordIds
     */
    public abstract List<Integer> getListOfIds(final boolean returnAll);

    /**
     * @return a list of the indexes (row numbers) of selected rows.
     */
    public abstract int[] getSelectedRows();
    
    /** 
     * Register a single property listener.
     * @param pcl the listener
     */
    public abstract void setPropertyChangeListener(PropertyChangeListener pcl);
    
    
    /**
     * @return
     */
    public abstract QueryForIdResultsIFace getResults();
    
    /**
     * Expand the view of results if it is appropriate.
     */
    public abstract void expandView();
    
    /**
     * @return true if there are results, false if none were found.
     */
    public abstract boolean hasResults();
    
}
