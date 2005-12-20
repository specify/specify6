/* Filename:    $RCSfile: StatsPane.java,v $
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

import static edu.ku.brc.specify.helpers.UIHelper.createDuplicateJGoodiesDef;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.dbsupport.QueryResultsContainer;
import edu.ku.brc.specify.helpers.XMLHelper;
import edu.ku.brc.specify.stats.StatGroup;
import edu.ku.brc.specify.stats.StatGroupFromQuery;
import edu.ku.brc.specify.stats.StatItem;

/**
 * A class that loads a page of statistics from an XML description
 * 
 * @author rods
 *
 */
public class StatsPane extends BaseSubPane
{
    // Static Data Members
    private static Log log = LogFactory.getLog(StatsPane.class);
    
    // Data Members
    protected String  fileName           = null;
    protected Color   bgColor            = Color.WHITE;
    protected boolean useSeparatorTitles = false;
    
    /**
     * Creates a StatsPane
     * @param name name of pane
     * @param task the owning task
     * @param fileName the name of the file that contains the configration
     * @param useSeparatorTitles indicates the group panels should use separator titles instead of boxes
     * @param bgColor the background color
    */
    public StatsPane(final String name, 
                     final Taskable task,
                     final String fileName,
                     final boolean useSeparatorTitles,
                     final Color bgColor)
    {
        super(name, task);
        
        this.fileName = fileName;
        this.useSeparatorTitles = useSeparatorTitles;
        
        if (bgColor != null)
        {
            this.bgColor = bgColor;
        }
        setLayout(new BorderLayout());
        
        setBackground(bgColor);
        
        init();
    }
    
    /**
     * Helper - Needs to be moved
     * @param element XXX
     * @param attrName XXX
     * @param defaultValue XXX
     * @return the int for the string
     */
    public static int getIntFromAttr(Element element, String attrName, int defaultValue)
    {
        String attr = element.attributeValue(attrName);
        if (attr != null)
        {
            try
            {
                return Integer.parseInt(attr);
            } catch (Exception e){}
        }
        return defaultValue;
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
            rootElement = XMLHelper.readDOMFromConfigDir(fileName);
            
            // count up rows and column
            StringBuffer rowsDef = new StringBuffer();
                
            List rows = rootElement.selectNodes("/panel/row");
            int maxCols = 0;
            for (Object obj : rows) 
            {
                Element rowElement = (Element)obj;
                List boxes = rowElement.selectNodes("box");
                maxCols = Math.max(maxCols, boxes.size());
                if (rowsDef.length() > 0)
                {
                    rowsDef.append(",15dlu,");
                }
                rowsDef.append("top:p");
            }
                       
            FormLayout      formLayout = new FormLayout(createDuplicateJGoodiesDef("f:max(250px;p)", "35dlu", maxCols), rowsDef.toString());
            PanelBuilder    builder    = new PanelBuilder(formLayout);
            CellConstraints cc         = new CellConstraints();

            int y = 1;
            for (Object obj : rows) 
            {
                Element rowElement = (Element)obj;
                
                int x = 1;
                List boxes = rowElement.selectNodes("box");
                for (Object bo : boxes) 
                {
                    Element boxElement = (Element)bo;
                    
                    int descCol = getIntFromAttr(boxElement, "descCol", -1);
                    int valCol  = getIntFromAttr(boxElement, "valCol", -1);
                    Element sqlElement = (Element)boxElement.selectSingleNode("sql");
                    
                    StatGroup group = null;
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
                        group = new StatGroupFromQuery(boxElement.attributeValue("title"), 
                                                       sqlElement.getText(), 
                                                       descCol, 
                                                       valCol,
                                                       useSeparatorTitles);
                        ((StatGroupFromQuery)group).setLinkInfo(linkStr, colId);
                        
                    } else
                    {
                        group = new StatGroup(boxElement.attributeValue("title"), useSeparatorTitles);
                        
                        List items = boxElement.selectNodes("item");
                        for (Object io : items)
                        {
                            Element itemElement = (Element)io;
                            
                            Element link = (Element)itemElement.selectSingleNode("link");
                            String linkStr = null;
                            if (link != null)
                            {                               
                                linkStr = link.getTextTrim();  
                            }
                            
                            StatItem statItem   = new StatItem(itemElement.attributeValue("title"), linkStr);
                            List     statements = itemElement.selectNodes("sql/statement");

                             if (statements.size() == 1)
                            {
                                statItem.add(((Element)statements.get(0)).getText(), 1, 1, StatItem.VALUE_TYPE.Value);
                                
                            } else if (statements.size() > 0)
                            {
                                int cnt = 0;
                                QueryResultsContainer qrc = null;
                                for (Object stObj : statements) 
                                {
                                    Element stElement = (Element)stObj;
                                    int vRowInx = getIntFromAttr(stElement, "row", -1);
                                    int vColInx = getIntFromAttr(stElement, "col", -1);
                                    if (vRowInx == -1 || vColInx == -1)
                                    {
                                        qrc = statItem.add(stElement.getText());
                                    } else
                                    {
                                        qrc = statItem.add(stElement.getText(), vRowInx, vColInx, StatItem.VALUE_TYPE.Value);
                                    }
                                    cnt++;
                                }
                            }
                            group.add(statItem);
                            statItem.startUp();
                        }
                    }
                    log.info(boxElement.attributeValue("title")+" "+x+","+y);
                    builder.add(group, cc.xy(x, y));
                    x += 2;
                }
                y += 2;
            }
            
            JPanel statPanel = builder.getPanel();
            statPanel.setBackground(bgColor);
            
            builder    = new PanelBuilder(new FormLayout("C:P:G", "p"));
            builder.add(statPanel, cc.xy(1,1));
 
            builder.getPanel().setBackground(Color.WHITE);
            builder.getPanel().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); 
            
            add(builder.getPanel(), BorderLayout.CENTER);
            
            builder.getPanel().invalidate();
            doLayout();
            
        } catch (Exception ex)
        {
            log.error(ex);
            ex.printStackTrace();
        }

    }
    
}
