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

import java.awt.Color;
import java.util.Vector;

import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.CustomQueryResultsContainer;
import edu.ku.brc.dbsupport.QueryResultsContainer;
import edu.ku.brc.dbsupport.QueryResultsContainerIFace;
import edu.ku.brc.dbsupport.QueryResultsDataObj;
import edu.ku.brc.dbsupport.QueryResultsListener;
import edu.ku.brc.dbsupport.QueryResultsSerializedGetter;
import edu.ku.brc.ui.CommandAction;

/**
 * A Single Statitem that creates a QueryResultsContainer and then gets the result and displays it.
 * It is capable of getting both the descriptive part (label) and the the value.
 
 * @code_status Unknown (auto-generated)
 **
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
    public enum VALUE_TYPE {Description, Value, Ignore}

    protected String     description;
    protected String     sql;
    protected boolean    useProgress        = false;
    
    protected StatGroupTableModel model     = null;
    protected CommandAction       cmdAction = null;
    
    // The Data 
    protected Object value  = "...";
    
    protected boolean hasStarted            = false;
    protected boolean hasData               = false;

    protected Vector<QueryResultsContainerIFace> qrcs       = new Vector<QueryResultsContainerIFace>();
    protected Vector<VALUE_TYPE>                 valuesType = new Vector<VALUE_TYPE>();

     // XXX need to get Colors from L&F
    protected Color      linkColor      = Color.BLUE;
    protected Color      defColor;

    /**
     *  Constructor for a single statistical data item.
     * @param description the textual description of the statistic
     * @param cmdAction the CommandAction to be cloned and sent.
     * @param colId column containing the primary key id
     * @param useProgress use progress indicator
     */
    public StatDataItem(final String        description, 
                        final CommandAction cmdAction, 
                        final boolean       useProgress)
    {
        this.description = description;
        this.cmdAction    = cmdAction;
        this.useProgress = useProgress;
    }

    /**
     * Constructor for a single statistical data item.
     * @param description the textual description of the statistic
     * @param type the type of query "sql" or "hql"
     * @param sql the SQL string that returns a single number
     * @param link the link
     * @param useProgress use progress indicator
     * @param formatStr optional format string (%d or %5.2% etc)
     */
    public StatDataItem(final String description, 
                        final String type, 
                        final String sql, 
                        final CommandAction cmdAction, 
                        final boolean useProgress,
                        final String formatStr)
    {
        this(description, cmdAction, useProgress);
        
        if (type.equals("sql"))
        {
            this.sql = sql;
    
            QueryResultsContainer qrc = new QueryResultsContainer(sql);
            qrc.add(new QueryResultsDataObj(1, 1, formatStr));
            qrcs.addElement(qrc);
    
        } else
        {
            CustomQueryResultsContainer qrc = new CustomQueryResultsContainer(sql);
            qrc.add(new QueryResultsDataObj(1, 1, formatStr));
            qrcs.addElement(qrc);
        }
        
        valuesType.addElement(VALUE_TYPE.Value);     
        startUp();
    }
    
    /**
     * Adds Custom Query for Stat instead of Query String
     * @param customQuery the custom query
     * @param formatStr format
     */
    public void addCustomQuery(final String     customQueryName,
                               final String     formatStr)
    {
        CustomQueryResultsContainer qrc = new CustomQueryResultsContainer(customQueryName);
        qrc.add( new QueryResultsDataObj(1, 1, formatStr));
        valuesType.addElement(VALUE_TYPE.Value);
        qrcs.addElement(qrc);
    }
    
    /**
     * Adds Custom Query for Stat instead of Query String
     * @param customQuery the custom query
     * @param formatStr format
     */
    public void addCustomQuery(final CustomQueryIFace customQuery,
                               final String      formatStr)
    {
        CustomQueryResultsContainer qrc = new CustomQueryResultsContainer(customQuery);
        qrc.add( new QueryResultsDataObj(1, 1, formatStr));
        valuesType.addElement(VALUE_TYPE.Value);
        qrcs.addElement(qrc);
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
     * @param sqlStr the SQl statement
     * @param formatStr optional format string (%d or %5.2% etc)
     * @return Returns a QueryResultsContainer with a single QueryResultsDataObj initialized to row,col
     */
    public QueryResultsContainer add(final String sqlStr,
                                     final String formatStr)
    {
        if (sqlStr == null)
        {
            throw new RuntimeException("sql is null for ["+description+"]");
        }
        QueryResultsContainer qrc = new QueryResultsContainer(sqlStr);
        qrc.add( new QueryResultsDataObj(1, 1, formatStr));
        valuesType.addElement(VALUE_TYPE.Ignore);
        qrcs.addElement(qrc);

        return qrc;
    }

    /**
     * Returns a QueryResultsContainer with a single QueryResultsDataObj initialized to row,col
     * @param sqlStr the SQL to be executed
     * @param row the QueryResultsDataObj row in the resultset
     * @param col the QueryResultsDataObj column in the resultset
     * @param valType whether to ignore the value or indicate it is the description or value
     * @param formatStr optional format string (%d or %5.2% etc)
     * @return Returns a QueryResultsContainer with a single QueryResultsDataObj initialized to row,col
     */
    public QueryResultsContainer add(final String     sqlStr, 
                                     final int        row, 
                                     final int        col, 
                                     final VALUE_TYPE valType,
                                     final String     formatStr)
    {
        if (sqlStr == null)
        {
            throw new RuntimeException("sql is null for ["+description+"]");
        }
        QueryResultsContainer qrc = new QueryResultsContainer(sqlStr);
        qrc.add( new QueryResultsDataObj(row, col, formatStr));
        valuesType.addElement(valType);
        qrcs.addElement(qrc);

        return qrc;
    }

    /**
     * Returns a QueryResultsContainer with a single QueryResultsDataObj initialized to row,col.
     * @param qrc the QueryResultsContainer to be executed
     * @param row the QueryResultsDataObj row in the resultset
     * @param col the QueryResultsDataObj column in the resultset
     * @param valType whether to ignore the value or indicate it is the description or value
     * @param formatStr optional format string (%d or %5.2% etc)
     */
    public void add(final QueryResultsContainer qrc, 
                    final int        row, 
                    final int        col, 
                    final VALUE_TYPE valType,
                    final String     formatStr)
    {
        qrc.add( new QueryResultsDataObj(row, col, formatStr));
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
    
    public boolean shouldShowLinkCursor()
    {
        return cmdAction != null;
    }

    /**
     * @return the cmdAction
     */
    public CommandAction getCmdAction()
    {
        return cmdAction;
    }

    public String getDescription()
    {
        return description;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object valStr)
    {
        this.value = valStr;
    }

    public boolean isUseProgress()
    {
        return useProgress && !hasData;
    }

    //--------------------------------------
    // QueryResultsListener
    //--------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.QueryResultsListener#allResultsBack(edu.ku.brc.dbsupport.QueryResultsContainerIFace)
     */
    public synchronized void allResultsBack(final QueryResultsContainerIFace qrcArg)
    {
        if (qrcs.size() != valuesType.size())
        {
            throw new RuntimeException("There is an unequal number of QRCs and Value Types!["+description+"]");
        }

        Vector<Object> list = new Vector<Object>();
        int inx = 0;
        for (QueryResultsContainerIFace qrc : qrcs)
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
                    description = dataObj != null ?  dataObj.toString() : "";

                } else if (valType == VALUE_TYPE.Value)
                {
                    value = dataObj;

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
    public void resultsInError(final QueryResultsContainerIFace qrc)
    {
        value = "N/A"; // XXX I18N
        model.fireNewData();
        hasData = true;
    }


}
