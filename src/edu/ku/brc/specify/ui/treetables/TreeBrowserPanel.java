/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.ui.treetables;

import static edu.ku.brc.ui.UIRegistry.clearSimpleGlassPaneMsg;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getStatusBar;
import static edu.ku.brc.ui.UIRegistry.writeSimpleGlassPaneMsg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.treeutils.HibernateTreeDataServiceImpl;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 17, 2009
 *
 */
public class TreeBrowserPanel extends JPanel
{
    protected static final Logger log = Logger.getLogger(TreeBrowserPanel.class);
    
    protected String               tableName;
    protected Class<?>             clazz;
    protected DBTableInfo          tableInfo;
    protected String               columns;

    
    protected JTree                tree;
    protected BrowseTreeNode       root;
    protected TreeBrowserFindPanel treeBrowserFindPanel;
    
    protected Vector<Integer>      foundNodesList             = new Vector<Integer>();
    protected int                  currentFoundIndex;
    protected String               searchText = "";
    
    protected Hashtable<Integer, String> treeDefRankNamesHash = new Hashtable<Integer, String>();
    
    /**
     * @param tableName
     * @param clazz
     * @param treeDefId
     * @param rootId
     */
    public TreeBrowserPanel(final String   tableName, 
                            final Class<?> clazz,
                            final int      treeDefId,
                            final int      rootId)
    {
        super(new BorderLayout());
        
        this.tableName = tableName;
        this.clazz     = clazz;
        
        tableInfo = DBTableIdMgr.getInstance().getByClassName(clazz.getName());
        columns   = QueryAdjusterForDomain.getInstance().getSpecialColumns(tableInfo, false);


        String sql = String.format("select RankID, Name FROM %streedefitem WHERE TaxonTreeDefID = %d AND RankID > 0 ORDER BY RankID", clazz.getSimpleName().toLowerCase(), treeDefId);
        for (Object[] row : BasicSQLUtils.query(sql))
        {
            treeDefRankNamesHash.put((Integer)row[0], row[1].toString());
        }

        Vector<Object[]> titleVector = BasicSQLUtils.query(String.format("SELECT FullName, Name, RankID FROM %s WHERE %sID = %d", tableName, clazz.getSimpleName(), rootId));
        Object[]         titleRow    = titleVector.get(0);

        root = new BrowseTreeNode(rootId, (Integer)titleRow[2], titleRow[0] != null ? titleRow[0].toString() : titleRow[1].toString());
        tree = new JTree(new DefaultTreeModel(root));
        
        tree.setCellRenderer(new SpTreeCellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        treeBrowserFindPanel = new TreeBrowserFindPanel(this, TreeBrowserFindPanel.EXPANDED);
        
        add(UIHelper.createScrollPane(tree, true), BorderLayout.CENTER);
        add(treeBrowserFindPanel, BorderLayout.SOUTH);
    }
    
    /**
     * @param key
     * @param where
     * @param wrap
     * @param isExact
     */
    public void find(final String key, final boolean wrap, final boolean isExact)
    {
        searchText = key;
        
        boolean doOldWay = true;
        if (doOldWay)
        {
            findByName(key, isExact);
            
        } else
        {
            final String BROWSERTREESEARCH = "BROWSERTREESEARCH";
            final JStatusBar statusBar = getStatusBar();
            statusBar.setIndeterminate(BROWSERTREESEARCH, true);
            
            String msg = getLocalizedMessage("TREEBROWSESEARCH", searchText);
            statusBar.setText(msg);
            writeSimpleGlassPaneMsg(msg, 24);
            
            final SwingWorker worker = new SwingWorker()
            {
                @Override
                public Object construct()
                {
                    findByName(key, isExact);
                    return null;
                }
    
                //Runs on the event-dispatching thread.
                @Override
                public void finished()
                {
                    statusBar.setProgressDone(BROWSERTREESEARCH);
                    statusBar.setText(getLocalizedMessage("TREEBROWSESEARCH_FND", foundNodesList.size(), searchText));
                    clearSimpleGlassPaneMsg();
                }
            };
            worker.start();
        }
    }
    
    public boolean doFindNext()
    {
        boolean doOldWay = true;
        if (doOldWay)
        {
            if (currentFoundIndex < foundNodesList.size())
            {
                doFind(currentFoundIndex++);
            }
        } else
        {
            if (currentFoundIndex < foundNodesList.size()-1)
            {
                currentFoundIndex++;
                
                final String BROWSERTREESEARCH = "BROWSERTREESEARCH";
                final JStatusBar statusBar = getStatusBar();
                statusBar.setIndeterminate(BROWSERTREESEARCH, true);
                
                String msg = getLocalizedMessage("TREEBROWSESEARCH", searchText);
                statusBar.setText(msg);
                writeSimpleGlassPaneMsg(msg, 24);
                
                final SwingWorker worker = new SwingWorker()
                {
                    @Override
                    public Object construct()
                    {
                        doFind(currentFoundIndex++);
                        return null;
                    }
        
                    //Runs on the event-dispatching thread.
                    @Override
                    public void finished()
                    {
                        statusBar.setProgressDone(BROWSERTREESEARCH);
                        statusBar.setText(getLocalizedMessage("TREEBROWSESEARCH_FND", foundNodesList.size(), searchText));
                        clearSimpleGlassPaneMsg();
                    }
                };
                worker.start();
                
            }
        }
        
        return currentFoundIndex < foundNodesList.size();
    }
    
    protected void doFind(final int index)
    {
        Integer id = foundNodesList.get(index);
        Stack<Integer> idPath = new Stack<Integer>();
        while (id != null)
        {
            //System.out.println(id);
            idPath.push(id);
            String sql = String.format(" SELECT ParentID FROM %s WHERE %s = %d", tableName, tableInfo.getPrimaryKeyName(), id);
            System.out.println(sql);
            id  = BasicSQLUtils.getCount(sql);
        }
        
        ArrayList<BrowseTreeNode> treePath = new ArrayList<BrowseTreeNode>();
        BrowseTreeNode parentNode = root;
        treePath.add(root);
        for (int i=idPath.size()-2;i>=0;i--)
        {
            int searchId = idPath.get(i);
            for (int j=0;j<parentNode.getChildCount();j++)
            {
                BrowseTreeNode childNode = (BrowseTreeNode)parentNode.getChildAt(j);
                if (childNode.getId() == searchId)
                {
                    parentNode = childNode;
                    treePath.add(childNode);
                    childNode.getChildCount();
                    break;
                }
            }
        }
        for (BrowseTreeNode btn : treePath)
        {
           System.out.println("-> "+btn.getTitle());
        }
        TreePath selTreePath = new TreePath(treePath.toArray());
        tree.getSelectionModel().setSelectionPath(selTreePath);
        tree.scrollPathToVisible(selTreePath);
    }
    
    /**
     * @param name
     * @param isExact
     * @return
     */
    public synchronized void findByName(final String name, final boolean isExact)
    {
        foundNodesList.clear();
        
        try
        {
            String newName = name;
            if (!isExact)
            {
                if (newName.contains("*"));
                {
                    newName = newName.replace("*", "%");
                }
                if (!newName.endsWith("%"))
                {
                    newName += "%";
                }
            }
            
            String      sql       = String.format(" SELECT %s FROM %s WHERE "+columns+" AND Name LIKE '%s'", tableInfo.getPrimaryKeyName(), tableName, newName);
            log.debug(sql);
            
            for (Object idObj : BasicSQLUtils.querySingleCol(sql))
            {
                foundNodesList.add((Integer)idObj);
                log.debug((Integer)idObj);
            }
            
            if (!foundNodesList.isEmpty())
            {
                currentFoundIndex = 0;
                treeBrowserFindPanel.getNextButton().setEnabled(doFindNext());
            }
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(HibernateTreeDataServiceImpl.class, ex);
            log.error(ex);
            
        } finally
        {
            
        }
    }
    
    //-------------------------------------------------------
    //-- Browse Tree Node
    //-------------------------------------------------------
    class SpTreeCellRenderer extends JPanel implements TreeCellRenderer
    {
        protected JLabel leftLbl;
        protected JLabel rightLbl;
        
        protected DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();
        
        /**
         * 
         */
        public SpTreeCellRenderer()
        {
            super();
            
            setOpaque(false);
            
            leftLbl  = new JLabel();
            rightLbl = new JLabel();
            rightLbl.setForeground(Color.LIGHT_GRAY);
            
            leftLbl.setOpaque(true);
            
            CellConstraints cc = new CellConstraints();
            PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g,2px,f:p:g", "p"), this);
            
            pb.add(leftLbl,  cc.xy(1, 1));
            pb.add(rightLbl, cc.xy(3, 1));
        }

        /* (non-Javadoc)
         * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
         */
        @Override
        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean selected,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus)
        {
            BrowseTreeNode node = (BrowseTreeNode)value;
            
            String level = "";
            if (node.getParent() == null || node.getParent().getChildAt(0) == node)
            {
                level = " (" + treeDefRankNamesHash.get(node.getRankId()) + ")";
            }
            if (selected)
            {
                leftLbl.setForeground(defaultRenderer.getTextSelectionColor());
                leftLbl.setBackground(defaultRenderer.getBackgroundSelectionColor());
            } else
            {
                leftLbl.setForeground(defaultRenderer.getTextNonSelectionColor());
                leftLbl.setBackground(defaultRenderer.getBackgroundNonSelectionColor());
            }
            leftLbl.setText(node.getTitle());
            rightLbl.setText(level);
            return this;
        }
        
    }
    
    //-------------------------------------------------------
    //-- Browse Tree Node
    //-------------------------------------------------------
    class BrowseTreeNode extends DefaultMutableTreeNode
    {
        private boolean  areChildrenDefined = false;
        private int      id;
        private int      rankId;
        private Integer  numChildren = null;
        private String   title;

        /**
         * @param tableName
         * @param clazz
         * @param id
         * @param title
         */
        public BrowseTreeNode(final int id, 
                              final int rankId,
                              final String title)
        {
            this.id        = id;
            this.rankId    = rankId;
            this.title     = title;
        }

        /* (non-Javadoc)
         * @see javax.swing.tree.DefaultMutableTreeNode#isLeaf()
         */
        public boolean isLeaf()
        {
            if (numChildren == null)
            {
                numChildren = BasicSQLUtils.getCount(String.format("SELECT COUNT(*) FROM %s WHERE ParentID = %d", tableName, id));
            }
            return numChildren != null ? numChildren == 0 : true;
        }
        
        /**
         * @return
         */
        public boolean isLoaded()
        {
            return numChildren != null;
        }

        /* (non-Javadoc)
         * @see javax.swing.tree.DefaultMutableTreeNode#getChildCount()
         */
        public int getChildCount()
        {
            if (!areChildrenDefined)
            {
                loadChildNodes();
            }
            return super.getChildCount();
        }

        /**
         * @return the id
         */
        public int getId()
        {
            return id;
        }

        /**
         * @return the rankId
         */
        public int getRankId()
        {
            return rankId;
        }

        /**
         * @return the title
         */
        public String getTitle()
        {
            return title;
        }

        /**
         * 
         */
        public void loadChildNodes()
        {
            areChildrenDefined = true;
            String sql = String.format("SELECT %sID, FullName, RankID FROM %s WHERE ParentID = %d ORDER BY FullName", clazz.getSimpleName(), tableName, id);
            Vector<Object[]> rows = BasicSQLUtils.query(sql);
            numChildren = rows.size();
            
            for (Object[] row : rows)
            {
                Integer nodeId = (Integer)row[0];
                String title   = row[1].toString();
                add(new BrowseTreeNode(nodeId, (Integer)row[2], title));
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.tree.DefaultMutableTreeNode#toString()
         */
        public String toString()
        {
            return title;
        }
    }
}
