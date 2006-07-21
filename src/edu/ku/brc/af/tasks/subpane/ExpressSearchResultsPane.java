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

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.apache.lucene.search.Hits;

import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxLayoutManager;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.ExpressResultsTableInfo;
import edu.ku.brc.ui.IconManager;

/**
 * A pane with a text field for entring in a query and then the results are displayed in a table.
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ExpressSearchResultsPane extends BaseSubPane implements ExpressSearchResultsPaneIFace
{
    private static final Logger log = Logger.getLogger(ExpressSearchResultsPane.class);

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
     * Add serach results box to UI
     * @param tableInfo the information about the table being added
     * @param hits the "hits" results of the search
     */
    public void addSearchResults(final ExpressResultsTableInfo tableInfo, final Hits hits)
    {

        if (tableInfo.isUseHitsCache())
        {
            contentPanel.add(new ExpressTableResultsHitsCache(this, tableInfo, true, hits));
        } else
        {
            contentPanel.add(new ExpressTableResults(this, tableInfo, true));
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
