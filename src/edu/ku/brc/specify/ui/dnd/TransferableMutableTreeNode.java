package edu.ku.brc.specify.ui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A DefaultMutableTreeNode customized to allow for transfer using a
 * TreeNodeTransferHandler
 * 
 * @author jstewart
 */
public class TransferableMutableTreeNode extends DefaultMutableTreeNode
        implements Transferable
{

	public TransferableMutableTreeNode()
    {
	    super();
    }

	public TransferableMutableTreeNode(Object userObject, boolean allowsChildren)
    {
	    super(userObject, allowsChildren);
    }

	public TransferableMutableTreeNode(Object userObject)
    {
	    super(userObject);
    }

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	public DataFlavor[] getTransferDataFlavors()
	{
	    DataFlavor[] flavors = new DataFlavor[1];
	    try
        {
	        flavors[0] = new DataFlavor(TreeNodeTransferHandler.mimeType);
        }
        catch (ClassNotFoundException e)
        {
	        // TODO What do we do here?
	        e.printStackTrace();
        }
	    return flavors;
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
	    DataFlavor[] flavors = new DataFlavor[1];
	    try
	    {
	        flavors[0] = new DataFlavor(TreeNodeTransferHandler.mimeType);
	    }
	    catch( ClassNotFoundException ex )
	    {
	        //TODO: What do we want to do here?
	    }

		for( DataFlavor df: flavors )
		{
			if( df.equals(flavor) )
			{
				return true;
			}
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
	 */
	public Object getTransferData(DataFlavor flavor)
	        throws UnsupportedFlavorException, IOException
	{
	    DataFlavor[] flavors = new DataFlavor[1];
	    try
	    {
	        flavors[0] = new DataFlavor(TreeNodeTransferHandler.mimeType);
	    }
	    catch( ClassNotFoundException ex )
	    {
	        //TODO: What do we want to do here?
	    	ex.printStackTrace();
	    }

		if( flavor.equals(flavors[0]) )
		{
			return this;
		}
		else
		{
			return null;
		}
	}
}
