/* Filename:    $RCSfile: PieChartPane.java,v $
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

package edu.ku.brc.specify.core.subpane;

import java.awt.BorderLayout;

import javax.swing.Icon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.dbsupport.QueryResultsContainer;
import edu.ku.brc.specify.dbsupport.QueryResultsGetter;
import edu.ku.brc.specify.dbsupport.QueryResultsHandlerIFace;
import edu.ku.brc.specify.dbsupport.QueryResultsListener;
import edu.ku.brc.specify.dbsupport.QueryResultsProcessable;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.UICacheManager;

public class PieChartPane extends BaseSubPane implements QueryResultsListener, QueryResultsProcessable
{
    // Static Data Members
    private static Log log = LogFactory.getLog(BarChartPane.class);
    
    // Data Members
    private QueryResultsGetter    getter;
    private QueryResultsContainer qrContainer;
    private QueryResultsHandlerIFace     processor = null;
    

    /**
     * 
     *
     */
    public PieChartPane(final String name, 
                        final Taskable task)
    {
        super(name, task);
        
        setLayout(new BorderLayout());

        getter      = new QueryResultsGetter(this); 
        qrContainer = new QueryResultsContainer(name);
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getIcon()
     */
    public Icon getIcon()
    {
        return IconManager.getInstance().getIcon(UICacheManager.getResourceString("Pie_Chart"), IconManager.IconSize.Std16);
    }
    
    
    //--------------------------------------
    // QueryResultsProcessable
    //--------------------------------------
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsProcessable#setProcessor()
     */
    public void setProcessor(final QueryResultsHandlerIFace processor)
    {
        this.processor = processor;
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsProcessable#getProcessor()
     */
    public QueryResultsHandlerIFace getProcessor()
    {
        return processor;
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
        // create a dataset... 
        DefaultPieDataset dataset = new DefaultPieDataset(); 
        
        /*
        for (QueryResultsContainer container : getter.getQueryResultsContainers())
        {
            java.util.List<QueryResultsDataObj> dataObjs = container.getQueryResultsDataObjs();
           
            for (int i=0;i<dataObjs.size();i++)
            {
                QueryResultsDataObj dataObj = dataObjs.get(i++);
                Object descObj = dataObj.getResult();
                
                dataObj = dataObjs.get(i);
                Object valObj = dataObj.getResult();
                
                dataset.setValue(BarChartPane.getString(descObj), BarChartPane.getInt(valObj));
            }
        }*/
        java.util.List<Object> list = processor.getDataObjects();
        for (int i=0;i<list.size();i++)
        {         
            Object descObj = list.get(i++);
            Object valObj  = list.get(i);
            dataset.setValue(BarChartPane.getString(descObj), BarChartPane.getInt(valObj));
        }
        list.clear();        
        
        // create a chart... 
        JFreeChart chart = ChartFactory.createPieChart( 
                "Top 5 Species", 
                dataset, 
                false, // legend? 
                true, // tooltips? 
                false // URLs? 
            );
        
        /*JPanel outerPanel = new JPanel() 
        {
            Dimension dim = new Dimension(400,400);
            public Rectangle getBounds()
            {
              return new Rectangle(getLocation().x, getLocation().y, dim.width, dim.height);
            }
            public void setBounds(Rectangle r) 
            {
                setBounds(r.x, r.y, r.width, r.height);
            }
            public void setBounds(int x, int y, int width, int height) 
            {
                if (width <= 400) 
                    dim.width = width;
                else
                    dim.width = 400;
                if (height <= 400) 
                    dim.height = height;
                else 
                    dim.height = 400;
                super.setBounds(x, y, width, height);
            }
            public Rectangle getBounds(Rectangle rv) 
            {
                rv.setBounds(getLocation().x, getLocation().y, dim.width, dim.height);
                return rv;
            }
            public Dimension getSize()
            {
                return new Dimension(dim);
            }
            public Dimension getSize(Dimension rv) 
            {
                rv.setSize(dim);
                return rv;
            }
        };*/
        // create and display a frame... 
        ChartPanel panel = new ChartPanel(chart, true, true, true, true, true); 

        //outerPanel.setLayout(new BorderLayout());
        //outerPanel.add(panel);

        add(panel, BorderLayout.CENTER);

        doLayout();
        repaint();
        
    }


    
}
