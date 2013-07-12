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
package edu.ku.brc.af.ui.db;

import java.util.List;
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
     * @param isForCount true if the result is to be used to determine the count of matches.
     * @return the full SQL String
     */
    public abstract String buildSQL(String searchText, boolean isForCount);
    
    /**
     * Builds and returns the query.
     * @param dataMap the dataMap holds Named/Value pairs where the name is the name of the control on the form
     * and the value is the string they typed in for that value. Note pairs are not added to the dataMap if
     * the value was empty.
     * @param fieldNames the list of names of the fields in the form.
     * @return the full SQL String
     */
    public abstract String buildSQL(Map<String, Object> dataMap, List<String> fieldNames);
    
    /**
     * @return the description of how the results are to be displayed
     */
    public abstract QueryForIdResultsIFace createQueryForIdResults();
    
}
