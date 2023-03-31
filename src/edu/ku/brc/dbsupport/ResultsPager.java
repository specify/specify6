/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.dbsupport;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;

/**
 * Class that enables a consumer (usually a form) to easily page through a set of resultsList.
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class ResultsPager 
{
    
    private List<?> resultsList;
    private int     pageSize;
    private int     page;
    private Query   query;
    private boolean onLastPage = false;
    
    /**
     * @param query The hibernate Query object defining what will come back
     * @param page the intial starting page
     * @param pageSize the number of records per page
     */
    public ResultsPager(final Query query, final int page, final int pageSize) 
    {
        
        this.page     = page;
        this.pageSize = pageSize;
        this.query    = query;
        //resultsList   = query.setFirstResult(page * pageSize).setMaxResults(pageSize+1).list();
    
    }
    
    /**
     * @param criteria The hibernate Criteria object defining what will come back
     * @param page the intial starting page
     * @param pageSize the number of records per page
     */
    public ResultsPager(final Criteria criteria, final int page, final int pageSize) 
    {
        
        this.page     = page;
        this.pageSize = pageSize;
        //resultsList   = criteria.setFirstResult(page * pageSize).setMaxResults(pageSize+1).list();
    
    }
    
    /**
     * Returns whether there is a next page or not
     * @return whether there is a next page or not
     */
    public boolean isNextPage() 
    {
        return !onLastPage;
    }
    
    /**
     * Returns whether there is a previous page or not
     * @return whether there is a previous page or not
     */
    public boolean isPreviousPage() 
    {
        return page > 0;
    }
    
    /**
     * Returns a list of objects for the page,
     * (Note: the number of objects returned maybe less than the number defined for the page)
     * @return a list of objects for the page
     */
    public List<?> getList() 
    {
        if (isNextPage())
        {
            resultsList  = query.setFirstResult(page * pageSize).setMaxResults(pageSize+1).list();
            onLastPage = resultsList.size() < pageSize;
            page++;
            
        } else
        {
            resultsList.clear();
        }
        return resultsList;
        //return isNextPage() ? resultsList.subList(0, pageSize-1) : resultsList;
    }

 }
