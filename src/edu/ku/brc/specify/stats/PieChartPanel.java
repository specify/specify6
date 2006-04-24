/* Filename:    $RCSfile: PieChartPanel.java,v $
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

import static edu.ku.brc.specify.helpers.UIHelper.getInt;
import static edu.ku.brc.specify.helpers.UIHelper.getString;
import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Icon;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.dbsupport.QueryResultsContainer;
import edu.ku.brc.specify.dbsupport.QueryResultsGetter;
import edu.ku.brc.specify.dbsupport.QueryResultsHandlerIFace;
import edu.ku.brc.specify.dbsupport.QueryResultsListener;
import edu.ku.brc.specify.dbsupport.QueryResultsProcessable;
import edu.ku.brc.specify.ui.IconManager;

/**
 * Creates a pane that can listener for Query Results and then create a Pie Chart
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class PieChartPanel extends ChartPanel implements QueryResultsListener, QueryResultsProcessable
{
    // Static Data Members
    //private static Log log = LogFactory.getLog(BarChartPanel.class);

    // Data Members
    private QueryResultsGetter       getter;
    private QueryResultsContainer    qrContainer;
    private QueryResultsHandlerIFace handler = null;


    /**
     * @param name name of pane
     * @param task the owning task
     */
    public PieChartPanel()
    {
        getter      = new QueryResultsGetter(this);
        qrContainer = new QueryResultsContainer("Pie Chart");
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getIcon()
     */
    public Icon getIcon()
    {
        return IconManager.getIcon("Pie_Chart", IconManager.IconSize.Std16);
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

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsListener#allResultsBack()
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
        SquareChartPanel panel = new SquareChartPanel(chart, true, true, true, true, true);
        setBackground(Color.BLUE);
        //outerPanel.setLayout(new BorderLayout());
        //outerPanel.add(panel);

        removeAll(); // remove progress bar

        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p:g,p,p:g", "f:p:g"));
        CellConstraints cc         = new CellConstraints();
        builder.add(panel, cc.xy(3,1));
        add(builder.getPanel(), BorderLayout.CENTER);

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
