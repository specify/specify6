package edu.ku.brc.ui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.apache.log4j.Logger;

public class LocalTransferEnvelope implements Transferable
{
	protected Object transferObject;
	protected static final Logger log = Logger.getLogger(LocalTransferEnvelope.class);

	protected static DataFlavor localObjectFlavor;
	static
	{
		try
		{
			localObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
		}
		catch( ClassNotFoundException cnfe )
		{
			log.error("Failed to create local object DataFlavor in static code block", cnfe);
		}
	}
	protected static DataFlavor[] flavors = {localObjectFlavor};
	
	public LocalTransferEnvelope(Object localObject)
	{
		transferObject = localObject;
	}

	public DataFlavor[] getTransferDataFlavors()
	{
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return (flavor.equals(localObjectFlavor));
	}

	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException
	{
		if( isDataFlavorSupported(flavor) )
		{
			return transferObject;
		}
		else
		{
			throw new UnsupportedFlavorException(flavor);
		}
	}

}
