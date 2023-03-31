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
package edu.ku.brc.stats;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.layout.CellConstraints;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.SQLExecutionListener;
import edu.ku.brc.dbsupport.SQLExecutionProcessor;

/**
 *
 * Class to create an entire group from a single query.
 * Groups are typically made up of individual StatItems where each statistic requires it's
 * own query and the usually just the right hand side comes from the query, although the
 * description part can come from the query also. With this class you describe whcih columns in the resultset
 * that the description and value (left and right) comes from.
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class StatGroupTableFromQuery extends StatGroupTable implements SQLExecutionListener
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(StatGroupTableFromQuery.class);

    // Data Members
    protected SQLExecutionProcessor sqle;

    protected int           descCol;
    protected int           valCol;
    protected String        noResultsMsg;
    protected boolean       hasData       = false;

    /**
     * Constructor that describes where we get everything from
     * @param name the name or title
     * @param sql the SQL statement to be executed
     * @param descCol the column where the description comes form
     * @param valCol the column where the value comes from
     * @param useSeparator use non-border separator titles
     */
    public StatGroupTableFromQuery(final String   name,
                                   final String[] columnNames,
                                   final String   sql,
                                   final int      descCol,
                                   final int      valCol,
                                   boolean        useSeparator,
                                   final String   noResultsMsg)
    {
        super(name, columnNames, useSeparator, 100); // this is an arbitrary number only to tell it to make scrollbars

        this.descCol      = descCol;
        this.valCol       = valCol;
        this.noResultsMsg = noResultsMsg;

        StatDataItem statItem = new StatDataItem("RetrievingData", null, false);
        model.addDataItem(statItem);
        
        String adjustedSQL = QueryAdjusterForDomain.getInstance().adjustSQL(sql);

        sqle = new SQLExecutionProcessor(this, adjustedSQL);
        sqle.start();
    }

    /**
     * Requests that all the data be reloaded (Not implemented yet)
     */
    public void reloadData()
    {

    }

    /* (non-Javadoc)
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize()
    {
        // this is needed to the box isn't huge before it has data
        return hasData ? super.getPreferredSize() : new Dimension(100,100);
    }


     /**
     * Removes the table and adds the None Available message
     * @param msg the message to be displayed
     */
    protected void addNoneAvailableMsg(final String msg)
    {
        JLabel label = createLabel(noResultsMsg != null ? noResultsMsg : getResourceString("NoneAvail"));

        if (useSeparator)
        {
            builder.getPanel().remove(scrollPane != null ? scrollPane : table);
            builder.add(label, new CellConstraints().xy(1,2));

        } else
        {
            remove(scrollPane != null ? scrollPane : table);
            add(label, BorderLayout.CENTER);
         }
    }

    //-----------------------------------------------------
    //-- SQLExecutionListener
    //-----------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.specify.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
     */
    public synchronized void exectionDone(final SQLExecutionProcessor processor, 
                                          final java.sql.ResultSet resultSet)
    {
        model.clear();
        hasData = true;

        List<Object> data = new Vector<Object>();
        try
        {
            if (resultSet.next())
            {
                do
                {
                    if (descCol != -1)
                    {
                        data.add(resultSet.getObject(descCol));
                    }
                    data.add(resultSet.getObject(valCol));
                    data.add(colId > 0 ? resultSet.getObject(colId) : null);

                } while (resultSet.next());


                for (int i=0;i<data.size();i++)
                {
                    String desc     = descCol != -1 ? data.get(i++).toString() : "";
                    Object val      = data.get(i++);
                    Object colIdObj = data.get(i);
                    
                    StatDataItem statItem = new StatDataItem(desc, createCommandAction(colIdObj), false);
                    model.addDataItem(statItem);
                    statItem.setValue(val);
                }
                data.clear();
            } else
            {
                addNoneAvailableMsg(noResultsMsg);
            }
            model.fireNewData();

        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(StatGroupTableFromQuery.class, ex);
            log.error(ex);
            ex.printStackTrace();
        }

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.SQLExecutionListener#executionError(edu.ku.brc.specify.dbsupport.SQLExecutionProcessor, java.lang.Exception)
     */
    public synchronized void executionError(final SQLExecutionProcessor processor, final Exception ex)
    {
        addNoneAvailableMsg(getResourceString("GetStatsError"));

    }


}
