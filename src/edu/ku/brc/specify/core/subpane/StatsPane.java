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

import java.awt.BorderLayout;
import java.util.List;

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

    /**
     * 
     * @param name
     * @param task
     */
    public StatsPane(final String name, 
                     final Taskable task)
    {
        super(name, task);
        
        setLayout(new BorderLayout());

        loadUI();
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
     * 
     *
     */
    protected void loadUI()
    {
        Element rootElement = null;
        try
        {
            rootElement = XMLHelper.readDOMFromConfigDir("stats_summary_panel.xml");
            
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
            
            StringBuffer colsDef = new StringBuffer();
            for (int i=0;i<maxCols;i++)
            {
                if (colsDef.length() > 0)
                {
                    colsDef.append(",15dlu,");
                }
                colsDef.append("f:p");                
            }
            
            log.info(rowsDef.toString()+", "+colsDef.toString());
            FormLayout      formLayout = new FormLayout(colsDef.toString(), rowsDef.toString());
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
                        group = new StatGroupFromQuery(boxElement.attributeValue("title"), sqlElement.getText(), descCol, valCol);
                        
                    } else
                    {
                        group = new StatGroup(boxElement.attributeValue("title"));
                        
                        
                        
                        List items = boxElement.selectNodes("item");
                        for (Object io : items)
                        {
                            Element itemElement = (Element)io;
                            
                            StatItem statItem = new StatItem(itemElement.attributeValue("title"));
                            List statements = itemElement.selectNodes("sql/statement");
                            
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
            
            builder    = new PanelBuilder(new FormLayout("C:P:G", "p"));
            builder.add(statPanel, cc.xy(1,1));
 
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
