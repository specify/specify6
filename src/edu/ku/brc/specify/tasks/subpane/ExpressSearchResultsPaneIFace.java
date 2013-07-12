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
package edu.ku.brc.specify.tasks.subpane;

import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;


/**
 * Interface for objects that can display the results of a search.
 *
 * @code_status Alpha
 * 
 * @author rods
 *
 */
public interface ExpressSearchResultsPaneIFace
{
    /**
     * Add serach results box to UI.
     * @param tableInfo the information about the table being added
     * @param hits the "hits" results of the search
     */
    public abstract void addSearchResults(final QueryForIdResultsIFace results);

    /**
     * Removes a table from the content pane
     * @param table the table of results to be removed
     */
    public abstract void removeTable(ESResultsTablePanelIFace table);

    /**
     * Adds a table to the content pane.
     * @param table the table of results to be added
     */
    public abstract void addTable(ESResultsTablePanelIFace table);

    /**
     * Revalidate the scroll pane
     */
    public abstract void revalidateScroll();
    
    /**
     * @return true if there are results, false if none were found.
     */
    public abstract boolean hasResults();
    
    /**
     * @return returns whether the Queries should be executed in a synchronous fashion.
     */
    public boolean doQueriesSynchronously();
    
    /**
     * 
     */
    public void done();

}
