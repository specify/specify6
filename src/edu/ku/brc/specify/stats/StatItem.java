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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.core.ContextMgr;
import edu.ku.brc.specify.core.StatsTask;
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
public class StatItem extends JPanel implements QueryResultsListener
{
    /**
     * Describes which part of the UI an individual query results data object will be placed.
     * The "Description" is the right side, the "Value" is the left side, and it can be asked to be ignored
     */
    public enum VALUE_TYPE {Description, Value, Ignore};
    
    protected String  description;
    protected String  sql;
    protected String  link       = null;
    
    protected boolean hasStarted = false;
    
    protected Vector<QueryResultsContainer> qrcs       = new Vector<QueryResultsContainer>();
    protected Vector<VALUE_TYPE>            valuesType = new Vector<VALUE_TYPE>();
    
    // UI
    protected JLabel  descLabel     = null;
    protected JButton descBtn       = null;
    
    protected JLabel  resultsLabel;
    //protected InfiniteProgressPanel infProgress;

    // XXX need to get Colors from L&F
    protected Color      linkColor = Color.BLUE;
    protected Color      defColor;

    protected static final Cursor handCursor    = new Cursor(Cursor.HAND_CURSOR);
    protected static final Cursor defCursor     = new Cursor(Cursor.DEFAULT_CURSOR);


    
    /**
     * Constructor for a single statistical item
     * @param description the textual description of the statistic
     */
    public StatItem(final String description, final String link)
    {
        this.description = description;
        this.link        = link;
        initUI();
    }

    /**
     * Constructor for a single statistical item
     * @param description the textual description of the statistic
     * @param sql the SQL string that returns a single number
     */
    public StatItem(final String description, final String sql, final String link)
    {
        
        this.description = description;
        this.sql         = sql;
        this.link        = link;
        
        initUI();
        
        QueryResultsContainer qrc = new QueryResultsContainer(sql);
        qrc.add(new QueryResultsDataObj(1, 1));
        
        qrcs.addElement(qrc);
        
        valuesType.addElement(VALUE_TYPE.Value);
        
        startUp(); 
    }
    
    /**
     * Creates the two labels and lays them out left and right
     *
     */
    protected void initUI()
    {
        setLayout(new BorderLayout());
        
        JComponent descComp;
        if (link != null)
        {
            descBtn    = new JButton(description);
            descBtn.setBorder(new EmptyBorder(1,1,1,1));
            descBtn.setCursor(handCursor);
            descBtn.setForeground(linkColor);
            
            descBtn.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            StatsTask statTask = (StatsTask)ContextMgr.getInstance().getTaskByName(StatsTask.STATISTICS);
                            statTask.createStatPane(link);
                        }
                    }); 
            
            /*MouseInputAdapter mouseMotionListener = new MouseInputAdapter() {
                public void mouseEntered(MouseEvent e) 
                {
                    defColor = getForeground();
                    descBtn.setForeground(linkColor); // XXX need to get highlight
                    repaint();
                }                
                public void mouseExited(MouseEvent e) 
                {
                    descBtn.setForeground(defColor); // XXX need to get highlight
                    repaint();
               }
            };
            descBtn.addMouseListener(mouseMotionListener);   */     

            descComp = descBtn;
        } else
        {
            descLabel = new JLabel(description);
            descComp = descLabel;
        }
        resultsLabel = new JLabel("?", JLabel.RIGHT);
                
        FormLayout      formLayout = new FormLayout("left:p:g,4dlu,right:p:g", "p");
        PanelBuilder    builder    = new PanelBuilder(formLayout);
        CellConstraints cc         = new CellConstraints();
       
        builder.add(descComp,     cc.xy(1,1));
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
     * sets a strin into the results label
     * @param value the value to be set into the label
     */
    public void setValueText(final String value)
    {
        resultsLabel.setText(value);
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

    /**
     * Returns the link string
     * @return Returns the link string
     */
    public String getLink()
    {
        return link;
    }
    
    /**
     * Set the appropriate control
     * @param str the new title string
     */
    protected void setDesc(final String str)
    {
        if (descLabel != null)
            descLabel.setText(str);
        else
            descBtn.setText(str);
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
                    setDesc(dataObj.toString());
                    
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
