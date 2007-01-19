/* This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.tasks.subpane;

import org.apache.lucene.search.Hits;

import edu.ku.brc.af.core.ExpressSearchResults;


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
     * Add serach results box to UI
     * @param tableInfo the information about the table being added
     * @param hits the "hits" results of the search
     */
    public void addSearchResults(final ExpressSearchResults results, final Hits hits);

    /**
     * Removes a table from the content pane
     * @param table the table of results to be removed
     */
    public void removeTable(ExpressTableResultsBase table);

    /**
     * Adds a table to the content pane
     * @param table the table of results to be added
     */
    public void addTable(ExpressTableResultsBase table);

    /**
     * Revalidate the scroll pane
     */
    public void revalidateScroll();

}
