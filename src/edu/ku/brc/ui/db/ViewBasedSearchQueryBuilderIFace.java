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
/**
 * 
 */
package edu.ku.brc.ui.db;

import java.util.Map;

/**
 * Interface that enables the query for the things like the ValComboBoxFromQuery to ovverride
 * and pre-built or XML specified query.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Nov 16, 2007
 *
 */
public interface ViewBasedSearchQueryBuilderIFace
{
    /**
     * Builds and returns the query.
     * @param searchText the text that needs to be search for by the query.
     * @return the full SQL String
     */
    public String buildSQL(String searchText);
    
    /**
     * Builds and returns the query.
     * @param dataMap the dataMap hdols Named/Value pairs where the name is the name of the control on the form
     * and the value is the string they typed in for that value.
     * @return the full SQL String
     */
    public String buildSQL(Map<String, Object> dataMap);
    
    /**
     * @return the description of how the results are to be displayed
     */
    public QueryForIdResultsIFace createQueryForIdResults();
    
}
