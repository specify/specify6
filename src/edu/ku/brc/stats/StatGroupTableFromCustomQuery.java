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
package edu.ku.brc.stats;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JLabel;

import com.jgoodies.forms.layout.CellConstraints;

import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.CustomQueryListener;
import edu.ku.brc.ui.CommandAction;

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
public class StatGroupTableFromCustomQuery extends StatGroupTable implements CustomQueryListener
{
    // Static Data Members
    //private static final Logger log = Logger.getLogger(StatGroupTableFromCustomQuery.class);

    // Data Members
    protected String        noResultsMsg;
    protected boolean       hasData       = false;

    /**
     * Constructor that describes where we get everything from.
     * @param name the name or title
     * @param sql the SQL statement to be executed
     * @param descCol the column where the description comes form
     * @param valCol the column where the value comes from
     * @param noResultsMsg the message to display when there is no results
     */
    public StatGroupTableFromCustomQuery(final String   name,
                                         final String[] columnNames,
                                         final String   sql,
                                         final String   noResultsMsg)
    {
        super(name, columnNames);

        this.noResultsMsg = noResultsMsg;

        StatDataItem statItem = new StatDataItem("RetrievingData", null , false);
        model.addDataItem(statItem);

        CustomQueryIFace customQuery = CustomQueryFactory.getInstance().getQuery(sql);
        customQuery.execute(this);

    }

    /**
     * Constructor that describes where we get everything from.
     * @param name the name or title
     * @param jpaQuery the JPA CustomQuery Processor
     * @param descCol the column where the description comes form
     * @param valCol the column where the value comes from
     * @param useSeparator use non-border separator titles
     * @param noResultsMsg the message to display when there is no results
     */
    public StatGroupTableFromCustomQuery(final String      name,
                                         final String[]    columnNames,
                                         final CustomQueryIFace jpaQuery,
                                         boolean           useSeparator,
                                         final String      noResultsMsg)
    {
        super(name, columnNames);

        this.noResultsMsg = noResultsMsg;

        StatDataItem statItem = new StatDataItem("RetrievingData", null , false);
        model.addDataItem(statItem);

        jpaQuery.execute(this);

    }

    /**
     * Constructor that describes where we get everything from.
     * @param name the name or title
     * @param sql the SQL statement to be executed
     * @param descCol the column where the description comes form
     * @param valCol the column where the value comes from
     * @param useSeparator use non-border separator titles
     * @param noResultsMsg the message to display when there is no results
     */
    public StatGroupTableFromCustomQuery(final String   name,
                                         final String[] columnNames,
                                         final String   sql,
                                         boolean        useSeparator,
                                         final String   noResultsMsg)
    {
        super(name, columnNames, useSeparator, 100); // this is an arbitrary number only to tell it to make scrollbars

        this.noResultsMsg = noResultsMsg;

        StatDataItem statItem = new StatDataItem("RetrievingData", null , false);
        model.addDataItem(statItem);

        CustomQueryIFace customQuery = CustomQueryFactory.getInstance().getQuery(sql);
        customQuery.execute(this);
    }
    

    /**
     * Sets info needed to send commands
     * @param commandAction the command to be cloned and sent
     * @param colId the column of the id which is used to build the link
     */
    public void setCommandAction(final CommandAction commandAction, 
                                 final int           colId)
    {
        this.cmdAction = commandAction;
        this.colId     = colId;
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
    //-- CustomQueryListener
    //-----------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#exectionDone(edu.ku.brc.dbsupport.CustomQuery)
     */
    public synchronized void exectionDone(final CustomQueryIFace customQuery)
    {
        model.clear();
        hasData = true;

        List<?> results = customQuery.getDataObjects();

        if (results != null && results.size() > 0)
        {
            for (int i=0;i<results.size();i++)
            {
                String desc     = results.get(i++).toString();
                Object val      = results.get(i++);
                Object colIdObj = results.get(i);
                
                StatDataItem statItem = new StatDataItem(desc, createCommandAction(colIdObj), false);
                statItem.setValue(val);
                model.addDataItem(statItem);
            }
            results.clear();
            
        } else
        {
            addNoneAvailableMsg(noResultsMsg);
        }
        model.fireNewData();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#executionError(edu.ku.brc.dbsupport.CustomQuery)
     */
    public synchronized void executionError(CustomQueryIFace customQuery)
    {
        addNoneAvailableMsg(getResourceString("GetStatsError"));
        
    }
    
    



}
