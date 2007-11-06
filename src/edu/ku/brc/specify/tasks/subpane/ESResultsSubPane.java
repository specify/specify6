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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxLayoutManager;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;

/**
 * This pane contains all the Express Search Result table panes. 
 * It also then adds a NavBox with the list of the types of items that were returned.<br>
 * <br>
 * It also makes sure that the Results stay sorted by TableInfo's Title and imakes sure all the indexed results
 * are above the unindexed results. To do this it keeps two list one for indexed and one for un-indexed. Then
 * And it know that the comps in the layout manager are ExpressTableResultsBase items which are also JPanels
 * so it means it can get the list of comps from the LayoutManager and sort then and then put them part. It cheating a little
 * bit but it works.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ESResultsSubPane extends BaseSubPane implements ExpressSearchResultsPaneIFace
{
    private static final Logger log = Logger.getLogger(ESResultsSubPane.class);

    protected JPanel               contentPanel;
    protected NavBoxLayoutManager  layoutMgr;
    protected JScrollPane          scrollPane;
    
    protected List<ESResultsTablePanelIFace> expTblResults        = new Vector<ESResultsTablePanelIFace>();
    
    protected NavBox      navBox  = null;
    protected int         positionOfUnIndexed = -1;
    protected boolean     showing = false;
    protected boolean     added   = false;
    
    // Tables are added here waiting for their first results to come back.
    protected Vector<ESResultsTablePanelIFace> expTblResultsCache = new Vector<ESResultsTablePanelIFace>();
    
    protected JPanel      explainPanel = null;
    
    protected Comparator<ESResultsTablePanelIFace> sorter;
    
    //protected TableOrderingService tableOrderingService = new TableOrderingService();

    /**
     * Default Constructor.
     * @param name the name of the pane
     * @param task the owning task
     */
    public ESResultsSubPane(final String name,
                                    final Taskable task,
                                    final boolean includeExplainPane)
    {
        super(name, task, false);

        setPreferredSize(new Dimension(600,600));
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        contentPanel = new JPanel(layoutMgr = new NavBoxLayoutManager(0, 2));

        scrollPane = new JScrollPane(contentPanel);
        add(scrollPane, BorderLayout.CENTER);
        
        sorter = new Comparator<ESResultsTablePanelIFace>()
        {
            public int compare(ESResultsTablePanelIFace left, ESResultsTablePanelIFace right)
            {
                Integer leftOrder = left.getResults().getDisplayOrder();
                Integer rightOrder = right.getResults().getDisplayOrder();
                return leftOrder.compareTo(rightOrder);
            }
        };
        
        if (includeExplainPane)
        {
            explainPanel = new JPanel(new BorderLayout());
            explainPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            JButton btn = new JButton(UIRegistry.getResourceString("EXPRESSSEARCH_TELL_ME_MORE"), IconManager.getIcon("InfoIcon"));
            btn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            btn.setFocusable(false);
            btn.setForeground(Color.GRAY);
            explainPanel.add(btn, BorderLayout.WEST);
            HelpMgr.registerComponent(btn, "ExpressSearchTellMeMore");
        }
    }

    /**
     * Add serach results box to UI.
     * @param tableInfo the information about the table being added
     * @param hits the "hits" results of the search
     */
    public void addSearchResults(final QueryForIdResultsIFace results)
    {

        // This will start itself up and if there are results from the query 
        // it will add itself to the pane (So it is OK that it isn't referenced)
        @SuppressWarnings("unused")
        ESResultsTablePanel resultsTable = new ESResultsTablePanel(this, results, true, results.isExpanded());
    }

    
    /**
     * Removes a table from the content pane.
     * @param expressTableResultsBase the table of results to be removed
     */
    public synchronized void removeTable(final ESResultsTablePanelIFace expressTableResultsBase)
    {

        expTblResults.remove(expressTableResultsBase);
        
        expressTableResultsBase.cleanUp();
        
        contentPanel.remove(expressTableResultsBase.getUIComponent());
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
    public synchronized void addTable(final ESResultsTablePanelIFace expTblRes)
    {
        expTblResultsCache.remove(expTblRes);
        
        QueryForIdResultsIFace results = expTblRes.getResults();
        
        if (results.getIconName() == null)
        {
            log.error("Icon name is null for ["+results.getTitle()+"]");
        }
        
        contentPanel.removeAll();
        
        // Add it to the appropriate list to be sorted

        expTblResults.add(expTblRes);
        
        Collections.sort(expTblResults, sorter);
        
        for (ESResultsTablePanelIFace etr : expTblResults)
        {
            contentPanel.add(etr.getUIComponent()); 
        }
        
        List<Component> comps = layoutMgr.getComponentList();
        comps.clear();
        for (ESResultsTablePanelIFace etr : expTblResults)
        {
            comps.add(etr.getUIComponent()); 
        }
        
        if (explainPanel != null)
        {
            contentPanel.add(explainPanel);
        }            
            
        final JPanel panel = this;
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                panel.validate();
                panel.repaint();
                expTblRes.getUIComponent().repaint();
            }
        });
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#showingPane(boolean)
     */
    @Override
    public void showingPane(boolean show)
    {
        /*
         * See Bugs 4072 and 4073 - Adding Items to the NavBox just creates a boat load of bugs.
         * 
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
        */
    }

    /**
     * Revalidate the scroll pane.
     */
    public synchronized void revalidateScroll()
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
            if (comp instanceof ESResultsTablePanel)
            {
                ((ESResultsTablePanel)comp).cleanUp();
            }
        }
        
        expTblResults.clear();
        
        return true;
    }

    /*
     * See Bugs 4072 and 4073 - Adding Items to the NavBox just creates a boat load of bugs.
     * 
     */
    /*
    class ResInfoForSort implements Comparable<ResInfoForSort>
    {
        protected ExpressTableResultsBase erb;
        protected NavBoxItemIFace         nbi;
        
        public ResInfoForSort(ExpressTableResultsBase erb, NavBoxItemIFace nbi)
        {
            super();
            this.erb = erb;
            this.nbi = nbi;
        }

        public ExpressTableResultsBase getErb()
        {
            return erb;
        }

        public NavBoxItemIFace getNbi()
        {
            return nbi;
        }


        public int compareTo(ResInfoForSort obj)
        {
            return erb.getResults().getTableInfo().getTitle().compareTo(obj.getErb().getResults().getTableInfo().getTitle());
        }
        
    }*/

}
