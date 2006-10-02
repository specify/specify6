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
package edu.ku.brc.ui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import org.apache.log4j.Logger;

/*
 * @code_status Beta
 **
 * @author rods
 *
 */
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
			throws UnsupportedFlavorException
	{
		if( isDataFlavorSupported(flavor) )
		{
			return transferObject;
		}
        // else
        throw new UnsupportedFlavorException(flavor);
	}

}
