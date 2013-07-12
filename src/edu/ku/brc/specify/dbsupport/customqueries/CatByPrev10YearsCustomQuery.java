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
package edu.ku.brc.specify.dbsupport.customqueries;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.NotImplementedException;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.CustomQueryListener;
import edu.ku.brc.dbsupport.QueryResultsContainer;
import edu.ku.brc.dbsupport.QueryResultsContainerIFace;
import edu.ku.brc.dbsupport.QueryResultsDataObj;

/**
 * Custom Query for calculating the how many items were calculated for each year
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class CatByPrev10YearsCustomQuery implements CustomQueryIFace
{

    public CatByPrev10YearsCustomQuery()
    {
    }

    /**
     * @param year
     * @param inc
     * @return
     */
    protected String createTitle(final int year, final int inc)
    {
        return year + " - " + (year - inc);
    }

    //-------------------------------------------
    // CustomQuery Interface
    //-------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#execute()
     */
    public boolean execute()
    {
        throw new NotImplementedException("execute is not implemented!");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getResults()
     */
    public List<QueryResultsDataObj> getResults()
    {
        throw new NotImplementedException("getResults is not implemented!");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#execute(edu.ku.brc.dbsupport.CustomQueryListener)
     */
    public void execute(final CustomQueryListener cql)
    {
        throw new NotImplementedException();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getDataObjects()
     */
    public List<?> getDataObjects()
    {
        throw new NotImplementedException();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getQueryDefinition()
     */
    public List<QueryResultsContainerIFace> getQueryDefinition()
    {
        int inc  = 5;
        int year = Calendar.getInstance().get(Calendar.YEAR) - 1;

        Vector<QueryResultsContainerIFace> list = new Vector<QueryResultsContainerIFace>();
        QueryResultsContainer ndbrc;

        for (int i=0;i<10;i++)
        {
            String sql = QueryAdjusterForDomain.getInstance().adjustSQL("SELECT count(*) FROM collectionobject WHERE CollecionMemberID = COLMEMID AND YEAR(CatalogedDate) = "+year);
            ndbrc = new QueryResultsContainer(sql);
            ndbrc.add(new QueryResultsDataObj(createTitle(year, inc)));
            ndbrc.add(new QueryResultsDataObj(1, 1));
            list.addElement(ndbrc);
            year -= inc;
        }
        return list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#getTableIds()
     */
    @Override
    public List<Integer> getTableIds()
    {
        Vector<Integer> list = new Vector<Integer>();
        Collections.addAll(list, new Integer[] {1});
        return list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getName()
     */
    public String getName()
    {
        // TODO Auto-generated method stub
        return getClass().getSimpleName();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#isInError()
     */
    //@Override
    public boolean isInError()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#isCancelled()
     */
    //@Override
    public boolean isCancelled()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#cancel()
     */
    //@Override
    public void cancel()
    {
        // ignore
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.dbsupport.CustomQueryIFace#getMaxResults()
	 */
	@Override
	public int getMaxResults() 
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.dbsupport.CustomQueryIFace#setMaxResults(int)
	 */
	@Override
	public void setMaxResults(int maxResults)
	{
		// ignore
	}
   
    
}
