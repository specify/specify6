package edu.ku.brc.specify.stats;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.dbsupport.SQLExecutionListener;
import edu.ku.brc.specify.dbsupport.SQLExecutionProcessor;

public class StatGroupFromQuery extends StatGroup  implements SQLExecutionListener
{
    // Static Data Members
    private static Log log = LogFactory.getLog(StatGroupFromQuery.class);
    
    // Data Members
    protected SQLExecutionProcessor sqle;
    
    protected int descCol;
    protected int valCol;
    
    
    public StatGroupFromQuery(final String name, final String sql, final int descCol, final int valCol)
    {
        super(name);
        
        this.descCol = descCol;
        this.valCol  = valCol;
        
        sqle = new SQLExecutionProcessor(this, sql);
        sqle.start();
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
                
                FormLayout      formLayout = new FormLayout("f:p,15dlu,f:p", rowsDef.toString());
                PanelBuilder    builder    = new PanelBuilder(formLayout);
                CellConstraints cc         = new CellConstraints();
                
                for (int i=0;i<data.size();i++)
                {
                    StatItem statItem = new StatItem(data.get(i++).toString());
                    statItem.setValueText(data.get(i).toString());
                    addItem(statItem);
                    statItem.refreshUI();
                }    
                data.clear();
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
        
        //hasFailed = true;   
    }


}
