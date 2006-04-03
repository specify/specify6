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

import edu.ku.brc.specify.datamodel.Treeable;

@SuppressWarnings("serial")
public class TreeNodeTransferHandler extends TransferHandler
{
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

	@Override
	public boolean canImport( JComponent comp, DataFlavor[] transferFlavors )
	{
		return hasNodeFlavor(transferFlavors);
	}

	@Override
	public boolean importData( JComponent comp, Transferable t )
	{
		System.out.println("TreeNodeTransferHandler.importData("+comp+", "+t+") called");
		try
		{
			JTree tree = (JTree)comp;
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)t.getTransferData(nodeFlavor);
			
			// disallow moving the root
			if( node.isRoot() )
			{
				System.out.println("You tried moving the root node.  Don't do this.");
				System.out.println("TreeNodeTransferHandler.canImport() returning 'false'");
				return false;
			}
			
			DefaultMutableTreeNode newParentNode = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
			DefaultMutableTreeNode oldParentNode = (DefaultMutableTreeNode)node.getParent();

//			// ignore dropping on current parent... it's just extra work
//			if( newParentNode == oldParentNode )
//			{
//				System.out.println("End location is same as start location");
//				System.out.println("TreeNodeTransferHandler.canImport() returning 'false'");
//				return false;
//			}
			
			// disallow dropping on wrong rank
			Treeable newParentTreeable = (Treeable)newParentNode.getUserObject();
			Treeable oldParentTreeable = (Treeable)oldParentNode.getUserObject();
			if( newParentTreeable.getRankId().intValue() != oldParentTreeable.getRankId().intValue() )
			{
				System.out.println("Cannot reparent to new rank");
				System.out.println("TreeNodeTransferHandler.canImport() returning 'false'");
				return false;
			}
			
			//TODO: disallow dropping on wrong rank (e.g. county on continent)
			
			Treeable movingNode = (Treeable)node.getUserObject();
						
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
			System.out.println("TreeNodeTransferHandler.canImport() returning 'true'");
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
		
		System.out.println("TreeNodeTransferHandler.canImport() returning 'false'");
		return false;
	}

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

	@Override
	protected void exportDone( JComponent source, Transferable data, int action )
	{
		System.out.println("TreeNodeTransferHandler.exportDone("+source+", "+data+", "+action+") called.");

		JTree tree = (JTree)source;
		DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)data;

		TreeNode[] pathArray = model.getPathToRoot(node);
		TreePath path = new TreePath(pathArray);
		tree.expandPath(path.getParentPath());
		
		System.out.println("exportDone completed");
	}

	@Override
	public int getSourceActions( JComponent c )
	{
		return TransferHandler.MOVE;
	}
}
