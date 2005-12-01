/* Filename:    $RCSfile: BarChartPane.java,v $
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

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JProgressBar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.dbsupport.QueryResultsContainer;
import edu.ku.brc.specify.dbsupport.QueryResultsHandlerIFace;
import edu.ku.brc.specify.dbsupport.QueryResultsListener;
import edu.ku.brc.specify.dbsupport.QueryResultsProcessable;
import edu.ku.brc.specify.ui.IconManager;

public class BarChartPane extends ChartPane implements QueryResultsListener, QueryResultsProcessable
{
    // Static Data Members
    private static Log log = LogFactory.getLog(BarChartPane.class);
    
    // Data Members
    private QueryResultsHandlerIFace processor = null;
    

    /**
     * 
     * @param name
     * @param task
     */
    public BarChartPane(final String name, 
                        final Taskable task)
    {
        super(name, task);
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getIcon()
     */
    public Icon getIcon()
    {
        return IconManager.getInstance().getIcon(getResourceString("Bar_Chart"), IconManager.IconSize.Std16);
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
        String cat = "";
        DefaultCategoryDataset dataset = new DefaultCategoryDataset(); 
        
        java.util.List<Object> list = processor.getDataObjects();
        for (int i=0;i<list.size();i++)
        {         
            Object descObj = list.get(i++);
            Object valObj  = list.get(i);
            dataset.addValue(BarChartPane.getInt(valObj), BarChartPane.getString(descObj), cat);
        }
        list.clear();

        // create the chart... 
        JFreeChart chart = ChartFactory.createBarChart3D( 
                title,      // chart title 
                xAxisTitle, // domain axis label 
                yAxisTitle, // range axis label 
                dataset,    // data 
                isVertical ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL, 
                true,       // include legend 
                true,       // tooltips? 
                false       // URLs? 
            ); 
        // create and display a frame... 
        ChartPanel panel = new ChartPanel(chart, true, true, true, true, true); 
        panel.setMaximumSize(new Dimension(100,100));
        panel.setPreferredSize(new Dimension(100,100));
        
        removeAll(); // remove progress bar
        add(panel, BorderLayout.CENTER);
        
        processor.cleanUp();
        processor = null;
        

        doLayout();
        repaint();
        

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsListener#resultsInError(edu.ku.brc.specify.dbsupport.QueryResultsContainer)
     */
    public void resultsInError(final QueryResultsContainer qrc)
    {
        
    }

   
}
