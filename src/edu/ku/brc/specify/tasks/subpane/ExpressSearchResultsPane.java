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

package edu.ku.brc.specify.tasks.subpane;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.apache.lucene.search.Hits;

import edu.ku.brc.af.core.ExpressSearchResults;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxLayoutManager;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.ui.IconManager;

/**
 * This pane contains all the Express Search Result table panes. 
 * It also then adds a NavBox with the list of the types of items that were returned.
 *
 * @code_status Complete
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
    
    protected NavBox      navBox  = null;
    protected boolean     showing = false;
    protected boolean     added   = false;

    /**
     * Default Constructor.
     * @param name the name of the pane
     * @param task the owning task
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
     * Add serach results box to UI.
     * @param tableInfo the information about the table being added
     * @param hits the "hits" results of the search
     */
    public void addSearchResults(final ExpressSearchResults results, final Hits hits)
    {

        if (results.getTableInfo().isUseHitsCache())
        {
            addTable(new ExpressTableResultsHitsCache(this, results, true, hits));
            
        } else
        {
            // This will start itself up and if there are results from the query 
            // it will add itself to the pane (So it is OK that it isn't referenced)
            @SuppressWarnings("unused")
            ExpressTableResults resultsTable = new ExpressTableResults(this, results, true);
        }
    }


    /**
     * Removes a table from the content pane.
     * @param expressTableResultsBase the table of results to be removed
     */
    public void removeTable(final ExpressTableResultsBase expressTableResultsBase)
    {
        expressTableResultsBase.cleanUp();
        
        contentPanel.remove(expressTableResultsBase);
        contentPanel.invalidate();
        contentPanel.doLayout();
        contentPanel.repaint();

        scrollPane.revalidate();
        scrollPane.doLayout();
        scrollPane.repaint();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#addTable(edu.ku.brc.specify.tasks.subpane.ExpressTableResultsBase)
     */
    public void addTable(ExpressTableResultsBase expTblRes)
    {
        ExpressSearchResults results = expTblRes.getResults();
        if (results.getTableInfo().getIconName() == null)
        {
            log.error("Icon name is null for ["+results.getTableInfo().getTitle()+"]");
        }
        
        if (navBox == null)
        {
            navBox = new NavBox(getResourceString("ESResults"));
        }
        
        navBox.add(NavBox.createBtn(results.getTableInfo().getTitle(), results.getTableInfo().getIconName(), IconManager.IconSize.Std16, null), true);
        
        if (showing && !added)
        {
            added = true;
            NavBoxMgr.getInstance().addBox(navBox);
        } 
        
        //if (added)
        //{
            NavBoxMgr.getInstance().validate();
        //}

        contentPanel.add(expTblRes);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#showingPane(boolean)
     */
    @Override
    public void showingPane(boolean show)
    {
        log.info(show+"  "+showing+"  "+ added+"  "+(navBox != null));
        if (show)
        {
            showing = true;
            if (!added)
            {
                if (navBox != null)
                {
                    added  = true;
                    NavBoxMgr.getInstance().addBox(navBox);
                }
            }
        } else if (added && navBox != null)
        {
            NavBoxMgr.getInstance().removeBox(navBox, false);
        }

    }

    /**
     * Revalidate the scroll pane.
     */
    public void revalidateScroll()
    {
        contentPanel.invalidate();
        scrollPane.revalidate();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#aboutToShutdown()
     */
    @Override
    public boolean aboutToShutdown()
    {
        super.aboutToShutdown();
        
        for (int i=0;i<contentPanel.getComponentCount();i++)
        {
            Component comp = contentPanel.getComponent(i);
            if (comp instanceof ExpressTableResultsBase)
            {
                ((ExpressTableResultsBase)comp).cleanUp();
            }
        }
        return true;
    }


}
