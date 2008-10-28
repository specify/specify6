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
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.JPAQuery;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.stats.BarChartPanel;
import edu.ku.brc.stats.StatDataItem;
import edu.ku.brc.stats.StatGroupTable;
import edu.ku.brc.stats.StatGroupTableFromCustomQuery;
import edu.ku.brc.stats.StatGroupTableFromQuery;
import edu.ku.brc.stats.StatsMgr;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.UIHelper;

/**
 * A class that loads a page of statistics from an XML description
 
 * @code_status Complete
 **
 * @author rods
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class StatsPane extends BaseSubPane
{
    protected enum QueryType {SQL, JPA, CUSTOM}
    
    // Static Data Members
    private static final Logger log = Logger.getLogger(StatsPane.class);

    // Data Members
    protected String  resourceName       = null;
    protected Color   bgColor            = Color.WHITE;
    protected boolean useSeparatorTitles = false;

    protected int     PREFERREDWIDTH     = 300;
    protected int     SPACING            = 35;

    /**
     * Creates a StatsPane.
     * @param name name of pane
     * @param task the owning task
     * @param resourceName the name of the resource that contains the configuration
     * @param useSeparatorTitles indicates the group panels should use separator titles instead of boxes
     * @param bgColor the background color
     * @param upperDisplayComp a display component for the upper half of the screen
    */
    public StatsPane(final String   name,
                     final Taskable task,
                     final String   resourceName,
                     final boolean  useSeparatorTitles,
                     final Color    bgColor,
                     final JComponent upperDisplayComp)
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

        init(upperDisplayComp);
        
        registerPrintContextMenu();
    }

    /**
     * Converts a string to a QueryType (default conversion is SQL)
     * @param type the string to be converted
     * @return the QueryType
     */
    protected QueryType getQueryType(final String type)
    {
        try
        {
            return QueryType.valueOf(type.toUpperCase());
        } catch (Exception ex)
        {
            log.error(ex);
        }
        return QueryType.SQL;
    }
    
    /**
     * @param command
     * @return
     */
    protected CommandAction createCommandActionFromElement(final Element command)
    {
        CommandAction cmdAction = null;
        if (command != null)
        {
            String typeStr   = getAttr(command, "type",  null); //$NON-NLS-1$
            String actionStr = getAttr(command, "action", null); //$NON-NLS-1$
            String className = getAttr(command, "class",  null); //$NON-NLS-1$
            String data      = getAttr(command, "data",  null); //$NON-NLS-1$
            
            if (StringUtils.isNotEmpty(typeStr) && 
                StringUtils.isNotEmpty(actionStr))
            {
                Class<? extends DataModelObjBase> classObj = null;
                
                if (StringUtils.isNotEmpty(className))
                {
                    try
                    {
                        classObj = Class.forName(className).asSubclass(DataModelObjBase.class);
                        
                    } catch (Exception ex)
                    {
                        
                    }
                    if (classObj != null)
                    {
                        cmdAction = new CommandAction(typeStr, actionStr, classObj);
                    }
                } else
                {
                    cmdAction = new CommandAction(typeStr, actionStr, data);
                }
            }
        }
        return cmdAction;
    }
    
    /**
     * @param boxElement
     * @param title
     * @param colNames
     * @return
     */
    protected StatGroupTable processGroupItems(final Element  boxElement, 
                                               final String   title, 
                                               final String[] colNames)
    {
        StatGroupTable groupTable = null;
        
        List<?> items = boxElement.selectNodes("item"); //$NON-NLS-1$
        if (items != null)
        {
            groupTable = new StatGroupTable(title, colNames, useSeparatorTitles, items.size());
            for (Object io : items)
            {
                Element itemElement = (Element)io;
                String  itemTitle   = getAttr(itemElement, "title", "N/A"); //$NON-NLS-1$ //$NON-NLS-2$
                
                String  formatStr  = null;
                Element formatNode = (Element)itemElement.selectSingleNode("sql/format"); //$NON-NLS-1$
                if (formatNode != null)
                {
                    formatStr = formatNode.getTextTrim();
                }
                
                Element       command   = (Element)itemElement.selectSingleNode("command"); //$NON-NLS-1$
                CommandAction cmdAction = null;
                if (command != null)
                {
                    cmdAction = createCommandActionFromElement(command);
                }
    
                Element      subSqlEl  = (Element)itemElement.selectSingleNode("sql"); //$NON-NLS-1$
                QueryType    queryType  = getQueryType(getAttr(subSqlEl, "type", "sql")); //$NON-NLS-1$ //$NON-NLS-2$
                
                CustomQueryIFace customQuery = null;
                if (queryType == QueryType.CUSTOM)
                {
                    String customQueryName = getAttr(subSqlEl, "name", null); //$NON-NLS-1$
                    if (StringUtils.isNotEmpty(customQueryName))
                    {
                        customQuery = CustomQueryFactory.getInstance().getQuery(customQueryName);
                        if (customQuery == null)
                        {
                            return null;
                        }
                        
                        if (!isPermissionOK(customQuery.getTableIds()))
                        {
                            return null;
                        }
                    } else
                    {
                        log.error("Name is empty for box item ["+getAttr(itemElement, "title", "N/A")+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        return null;
                    }
                    
                } else 
                {
                    List<Integer> tableIds = getTabledIds(subSqlEl);
                    if (!isPermissionOK(tableIds))
                    {
                        return null;
                    }
                }
                
                StatDataItem statItem  = new StatDataItem(itemTitle, cmdAction, getAttr(itemElement, "useprogress", false)); //$NON-NLS-1$
                
                //System.out.println("["+queryType+"]");
                switch (queryType)
                {
                    case SQL :
                    {
                        List<?> statements = itemElement.selectNodes("sql/statement"); //$NON-NLS-1$
                        
                        if (statements.size() == 1)
                        {
                            String sql = QueryAdjusterForDomain.getInstance().adjustSQL(((Element)statements.get(0)).getText());
                            statItem.add(sql, 1, 1, StatDataItem.VALUE_TYPE.Value, formatStr);

                        } else if (statements.size() > 0)
                        {
                            int cnt = 0;
                            for (Object stObj : statements)
                            {
                                Element stElement = (Element)stObj;
                                int    vRowInx = getAttr(stElement, "row", -1); //$NON-NLS-1$
                                int    vColInx = getAttr(stElement, "col", -1); //$NON-NLS-1$
                                String format  = getAttr(stElement, "format", null); //$NON-NLS-1$
                                String sql     = QueryAdjusterForDomain.getInstance().adjustSQL(stElement.getText());
                                
                                if (vRowInx == -1 || vColInx == -1)
                                {
                                    statItem.add(sql, format); // ignore return object
                                } else
                                {
                                    statItem.add(sql, vRowInx, vColInx, StatDataItem.VALUE_TYPE.Value, format); // ignore return object
                                }
                                cnt++;
                            }
                        }
                    } break;
                    
                    case JPA :
                    {
                        List<?> statements = itemElement.selectNodes("sql/statement"); //$NON-NLS-1$
                        String sql = QueryAdjusterForDomain.getInstance().adjustSQL(((Element)statements.get(0)).getText());
                        statItem.addCustomQuery(new JPAQuery(sql), formatStr);

                    } break;
                    
                    case CUSTOM :
                        statItem.addCustomQuery(customQuery, formatStr);
                        break;
                }

                groupTable.addDataItem(statItem);
                statItem.startUp();
            }
            groupTable.relayout();
        }
        return groupTable;
    }
    
    /**
     * @param boxElement
     * @return
     */
    protected Component processBox(final Element boxElement)
    {
        Component comp = null;
        
        int    descCol   = getAttr(boxElement, "desccol", -1); //$NON-NLS-1$
        int    valCol    = getAttr(boxElement, "valcol", -1); //$NON-NLS-1$
        String descTitle = getAttr(boxElement, "desctitle", " "); //$NON-NLS-1$ //$NON-NLS-2$
        String title     = getAttr(boxElement, "title", " "); //$NON-NLS-1$ //$NON-NLS-2$
        String noresults = getAttr(boxElement, "noresults", null); //$NON-NLS-1$
        
        //log.debug("***** "+title+" *******");
        
        String[] colNames = null;
        if (valCol != -1 && descCol == -1)
        {
            colNames = new String[] {getAttr(boxElement, "valtitle", " ")}; //$NON-NLS-1$ //$NON-NLS-2$
            
        } else if (descCol != -1 && valCol == -1 && StringUtils.isNotEmpty(descTitle))
        {
            colNames = new String[] {descTitle};
            
        } else
        {
            colNames = new String[] {descTitle, getAttr(boxElement, "valtitle", " ")}; //$NON-NLS-1$ //$NON-NLS-2$
        }

        Element sqlElement = (Element)boxElement.selectSingleNode("sql"); //$NON-NLS-1$
        if (valCol > -1 && sqlElement != null)
        {
            List<Integer> tableIds = getTabledIds(sqlElement);
            if (isPermissionOK(tableIds))
            {
                QueryType queryType = getQueryType(getAttr(sqlElement, "type", "sql")); //$NON-NLS-1$ //$NON-NLS-2$
                
                Element       command   = (Element)boxElement.selectSingleNode("command"); //$NON-NLS-1$
                int           colId     = -1;
                CommandAction cmdAction = null;
                
                if (command != null)
                {
                    colId     = getAttr(command, "colid", -1); //$NON-NLS-1$
                    cmdAction = createCommandActionFromElement(command);
                }
                
                //System.out.println("["+queryType+"]");
                try
                {
                    Element sqlStmt = (Element)sqlElement.selectSingleNode("statement"); //$NON-NLS-1$
                    if (sqlStmt == null)
                    {
                        int xx = 0;
                        xx++;
                    }
                    String sql =  QueryAdjusterForDomain.getInstance().adjustSQL(sqlStmt.getText());
                    
                    switch (queryType)
                    {
                        case SQL :
                        {
                            sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
                            StatGroupTableFromQuery group = new StatGroupTableFromQuery(title,
                                                                    colNames,
                                                                    sql,
                                                                    descCol,
                                                                    valCol,
                                                                    useSeparatorTitles,
                                                                    noresults);
                            if (cmdAction != null)
                            {
                                group.setCommandAction(cmdAction, colId);
                            }
                            comp = group;
                            group.relayout();
                        } break;
                        
                        case JPA :
                        {
                            StatGroupTableFromCustomQuery group = new StatGroupTableFromCustomQuery(title,
                                                                            colNames,
                                                                            new JPAQuery(sql),
                                                                            useSeparatorTitles,
                                                                            noresults);
                            if (cmdAction != null)
                            {
                                group.setCommandAction(cmdAction, colId);
                            }
                            comp = group;
                            group.relayout();
                        } break;
                        
                        case CUSTOM :
                        {
                            StatGroupTableFromCustomQuery group = new StatGroupTableFromCustomQuery(title,
                                                                                    colNames,
                                                                                    sql, // the name
                                                                                    useSeparatorTitles,
                                                                                    noresults);
                            if (cmdAction != null)
                            {
                                group.setCommandAction(cmdAction, colId);
                            }
                            comp = group;
                            group.relayout();
                        } break;
                        
                    } // switch

                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            //log.debug("After Relayout: "+group.getPreferredSize()+" "+group.getSize()+" "+group.getComponentCount());

        } else
        {
            comp = processGroupItems(boxElement, title, colNames);
        }
        
        return comp;
    }
    
    /**
     * Loads all the panels
     * @param upperDisplayComp upper half display component
     */
    protected void init(final JComponent upperDisplayComp)
    {
        Element rootElement = null;
        try
        {
            rootElement = AppContextMgr.getInstance().getResourceAsDOM(resourceName);
            if (rootElement == null)
            {
                throw new RuntimeException("Couldn't find resource ["+resourceName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            // count up rows and column
            StringBuilder rowsDef = new StringBuilder(128);

            List<?> rows = rootElement.selectNodes("/panel/row"); //$NON-NLS-1$
            int maxCols = 0;
            for (Object obj : rows)
            {
                Element rowElement = (Element)obj;
                List<?>    boxes   = rowElement.selectNodes("box"); //$NON-NLS-1$
                maxCols = Math.max(maxCols, boxes.size());
                if (rowsDef.length() > 0)
                {
                    rowsDef.append(",15dlu,"); //$NON-NLS-1$
                }
                rowsDef.append("top:p"); //$NON-NLS-1$
            }

            int preferredWidth = PREFERREDWIDTH;
            int spacing        = SPACING;

            String          colDefs    = createDuplicateJGoodiesDef("f:min("+preferredWidth+"px;p)", spacing+"px", maxCols); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            FormLayout      formLayout = new FormLayout(colDefs, rowsDef.toString());
            PanelBuilder    builder    = new PanelBuilder(formLayout);
            CellConstraints cc         = new CellConstraints();

            int y = 1;
            for (Object obj : rows)
            {
                Element rowElement = (Element)obj;

                int x = 1;
                List<?> boxes = rowElement.selectNodes("box"); //$NON-NLS-1$
                for (Object bo : boxes)
                {
                    Element boxElement = (Element)bo;

                    String type = getAttr(boxElement, "type", "box"); //$NON-NLS-1$ //$NON-NLS-2$
                    int colSpan = getAttr(boxElement, "colspan", 1); //$NON-NLS-1$

                    Component comp = null;
                    if (type.equalsIgnoreCase("bar chart")) //$NON-NLS-1$
                    {
                        String statName = getAttr(boxElement, "name", null); //$NON-NLS-1$

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
                        comp = processBox(boxElement);
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
            
            boolean hasUpper = upperDisplayComp != null;

            builder    = new PanelBuilder(new FormLayout("C:P:G", hasUpper ? "50px,p,20px,p" : "p")); //$NON-NLS-1$ //$NON-NLS-2$
            
            if (hasUpper)
            {
                y = 2;
                builder.add(upperDisplayComp, cc.xy(1, y));
                y += 2;
                
            } else
            {
                y = 1;
            }
            builder.add(statPanel, cc.xy(1, y));
            JPanel centerPanel = builder.getPanel();

            centerPanel.setBackground(Color.WHITE);
            //centerPanel.setOpaque(false);
            centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            add(centerPanel, BorderLayout.CENTER);

            centerPanel.validate();
            validate();
            doLayout();

        } catch (Exception ex)
        {
            log.error(ex);
            ex.printStackTrace();
        }

    }
    
    /**
     * @param item
     * @return
     */
    protected static List<Integer> getTabledIds(final Element item)
    {
        if (item != null)
        {
            List<?> tables = item.selectNodes("tables/id");//$NON-NLS-1$
            if (tables != null)
            {
                List<Integer> list = new Vector<Integer>();
                for (Object obj : tables)
                {
                    Element tbl = (Element)obj;
                    list.add(Integer.parseInt(tbl.getTextTrim()));
                }
                return list;
            }
        }
        return null;
    }

    public static boolean isPermissionOK(final List<Integer> list)
    {
        // not sure if the default should be true or false
        // certainly is security is off it should return true.
        boolean isOK = true; 
        if (UIHelper.isSecurityOn())
        {
            if (list != null)
            {
                for (Integer tableId : list)
                {
                    DBTableInfo tInfo = DBTableIdMgr.getInstance().getInfoById(tableId);
                    if (tInfo != null)
                    {
                        if (!tInfo.getPermissions().canView())
                        {
                            return false;
                        }
                    }
                }
            }
        }
        return isOK;
    }

}
