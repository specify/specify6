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

import java.awt.*;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.dbsupport.QueryResultsContainer;
import edu.ku.brc.specify.dbsupport.QueryResultsDataObj;
import edu.ku.brc.specify.dbsupport.QueryResultsListener;
import edu.ku.brc.specify.dbsupport.QueryResultsSerializedGetter;
//import edu.ku.brc.specify.ui.InfiniteProgressPanel;

/**
 * 
 * @author rods
 *
 */
public class StatItem extends JPanel implements QueryResultsListener
{
    public enum VALUE_TYPE {Description, Value, Ignore};
    
    protected String  description;
    protected String  sql;
    
    protected boolean hasStarted = false;
    
    protected Vector<QueryResultsContainer> qrcs       = new Vector<QueryResultsContainer>();
    protected Vector<VALUE_TYPE>            valuesType = new Vector<VALUE_TYPE>();
    
    // UI
    protected JLabel descLabel;
    protected JLabel resultsLabel;
    //protected InfiniteProgressPanel infProgress;
    

    
    /**
     * Constructor for a single statistical item
     * @param description the textual description of the statistic
     */
    public StatItem(final String description)
    {
        this.description = description;
        initUI();
    }

    /**
     * Constructor for a single statistical item
     * @param description the textual description of the statistic
     * @param sql the SQL string that returns a single number
     */
    public StatItem(final String description, final String sql)
    {
        
        this.description = description;
        this.sql         = sql;
        
        initUI();
        
        QueryResultsContainer qrc = new QueryResultsContainer(sql);
        qrc.add(new QueryResultsDataObj(1, 1));
        
        qrcs.addElement(qrc);
        
        valuesType.addElement(VALUE_TYPE.Value);
        
        startUp(); 
    }
    
    protected void initUI()
    {
        setLayout(new BorderLayout());
        
        descLabel    = new JLabel(description, JLabel.LEFT);
        resultsLabel = new JLabel("?", JLabel.RIGHT);
                
        FormLayout      formLayout = new FormLayout("left:p:g,4dlu,right:p:g", "p");
        PanelBuilder    builder    = new PanelBuilder(formLayout);
        CellConstraints cc         = new CellConstraints();
       
        builder.add(descLabel, cc.xy(1,1));
        builder.add(resultsLabel, cc.xy(3,1));
        
        //infProgress = new InfiniteProgressPanel(); 
        //infProgress.setForeground(Color.GRAY);
        //infProgress.setPreferredSize(new Dimension(25,descLabel.getPreferredSize().height));
        //infProgress.setMinimumSize(infProgress.getPreferredSize());
        
        //builder.add(infProgress, cc.xy(3,1));
        //infProgress.start();
        
        
        add(builder.getPanel(), BorderLayout.CENTER);       
    }
    
    /**
     * 
     * @param value
     */
    public void setValueText(final String value)
    {
        resultsLabel.setText(value);
    }
    
    /**
     * 
     * @param sql
     * @param row
     * @param col
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
     * 
     * @param sql
     * @param row
     * @param col
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
     * 
     * @param qrc
     * @param row
     * @param col
     * @param valType
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
        
        QueryResultsSerializedGetter getter = new QueryResultsSerializedGetter(this);
        getter.add(qrcs); // NOTE: this start up the entire process

    }

    /**
     * Tells the UI to re-layout and repaint on UI thread
     *
     */
    protected void refreshUI()
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                invalidate();
                ((StatGroup)getParent()).relayout();
            }
          });
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
                    descLabel.setText(dataObj.toString());
                    
                } else if (valType == VALUE_TYPE.Value)
                {
                    resultsLabel.setText(dataObj.toString());
                    
                } else if (valType == VALUE_TYPE.Ignore)
                {
                    // no-op (Leave this here in case others are added
                }
                inx++;
            }
            list.clear();  
        }
        
        refreshUI();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsListener#resultsInError(edu.ku.brc.specify.dbsupport.QueryResultsContainer)
     */
    public void resultsInError(final QueryResultsContainer qrc)
    {
        resultsLabel.setText("#");
    }    
    
    
}
