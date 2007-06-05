/* This library is free software; you can redistribute it and/or
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

package edu.ku.brc.af.tasks.subpane;

import static edu.ku.brc.ui.UIHelper.getInt;
import static edu.ku.brc.ui.UIHelper.getString;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.Icon;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.dbsupport.QueryResultsContainer;
import edu.ku.brc.dbsupport.QueryResultsContainerIFace;
import edu.ku.brc.dbsupport.QueryResultsGetter;
import edu.ku.brc.dbsupport.QueryResultsHandlerIFace;
import edu.ku.brc.ui.IconManager;

/**
 * Creates a pane that can listener for Query Results and then create a Pie Chart.
 
 * @code_status Complete
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class PieChartPane extends ChartPane
{
    // Static Data Members
    //private static final Logger log = Logger.getLogger(BarChartPane.class);

    // Data Members
    @SuppressWarnings("unused")
    private QueryResultsGetter       getter;
    @SuppressWarnings("unused")
    private QueryResultsContainer    qrContainer;
    private QueryResultsHandlerIFace handler = null;


    /**
     * Constructor.
     * @param name name of pane
     * @param task the owning task
     */
    public PieChartPane(final String name,
                        final Taskable task)
    {
        super(name, task);

        progressLabel.setText(getResourceString("BuildingPieChart"));

        getter      = new QueryResultsGetter(this);
        qrContainer = new QueryResultsContainer(name);
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getIcon()
     */
    @Override
    public Icon getIcon()
    {
        return IconManager.getIcon("Pie_Chart", IconManager.IconSize.Std16);
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

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.af.dbsupport.QueryResultsListener#allResultsBack()
     */
    public synchronized void allResultsBack()
    {
        // create a dataset...
        DefaultPieDataset dataset = new DefaultPieDataset();

        java.util.List<Object> list = handler.getDataObjects();
        for (int i=0;i<list.size();i++)
        {
            Object descObj = list.get(i++);
            Object valObj  = list.get(i);
            dataset.setValue(getString(descObj), getInt(valObj));
        }
        list.clear();

        // create a chart...
        JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset,
                false, // legend?
                true, // tooltips?
                false // URLs?
            );
        
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        
        /*
        PiePlot3D plot = (PiePlot3D) chart.getPlot();
        //plot.setSectionOutlinesVisible(false);
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        plot.setNoDataMessage("No data available");
        plot.setCircular(true);
        plot.setLabelGap(0.02);
        //plot.setBackgroundAlpha(0.5f);
        plot.setForegroundAlpha(0.5f);
        plot.setDepthFactor(0.05);
        */
        
        removeAll(); // remove progress bar
        
        ChartPanel panel = new ChartPanel(chart, true, true, true, true, true);
        
        add(panel, BorderLayout.CENTER);

        doLayout();
        repaint();

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.dbsupport.QueryResultsListener#resultsInError(edu.ku.brc.af.dbsupport.QueryResultsContainer)
     */
    public void resultsInError(final QueryResultsContainerIFace qrc)
    {
        // do nothing
    }
}
