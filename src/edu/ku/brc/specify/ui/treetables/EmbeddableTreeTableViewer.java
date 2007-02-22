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

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
@SuppressWarnings("serial")
public class EmbeddableTreeTableViewer <T extends Treeable<T,D,I>,
                                        D extends TreeDefIface<T,D,I>,
                                        I extends TreeDefItemIface<T,D,I>>
            extends TreeTableViewer<T,D,I> implements ListSelectionListener
{
    protected String leafNodeName;
    protected List<ListSelectionListener> selectionListeners;

    /**
     * @param treeDef
     * @param name
     * @param task
     */
    public EmbeddableTreeTableViewer(D treeDef)
    {
        super(treeDef, null, null);
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
        listModel = new FilteredTreeDataListModel<T,D,I>(treeDef,leafNodeName);
        // setup a thread to load the objects from the DB
        Runnable runnable = new Runnable()
        {
            @SuppressWarnings("synthetic-access")
            public void run()
            {
                final List<T> matchingNodes = listModel.findByName(leafNodeName);

                // from these nodes, create a new node tree all the way up to the root

                //nodes.clear();
                //final T root = buildTreeFromLeafNodes(matchingNodes);
                if (matchingNodes.isEmpty())
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
                        showTree();
                        
                        // redo the construction of the actual JTree
                        EmbeddableTreeTableViewer.this.add(scrollers[0], BorderLayout.CENTER);
                        EmbeddableTreeTableViewer.this.revalidate();
                        EmbeddableTreeTableViewer.this.repaint();
                        lists[0].addListSelectionListener(EmbeddableTreeTableViewer.this);
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                //doExpandAllDescendants(root);
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
    
    public void addChildToSelectedNode()
    {
        addChildToSelectedNode(lists[0]);
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
