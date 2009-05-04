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
package edu.ku.brc.af.tasks.subpane;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.dbsupport.QueryResultsContainerIFace;
import edu.ku.brc.dbsupport.QueryResultsHandlerIFace;
import edu.ku.brc.dbsupport.QueryResultsListener;
import edu.ku.brc.dbsupport.QueryResultsProcessable;

/**
 * This base class implements the Chartable interface 
 * that enables derived classes to easily accept and have access to information needed to decorate or describe the chart.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class ChartPane extends BaseSubPane implements Chartable, QueryResultsProcessable, QueryResultsListener
{
    // Static Data Members
    //private static final Logger log = Logger.getLogger(ChartPane.class);
    
    // Data Members
    protected String  title       = ""; //$NON-NLS-1$
    protected String  xAxisTitle  = ""; //$NON-NLS-1$
    protected String  yAxisTitle  = ""; //$NON-NLS-1$
    protected boolean isVertical  = true;
    
    /**
     * A generic Chart Pane contructor.
     * @param name the name of the subpane
     * @param task the owning task
     */
    public ChartPane(final String name, 
                     final Taskable task)
    {
        super(name, task);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#getName()
     */
    @Override
    public String getName()
    {
        return isNotEmpty(title) ? title : super.getName();
    }
       
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.Chartable#setTitle(java.lang.String)
     */
    public void setTitle(final String title)
    {
        this.title = title;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.Chartable#setXAxis(java.lang.String)
     */
    public void setXAxis(final String title)
    {
        xAxisTitle = title;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.Chartable#setYAxis(java.lang.String)
     */
    public void setYAxis(final String title)
    {
       yAxisTitle = title;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.Chartable#setVertical(boolean)
     */
    public void setVertical(boolean isVertical)
    {
        this.isVertical = isVertical;
    }
    
    //--------------------------------------
    // QueryResultsProcessable
    //--------------------------------------

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.af.dbsupport.QueryResultsProcessable#setHandler()
     */
    public void setHandler(final QueryResultsHandlerIFace handler)
    {
        throw new RuntimeException("Not Implemented."); //$NON-NLS-1$
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.af.dbsupport.QueryResultsProcessable#getHandler()
     */
    public QueryResultsHandlerIFace getHandler()
    {
        throw new RuntimeException("Not Implemented."); //$NON-NLS-1$
    }
    
    //--------------------------------------
    // QueryResultsListener
    //--------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.QueryResultsListener#allResultsBack(edu.ku.brc.dbsupport.QueryResultsContainerIFace)
     */
    public synchronized void allResultsBack(final QueryResultsContainerIFace qrc)
    {
        throw new RuntimeException("Not Implemented."); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.dbsupport.QueryResultsListener#resultsInError(edu.ku.brc.af.dbsupport.QueryResultsContainer)
     */
    public void resultsInError(final QueryResultsContainerIFace qrc)
    {
        // do nothing it ok if it isn't implemented
    }
}
