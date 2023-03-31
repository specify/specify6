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

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.lang.ref.SoftReference;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.tasks.StatsTask;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.PairsMultipleQueryResultsHandler;
import edu.ku.brc.dbsupport.PairsSingleQueryResultsHandler;
import edu.ku.brc.dbsupport.QueryResultsContainer;
import edu.ku.brc.dbsupport.QueryResultsDataObj;
import edu.ku.brc.dbsupport.QueryResultsHandlerIFace;
import edu.ku.brc.dbsupport.QueryResultsListener;
import edu.ku.brc.dbsupport.QueryResultsProcessable;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.tasks.subpane.SQLQueryPane;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.UIRegistry;

/**
 * This class reads in the statistics.xml file and then enables others to ask for a statistic by name.
 * A "statistic" describes the SQL needed to produce the stat and then what columns should be used to get the specific results.
 * 
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class StatsMgr
{
    private static final Logger log = Logger.getLogger(StatsMgr.class);
    
    protected static SoftReference<Element> statDOM = null;

    protected static final String DISPLAY   = "display";
    protected static final String BAR_CHART = "bar chart";
    protected static final String PIE_CHART = "pie chart";
    protected static final String TABLE     = "table";
    protected static final String FORM      = "form";
    
    protected static String         resourceName = null;
    protected static ResourceBundle resBundle    = null;
    
    protected static Hashtable<String, String> nameToToolTipHash = new Hashtable<String, String>();


    // Data Members
    protected static StatsMgr instance = new StatsMgr();

    /**
     * Singleton Constructor.
     */
    protected StatsMgr()
    {

    }
    
    /**
     * Returns the Statistics DOM.
     * @return the Statistics DOM.
     */
    public static Element getDOM()
    {
        Element dom = null;
        if (statDOM != null)
        {
            dom = statDOM.get();
        }
        
        if (dom == null)
        {
            
            statDOM = new SoftReference<Element>(loadDOM());
            dom     = statDOM.get();
        }
        
        if (resourceName == null)
        {
            resourceName = XMLHelper.getAttr(dom, "resource", null);
            loadStatTooltips();
        }
        
        return dom;
    }
    
    /**
     * @return the resourceName
     */
    public static String getResourceName()
    {
        return resourceName;
    }

    /**
     * Load the Statistics DOM.
     * @return return s the DOM
     */
    protected static Element loadDOM()
    {
        Element dom = null;
        try
        {
            dom  = AppContextMgr.getInstance().getResourceAsDOM("Statistics"); // Describes each Statistic, its SQL and how it is to be displayed
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(StatsMgr.class, ex);
            log.error(ex);
        }
        return dom;
    }
    
    /**
     * Returns the DOM element for a named Statistic.
     * @param name the name of the statistic
     * @return DOM Element or null if not found
     */
    public static Element getStatisticDOMElement(final String name)
    {
        return (Element)getDOM().selectSingleNode("/statistics/stat[@name='"+name+"']");
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    private static void loadStatTooltips()
    {
        nameToToolTipHash.clear();
        
        boolean hasResBundle = false;
        if (StringUtils.isNotEmpty(StatsMgr.getResourceName()))
        {
            hasResBundle = UIRegistry.loadAndPushResourceBundle(StatsMgr.getResourceName()) != null;
        }
        
        try
        {
            Element dom = getDOM();
            if (dom != null)
            {
                for (Element element : (List<Element>)(dom.selectNodes("/statistics/stat")))
                {
                    String name = XMLHelper.getAttr(element, "name", null);
                    if (name != null)
                    {
                        String tooltipName = null;
                        Element el = (Element)element.selectSingleNode("chartinfo/tooltip");
                        if (el != null)
                        {
                            tooltipName = el.getTextTrim();
                        } else
                        {
                            el = (Element)element.selectSingleNode("tooltip");
                            if (el != null)
                            {
                                tooltipName = el.getTextTrim();
                            }
                        }
                        
                        if (tooltipName != null)
                        {
                            nameToToolTipHash.put(name, UIRegistry.getResourceString(tooltipName));
                        }
                    }
                }
            }
            
        } finally
        {
            if (hasResBundle)
            {
                UIRegistry.popResourceBundle();
            }
        }
    }

    /**
     * Reads the Chart INfo from the DOM and sets it into a Chartable object. The info are string used to decorate the chart,
     * like title, X Axis Title, Y Axis Title etc.
     * @param element the 'chartinfo' element to be processed
     * @param name the name of the child element that needs to be looked up to get its value
     * @param resBundle the ResourceBundle for localization
     * @return returns the string value for the info element or an empty string
     */
    protected String getChartInfo(final Element element, 
                                  final String name,
                                  final boolean isI18NKey)
    {
        Element el = (Element)element.selectSingleNode("chartinfo/"+name);
        if (el != null)
        {
            String str = el.getText();
            if (isI18NKey)
            {
                return UIRegistry.getResourceString(str);
            }
            return str;
        }
        return "";
    }

    /**
     * Fills the ChartPanel with any extra description information.
     * @param chartPane the chart pane to be augmented
     */
    protected String fillWithChartInfo(final Element element, final Chartable chartable)
    {
        boolean isI18NKey = resBundle != null;
        chartable.setTitle(getChartInfo(element, "title", isI18NKey));
        chartable.setXAxis(getChartInfo(element, "xaxis", isI18NKey));
        chartable.setYAxis(getChartInfo(element, "yaxis", isI18NKey));
        
        String toolTip = getChartInfo(element, "tooltip", isI18NKey);

        String vert = getChartInfo(element, "vertical", false);
        if (isNotEmpty(vert))
        {
            chartable.setVertical(vert.toLowerCase().equals("true"));
        }
        return toolTip;
    }
    
    /**
     * Helper method for adding row/col desc and values to a container.
     * @param qrc the QueryResultsContainer that will be added to
     * @param descRow the textual description's row position
     * @param descCol the textual description's column position
     * @param valueRow the value's row position
     * @param valueCol the value's column position
     */
    public static void add(final QueryResultsContainer qrc, final int descRow, final int descCol, final int valueRow, final int valueCol)
    {
        qrc.add(new QueryResultsDataObj(descRow, descCol));
        qrc.add(new QueryResultsDataObj(valueRow, valueCol));
    }

    /**
     * Creates a chart from an XML definition. The query may be defined in the XML so it could be a custom query
     * that comes from an instance of a class.
     * @param chartName the name of the chart
     * @param element the DOM element of the chart
     * @param qrProcessable the processor to take care of the results
     * @param chartable the chartable to be added to the UI
     * @param listener the listener who needs to know when the query is done and all the results are available
     */
    private void createChart(final String                  chartName,
                             final Element                 element,
                             final QueryResultsProcessable qrProcessable,
                             final Chartable               chartable,
                             final QueryResultsListener    listener)
    {
        if (element != null)
        {
            // Fill the chart with extra description and decoration info
            String tooltip = fillWithChartInfo(element, chartable);
            if (tooltip != null)
            {
                nameToToolTipHash.put(chartName, tooltip);
            }

            // It better have some SQL
            Element sqlElement = (Element)element.selectSingleNode("sql");
            if (sqlElement == null)
            {
                throw new RuntimeException("sql element is null!");
            }

            // The SQL can be of two types "text" or "builtin"
            // Text is just a text string of SQL that will be executed
            // Builtin is a class that will provide SQL
            String sqlType = sqlElement.attributeValue("type");
            if (sqlType.equals("text"))
            {

                QueryResultsContainer    container   = new QueryResultsContainer();
                QueryResultsHandlerIFace singlePairs = new PairsSingleQueryResultsHandler();
                qrProcessable.setHandler(singlePairs);

                container.setSql(QueryAdjusterForDomain.getInstance().adjustSQL(sqlElement.getText().trim()));

                String displayType = element.attributeValue("display");
                List<?> parts = element.selectNodes(displayType.equals("Pie Chart") ? "slice" : "bar");
                for ( Iterator<?> iter = parts.iterator(); iter.hasNext(); )
                {
                    Element slice = (Element) iter.next();
                    int descRow  = Integer.parseInt(slice.valueOf( "desc/@row" ));
                    int descCol  = Integer.parseInt(slice.valueOf( "desc/@col" ));
                    int valueRow = Integer.parseInt(slice.valueOf( "value/@row" ));
                    int valueCol = Integer.parseInt(slice.valueOf( "value/@col" ));

                    add(container, descRow, descCol, valueRow, valueCol);
                }
                singlePairs.init(listener, container);
                singlePairs.startUp();

            } else if (sqlType.equals("custom"))
            {

                CustomQueryIFace customQuery   = CustomQueryFactory.getInstance().getQuery(sqlElement.attributeValue("name"));
                PairsMultipleQueryResultsHandler multiplePairs = new PairsMultipleQueryResultsHandler();
                qrProcessable.setHandler(multiplePairs);

                multiplePairs.init(listener, customQuery.getQueryDefinition());
                multiplePairs.startUp();


            } else
            {
                throw new RuntimeException("unrecognizable type for sql element["+sqlType+"]");
            }
        }
    }

    /**
     * Dispatches a request to show a Form.
     * @param domElement the DOM element that contains the info we need to display the form
     */
    protected void createView(final Element domElement, final String idStr)
    {
        String viewSetName = domElement.attributeValue("viewset");
        String viewName    = domElement.attributeValue("view");
        String mode        = domElement.attributeValue("mode");

        ViewIFace view = AppContextMgr.getInstance().getView(viewSetName, viewName);
        if (view != null)
        {
            CommandDispatcher.dispatch(new CommandAction("Data_Entry", "ShowView", new Object[] {view, mode, idStr}));
        } else
        {
            log.error("Couldn't dispatch request for new View because the view wasn't found: ViewSet["+viewSetName+"] View["+viewName+"]");
        }
    }

    /**
     * Looks up statName and creates the appropriate SubPane.
     * @param statName the name of the stat to be displayed
     */
    private JPanel createStatPaneInternal(final String statName)
    {
        if (StringUtils.isNotEmpty(resourceName))
        {
            resBundle = UIRegistry.loadAndPushResourceBundle(resourceName);
        }

        try
        {
            String nameStr;
            String idStr    = null;
            int    inx      = statName.indexOf(',');
            if (inx == -1)
            {
                nameStr = statName;
            } else
            {
                nameStr = statName.substring(0,inx);
                idStr   = statName.substring(inx+4, statName.length());
            }
    
            Element dom = getDOM();
            if (dom != null)
            {
                Element element = (Element)dom.selectSingleNode("/statistics/stat[@name='"+nameStr+"']");
                if (element != null)
                {
                    String displayType = element.attributeValue(DISPLAY).toLowerCase();
        
        
                    if (displayType.equalsIgnoreCase(BAR_CHART))
                    {
                        BarChartPanel barChart = new BarChartPanel();
                        createChart(nameStr, element, barChart, barChart, barChart);
                        return barChart;
        
                    } else if (displayType.equalsIgnoreCase(PIE_CHART))
                    {
                        PieChartPanel pieChart = new PieChartPanel();
                        createChart(nameStr, element, pieChart, pieChart, pieChart);
                        return pieChart;
        
        
                    } else if (displayType.equalsIgnoreCase(FORM))
                    {
                        createView(element, idStr);
        
                    } else if (displayType.equals(TABLE))
                    {
                        Element sqlElement = (Element)element.selectSingleNode("sql");
                        if (sqlElement == null)
                        {
                            throw new RuntimeException("sql element is null!");
                        }
        
                        Element titleElement = (Element)element.selectSingleNode("title");
                        if (titleElement == null)
                        {
                            throw new RuntimeException("sql element is null!");
                        }
        
                        String tooltip = XMLHelper.getAttr((Element)element.selectSingleNode("tooltip"), "tooltip", null);
                        if (tooltip != null)
                        {
                            nameToToolTipHash.put(statName, tooltip);
                        }
        
                        StatsTask    statTask  = (StatsTask)ContextMgr.getTaskByName(StatsTask.STATISTICS);
                        SQLQueryPane queryPane = new SQLQueryPane(titleElement.getTextTrim(), statTask, true, true);
                        String       sqlStr    = sqlElement.getTextTrim();
                        if (idStr != null)
                        {
                            int substInx = sqlStr.lastIndexOf("%s");
                            if (substInx > -1)
                            {
                                sqlStr = sqlStr.substring(0, substInx-1) + idStr + sqlStr.substring(substInx+2, sqlStr.length());
                            } else
                            {
                                log.error("Couldn't find the substitue string \"%s\" in ["+sqlStr+"]");
                            }
        
                            //sqlStr = String.format(sqlStr, new Object[] {idStr});
                        }
                        //System.out.println(sqlStr);
                        queryPane.setSQLStr(sqlStr);
                        queryPane.doQuery();
                        SubPaneMgr.getInstance().addPane(queryPane);
        
                    } else
                    {
                        // error
                        log.error("Wrong type of display ["+displayType+"] this type is not supported!");
                    }
                }
            } else
            {
                log.error("DOM is NULL!!"); 
            }
            
        } finally
        {
            if (resBundle != null)
            {
                UIRegistry.popResourceBundle();
            }
        }
        
        return null;
    }
    
    /**
     * Looks up statName and creates the appropriate SubPane.
     * @param statName the name of the stat to be displayed
     */
    public static JPanel createStatPane(final String statName)
    {
        return instance.createStatPaneInternal(statName);
    }

    /**
     * @param statName the statistic name
     * @return the localized tooltip string
     */
    public static String getTooltipForStat(final String statName)
    {
        return nameToToolTipHash.get(statName);
    }


}
