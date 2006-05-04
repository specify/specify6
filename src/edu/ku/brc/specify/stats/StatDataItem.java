/* Filename:    $RCSfile: StatItem.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.stats;

import java.awt.Color;
import java.util.Vector;

import edu.ku.brc.specify.dbsupport.QueryResultsContainer;
import edu.ku.brc.specify.dbsupport.QueryResultsDataObj;
import edu.ku.brc.specify.dbsupport.QueryResultsListener;
import edu.ku.brc.specify.dbsupport.QueryResultsSerializedGetter;

/**
 * A Single Statitem that creates a QueryResultsContainer and then gets the result and displays it.
 * It is capable of getting both the descriptive part (label) and the the value.
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class StatDataItem implements QueryResultsListener
{
    /**
     * Describes which part of the UI an individual query results data object will be placed.
     * The "Description" is the right side, the "Value" is the left side, and it can be asked to be ignored
     */
    public enum VALUE_TYPE {Description, Value, Ignore};

    protected String     description;
    protected String     sql;
    protected String     link        = null;
    protected boolean    useProgress = false;
    
    protected StatGroupTableModel model       = null;
    
    // The Data 
    protected String valStr  = "...";
    
    protected boolean hasStarted  = false;
    protected boolean hasData     = false;

    protected Vector<QueryResultsContainer> qrcs       = new Vector<QueryResultsContainer>();
    protected Vector<VALUE_TYPE>            valuesType = new Vector<VALUE_TYPE>();

     // XXX need to get Colors from L&F
    protected Color      linkColor = Color.BLUE;
    protected Color      defColor;

    /* Constructor for a single statistical data item
     * @param description the textual description of the statistic
     * @param link
     * @param useProgress
     */
    public StatDataItem(final String description, final String link, final boolean useProgress)
    {
        this.description = description;
        this.useProgress = useProgress;
        this.link        = link;
    }

    /**
     * Constructor for a single statistical data item
     * @param description the textual description of the statistic
     * @param sql the SQL string that returns a single number
     * @param link
     * @param useProgress
     */
    public StatDataItem(final String description, final String sql, final String link, final boolean useProgress)
    {
        this(description, link, useProgress);
        this.sql = sql;

        QueryResultsContainer qrc = new QueryResultsContainer(sql);
        qrc.add(new QueryResultsDataObj(1, 1));

        qrcs.addElement(qrc);

        valuesType.addElement(VALUE_TYPE.Value);

        startUp();
    }
    
    
    /**
     * Clear the internal data and relationships 
     */
    public void clear()
    {
        qrcs.clear();
        qrcs = null;
        
        valuesType.clear();
        valuesType = null;
        
        model = null;
    }
    
    /**
     * Sets the TableModel into the item so it can notifiy it when the data has arrived
     * @param model the table model to be notified
     */
    protected void setTableModel(final StatGroupTableModel model)
    {
        this.model = model;
    }

    /**
     * Returns a QueryResultsContainer with a single QueryResultsDataObj initialized to 1,1
     * @param sql the SQl statement
     * @return Returns a QueryResultsContainer with a single QueryResultsDataObj initialized to row,col
     */
    public QueryResultsContainer add(final String sql)
    {
        if (sql == null)
        {
            throw new RuntimeException("sql is null for ["+description+"]");
        }
        QueryResultsContainer qrc = new QueryResultsContainer(sql);
        qrc.add( new QueryResultsDataObj(1, 1));
        valuesType.addElement(VALUE_TYPE.Ignore);
        qrcs.addElement(qrc);

        return qrc;
    }

    /**
     * Returns a QueryResultsContainer with a single QueryResultsDataObj initialized to row,col
     * @param sql the SQL to be executed
     * @param row the QueryResultsDataObj row in the resultset
     * @param col the QueryResultsDataObj column in the resultset
     * @param valType whether to ignore the value or indicate it is the description or value
     * @return Returns a QueryResultsContainer with a single QueryResultsDataObj initialized to row,col
     */
    public QueryResultsContainer add(final String sql, final int row, final int col, final VALUE_TYPE valType)
    {
        if (sql == null)
        {
            throw new RuntimeException("sql is null for ["+description+"]");
        }
        QueryResultsContainer qrc = new QueryResultsContainer(sql);
        qrc.add( new QueryResultsDataObj(row, col));
        valuesType.addElement(valType);
        qrcs.addElement(qrc);

        return qrc;
    }

    /**
     * Returns a QueryResultsContainer with a single QueryResultsDataObj initialized to row,col
     * @param qrc the QueryResultsContainer to be executed
     * @param row the QueryResultsDataObj row in the resultset
     * @param col the QueryResultsDataObj column in the resultset
     * @param valType whether to ignore the value or indicate it is the description or value
     */
    public void add(final QueryResultsContainer qrc, final int row, final int col, final VALUE_TYPE valType)
    {
        qrc.add( new QueryResultsDataObj(row, col));
    }

    /**
     * Creates a SQLExecutionProcessor to go get the statisitic
     *
     */
    public void startUp()
    {
        if (hasStarted)
        {
            throw new RuntimeException("The execution has already been started!");
        }

        hasStarted = true;
        hasData    = false;


        QueryResultsSerializedGetter getter = new QueryResultsSerializedGetter(this);
        getter.add(qrcs); // NOTE: this start up the entire process

    }

    /**
     * Returns the link string
     * @return Returns the link string
     */
    public String getLink()
    {
        return link;
    }

    public String getDescription()
    {
        return description;
    }

    public String getValStr()
    {
        return valStr;
    }

    public void setValStr(String valStr)
    {
        this.valStr = valStr;
    }

    public boolean isUseProgress()
    {
        if (useProgress) System.out.println(hasData);
        return useProgress && !hasData;
    }
    
    

    //--------------------------------------
    // QueryResultsListener
    //--------------------------------------
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsListener#allResultsBack()
     */
    public synchronized void allResultsBack()
    {
        if (qrcs.size() != valuesType.size())
        {
            throw new RuntimeException("There is an unequal number of QRCs and Value Types!["+description+"]");
        }

        Vector<Object> list = new Vector<Object>();
        int inx = 0;
        for (QueryResultsContainer qrc : qrcs)
        {
            for (QueryResultsDataObj qrcdo : qrc.getQueryResultsDataObjs())
            {
                Object     dataObj = qrcdo.getResult();
                VALUE_TYPE valType = valuesType.elementAt(inx);
                if (dataObj == null && valType != VALUE_TYPE.Ignore)
                {
                    throw new RuntimeException("Null data that isn't Ignore");
                } else if (valType == VALUE_TYPE.Description)
                {
                    description = dataObj.toString();

                } else if (valType == VALUE_TYPE.Value)
                {
                    valStr = dataObj.toString();

                } else if (valType == VALUE_TYPE.Ignore)
                {
                    // no-op (Leave this here in case others are added
                }
                inx++;
            }
            list.clear();
        }
        model.fireNewData();
        hasData = true;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsListener#resultsInError(edu.ku.brc.specify.dbsupport.QueryResultsContainer)
     */
    public void resultsInError(final QueryResultsContainer qrc)
    {
        valStr = "N/A"; // XXX I18N
        model.fireNewData();
        hasData = true;
    }


}
