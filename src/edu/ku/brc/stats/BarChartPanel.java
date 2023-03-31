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
package edu.ku.brc.stats;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.getInt;
import static edu.ku.brc.ui.UIHelper.getString;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import edu.ku.brc.dbsupport.QueryResultsContainerIFace;
import edu.ku.brc.dbsupport.QueryResultsHandlerIFace;
import edu.ku.brc.dbsupport.QueryResultsListener;
import edu.ku.brc.dbsupport.QueryResultsProcessable;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;


/**
 * Creates a pane that can listener for Query Results and then create a Bar Chart
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class BarChartPanel extends ChartPanel implements QueryResultsListener, QueryResultsProcessable
{
    // Static Data Members
    //private static final Logger log = Logger.getLogger(BarChartPanel.class);

    // Data Members
    private QueryResultsHandlerIFace   handler = null;

    /**
     * Creates a BarChart.
     */
    public BarChartPanel()
    {
        super(getResourceString("BuildingBarChart"));
        setBorder(null);
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getIcon()
     */
    public Icon getIcon()
    {
        return IconManager.getIcon("Bar_Chart", IconManager.IconSize.Std16);
    }

    //--------------------------------------
    // QueryResultsProcessable
    //--------------------------------------

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsProcessable#setHandler()
     */
    public void setHandler(final QueryResultsHandlerIFace handler)
    {
        this.handler = handler;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsProcessable#getHandler()
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
        /*
        CellConstraints cc      = new CellConstraints();
        //PanelBuilder builder    = new PanelBuilder(new FormLayout("F:P:G", "F:P:G"), this);
        PanelBuilder builder    = new PanelBuilder(new FormLayout("p", "p"), this);
        builder.add(comp, cc.xy(1,1));
*/
        if (handler != null)
        {
            handler.cleanUp();
            handler = null;
        }

        /*
        validate();
        doLayout();
        repaint();
        getParent().getParent().validate();
        getParent().getParent().repaint();
        */
        UIRegistry.forceTopFrameRepaint();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.QueryResultsListener#allResultsBack(edu.ku.brc.dbsupport.QueryResultsContainerIFace)
     */
    public synchronized void allResultsBack(final QueryResultsContainerIFace qrc)
    {
        // create a dataset...
        String cat = "";
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        java.util.List<Object> list = handler.getDataObjects();
        for (int i=0;i<list.size();i++)
        {
            Object descObj = list.get(i++);
            Object valObj  = list.get(i);
            if (descObj != null && valObj != null)
            {
                dataset.addValue(getInt(valObj), getString(descObj), cat);
            }
        }
        list.clear();

        // create the chart...
        JFreeChart jgChart = ChartFactory.createBarChart3D(
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
        chartPanel = new org.jfree.chart.ChartPanel(jgChart, true, true, true, true, true);
        
        /*CustomColorBarChartRenderer ccbcr = new CustomColorBarChartRenderer()
        jgChart.getCategoryPlot().setRenderer(ccbcr);
        //Collection<LegendItem> items = jgChart.getCategoryPlot().getLegendItems();
        for (int i=0;i<jgChart.getCategoryPlot().getLegendItems().getItemCount();i++)
        {
            LegendItem item = jgChart.getCategoryPlot().getLegendItems().get(i);
            item.setFillPaintTransformer(transformer)
        }
        //jgChart.getCategoryPlot().setRenderer(new CustomColorBarChartRenderer());
         
         */
        
        removeAll();
        setLayout(new ChartLayoutManager(this));
        
        add(chartPanel);
        
        validate();
        doLayout();
        repaint();
        
        // TODO This is a kludge for now to get the BarChart to Paint Correctly
        UIRegistry.forceTopFrameRepaint();

    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsListener#resultsInError(edu.ku.brc.specify.dbsupport.QueryResultsContainer)
     */
    public void resultsInError(final QueryResultsContainerIFace qrc)
    {
        //JOptionPane.showMessageDialog(this, getResourceString("ERROR_CREATNG_BARCHART"), getResourceString("Error"), JOptionPane.ERROR_MESSAGE); // XXX LOCALIZE

        JLabel label = createLabel(getResourceString("ERROR_CREATNG_BARCHART"), JLabel.CENTER);
        addCompletedComp(label);
    }

    /*
    public void setBounds(Rectangle r)
    {
        setBounds(r.x, r.y, r.width, r.height);
        if (chartPanel != null)
        {
            setMaxChartSize(r.width, r.height);
        }
        System.out.println(r);
    }

    public void setBounds(int x, int y, int width, int height)
    {
        //System.out.print(x+" "+y+" "+width+" "+height);
        //System.out.println("  "+maxChartSize.width+" "+maxChartSize.height);

        super.setBounds(x, y, width, height);
        if (chartPanel != null)
        {
            setMaxChartSize(width, height);
        }
    }
    */
}
