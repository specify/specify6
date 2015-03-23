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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.ui.DateParser;
import edu.ku.brc.ui.DateWrapper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * May 6, 2008
 *
 */
public class ESTermParser implements SearchTermParserIFace
{
    private static final ESTermParser instance = new ESTermParser();
    
    protected DateWrapper             scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
    protected static SimpleDateFormat dbDateFormat  = new SimpleDateFormat("yyyy-MM-dd");
    
    protected List<SearchTermField> fields = new ArrayList<SearchTermField>(5);
    
    
    /**
     * 
     */
    protected ESTermParser()
    {
        
    }
    
    /**
     * @return
     */
    public static ESTermParser getInstance()
    {
        return instance;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.SearchTermParserIFace#getFields()
     */
    @Override
    public List<SearchTermField> getFields()
    {
        return instance.fields;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.SearchTermParserIFace#parse(java.lang.String, boolean)
     */
    @Override
    public boolean parse(final String searchTermArg, final boolean parseAsSingleTerm)
    {
        instance.fields.clear();
        
        //DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
        int        currentYear = Calendar.getInstance().get(Calendar.YEAR); 
        String     searchTerm  = searchTermArg;
        DateParser dd          = new DateParser(instance.scrDateFormat.getSimpleDateFormat().toPattern());
        
        //----------------------------------------------------------------------------------------------
        // NOTE: If a full date was type in and it was parsed as such
        // and it couldn't be something else, then it only searches date fields.
        //----------------------------------------------------------------------------------------------
        
        int cnt = 0;
        
        if (searchTerm.length() > 0)
        {
            if (StringUtils.contains(searchTerm, '\\'))
            {
                return false;
            }
            
            String[] terms;
            
            boolean startWith = searchTerm.startsWith("*");
            boolean endsWith  = searchTerm.endsWith("*");

            searchTerm = StringUtils.remove(searchTerm, '*');
            
            if (searchTerm.startsWith("\"") || searchTerm.startsWith("'") || searchTerm.startsWith("`"))
            {
                searchTerm = StringUtils.stripStart(searchTerm, "\"'`");
                searchTerm = StringUtils.stripEnd(searchTerm, "\"'`");
                terms = new String[] {searchTerm};
                
            } else if (parseAsSingleTerm)
            {
                terms = new String[] {searchTerm};
                
            } else
            {
                terms = StringUtils.split(searchTerm, ' ');
            }
            
            if (terms.length == 1)
            {
                terms[0] = (startWith ? "*" : "") + terms[0] + (endsWith ? "*" : "");
            } else
            {
                 
                terms[0] = (startWith ? "*" : "") + terms[0];
                terms[terms.length-1] = terms[terms.length-1] + (endsWith ? "*" : ""); 
            }

            for (String term : terms)
            {
                if (StringUtils.isEmpty(term))
                {
                    continue;
                }
                
                SearchTermField stf = new SearchTermField(term);
                
                if (stf.isSingleChar())
                {
                    return false;
                }
                instance.fields.add(stf);

                cnt += !stf.isSingleChar() ? 1 : 0;

                //log.debug(term);
                String  termStr = term;
                
                if (termStr.startsWith("*"))
                {
                    stf.setOption(SearchTermField.STARTS_WILDCARD);
                    termStr = termStr.substring(1);
                    stf.setTerm(termStr);
                }
                
                if (termStr.endsWith("*"))
                {
                    stf.setOption(SearchTermField.ENDS_WILDCARD);
                    termStr = termStr.substring(0, termStr.length()-1);
                    stf.setTerm(termStr);
                }
                
                // First check to see if it is all numeric.
                if (StringUtils.isNumeric(termStr))
                {
                    stf.setOption(SearchTermField.IS_NUMERIC);
                    if (StringUtils.contains(termStr, '.'))
                    {
                        stf.setOption(SearchTermField.HAS_DEC_POINT);
                    }
                            
                    if (!stf.isOn(SearchTermField.HAS_DEC_POINT) && termStr.length() == 4)
                    {
                        int year = Integer.parseInt(termStr);
                        if (year > 1000 && year <= currentYear)
                        {
                            stf.setOption(SearchTermField.IS_YEAR_OF_DATE);
                        }
                    }
                } else
                {
                    // Check to see if it is date
                    Date searchDate = dd.parseDate(searchTermArg);
                    if (searchDate != null)
                    {
                        try
                        {
                            termStr = dbDateFormat.format(searchDate);
                            stf.setTerm(termStr);
                            stf.setOption(SearchTermField.IS_DATE);
                            
                        } catch (Exception ex)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ESTermParser.class, ex);
                            // should never get here
                        }
                    }
                }
            }
        }
        
        return instance.fields.size() > 0 && cnt > 0;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.SearchTermParserIFace#createWhereClause(edu.ku.brc.af.core.expresssearch.SearchTermField, java.lang.String, java.lang.String)
     */
    @Override
    public String createWhereClause(final SearchTermField term,
                                    final String abbrevArg, 
                                    final String fieldName)
    {
        String abbrev = StringUtils.isNotEmpty(abbrevArg) ? (abbrevArg + '.') : "";
        
        boolean startWildCard = term.isOn(SearchTermField.STARTS_WILDCARD);
        boolean endWildCard   = term.isOn(SearchTermField.ENDS_WILDCARD);
        if (startWildCard || endWildCard)
        {
            return "LOWER(" + abbrev + fieldName + ") LIKE " + (startWildCard ? "'%" : "'") + term.getTermLowerCase() + (endWildCard ? "%'" : "'");
        }
        return "LOWER(" + abbrev + fieldName + ") = " + "'" + term.getTermLowerCase() + "'";
    }
    
}
