package edu.ku.brc.stats;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JLabel;

import com.jgoodies.forms.layout.CellConstraints;

import edu.ku.brc.dbsupport.CustomQuery;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.CustomQueryListener;

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
    protected String  linkStr       = null;
    protected int     colId         = -1;
    protected String  noResultsMsg;
    protected boolean hasData       = false;

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

        CustomQuery customQuery = CustomQueryFactory.getInstance().getQuery(sql);
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
                                         final CustomQuery jpaQuery,
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

        CustomQuery customQuery = CustomQueryFactory.getInstance().getQuery(sql);
        customQuery.execute(this);
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
     * Sets info need to make links
     * @param linkStr the name of the static link
     * @param colId the column of the id which is used to build the link
     */
    public void setLinkInfo(final String linkStr, final int colId)
    {
        this.linkStr = linkStr;
        this.colId   = colId;
    }

     /**
     * Removes the table and adds the None Available message
     * @param msg the message to be displayed
     */
    protected void addNoneAvailableMsg(final String msg)
    {
        JLabel label = new JLabel(noResultsMsg != null ? noResultsMsg : getResourceString("NoneAvail"));
        
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
    public void exectionDone(final CustomQuery customQuery)
    {
        model.clear();
        hasData = true;

        List<?> results = customQuery.getResults();

        if (results != null && results.size() > 0)
        {
            for (int i=0;i<results.size();i++)
            {
                String desc     = results.get(i++).toString();
                Object val      = results.get(i++);
                Object colIdObj = results.get(i);
                String columnId = colIdObj != null ? colIdObj.toString() : null;
                
                StatDataItem statItem = new StatDataItem(desc, linkStr == null || columnId == null ? null : (linkStr+",id="+columnId), false);
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
    public void executionError(CustomQuery customQuery)
    {
        addNoneAvailableMsg(getResourceString("GetStatsError"));
        
    }
    
    



}
