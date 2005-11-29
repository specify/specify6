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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Icon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.dbsupport.QueryResultsHandlerIFace;
import edu.ku.brc.specify.dbsupport.QueryResultsListener;
import edu.ku.brc.specify.dbsupport.QueryResultsProcessable;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.UICacheManager;

public class BarChartPane extends BaseSubPane implements QueryResultsListener, QueryResultsProcessable
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
        
        setLayout(new BorderLayout());
        
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getIcon()
     */
    public Icon getIcon()
    {
        return IconManager.getInstance().getIcon(UICacheManager.getResourceString("Bar_Chart"), IconManager.IconSize.Std16);
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
        String cat = "Years";
        DefaultCategoryDataset dataset = new DefaultCategoryDataset(); 
        
        java.util.List<Object> list = processor.getDataObjects();
        for (int i=0;i<list.size();i++)
        {         
            Object descObj = list.get(i++);
            Object valObj  = list.get(i);
            dataset.addValue(BarChartPane.getInt(valObj), BarChartPane.getString(descObj), cat);
        }
        list.clear();

        // create a chart... 
         
        // create the chart... 
        JFreeChart chart = ChartFactory.createBarChart3D( 
                "Cataloged By Year", // chart title 
                null, // domain axis label 
                "Number of Specimens", // range axis label 
                dataset, // data 
                PlotOrientation.VERTICAL, 
                true, // include legend 
                true, // tooltips? 
                false // URLs? 
            ); 
        // create and display a frame... 
        ChartPanel panel = new ChartPanel(chart, true, true, true, true, true); 
        panel.setMaximumSize(new Dimension(100,100));
        panel.setPreferredSize(new Dimension(100,100));
        add(panel, BorderLayout.CENTER);
        
        processor.cleanUp();
        processor = null;
        

        doLayout();
        repaint();
        

    }

    //-----------------------------------------------
    //-- Static Helpers
    //-----------------------------------------------
    public static float getFloat(Object valObj)
    {
        float value = 0.0f;
        if (valObj != null)
        {
            if (valObj instanceof Integer)
            {
                value = ((Integer)valObj).floatValue();
            } else if (valObj instanceof Long)
            {
                value = ((Long)valObj).floatValue();
            } else if (valObj instanceof Float)
            {
                value = ((Float)valObj).floatValue();
            } else if (valObj instanceof Double)
            {
                value = ((Double)valObj).floatValue();
            } else
            {
                System.out.println("getFloat - Class type is "+valObj.getClass().getName());
            }
        } else
        {
            log.error("getFloat - Result Object is null for["+valObj+"]");
        }
        return value;
    }
    
    public static int getInt(Object valObj)
    {
        int value = 0;
        if (valObj != null)
        {
            if (valObj instanceof Integer)
            {
                value = ((Integer)valObj).intValue();
            } else if (valObj instanceof Long)
            {
                value = ((Long)valObj).intValue();
            } else if (valObj instanceof Float)
            {
                value = ((Float)valObj).intValue();
            } else if (valObj instanceof Double)
            {
                value = ((Double)valObj).intValue();
            } else
            {
                System.out.println("getInt - Class type is "+valObj.getClass().getName());
            }
        } else
        {
            log.error("getInt - Result Object is null for["+valObj+"]");
        }
        return value;
    }
    
    public static String getString(Object valObj)
    {
        if (valObj != null)
        {
            if (valObj instanceof String)
            {
                return (String)valObj;
            } else
            {
                System.out.println("getString - Class type is "+valObj.getClass().getName()+" should be String");
            }
        } else
        {
            log.error("getString - Result Object is null for["+valObj+"] in getString");
        }
        return "";
   }
    
   
}
