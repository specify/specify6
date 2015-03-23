/*
* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute, 1345 Jayhawk Boulevard,
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Arrays;

import javax.swing.tree.DefaultMutableTreeNode;

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
public class TransferableNode implements Transferable
{
    public static final DataFlavor NODE_FLAVOR = new DataFlavor(
                                                       DataFlavor.javaJVMLocalObjectMimeType,
                                                       "Node");
    private DefaultMutableTreeNode node;
    private DataFlavor[]           flavors     = { NODE_FLAVOR };

    public TransferableNode(DefaultMutableTreeNode nd)
    {
        node = nd;
    }

    public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
    {
        if (flavor == NODE_FLAVOR)
        {
            return node;
        } else
        {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    public DataFlavor[] getTransferDataFlavors()
    {
        return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return Arrays.asList(flavors).contains(flavor);
    }
}
