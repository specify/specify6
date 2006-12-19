/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.treeutils.TreeFactory;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class EmbeddableTreeTableViewer <T extends Treeable<T,D,I>,
                                        D extends TreeDefIface<T,D,I>,
                                        I extends TreeDefItemIface<T,D,I>>
            extends TreeTableViewer<T,D,I> implements ListSelectionListener
{
    protected String leafNodeName;
    protected List<Object> nodes;
    protected List<ListSelectionListener> selectionListeners;

    /**
     * @param treeDef
     * @param name
     * @param task
     */
    public EmbeddableTreeTableViewer(D treeDef)
    {
        super(treeDef, null, null);
        nodes = new Vector<Object>();
        selectionListeners = new Vector<ListSelectionListener>();
    }
    
    public void setLeafNodeName(String name)
    {
        this.leafNodeName = name;
        initTreeLists();
    }

    @Override
    protected void initTreeLists()
    {
        // setup a thread to load the objects from the DB
        Runnable runnable = new Runnable()
        {
            @SuppressWarnings("synthetic-access")
            public void run()
            {
                final List<T> matchingNodes = dataService.findByName(treeDef, leafNodeName);

                // from these nodes, create a new node tree all the way up to the root

                nodes.clear();
                final T root = buildTreeFromLeafNodes(matchingNodes);
                if (root==null)
                {
                    EmbeddableTreeTableViewer.this.setSelectedNode(null);
                    EmbeddableTreeTableViewer.this.removeAll();
                    EmbeddableTreeTableViewer.this.add(new JLabel("No results found"), BorderLayout.CENTER);
                    EmbeddableTreeTableViewer.this.revalidate();
                    EmbeddableTreeTableViewer.this.repaint();
                    return;
                }
                
                SwingUtilities.invokeLater(new Runnable()
                {
                    @SuppressWarnings("synthetic-access")
                    public void run()
                    {
                        showTree(root);
                        
                        // redo the construction of the actual JTree
                        EmbeddableTreeTableViewer.this.add(scrollers[0], BorderLayout.CENTER);
                        EmbeddableTreeTableViewer.this.revalidate();
                        EmbeddableTreeTableViewer.this.repaint();
                        lists[0].addListSelectionListener(EmbeddableTreeTableViewer.this);
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                doExpandAllDescendants(root);
                            }
                        });
                    }
                });
            }
        };
        
        Thread t = new Thread(runnable);
        t.start();
    }
    
    @Override
    public void showPopup(MouseEvent e)
    {
        // do nothing
        // this method is being overridden in order to keep the popup menu from showing up
    }

    @SuppressWarnings("unchecked")
    protected T buildTreeFromLeafNodes(List<T> matchingNodes)
    {
        for (T node: matchingNodes)
        {
            buildPathToNode(node,null);
        }
        
        // find the root node
        for (Object o: nodes)
        {
            T node = (T)o;
            if (node.getRankId()==0)
            {
                return node;
            }
        }
        
        return null;
    }
    
    protected void buildPathToNode(T node, T child)
    {
        T newT = findNodeById(node.getTreeId());
        if (newT!=null)
        {
            // the node already exists in our 'fake' node tree
            // just add the child and return
            if (child!=null)
            {
                newT.getChildren().add(child);
                child.setParent(newT);
            }
            return;
        }
        
        // this node doesn't yet have a mirror of it in our 'fake' node tree
        // duplicate its values into a new node
        newT = duplicateNodeInfo(node);
        if (child!=null)
        {
            child.setParent(newT);
        }
        nodes.add(newT);
        if (child!=null)
        {
            newT.getChildren().add(child);
        }
        
        // then see if we need to continue up the tree
        if (node.getParent()!=null)
        {
            buildPathToNode(node.getParent(), newT);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected T findNodeById(Long id)
    {
        for (Object o: nodes)
        {
            T node = (T)o;
            if (node.getTreeId().longValue() == id.longValue())
            {
                return node;
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    protected T duplicateNodeInfo(T node)
    {
        T newT = TreeFactory.createNewTreeable(node, null);
        newT.setDefinition(node.getDefinition());
        newT.setDefinitionItem(node.getDefinitionItem());
        newT.setName(node.getName());
        newT.setFullName(node.getFullName());
        newT.setRankId(node.getRankId());
        newT.setTreeId(node.getTreeId());
        newT.setRemarks(node.getRemarks());
        
        return newT;
    }
    
    @SuppressWarnings("unchecked")
    public T getSelectedNode()
    {
        if (lists == null || lists[0] == null)
        {
            return null;
        }
        
        return (T)lists[0].getSelectedValue();
    }
    
    public void setSelectedNode(T selection)
    {
        if (selection != null)
        {
            lists[0].setSelectedValue(selection, true);
        }
        else
        {
            if (lists==null)
            {
                return;
            }
            lists[0].clearSelection();
        }
    }

    public void valueChanged(ListSelectionEvent e)
    {
        ListSelectionEvent event = new ListSelectionEvent(this,e.getFirstIndex(),e.getLastIndex(),e.getValueIsAdjusting());
        for (ListSelectionListener listener: selectionListeners)
        {
            listener.valueChanged(event);
        }
    }
    
    public synchronized void addListSelectionListener(ListSelectionListener listener)
    {
        selectionListeners.add(listener);
    }

    public synchronized void removeListSelectionListener(ListSelectionListener listener)
    {
        selectionListeners.remove(listener);
    }
}
