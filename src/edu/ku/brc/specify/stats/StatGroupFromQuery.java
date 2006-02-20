package edu.ku.brc.specify.stats;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.dbsupport.SQLExecutionListener;
import edu.ku.brc.specify.dbsupport.SQLExecutionProcessor;

/**
 *
 * Class to create an entire group from a single query.
 * Groups are typically made up of individual StatItems where each statistic requires it's
 * own query and the usually just the right hand side comes from the query, although the
 * description part can come from the query also. With this class you describe whcih columns in the resultset
 * that the description and value (left and right) comes from.
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class StatGroupFromQuery extends StatGroup  implements SQLExecutionListener
{
    // Static Data Members
    private static Log log = LogFactory.getLog(StatGroupFromQuery.class);

    // Data Members
    protected SQLExecutionProcessor sqle;

    protected int    descCol;
    protected int    valCol;
    protected String linkStr = null;
    protected int    colId   = -1;

    /**
     * Constructor that describes where we get everything from
     * @param name the name or title
     * @param sql the SQL statement to be executed
     * @param descCol the column where the description comes form
     * @param valCol the column where the value comes from
     */
    public StatGroupFromQuery(final String name,
                              final String sql,
                              final int descCol,
                              final int valCol)
    {
        super(name);

        this.descCol = descCol;
        this.valCol  = valCol;

        sqle = new SQLExecutionProcessor(this, sql);
        sqle.start();
    }

    /**
     * Constructor that describes where we get everything from
     * @param name the name or title
     * @param sql the SQL statement to be executed
     * @param descCol the column where the description comes form
     * @param valCol the column where the value comes from
     * @param useSeparator use non-border separator titles
     */
    public StatGroupFromQuery(final String name,
                              final String sql,
                              final int descCol,
                              final int valCol,
                              boolean useSeparator)
    {
        super(name, useSeparator);

        this.descCol = descCol;
        this.valCol  = valCol;

        sqle = new SQLExecutionProcessor(this, sql);
        sqle.start();
    }

    /**
     * Sets info need to make links
     * @param linkStr the name of the static link
     * @param idCol the column of the id which is used to build the link
     */
    public void setLinkInfo(final String linkStr, final int colId)
    {
        this.linkStr = linkStr;
        this.colId = colId;
    }

    //-----------------------------------------------------
    //-- SQLExecutionListener
    //-----------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.specify.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
     */
    public synchronized void exectionDone(final SQLExecutionProcessor processor, final java.sql.ResultSet resultSet)
    {

        List<Object> data = new Vector<Object>();
        try
        {
            if (resultSet.first())
            {
                StringBuffer rowsDef = new StringBuffer();
                do
                {
                    data.add(resultSet.getObject(descCol));
                    data.add(resultSet.getObject(valCol));
                    if (rowsDef.length() > 0)
                    {
                        rowsDef.append(",15dlu,");
                    }
                    rowsDef.append("top:p");

                } while (resultSet.next());

                for (int i=0;i<data.size();i++)
                {
                    StatItem statItem = new StatItem(data.get(i++).toString(), linkStr == null ? null : (linkStr+",id="+colId), false);
                    statItem.setValueText(data.get(i).toString());
                    addItem(statItem);
                    statItem.refreshUI();
                }
                data.clear();
            } else
            {
                StatItem statItem = new StatItem(getResourceString("NoneAvail"), null, false);
                statItem.setValueText(" ");
                addItem(statItem);
                statItem.refreshUI();
            }
        } catch (Exception ex)
        {
            log.error(ex);
        }

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.SQLExecutionListener#executionError(edu.ku.brc.specify.dbsupport.SQLExecutionProcessor, java.lang.Exception)
     */
    public synchronized void executionError(final SQLExecutionProcessor processor, final Exception ex)
    {
        StatItem statItem = new StatItem(getResourceString("NoneAvail"), null, false);
        statItem.setValueText(" ");
        addItem(statItem);
        statItem.refreshUI();
    }


}
