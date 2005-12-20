/* Filename:    $RCSfile: ExpressSearchResultsPane.java,v $
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

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.Hits;

import edu.ku.brc.specify.core.ExpressResultsTableInfo;
import edu.ku.brc.specify.core.NavBox;
import edu.ku.brc.specify.core.NavBoxLayoutManager;
import edu.ku.brc.specify.core.NavBoxMgr;
import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.ui.IconManager;
/**
 * A pane with a text field for entring in a query and then the results are displayed in a table.
 * 
 * @author rods
 *
 */
public class ExpressSearchResultsPane extends BaseSubPane
{
    private static Log log = LogFactory.getLog(ExpressSearchResultsPane.class);
    
    protected JPanel      contentPanel;
    protected JScrollPane scrollPane;
    protected NavBox      navBox = null;
    
    /**
     * Default Constructor
     *
     */
    public ExpressSearchResultsPane(final String name, 
                                    final Taskable task)
    {
        super(name, task);
        removeAll();
        
        setPreferredSize(new Dimension(600,600));
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
       
        contentPanel = new JPanel(new NavBoxLayoutManager(0,2));
       
        scrollPane = new JScrollPane(contentPanel);
        add(scrollPane, BorderLayout.CENTER);
        
    }
    
    /**
     * Adds a new results table to the panel
     * @param title the title of the table of results
     * @param sqlStr the sql to be executed to fill in the table (box)
     */
    public void addSearchResults(final ExpressResultsTableInfo tableInfo, final Hits hits)
    {
        if (tableInfo.isUseHitsCache())
        {
            contentPanel.add(new ExpressTableResultsHitsCache(this, tableInfo, hits));
        } else
        {
            contentPanel.add(new ExpressTableResults(this, tableInfo));
        }
        
        if (navBox == null)
        {
            navBox = new NavBox(getResourceString("ESResults"));
        }
        
        if (tableInfo.getIconName() == null)
        {
            log.error("Icon name is null for ["+tableInfo.getTitle()+"]");
        }
        navBox.add(NavBox.createBtn(tableInfo.getTitle(), tableInfo.getIconName(), IconManager.IconSize.Std16, null), true);
        
    }
    
    
    /**
     * Removes a table from the content pane
     * @param table the table of results to be removed
     */
    public void removeTable(ExpressTableResultsBase table)
    {
        contentPanel.remove(table);
        contentPanel.invalidate();
        contentPanel.doLayout();
        contentPanel.repaint();

        scrollPane.revalidate();
        scrollPane.doLayout();
        scrollPane.repaint();
    }
        
    /* (non-Javadoc)
     * @see java.awt.Component#showingPane(boolean)
     */
    public void showingPane(boolean show)
    {
        if (navBox != null)
        {
            if (show)
            {
                NavBoxMgr.getInstance().addBox(navBox);
            } else
            {
                NavBoxMgr.getInstance().removeBox(navBox, false);
            }
        }
    }

    /**
     * Revalidate the scroll pane 
     */
    public void revalidateScroll()
    {
        contentPanel.invalidate();
        scrollPane.revalidate();
    }
    
}
