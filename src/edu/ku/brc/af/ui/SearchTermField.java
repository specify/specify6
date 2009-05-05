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
package edu.ku.brc.af.ui;

import org.apache.commons.lang.StringUtils;


/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jun 18, 2008
 *
 */
public class SearchTermField
{
    public static final int NO_OPTIONS      = 0; // Indicates there are no options
    public static final int IS_STRING       = 1; // String Only
    public static final int IS_DATE         = 2; // Is fully
    public static final int IS_YEAR_OF_DATE = 4; // It could be just the year 
    public static final int IS_NUMERIC      = 8; // Is a numeric number
    public static final int HAS_DEC_POINT   = 16; // has a decimal point in the number
    public static final int STARTS_WILDCARD = 32; // starts with wild card
    public static final int ENDS_WILDCARD   = 64; // ends with wild card

    protected String term;
    protected int    options = NO_OPTIONS;
    
    /**
     * @param term
     * @param options
     */
    public SearchTermField(final String term)
    {
        super();
        this.term = StringUtils.replace(term, "'", "''");
        this.options = NO_OPTIONS;
    }
    
    public boolean isSingleChar()
    {
        return term.length() == 1 && term.equals("*");
    }

    public void setTerm(String term)
    {
        this.term = term;
    }

    public String getTerm()
    {
        return term;
    }

   public int getOptions()
    {
        return options;
    }
    
    public void setOption(final int option)
    {
        options |= option;
    }
    
    public boolean isOptionOn(final int opt)
    {

        return (options & opt) == opt;
    }
    
    public boolean isOn(final int option)
    {
        return isOptionOn(options, option);
    }
    
    /**
     * Helper method to see if an option is turned on.
     * @param options the range of options that can be turned on
     * @param opt the actual option that may be turned on
     * @return true if the opt bit is on
     */
    public static boolean isOptionOn(final int options, final int opt)
    {

        return (options & opt) == opt;
    }
}
