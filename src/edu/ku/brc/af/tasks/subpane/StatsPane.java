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

import static edu.ku.brc.helpers.XMLHelper.getAttr;
import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.stats.BarChartPanel;
import edu.ku.brc.stats.StatDataItem;
import edu.ku.brc.stats.StatGroupTable;
import edu.ku.brc.stats.StatGroupTableFromQuery;
import edu.ku.brc.stats.StatsMgr;

/**
 * A class that loads a page of statistics from an XML description
 
 * @code_status Complete
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class StatsPane extends BaseSubPane
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(StatsPane.class);

    // Data Members
    protected String  resourceName           = null;
    protected Color   bgColor            = Color.WHITE;
    protected boolean useSeparatorTitles = false;

    protected int PREFERREDWIDTH = 260;
    protected int SPACING        = 35;

    /**
     * Creates a StatsPane.
     * @param name name of pane
     * @param task the owning task
     * @param resourceName the name of the resource that contains the configration
     * @param useSeparatorTitles indicates the group panels should use separator titles instead of boxes
     * @param bgColor the background color
    */
    public StatsPane(final String   name,
                     final Taskable task,
                     final String   resourceName,
                     final boolean  useSeparatorTitles,
                     final Color    bgColor)
    {
        super(name, task);

        this.resourceName = resourceName;
        this.useSeparatorTitles = useSeparatorTitles;

        if (bgColor != null)
        {
            this.bgColor = bgColor;
        } else
        {
            this.bgColor = Color.WHITE;
        }
        setBackground(this.bgColor);
        setLayout(new BorderLayout());

        init();
    }

    /**
     * Loads all the panels
     *
     */
    protected void init()
    {
        Element rootElement = null;
        try
        {
            rootElement = AppContextMgr.getInstance().getResourceAsDOM(resourceName);

            // count up rows and column
            StringBuilder rowsDef = new StringBuilder(128);

            List<?> rows = rootElement.selectNodes("/panel/row");
            int maxCols = 0;
            for (Object obj : rows)
            {
                Element rowElement = (Element)obj;
                List<?>    boxes      = rowElement.selectNodes("box");
                maxCols = Math.max(maxCols, boxes.size());
                if (rowsDef.length() > 0)
                {
                    rowsDef.append(",15dlu,");
                }
                rowsDef.append("top:p");
            }

            int preferredWidth = PREFERREDWIDTH;
            int spacing        = SPACING;

            FormLayout      formLayout = new FormLayout(createDuplicateJGoodiesDef("f:max("+preferredWidth+"px;p)", spacing+"px", maxCols), rowsDef.toString());
            //FormLayout      formLayout = new FormLayout(createDuplicateJGoodiesDef("f:min("+preferredWidth+"px;p):g", "p:g", maxCols), rowsDef.toString());
            PanelBuilder    builder    = new PanelBuilder(formLayout);
            CellConstraints cc         = new CellConstraints();

            int y = 1;
            for (Object obj : rows)
            {
                Element rowElement = (Element)obj;

                int x = 1;
                List<?> boxes = rowElement.selectNodes("box");
                for (Object bo : boxes)
                {
                    Element boxElement = (Element)bo;

                    String type = getAttr(boxElement, "type", "box");
                    int colSpan = getAttr(boxElement, "colspan", 1);

                    Component comp = null;
                    if (type.equalsIgnoreCase("bar chart"))
                    {
                        String statName = getAttr(boxElement, "name", null);

                        if (isNotEmpty(statName))
                        {
                            BarChartPanel bcp = (BarChartPanel)StatsMgr.createStatPane(statName);
                            int width = colSpan > 1 ? ((maxCols * preferredWidth) + ((maxCols-1) * spacing)) : preferredWidth;
                            // We start by assuming the chart will be square which is why we use
                            // preferredWidth as the height, and then we calculate the new width
                            bcp.setPreferredChartSize(width, preferredWidth);
                            comp = bcp;
                            //comp.setSize(new Dimension(preferredWidth, preferredWidth));
                            //comp.setPreferredSize(new Dimension(preferredWidth, preferredWidth));
                            //comp.invalidate();
                            //comp.doLayout();
                            //System.out.println(comp.getSize());
                            validate();
                            doLayout();
                            repaint();


                        }

                    } else // The default is "Box"
                    {
                        int descCol = getAttr(boxElement, "desccol", -1);
                        int valCol  = getAttr(boxElement, "valcol", -1);

                        Element sqlElement = (Element)boxElement.selectSingleNode("sql");

                        if (descCol > -1 && valCol > -1 && sqlElement != null)
                        {
                            String linkStr = null;
                            int colId      = -1;
                            Element link = (Element)boxElement.selectSingleNode("link");
                            if (link != null)
                            {
                                linkStr = link.getTextTrim();
                                colId   = Integer.parseInt(link.attributeValue("colid"));
                            }
                            StatGroupTableFromQuery group = new StatGroupTableFromQuery(boxElement.attributeValue("title"),
                                                                                        new String[] {getAttr(boxElement, "desctitle", " "),getAttr(boxElement, "valtitle", " ")},
                                                                                        sqlElement.getText(),
                                                                                        descCol,
                                                                                        valCol,
                                                                                        useSeparatorTitles,
                                                                                        getAttr(boxElement, "noresults", null));
                            group.setLinkInfo(linkStr, colId);
                            comp = group;

                            group.relayout();
                            //log.debug("After Relayout: "+group.getPreferredSize()+" "+group.getSize()+" "+group.getComponentCount());

                        } else
                        {
                            List<?> items = boxElement.selectNodes("item");
                            StatGroupTable groupTable = new StatGroupTable(boxElement.attributeValue("title"),
                                                                           new String[] {getAttr(boxElement, "desctitle", " "),getAttr(boxElement, "valtitle", " ")},
                                                                           useSeparatorTitles, items.size());
                            for (Object io : items)
                            {
                                Element itemElement = (Element)io;

                                //log.debug("STAT["+getAttr(itemElement, "title", "N/A")+"]");

                                Element link    = (Element)itemElement.selectSingleNode("link");
                                String  linkStr = null;
                                if (link != null)
                                {
                                    linkStr = link.getTextTrim();
                                }

                                StatDataItem statItem   = new StatDataItem(itemElement.attributeValue("title"), linkStr, getAttr(itemElement, "useprogress", false));
                                List<?>      statements = itemElement.selectNodes("sql/statement");

                                if (statements.size() == 1)
                                {
                                    statItem.add(((Element)statements.get(0)).getText(), 1, 1, StatDataItem.VALUE_TYPE.Value);

                                } else if (statements.size() > 0)
                                {
                                    int cnt = 0;
                                    for (Object stObj : statements)
                                    {
                                        Element stElement = (Element)stObj;
                                        int vRowInx = getAttr(stElement, "row", -1);
                                        int vColInx = getAttr(stElement, "col", -1);
                                        if (vRowInx == -1 || vColInx == -1)
                                        {
                                            statItem.add(stElement.getText()); // ignore return object
                                        } else
                                        {
                                            statItem.add(stElement.getText(), vRowInx, vColInx, StatDataItem.VALUE_TYPE.Value); // ignore return object
                                        }
                                        cnt++;
                                    }
                                }
                                groupTable.addDataItem(statItem);
                                statItem.startUp();

                            }
                            groupTable.relayout();
                            //log.debug(groupTable.getPreferredSize());
                            comp = groupTable;
                            //comp = scrollPane;
                        }

                    }

                    if (comp != null)
                    {
                        if (colSpan == 1)
                        {
                            builder.add(comp, cc.xy(x, y));

                        } else
                        {
                            builder.add(comp, cc.xywh(x, y, colSpan, 1));
                        }
                        x += 2;
                    }
                } // boxes
                y += 2;
            }

            setBackground(bgColor);

            JPanel statPanel = builder.getPanel();
            statPanel.setBackground(Color.WHITE);
            //statPanel.setOpaque(false);

            builder    = new PanelBuilder(new FormLayout("C:P:G", "p"));
            builder.add(statPanel, cc.xy(1,1));
            JPanel centerPanel = builder.getPanel();

            centerPanel.setBackground(Color.WHITE);
            //centerPanel.setOpaque(false);
            centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            add(centerPanel, BorderLayout.CENTER);

            centerPanel.invalidate();
            doLayout();

        } catch (Exception ex)
        {
            log.error(ex);
            ex.printStackTrace();
        }

    }

}
