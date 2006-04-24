package edu.ku.brc.specify.stats;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import edu.ku.brc.specify.dbsupport.CustomQuery;
import edu.ku.brc.specify.dbsupport.CustomQueryFactory;
import edu.ku.brc.specify.dbsupport.PairsMultipleQueryResultsHandler;
import edu.ku.brc.specify.dbsupport.PairsSingleQueryResultsHandler;
import edu.ku.brc.specify.dbsupport.QueryResultsContainer;
import edu.ku.brc.specify.dbsupport.QueryResultsDataObj;
import edu.ku.brc.specify.dbsupport.QueryResultsHandlerIFace;
import edu.ku.brc.specify.dbsupport.QueryResultsListener;
import edu.ku.brc.specify.dbsupport.QueryResultsProcessable;
import edu.ku.brc.specify.tasks.subpane.BarChartPane;
import edu.ku.brc.specify.tasks.subpane.BaseSubPane;
import edu.ku.brc.specify.tasks.subpane.ChartPane;
import edu.ku.brc.specify.tasks.subpane.PieChartPane;
import edu.ku.brc.specify.tasks.subpane.SQLQueryPane;
import edu.ku.brc.specify.ui.CommandAction;
import edu.ku.brc.specify.ui.CommandDispatcher;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.forms.ViewMgr;
import edu.ku.brc.specify.ui.forms.persist.View;

public class StatsMgr
{

    private static Log log = LogFactory.getLog(StatsMgr.class);

    protected static final String DISPLAY   = "display";
    protected static final String BAR_CHART = "bar chart";
    protected static final String PIE_CHART = "pie chart";
    protected static final String TABLE     = "table";
    protected static final String FORM      = "form";


    // Data Members
    protected Element statDOM;
    protected Element panelDOM;


    public StatsMgr()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Reads the Chart INfo from the DOM and sets it into a Chartable object. The info are string used to decorate the chart,
     * like title, X Axis Title, Y Axis Title etc.
     * @param element the 'chartinfo' element ot be processed
     * @param name the name of the child element that needs to be looked up to get its value
     * @return returns the string value for the info element or an empty string
     */
    protected String getChartInfo(final Element element, final String name)
    {
        Element el = (Element)element.selectSingleNode("chartinfo/"+name);
        if (el != null)
        {
            return el.getText();
        }
        return "";
    }

    /**
     * Fills the ChartPanel with any extra desxcription information
     * @param chartPane the chart pane to be augmented
     */
    protected void fillWithChartInfo(final Element element, final Chartable chartable)
    {
        chartable.setTitle(getChartInfo(element, "title"));
        chartable.setXAxis(getChartInfo(element, "xaxis"));
        chartable.setYAxis(getChartInfo(element, "yaxis"));

        String vert = getChartInfo(element, "vertical");
        if (isNotEmpty(vert))
        {
            chartable.setVertical(vert.toLowerCase().equals("true"));
        }
    }

    /**
     * Helper method for adding row/col desc and values to a container
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
     * @param element the DOM element of the chart
     * @param qrProcessable the processor to take care of the results
     * @param subPane the sub pane to be added to the UI
     * @param listener the listener who nneds to know when the query is done and all the results are available
     */
    public void createChart(final Element                 element,
                            final QueryResultsProcessable qrProcessable,
                            final Chartable               chartable,
                            final QueryResultsListener    listener)
    {
        if (element != null)
        {
            // Fill the chart with extra descirption and decoration info
            fillWithChartInfo(element, chartable);

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

                container.setSql(sqlElement.getText().trim());

                String displayType = element.attributeValue("display");
                List parts = element.selectNodes(displayType.equals("Pie Chart") ? "slice" : "bar");
                for ( Iterator iter = parts.iterator(); iter.hasNext(); )
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

            } else if (sqlType.equals("builtin"))
            {
                try
                {
                    CustomQuery                      customQuery   = CustomQueryFactory.createCustomQuery(sqlElement.attributeValue("className"));
                    PairsMultipleQueryResultsHandler multiplePairs = new PairsMultipleQueryResultsHandler();
                    qrProcessable.setHandler(multiplePairs);

                    multiplePairs.init(listener, customQuery.getQueryDefinition());
                    multiplePairs.startUp();

                } catch (ClassNotFoundException ex)
                {
                    log.error(ex); // XXX what should we do here?
                } catch (IllegalAccessException ex)
                {
                    log.error(ex); // XXX what should we do here?
                } catch (InstantiationException ex)
                {
                    log.error(ex); // XXX what should we do here?
                }

            } else
            {
                throw new RuntimeException("unrecognizable type for sql element["+sqlType+"]");
            }
        }
    }

    /**
     * Dispatches a request to show a Form
     * @param domElement the DOM element that contains the info we need to display the form
     */
    protected void createView(final Element domElement, final String idStr)
    {
        String viewSetName = domElement.attributeValue("viewset");
        String viewName    = domElement.attributeValue("view");
        String mode        = domElement.attributeValue("mode");

        View view = ViewMgr.getView(viewSetName, viewName);

        /*int tableId = DBTableIdMgr.lookupIdByClassName(view.getClassName());

        RecordSet rs = new RecordSet("ViewRecord", tableId);
        RecordSetItem rsi = new RecordSetItem();
        rsi.setRecordId(idStr);
        rs.getItems().add(rsi);
        */

        CommandDispatcher.dispatch(new CommandAction("Data_Entry", "ShowView", new Object[] {view, mode, idStr}));
    }

    /**
     * Looks up statName and creates the appropriate SubPane
     * @param statName the name of the stat to be displayed
     */
    public JPanel createStatPane(final String statName)
    {
        String nameStr;
        String idStr = null;
        int inx = statName.indexOf(',');
        if (inx == -1)
        {
            nameStr = statName;
        } else
        {
            nameStr = statName.substring(0,inx);
            idStr   = statName.substring(inx+4, statName.length());
        }

        Element element = (Element)statDOM.selectSingleNode("/statistics/stat[@name='"+nameStr+"']");
        if (element != null)
        {
            String displayType = element.attributeValue(DISPLAY).toLowerCase();


            if (displayType.equalsIgnoreCase(BAR_CHART))
            {
                BarChartPanel barChart = new BarChartPanel();
                createChart(element, barChart, barChart, barChart);


            } else if (displayType.equalsIgnoreCase(PIE_CHART))
            {
                PieChartPanel pieChart = new PieChartPanel();
                createChart(element, pieChart, pieChart, pieChart);

            } else if (displayType.equalsIgnoreCase(PIE_CHART))
            {
                PieChartPanel pieChart = new PieChartPanel();
                createChart(element, pieChart, pieChart, pieChart);

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
                if (sqlElement == null)
                {
                    throw new RuntimeException("sql element is null!");
                }
/*
                SQLQueryPane queryPane = new SQLQueryPane(titleElement.getTextTrim(), this, true, true);
                String sqlStr = sqlElement.getTextTrim();
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
                UICacheManager.addSubPane(queryPane);*/

            } else
            {
                // error
                log.error("Wrong type of display ["+displayType+"] this type is not supported!");
            }
        }
        return null;
    }

}
