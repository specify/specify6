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
package edu.ku.brc.af.tasks.subpane;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.getInt;
import static edu.ku.brc.ui.UIHelper.getString;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.dbsupport.QueryResultsContainerIFace;
import edu.ku.brc.dbsupport.QueryResultsHandlerIFace;
import edu.ku.brc.ui.IconManager;


/**
 * Creates a pane that can listener for Query Results and then creates a Bar Chart.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class BarChartPane extends ChartPane
{
    // Static Data Members
    
    // Data Members
    private QueryResultsHandlerIFace handler = null;
    

    /**
     * Creates a BarChart pane with a name and a reference to the taskable that started it
     * @param name the name of the BarChart
     * @param task the starting task
     */
    public BarChartPane(final String name, 
                        final Taskable task)
    {
        super(name, task);
        progressLabel.setText(getResourceString("BarChartPane.BUILD_BARCHART")); //$NON-NLS-1$
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getIcon()
     */
    @Override
    public Icon getIcon()
    {
        return IconManager.getIcon("Bar_Chart", IconManager.IconSize.Std16); //$NON-NLS-1$
    }
    
    //--------------------------------------
    // QueryResultsProcessable
    //--------------------------------------
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.af.dbsupport.QueryResultsProcessable#setHandler()
     */
    public void setHandler(final QueryResultsHandlerIFace handler)
    {
        this.handler = handler;
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.af.dbsupport.QueryResultsProcessable#getHandler()
     */
    public QueryResultsHandlerIFace getHandler()
    {
        return handler;
    }

    //--------------------------------------
    // QueryResultsListener
    //--------------------------------------
    
    /**
     * Helper method for methods below
     */
    protected void addCompletedComp(JComponent comp)
    {
        removeAll(); // remove progress bar
        add(comp, BorderLayout.CENTER);
        
        if (handler != null)
        {
            handler.cleanUp();
            handler = null;
        }

        doLayout();
        repaint();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.ChartPane#allResultsBack(edu.ku.brc.dbsupport.QueryResultsContainerIFace)
     */
    public synchronized void allResultsBack(final QueryResultsContainerIFace qrc)
    {
        // create a dataset... 
        String cat = ""; //$NON-NLS-1$
        DefaultCategoryDataset dataset = new DefaultCategoryDataset(); 
        
        java.util.List<Object> list = handler.getDataObjects();
        for (int i=0;i<list.size();i++)
        {         
            Object descObj = list.get(i++);
            Object valObj  = list.get(i);
            dataset.addValue(getInt(valObj), getString(descObj), cat);
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
        
        //chart.getCategoryPlot().setRenderer(new CustomColorBarChartRenderer());
        
        addCompletedComp(panel);

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.dbsupport.QueryResultsListener#resultsInError(edu.ku.brc.af.dbsupport.QueryResultsContainer)
     */
    public void resultsInError(final QueryResultsContainerIFace qrc)
    {
        //JOptionPane.showMessageDialog(this, getResourceString("ERROR_CREATNG_BARCHART"), getResourceString("Error"), JOptionPane.ERROR_MESSAGE); // XXX LOCALIZE
        JLabel lbl = createLabel(getResourceString("BarChartPane.ERROR_CREATNG_BARCHART"), SwingConstants.CENTER); //$NON-NLS-1$
        addCompletedComp(lbl);
    }
}
