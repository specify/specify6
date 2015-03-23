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
package edu.ku.brc.af.ui;

import java.util.List;


/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 6, 2009
 *
 */
public interface SearchTermParserIFace
{

    /**
     * @return the fields
     */
    public abstract List<SearchTermField> getFields();

    /**
     * @param searchTermArg
     * @param parseAsSingleTerm
     * @return true if all the tokens are valid
     */
    public abstract boolean parse(final String searchTermArg, final boolean parseAsSingleTerm);

    /**
     * @param term
     * @param abbrevArg
     * @param fieldName
     * @param termStr
     * @return
     */
    public abstract String createWhereClause(final SearchTermField term,
                                             final String abbrevArg,
                                             final String fieldName);

}
