package edu.ku.brc.specify.ui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.datamodel.Treeable;

/**
 * A TransferHandler for use for transferring TransferableMutableTreeNodes
 * between JTrees.
 *
 * @author jstewart
 */
@SuppressWarnings("serial")
public class TreeNodeTransferHandler extends TransferHandler
{
	private static Log log  = LogFactory.getLog(TreeNodeTransferHandler.class);

	public static final String mimeType = DataFlavor.javaJVMLocalObjectMimeType+";class=javax.swing.tree.DefaultMutableTreeNode";
	private DataFlavor nodeFlavor;

	public TreeNodeTransferHandler()
	{
		try
		{
			nodeFlavor = new DataFlavor(mimeType);
		}
		catch( ClassNotFoundException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * @param flavors an array of DataFlavors that the dropped object has
	 * @return true is flavors contains the proper tree node data flavor
	 */
	protected boolean hasNodeFlavor(DataFlavor[] flavors)
	{
		if( nodeFlavor == null )
		{
			return false;
		}

		for( DataFlavor df: flavors )
		{
			if( nodeFlavor.equals(df) )
			{
				return true;
			}
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
	 */
	@Override
	public boolean canImport( JComponent comp, DataFlavor[] transferFlavors )
	{
		return hasNodeFlavor(transferFlavors);
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
	 */
	@Override
	public boolean importData( JComponent comp, Transferable t )
	{
		log.debug("TreeNodeTransferHandler.importData("+comp+", "+t+") called");
		try
		{
			JTree tree = (JTree)comp;
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)t.getTransferData(nodeFlavor);

			// disallow moving the root
			if( node.isRoot() )
			{
				log.debug("You tried moving the root node.  Don't do this.");
				log.debug("TreeNodeTransferHandler.canImport() returning 'false'");
				return false;
			}

			DefaultMutableTreeNode newParentNode = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
			DefaultMutableTreeNode oldParentNode = (DefaultMutableTreeNode)node.getParent();

//			// ignore dropping on current parent... it's just extra work
//			if( newParentNode == oldParentNode )
//			{
//				log.debug("End location is same as start location");
//				log.debug("TreeNodeTransferHandler.canImport() returning 'false'");
//				return false;
//			}

			// disallow dropping on wrong rank
			Treeable newParentTreeable = (Treeable)newParentNode.getUserObject();
			Treeable oldParentTreeable = (Treeable)oldParentNode.getUserObject();
			if( newParentTreeable.getRankId().intValue() != oldParentTreeable.getRankId().intValue() )
			{
				log.debug("Cannot reparent to new rank");
				log.debug("TreeNodeTransferHandler.canImport() returning 'false'");
				return false;
			}

			//TODO: disallow dropping on wrong rank (e.g. county on continent)

			//Treeable movingNode = (Treeable)node.getUserObject();

			TreeNode[] arrayPathToNode = node.getPath();
			TreePath pathToNode = new TreePath(arrayPathToNode);

			Collection<TreeNode> expandedChildren = new ArrayList<TreeNode>();
			Enumeration<TreePath> expPaths = tree.getExpandedDescendants(pathToNode);
			while( expPaths!=null && expPaths.hasMoreElements() )
			{
				expandedChildren.add((TreeNode)expPaths.nextElement().getLastPathComponent());
			}

			DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
			model.removeNodeFromParent(node);

			// find out where to insert this node in order to keep the model sorted
			if( newParentNode.getChildCount() == 0 )
			{
				model.insertNodeInto(node, newParentNode, 0);
			}
			else
			{
				DefaultMutableTreeNode child = (DefaultMutableTreeNode)newParentNode.getFirstChild();
				String childName = ((Treeable)child.getUserObject()).getName();
				String nodeName = ((Treeable)node.getUserObject()).getName();
				int index = 0;
				while (nodeName.compareTo(childName) > 0 )
				{
					++index;
					child = (DefaultMutableTreeNode)child.getNextSibling();
					if( child == null )
					{
						break;
					}
					childName = ((Treeable)child.getUserObject()).getName();
				}
				model.insertNodeInto(node, newParentNode, index);
			}

			for( TreeNode n: expandedChildren )
			{
				TreeNode[] pathArray = model.getPathToRoot(n);
				TreePath path = new TreePath(pathArray);
				tree.expandPath(path);
			}
			log.debug("TreeNodeTransferHandler.canImport() returning 'true'");
			return true;
		}
		catch( IOException ex )
		{
            // TODO Auto-generated catch block
            ex.printStackTrace();
		}
        catch (UnsupportedFlavorException ex)
        {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }

		log.debug("TreeNodeTransferHandler.canImport() returning 'false'");
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent)
	 */
	@Override
	protected Transferable createTransferable( JComponent c )
	{
		if( !(c instanceof JTree) )
		{
			return null;
		}

		JTree tree = (JTree)c;
		return (TransferableMutableTreeNode)tree.getLastSelectedPathComponent();
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#exportDone(javax.swing.JComponent, java.awt.datatransfer.Transferable, int)
	 */
	@Override
	protected void exportDone( JComponent source, Transferable data, int action )
	{
		log.debug("TreeNodeTransferHandler.exportDone("+source+", "+data+", "+action+") called.");

		JTree tree = (JTree)source;
		DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)data;

		TreeNode[] pathArray = model.getPathToRoot(node);
		TreePath path = new TreePath(pathArray);
		tree.expandPath(path.getParentPath());

		log.debug("exportDone completed");
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
	 */
	@Override
	public int getSourceActions( JComponent c )
	{
		return TransferHandler.MOVE;
	}
}
