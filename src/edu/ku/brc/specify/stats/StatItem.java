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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.dbsupport.SQLExecutionListener;
import edu.ku.brc.specify.dbsupport.SQLExecutionProcessor;

public class StatItem extends JPanel implements SQLExecutionListener
{
    protected final String description;
    protected final String sql;
    protected SQLExecutionProcessor sqlExecutor;
    
    protected JLabel descLabel;
    protected JLabel resultsLabel;
    
    /**
     * Constructor for a single statistical item
     * @param description the textual description of the statistic
     * @param sql the SQL string that returns a single number
     */
    public StatItem(final String description, final String sql)
    {
        super(new BorderLayout());
        
        this.description = description;
        this.sql         = sql;
        
        descLabel    = new JLabel(description, JLabel.LEFT);
        resultsLabel = new JLabel("", JLabel.RIGHT);
                
        FormLayout      formLayout = new FormLayout("left:p:g,4dlu,right:p:g", "p");
        PanelBuilder    builder    = new PanelBuilder(formLayout);
        CellConstraints cc         = new CellConstraints();
       
        builder.add(descLabel, cc.xy(1,1));
        builder.add(resultsLabel, cc.xy(3,1));
        
        add(builder.getPanel(), BorderLayout.CENTER);
        
        getStat();
    }
    
    /**
     * Creates a SQLExecutionProcessor to go get the statisitic
     *
     */
    public void getStat()
    {
        // XXX major hack for prototyping
        if (sql.charAt(0) == '#')
        {
            resultsLabel.setText(sql.substring(1,sql.length()));
        } else
        {
            sqlExecutor = new SQLExecutionProcessor(this, sql);
            sqlExecutor.start();
        }
    }
    
    //-----------------------------------------------------
    //-- SQLExecutionListener
    //-----------------------------------------------------
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.dbSupport.SQLExecutionListenere#exectionDone()
     */
    public synchronized void exectionDone(final SQLExecutionProcessor process, final java.sql.ResultSet resultSet)
    {
        try
        {
            
            resultSet.first();
            int count = resultSet.getInt(1);
            resultsLabel.setText(Integer.toString(count));
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    invalidate();
                    ((StatGroup)getParent()).relayout();
                }
              });

            invalidate();
            getParent().doLayout();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        sqlExecutor = null;
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.dbSupport.SQLExecutionListenere#executionError()
     */
    public synchronized void executionError(final SQLExecutionProcessor process, final Exception ex)
    {
        sqlExecutor = null;
    }

   
}
