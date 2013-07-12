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
package edu.ku.brc.specify.dbsupport;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.specify.dbsupport.customqueries.CustomStatQueries;

/**
 * Factory for creating Custom Specify Queries.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 2, 2007
 *
 */
public class SpecifyCustomQueryFactory extends CustomQueryFactory
{
    private static final Logger  log = Logger.getLogger(SpecifyCustomQueryFactory.class);
    
    /**
     * Constructor.
     */
    public SpecifyCustomQueryFactory()
    {
        // no-op
    }

    /**
     * Returns the singleton instance.
     * @return the singleton instance.
     */
    public static SpecifyCustomQueryFactory getInstance()
    {
        return (SpecifyCustomQueryFactory)CustomQueryFactory.getInstance();
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryFactory#getQuery(java.lang.String)
     */
    @Override
    public CustomQueryIFace getQuery(final String queryName)
    {
        if (queryName.equals("CatalogedLast7Days"))
        {
            return new CustomStatQueries(CustomStatQueries.Type.CatalogedLast7Days);
            
        } else if (queryName.equals("CatalogedLast30Days"))
        {
            return new CustomStatQueries(CustomStatQueries.Type.CatalogedLast30Days);
            
        } else if (queryName.equals("CatalogedLastYear"))
        {
            return new CustomStatQueries(CustomStatQueries.Type.CatalogedLastYear);
            
        } else if (queryName.equals("OverdueLoans"))
        {
            return new CustomStatQueries(CustomStatQueries.Type.OverdueLoans);
            
        } else 
        {
            try
            {
                return (CustomQueryIFace)Class.forName("edu.ku.brc.specify.dbsupport.customqueries." + queryName).newInstance();
                
            } catch(Exception ex)
            {
                log.error(ex);
            }
        }
        return null;
    }

}
