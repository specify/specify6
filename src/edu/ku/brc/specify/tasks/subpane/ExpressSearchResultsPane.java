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
import java.awt.Component;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.apache.lucene.search.Hits;

import edu.ku.brc.af.core.ExpressResultsTableInfo;
import edu.ku.brc.af.core.ExpressSearchResults;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxLayoutManager;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;

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
public class ExpressSearchResultsPane extends BaseSubPane implements ExpressSearchResultsPaneIFace
{
    private static final Logger log = Logger.getLogger(ExpressSearchResultsPane.class);

    protected JPanel               contentPanel;
    protected NavBoxLayoutManager  layoutMgr;
    protected JScrollPane          scrollPane;
    
    protected List<ExpressTableResultsBase> expTblResultsIndexed = new Vector<ExpressTableResultsBase>();
    protected List<ExpressTableResultsBase> expTblResults        = new Vector<ExpressTableResultsBase>();
    
    protected NavBox      navBox  = null;
    protected int         positionOfUnIndexed = -1;
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

        contentPanel = new JPanel(layoutMgr = new NavBoxLayoutManager(0, 2));

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
     * Finds the index for the ExpressTableResultsBase item.
     * @param list the list to be searched
     * @param expressTableResultsBase the item to look for
     * @return the index in the list
     */
    /*
    protected int getIndexFor(final List<ResInfoForSort> list, final ExpressTableResultsBase expressTableResultsBase)
    {
        int inx = 0;
        for (ResInfoForSort rfs : list)
        {
            if (rfs.getErb() == expressTableResultsBase)
            {
                return inx;
            }
            inx++;
        }
        throw new RuntimeException("Can't find expressTableResultsBase!");
    }*/
    
    /**
     * Removes a table from the content pane.
     * @param expressTableResultsBase the table of results to be removed
     */
    public synchronized void removeTable(final ExpressTableResultsBase expressTableResultsBase)
    {
        if (expressTableResultsBase.getResults().getTableInfo().isIndexed())
        {
            expTblResultsIndexed.remove(expressTableResultsBase);
            
        } else
        {
            expTblResults.remove(expressTableResultsBase);
        }
        
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
    public synchronized void addTable(ExpressTableResultsBase expTblRes)
    {
        ExpressSearchResults    results = expTblRes.getResults();
        ExpressResultsTableInfo tblInfo = results.getTableInfo();
        
        if (tblInfo.getIconName() == null)
        {
            log.error("Icon name is null for ["+tblInfo.getTitle()+"]");
        }
        
        /*
         * See Bugs 4072 and 4073 - Adding Items to the NavBox just creates a boat load of bugs.
         * 
        if (navBox == null)
        {
            navBox = new NavBox(getResourceString("ESResults"));
        }
        
        NavBoxItemIFace nbi = NavBox.createBtn(tblInfo.getTitle(), tblInfo.getIconName(), IconManager.IconSize.Std16, null);
        navBox.add(nbi, true);
        
        if (showing && !added)
        {
            added = true;
            NavBoxMgr.getInstance().addBox(navBox);
        } 

        NavBoxMgr.getInstance().validate();
        */
        
        contentPanel.add(expTblRes);
        
        // Add it to the appropriate list to be sorted
        if (tblInfo.isIndexed())
        {
            expTblResultsIndexed.add(expTblRes);
            Collections.sort(expTblResultsIndexed);
            
        } else
        {
            expTblResults.add(expTblRes);
            Collections.sort(expTblResults);
        }
        
        List<Component> comps   = layoutMgr.getComponentList();
        comps.clear();
        comps.addAll(expTblResultsIndexed);
        comps.addAll(expTblResults);
        
        /*
        // Now reach into the LayoutManager and clear the list of its items
        // and then put back all the sorted ones.
        List<Component> comps   = layoutMgr.getComponentList();
        //List<Component> nbComps = ((NavBoxLayoutManager2)navBox.getLayout()).getComponentList();
        
        comps.clear();
        //nbComps.clear();
        
        // these go on top
        for (ResInfoForSort rfs : expTblResultsIndexed)
        {
            comps.add(rfs.getErb());
            //nbComps.add(rfs.getNbi().getUIComponent());
        }
        // These go after
        for (ResInfoForSort rfs : expTblResults)
        {
            comps.add(rfs.getErb());
            //nbComps.add(rfs.getNbi().getUIComponent());
        }
*/
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
        
        expTblResultsIndexed.clear();
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
