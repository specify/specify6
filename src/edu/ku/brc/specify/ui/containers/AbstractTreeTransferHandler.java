/*
* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas, 1345 Jayhawk Boulevard,
 * Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package edu.ku.brc.specify.ui.containers;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * Taken from: 
 *  http://forums.sun.com/thread.jspa?threadID=296255&start=0
 * 
 * @author denis
 * 
 * @code_status Alpha
 * 
 * Oct 25, 2010
 * 
 */

public abstract class AbstractTreeTransferHandler implements DragGestureListener,
        DragSourceListener, DropTargetListener
{

    private GhostActionableTree           tree;
    private DragSource                    dragSource;              // dragsource
    @SuppressWarnings("unused")
    private DropTarget                    dropTarget;              // droptarget
    private static DefaultMutableTreeNode draggedNode;
    private DefaultMutableTreeNode        draggedNodeParent;
    private static BufferedImage          image  = null;           // buff image
    private Rectangle                     rect2D = new Rectangle();
    private boolean                       drawImage;

    protected AbstractTreeTransferHandler(GhostActionableTree tree, int action, boolean drawIcon)
    {
        this.tree = tree;
        drawImage = drawIcon;
        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(tree, action, this);
        dropTarget = new DropTarget(tree, action, this);
    }

    /* Methods for DragSourceListener */
    public void dragDropEnd(DragSourceDropEvent dsde)
    {
        if (dsde.getDropSuccess() && dsde.getDropAction() == DnDConstants.ACTION_MOVE
                && draggedNodeParent != null)
        {
            ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(draggedNodeParent);
        }
    }

    public final void dragEnter(DragSourceDragEvent dsde)
    {
        int action = dsde.getDropAction();
        if (action == DnDConstants.ACTION_COPY)
        {
            dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
        } else
        {
            if (action == DnDConstants.ACTION_MOVE)
            {
                dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
            } else
            {
                dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
            }
        }
    }

    public final void dragOver(DragSourceDragEvent dsde)
    {
        int action = dsde.getDropAction();
        if (action == DnDConstants.ACTION_COPY)
        {
            dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
        } else
        {
            if (action == DnDConstants.ACTION_MOVE)
            {
                dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
            } else
            {
                dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
            }
        }
    }

    public final void dropActionChanged(DragSourceDragEvent dsde)
    {
        int action = dsde.getDropAction();
        if (action == DnDConstants.ACTION_COPY)
        {
            dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
        } else
        {
            if (action == DnDConstants.ACTION_MOVE)
            {
                dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
            } else
            {
                dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
            }
        }
    }

    public final void dragExit(DragSourceEvent dse)
    {
        dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
    }

    /* Methods for DragGestureListener */
    public final void dragGestureRecognized(DragGestureEvent dge)
    {
        TreePath path = tree.getSelectionPath();
        if (path != null)
        {
            draggedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            draggedNodeParent = (DefaultMutableTreeNode) draggedNode.getParent();
            if (drawImage)
            {
                Rectangle pathBounds = tree.getPathBounds(path); // getpathbounds of selectionpath
                JComponent lbl = (JComponent) tree.getCellRenderer().getTreeCellRendererComponent(
                        tree, draggedNode, false, tree.isExpanded(path),
                        ((DefaultTreeModel) tree.getModel()).isLeaf(path.getLastPathComponent()),
                        0, false);// returning the label
                lbl.setBounds(pathBounds);// setting bounds to lbl
                image = new BufferedImage(lbl.getWidth(), lbl.getHeight(),
                        java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);// buffered image reference
                                                                        // passing the label's ht
                                                                        // and width
                Graphics2D graphics = image.createGraphics();// creating the graphics for buffered
                                                             // image
                graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); // Sets
                                                                                                  // the
                                                                                                  // Composite
                                                                                                  // for
                                                                                                  // the
                                                                                                  // Graphics2D
                                                                                                  // context
                lbl.setOpaque(false);
                lbl.paint(graphics); // painting the graphics to label
                graphics.dispose();
            }
            
            dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop, image, new Point(0, 0),
                    new TransferableNode(draggedNode), this);
        }
    }

    /* Methods for DropTargetListener */

    public final void dragEnter(DropTargetDragEvent dtde)
    {
        Point pt = dtde.getLocation();
        int action = dtde.getDropAction();
        if (drawImage)
        {
            paintImage(pt);
        }
        if (canPerformAction(tree, draggedNode, action, pt))
        {
            dtde.acceptDrag(action);
        } else
        {
            dtde.rejectDrag();
        }
    }

    public final void dragExit(DropTargetEvent dte)
    {
        if (drawImage)
        {
            clearImage();
        }
    }

    public final void dragOver(DropTargetDragEvent dtde)
    {
        Point pt = dtde.getLocation();
        int action = dtde.getDropAction();
        tree.autoscroll(pt);
        if (drawImage)
        {
            paintImage(pt);
        }
        if (canPerformAction(tree, draggedNode, action, pt))
        {
            dtde.acceptDrag(action);
        } else
        {
            dtde.rejectDrag();
        }
    }

    public final void dropActionChanged(DropTargetDragEvent dtde)
    {
        Point pt = dtde.getLocation();
        int action = dtde.getDropAction();
        if (drawImage)
        {
            paintImage(pt);
        }
        if (canPerformAction(tree, draggedNode, action, pt))
        {
            dtde.acceptDrag(action);
        } else
        {
            dtde.rejectDrag();
        }
    }

    public final void drop(DropTargetDropEvent dtde)
    {
        try
        {
            if (drawImage)
            {
                clearImage();
            }
            int action = dtde.getDropAction();
            Transferable transferable = dtde.getTransferable();
            Point pt = dtde.getLocation();
            if (transferable.isDataFlavorSupported(TransferableNode.NODE_FLAVOR)
                    && canPerformAction(tree, draggedNode, action, pt))
            {
                TreePath pathTarget = tree.getPathForLocation(pt.x, pt.y);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) transferable
                        .getTransferData(TransferableNode.NODE_FLAVOR);
                DefaultMutableTreeNode newParentNode = (DefaultMutableTreeNode) pathTarget
                        .getLastPathComponent();
                if (executeDrop(tree, node, newParentNode, action))
                {
                    dtde.acceptDrop(action);
                    dtde.dropComplete(true);
                    return;
                }
            }
            dtde.rejectDrop();
            dtde.dropComplete(false);
        } catch (Exception e)
        {
            System.out.println(e);
            dtde.rejectDrop();
            dtde.dropComplete(false);
        }
    }

    private final void paintImage(Point pt)
    {
        tree.paintImmediately(rect2D.getBounds());
        rect2D.setRect((int) pt.getX(), (int) pt.getY(), image.getWidth(), image.getHeight());
        tree.getGraphics().drawImage(image, (int) pt.getX(), (int) pt.getY(), tree);
    }

    private final void clearImage()
    {
        tree.paintImmediately(rect2D.getBounds());
    }

    public abstract boolean canPerformAction(GhostActionableTree target,
                                             DefaultMutableTreeNode draggedNode,
                                             int action,
                                             Point location);

    public abstract boolean executeDrop(GhostActionableTree tree,
                                        DefaultMutableTreeNode draggedNode,
                                        DefaultMutableTreeNode newParentNode,
                                        int action);
}
