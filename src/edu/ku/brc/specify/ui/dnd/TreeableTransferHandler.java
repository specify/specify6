package edu.ku.brc.specify.ui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;

import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.ui.db.DbStoreTreeModel;

@SuppressWarnings("serial")
public class TreeableTransferHandler extends TransferHandler
{
	private String mimeType = DataFlavor.javaJVMLocalObjectMimeType+";class=edu.ku.brc.specify.datamodel.Treeable";
	private DataFlavor nodeFlavor;
	
	public TreeableTransferHandler()
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
		if( hasNodeFlavor(t.getTransferDataFlavors()) )
		{
			try
			{
				JTree tree = (JTree)comp;
				DbStoreTreeModel model = (DbStoreTreeModel)tree.getModel();
				Treeable movingNode = (Treeable)t.getTransferData(nodeFlavor);
				
				// disallow moving the root node
				if( model.getRoot() == movingNode )
				{
					return false;
				}
				
				Treeable newParent = (Treeable)(tree.getLastSelectedPathComponent());
				Treeable oldParent = movingNode.getParentNode();

				//TODO: disallow dropping on current parent... it's just extra work
				//TODO: disallow dropping on wrong rank (e.g. county on continent)
				
				System.out.println("Re-parenting "+movingNode.getName()+" to "+newParent.getName());
				model.attachChildToParent(movingNode,oldParent,newParent);
				return true;
			}
			catch( UnsupportedFlavorException e )
			{
				e.printStackTrace();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
		
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
		return (Transferable)tree.getLastSelectedPathComponent();
	}

	@Override
	protected void exportDone( JComponent source, Transferable data, int action )
	{
		System.out.println("exportDone called.  Now delete the moved section from the old location");
		//JTree tree = (JTree)source;
		//DbStoreTreeModel model = (DbStoreTreeModel)tree.getModel();
		System.out.println("exportDone completed");
	}

	@Override
	public int getSourceActions( JComponent c )
	{
		return TransferHandler.MOVE;
	}
}
